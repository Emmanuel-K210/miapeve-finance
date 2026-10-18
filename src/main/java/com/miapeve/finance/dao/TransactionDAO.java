package com.miapeve.finance.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Transaction;
import com.miapeve.finance.enums.TypeCompte;
import com.miapeve.finance.enums.TypeTransaction;

public class TransactionDAO extends BaseDAO {

	private final CompteDAO compteDAO;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter ISO_FORMATTER = 
		    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]");

	public TransactionDAO() {
		compteDAO = new CompteDAO();
	}

	public Optional<Transaction> findById(Long id) {
		String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setLong(1, id);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return Optional.of(mapResultSetToTransaction(rs));
			}
		} catch (SQLException e) {
			System.err.println("❌ Erreur findById Transaction: " + e.getMessage());
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public List<Transaction> findByDateBeforeAndEqual(LocalDateTime date) {
		List<Transaction> transactions = new ArrayList<>();
		String sql = "SELECT * FROM transactions WHERE DATE(date_creation) <= DATE(?) ORDER BY date_creation DESC";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, Date.valueOf(date.toLocalDate()));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				transactions.add(mapResultSetToTransaction(rs));
			}
		} catch (SQLException e) {
			System.err.println("❌ Erreur findByDateBeforeAndEqual: " + e.getMessage());
			e.printStackTrace();
		}
		return transactions;
	}

	public BigDecimal findCumulByCompteAndTypeAndDateTime(TypeCompte type, TypeTransaction transactionType,
			LocalDateTime dateTime) throws Exception {
		Compte compte = compteDAO.findByType(type.name())
				.orElseThrow(() -> new Exception("Compte non trouvé pour le type: " + type));

		String sql = """
				SELECT COALESCE(SUM(montant), 0)
				FROM transactions
				WHERE compte_id = ?
				AND type_transaction = ?
				AND Date(date_creation) <= DATE(?)
				""";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setLong(1, compte.getCompteId());
			stmt.setString(2, transactionType.name());
			stmt.setTimestamp(3, Timestamp.valueOf(dateTime));

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getBigDecimal(1);
			}
			return BigDecimal.ZERO;

		} catch (SQLException e) {
			System.err.println("❌ Erreur findCumulByCompteAndTypeAndDateTime: " + e.getMessage());
			throw new Exception("Erreur d'accès aux données", e);
		}
	}

	public List<Transaction> findAll() {
		return findByCondition("1 = 1 ORDER BY date_creation DESC");
	}

	public List<Transaction> findByCompte(Long compteId) {
		String condition = "compte_id = ? ORDER BY date_creation DESC";
		return findByCondition(condition, compteId);
	}

	public List<Transaction> findByType(String typeTransaction) {
		String condition = "type_transaction = ? ORDER BY date_creation DESC";
		return findByCondition(condition, typeTransaction);
	}

	public List<Transaction> findByTypeTransaction(TypeTransaction type) {
		String condition = "type_transaction = ? ORDER BY date_creation DESC";
		return findByCondition(condition, type.name());
	}

	public List<Transaction> findByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
	    List<Transaction> transactions = new ArrayList<>();
	    
	    // 🔥 Nouvelle requête qui gère TOUS les formats
	    String sql = """
	        SELECT * FROM transactions 
	        WHERE (
	            -- Cas 1: Format datetime normal (YYYY-MM-DD HH:MM:SS ou avec T)
	            (date_creation LIKE '%-%' AND Date(date_creation) BETWEEN Date(?) AND Date(?))
	            OR
	            -- Cas 2: Timestamp Unix en millisecondes (13 chiffres)
	            (date_creation NOT LIKE '%-%' AND CAST(date_creation AS INTEGER) > 1000000000000 
	             AND datetime(CAST(date_creation AS INTEGER) / 1000, 'unixepoch', 'localtime') 
	                 BETWEEN datetime(?, 'localtime') AND datetime(?, 'localtime'))
	            OR
	            -- Cas 3: Timestamp Unix en secondes (10 chiffres)
	            (date_creation NOT LIKE '%-%' AND CAST(date_creation AS INTEGER) < 1000000000000
	             AND datetime(CAST(date_creation AS INTEGER), 'unixepoch', 'localtime') 
	                 BETWEEN datetime(?, 'localtime') AND datetime(?, 'localtime'))
	        )
	        ORDER BY 
	            CASE 
	                WHEN date_creation LIKE '%-%' THEN date_creation
	                WHEN CAST(date_creation AS INTEGER) > 1000000000000 
	                    THEN datetime(CAST(date_creation AS INTEGER) / 1000, 'unixepoch', 'localtime')
	                ELSE datetime(CAST(date_creation AS INTEGER), 'unixepoch', 'localtime')
	            END DESC
	        """;

	    try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
	        String dateStr = dateDebut.format(DATE_FORMATTER);
	        String dateStr2 = dateFin.format(DATE_FORMATTER);
	        
	        // Paramètres pour les 3 cas (6 au total)
	        stmt.setString(1, dateStr);   // Cas 1 début
	        stmt.setString(2, dateStr2);  // Cas 1 fin
	        stmt.setString(3, dateStr);   // Cas 2 début
	        stmt.setString(4, dateStr2);  // Cas 2 fin
	        stmt.setString(5, dateStr);   // Cas 3 début
	        stmt.setString(6, dateStr2);  // Cas 3 fin
	        
	        System.out.println("🔍 Recherche période: " + dateStr + " à " + dateStr2);
	        
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            transactions.add(mapResultSetToTransaction(rs));
	        }
	        
	        System.out.println("✅ Transactions trouvées: " + transactions.size());
	        
	    } catch (SQLException e) {
	        System.err.println("❌ Erreur findByPeriode: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return transactions;
	}

	public List<Transaction> findByProgrammeId(Long programmeId) {
		String condition = "programme_id = ? ORDER BY date_creation DESC";
		return findByCondition(condition, programmeId);
	}

	/**
	 * Méthode générique pour les requêtes avec conditions
	 */
	private List<Transaction> findByCondition(String condition, Object... params) {
		List<Transaction> transactions = new ArrayList<>();
		String sql = "SELECT * FROM transactions WHERE " + condition;

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				Object param = params[i];
				if (param instanceof Long) {
					stmt.setLong(i + 1, (Long) param);
				} else if (param instanceof String) {
					stmt.setString(i + 1, (String) param);
				} else if (param instanceof Integer) {
					stmt.setInt(i + 1, (Integer) param);
				} else if (param instanceof LocalDateTime) {
					// Formater la date pour SQLite
					String dateStr = ((LocalDateTime) param).format(TIMESTAMP_FORMATTER);
					stmt.setString(i + 1, dateStr);
				}
			}

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				transactions.add(mapResultSetToTransaction(rs));
			}
		} catch (SQLException e) {
			System.err.println("❌ Erreur findByCondition: " + e.getMessage());
			e.printStackTrace();
		}
		return transactions;
	}

	public BigDecimal getTotalTransactionsByCompte(Long compteId) {
		String sql = "SELECT SUM(montant) as total FROM transactions WHERE compte_id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setLong(1, compteId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				BigDecimal total = rs.getBigDecimal("total");
				return total != null ? total : BigDecimal.ZERO;
			}
		} catch (SQLException e) {
			System.err.println("❌ Erreur getTotalTransactionsByCompte: " + e.getMessage());
			e.printStackTrace();
		}
		return BigDecimal.ZERO;
	}

	public BigDecimal getTotalByTypeAndPeriode(TypeTransaction type, LocalDateTime debut, LocalDateTime fin) {
		String sql = "SELECT SUM(montant) as total FROM transactions WHERE type_transaction = ? AND date_creation BETWEEN ? AND ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, type.name());
			stmt.setTimestamp(2, Timestamp.valueOf(debut));
			stmt.setTimestamp(3, Timestamp.valueOf(fin));
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				BigDecimal total = rs.getBigDecimal("total");
				return total != null ? total : BigDecimal.ZERO;
			}
		} catch (SQLException e) {
			System.err.println("❌ Erreur getTotalByTypeAndPeriode: " + e.getMessage());
			e.printStackTrace();
		}
		return BigDecimal.ZERO;
	}

	public Transaction save(Transaction transaction) {
		if (transaction.getTransactionId() == null) {
			return insert(transaction);
		} else {
			return update(transaction);
		}
	}

	private Transaction insert(Transaction transaction) {
		String sql = """
				INSERT INTO transactions
				(type_transaction, montant, compte_id, description, reference, cree_par, date_creation)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, transaction.getTypeTransaction().name());
			stmt.setBigDecimal(2, transaction.getMontant());
			stmt.setLong(3, transaction.getCompteId());
			stmt.setString(4, transaction.getDescription());
			stmt.setString(5, transaction.getReference());
			stmt.setLong(6, transaction.getCreePar());
			stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

			int affectedRows = stmt.executeUpdate();

			if (affectedRows > 0) {
				ResultSet generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					transaction.setTransactionId(generatedKeys.getLong(1));
				}
			}

			System.out.println("✅ Transaction enregistrée: " + transaction.getTypeTransaction() + " - "
					+ transaction.getMontant() + " F CFA");

		} catch (SQLException e) {
			System.err.println("❌ Erreur insert Transaction: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Erreur lors de l'insertion de la transaction", e);
		}
		return transaction;
	}

	private Transaction update(Transaction transaction) {
		String sql = """
				UPDATE transactions
				SET type_transaction = ?, montant = ?, compte_id = ?, description = ?, reference = ?,
				    modifie_par = ?, date_modification = ?
				WHERE transaction_id = ?
				""";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, transaction.getTypeTransaction().name());
			stmt.setBigDecimal(2, transaction.getMontant());
			stmt.setLong(3, transaction.getCompteId());
			stmt.setString(4, transaction.getDescription());
			stmt.setString(5, transaction.getReference());

			if (transaction.getModifiePar() != null) {
				stmt.setLong(6, transaction.getModifiePar());
			} else {
				stmt.setNull(6, java.sql.Types.BIGINT);
			}

			stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setLong(8, transaction.getTransactionId());

			int affectedRows = stmt.executeUpdate();

			if (affectedRows == 0) {
				System.err.println("❌ Aucune transaction mise à jour pour l'ID: " + transaction.getTransactionId());
			} else {
				System.out.println("✅ Transaction mise à jour: " + transaction.getTransactionId());
			}

		} catch (SQLException e) {
			System.err.println("❌ Erreur update Transaction: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Erreur lors de la mise à jour de la transaction", e);
		}
		return transaction;
	}

	public boolean delete(Long id) {
		String sql = "DELETE FROM transactions WHERE transaction_id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setLong(1, id);
			int affectedRows = stmt.executeUpdate();

			if (affectedRows > 0) {
				System.out.println("✅ Transaction supprimée: " + id);
				return true;
			} else {
				System.out.println("⚠️ Aucune transaction trouvée avec l'ID: " + id);
				return false;
			}

		} catch (SQLException e) {
			System.err.println("❌ Erreur delete Transaction: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Erreur lors de la suppression de la transaction", e);
		}
	}

	public BigDecimal getTotalByCompteAndTypeBeforeDate(Long compteId, TypeTransaction type, LocalDateTime date) {

		String sql = """
				    SELECT SUM(montant)
				    FROM transactions
				    WHERE compte_id = ?
				    AND type_transaction = ?
				    AND date(date_creation) <= date(?)
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setLong(1, compteId);
			ps.setString(2, type.name());

			// Formater la date pour SQLite
			String dateStr = date.format(DATE_FORMATTER);
			System.out.println("DATE POUR SQLITE: " + dateStr);
			ps.setString(3, dateStr);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				BigDecimal result = rs.getBigDecimal(1);
				return result != null ? result : BigDecimal.ZERO;
			}
		} catch (SQLException e) {
			System.err.println("ERREUR getTotalByCompteAndTypeBeforeDate: " + e.getMessage());
			e.printStackTrace();
		}
		return BigDecimal.ZERO;
	}

	private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
	    Transaction transaction = new Transaction();
	    transaction.setTransactionId(rs.getLong("transaction_id"));

	    // Conversion du String vers TypeTransaction
	    String typeStr = rs.getString("type_transaction");
	    try {
	        transaction.setTypeTransaction(TypeTransaction.valueOf(typeStr.toUpperCase()));
	    } catch (IllegalArgumentException e) {
	        if (typeStr.equalsIgnoreCase("CREDIT")) {
	            transaction.setTypeTransaction(TypeTransaction.CREDIT);
	        } else if (typeStr.equalsIgnoreCase("DEBIT")) {
	            transaction.setTypeTransaction(TypeTransaction.DEBIT);
	        } else {
	            throw new SQLException("Type de transaction non reconnu: " + typeStr);
	        }
	    }

	    transaction.setMontant(rs.getBigDecimal("montant"));
	    transaction.setCompteId(rs.getLong("compte_id"));
	    transaction.setDescription(rs.getString("description"));
	    transaction.setReference(rs.getString("reference"));
	    transaction.setCreePar(rs.getLong("cree_par"));

	    // 🔥 CORRECTION : Utiliser parseFlexibleDate au lieu de parseDateString
	    try {
	        String dateCreationStr = rs.getString("date_creation");
	        if (dateCreationStr != null && !dateCreationStr.trim().isEmpty()) {
	            transaction.setDateCreation(parseFlexibleDate(dateCreationStr));
	        }
	    } catch (SQLException e) {
	        System.err.println("⚠️ Erreur lecture date_creation: " + e.getMessage());
	        transaction.setDateCreation(LocalDateTime.now());
	    }

	    // Gestion de date_modification
	    try {
	        String dateModificationStr = rs.getString("date_modification");
	        if (dateModificationStr != null && !dateModificationStr.trim().isEmpty()) {
	            transaction.setDateModification(parseFlexibleDate(dateModificationStr));
	        }
	    } catch (SQLException e) {
	        // Date modification optionnelle, ignorer si absente
	    }

	    long modifiePar = rs.getLong("modifie_par");
	    if (!rs.wasNull()) {
	        transaction.setModifiePar(modifiePar);
	    }

	    return transaction;
	}

	// Méthode utilitaire pour créer automatiquement une transaction
	public Transaction creerTransactionAutomatique(TypeTransaction type, BigDecimal montant, Long compteId,
			String description, Long utilisateurId) {
		Transaction transaction = new Transaction();
		transaction.setTypeTransaction(type);
		transaction.setMontant(montant);
		transaction.setCompteId(compteId);
		transaction.setDescription(description);
		transaction.setCreePar(utilisateurId);
		transaction.setReference("AUTO-" + System.currentTimeMillis());

		return save(transaction);
	}
	
	/**
	 * Parse une date en gérant plusieurs formats possibles
	 */
	private LocalDateTime parseFlexibleDate(String dateStr) {
	    if (dateStr == null || dateStr.trim().isEmpty()) {
	        return LocalDateTime.now();
	    }
	    
	    try {
	        // Format 1: Timestamp Unix en millisecondes (ex: 1767886227039)
	        if (dateStr.matches("^\\d{13}$")) {
	            long timestamp = Long.parseLong(dateStr);
	            return LocalDateTime.ofInstant(
	                Instant.ofEpochMilli(timestamp), 
	                ZoneId.systemDefault()
	            );
	        }
	        
	        // Format 2: Timestamp Unix en secondes (ex: 1767886227)
	        if (dateStr.matches("^\\d{10}$")) {
	            long timestamp = Long.parseLong(dateStr);
	            return LocalDateTime.ofInstant(
	                Instant.ofEpochSecond(timestamp), 
	                ZoneId.systemDefault()
	            );
	        }
	        
	        // Format 3: ISO DateTime avec T (ex: 2025-12-03T19:57:44.416768400)
	        if (dateStr.contains("T")) {
	            return LocalDateTime.parse(dateStr, ISO_FORMATTER);
	        }
	        
	        // Format 4: SQL DateTime (ex: 2025-12-03 19:57:44)
	        if (dateStr.contains(" ")) {
	            return LocalDateTime.parse(dateStr, TIMESTAMP_FORMATTER);
	        }
	        
	        // Format 5: Date seulement (ex: 2025-12-03)
	        if (dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
	            return LocalDate.parse(dateStr).atStartOfDay();
	        }
	        
	        // Fallback: essayer le parsing par défaut
	        return LocalDateTime.parse(dateStr);
	        
	    } catch (Exception e) {
	        System.err.println("⚠️ Erreur parsing date '" + dateStr + "': " + e.getMessage());
	        return LocalDateTime.now(); // Valeur par défaut
	    }
	}

	/**
	 * Nouvelle méthode pour obtenir le solde actuel d'un compte
	 */
	public BigDecimal getSoldeCompte(Long compteId) {
		String sql = """
				SELECT
				    COALESCE(SUM(CASE WHEN type_transaction = 'CREDIT' THEN montant ELSE 0 END), 0) -
				    COALESCE(SUM(CASE WHEN type_transaction = 'DEBIT' THEN montant ELSE 0 END), 0) as solde
				FROM transactions
				WHERE compte_id = ?
				""";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setLong(1, compteId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getBigDecimal("solde");
			}
		} catch (SQLException e) {
			System.err.println("❌ Erreur getSoldeCompte: " + e.getMessage());
			e.printStackTrace();
		}
		return BigDecimal.ZERO;
	}
}