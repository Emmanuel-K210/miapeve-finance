package com.miapeve.finance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.utils.DateConverter;

public class ProgrammeDAO extends BaseDAO {
    
    public Optional<Programme> findById(Long id) {
        String sql = """
            SELECT p.*, u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            WHERE p.programme_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<Programme> findAll() {
        List<Programme> programmes = new ArrayList<>();
        String sql = """
            SELECT p.*, u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            ORDER BY p.date_programme DESC
            """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    public Programme save(Programme programme) {
        if (programme.getId() == null) {
            return insert(programme);
        } else {
            return update(programme);
        }
    }
    
 // Dans la méthode insert de ProgrammeDAO
    private Programme insert(Programme programme) {
        String sql = """
            INSERT INTO programmes 
            (type_programme, date_programme, heure_debut, heure_fin, 
             utilisateur_id, cree_par, date_creation,hommes,femmes,jeunes,enfants) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ? , ? , ? , ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, programme.getTypeProgramme());
            stmt.setString(2, DateConverter.localDateToString(programme.getDateProgramme()));
            stmt.setString(3, DateConverter.localTimeToString(programme.getHeureDebut()));
            stmt.setString(4, DateConverter.localTimeToString(programme.getHeureFin()));
            stmt.setLong(5, programme.getUtilisateur().getId());
            stmt.setLong(6, programme.getCreePar());
            stmt.setString(7, LocalDateTime.now().toString());
            stmt.setInt(8, programme.getHommes());
            stmt.setInt(9, programme.getFemmes());
            stmt.setInt(10, programme.getJeunes());
            stmt.setInt(11, programme.getEnfants());
            
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                programme.setId(generatedKeys.getLong(1));
            }
            
            System.out.println("✅ Programme créé: " + programme.getTypeProgramme());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert programme: " + e.getMessage());
            e.printStackTrace();
        }
        return programme;
    }
    
    private Programme update(Programme programme) {
        String sql = """
            UPDATE programmes 
            SET type_programme = ?, date_programme = ?, heure_debut = ?, heure_fin = ?, utilisateur_id = ? ,
            modifie_par = ?,hommes = ?,femmes = ?,jeunes= ?,enfants = ?,date_modification = ?
            WHERE programme_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, programme.getTypeProgramme());
            stmt.setString(2, DateConverter.localDateToString(programme.getDateProgramme()));
            stmt.setString(3, DateConverter.localTimeToString(programme.getHeureDebut()));
            stmt.setString(4, DateConverter.localTimeToString(programme.getHeureFin()));
            stmt.setLong(5, programme.getUtilisateur().getId());
            stmt.setLong(6, programme.getModifiePar());
            stmt.setInt(7, programme.getHommes());
            stmt.setInt(8, programme.getFemmes());
            stmt.setInt(9, programme.getJeunes());
            stmt.setInt(10, programme.getEnfants());
            stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));  
            stmt.setLong(12, programme.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programme;
    }
    
    private Programme mapResultSetToProgramme(ResultSet rs) throws SQLException {
        Programme programme = new Programme();
        programme.setId(rs.getLong("programme_id"));
        programme.setTypeProgramme(rs.getString("type_programme"));
        
        // Conversion String → LocalDate (depuis SQLite)
        String dateString = rs.getString("date_programme");
        programme.setDateProgramme(DateConverter.stringToLocalDate(dateString));
        
        // Conversion String → LocalTime (depuis SQLite)
        String timeString = rs.getString("heure_debut");
        programme.setHeureDebut(DateConverter.stringToLocalTime(timeString));
        
        programme.setHeureFin(DateConverter.stringToLocalTime(rs.getString("heure_fin")));
        //DENOMBREMENT
        programme.setHommes(rs.getInt("hommes"));
        programme.setFemmes(rs.getInt("femmes"));        
        programme.setEnfants(rs.getInt("enfants"));
        programme.setJeunes(rs.getInt("jeunes"));
        // Mapping de l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getLong("utilisateur_id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setNomUtilisateur(rs.getString("nom_utilisateur"));
        
        
        programme.setUtilisateur(utilisateur);
        
        return programme;
    }
    
    // Méthodes spécifiques pour requêter par date
    public List<Programme> findByDate(LocalDate date) {
        List<Programme> programmes = new ArrayList<>();
        String sql = "SELECT p.* FROM programmes p WHERE p.date_programme = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(date));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    /**
     * Récupère tous les programmes d'un mois spécifique
     * @param date Une date quelconque dans le mois recherché
     * @return Liste des programmes du mois
     */
    public List<Programme> listeDesProgrammesParMois(LocalDate date) {
        List<Programme> programmes = new ArrayList<>();
        
        // Calculer le premier et dernier jour du mois
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        String sql = """
            SELECT p.*, 
                   u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            WHERE p.date_programme BETWEEN ? AND ?
            ORDER BY p.date_programme ASC, p.heure_debut ASC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(debutMois));
            stmt.setString(2, DateConverter.localDateToString(finMois));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    /**
     * Récupère les programmes par mois et année spécifiques
     * @param mois Mois (1-12)
     * @param annee Année
     * @return Liste des programmes du mois/année
     */
    public List<Programme> listeDesProgrammesParMois(int mois, int annee) {
        LocalDate date = LocalDate.of(annee, mois, 1);
        return listeDesProgrammesParMois(date);
    }
    
    /**
     * Récupère les programmes du mois en cours
     * @return Liste des programmes du mois en cours
     */
    public List<Programme> listeDesProgrammesMoisEnCours() {
        return listeDesProgrammesParMois(LocalDate.now());
    }
    
    /**
     * Récupère les programmes d'un utilisateur spécifique pour un mois donné
     * @param date Date dans le mois recherché
     * @param utilisateurId ID de l'utilisateur
     * @return Liste des programmes de l'utilisateur pour le mois
     */
    public List<Programme> listeDesProgrammesParMoisEtUtilisateur(LocalDate date, Long utilisateurId) {
        List<Programme> programmes = new ArrayList<>();
        
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        String sql = """
            SELECT p.*, 
                   u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            WHERE p.date_programme BETWEEN ? AND ? 
            AND p.utilisateur_id = ?
            ORDER BY p.date_programme ASC, p.heure_debut ASC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(debutMois));
            stmt.setString(2, DateConverter.localDateToString(finMois));
            stmt.setLong(3, utilisateurId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    /**
     * Compte le nombre de programmes par mois
     * @param date Date dans le mois recherché
     * @return Nombre de programmes du mois
     */
    public int countProgrammesParMois(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        String sql = "SELECT COUNT(*) as total FROM programmes WHERE date_programme BETWEEN ? AND ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(debutMois));
            stmt.setString(2, DateConverter.localDateToString(finMois));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Récupère les statistiques des programmes par mois
     * @param date Date dans le mois recherché
     * @return Tableau [totalProgrammes, programmesAvecSemences, programmesComplets]
     */
    public int[] getStatistiquesMois(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        String sql = """
            SELECT 
                COUNT(p.programme_id) as total,
                COUNT(DISTINCT s.programme_id) as avec_semences,
                SUM(CASE WHEN p.heure_fin IS NOT NULL AND p.heure_fin != '' THEN 1 ELSE 0 END) as complets
            FROM programmes p
            LEFT JOIN semences s ON p.programme_id = s.programme_id
            WHERE p.date_programme BETWEEN ? AND ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(debutMois));
            stmt.setString(2, DateConverter.localDateToString(finMois));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new int[] {
                    rs.getInt("total"),
                    rs.getInt("avec_semences"),
                    rs.getInt("complets")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[] {0, 0, 0};
    }
    
    public List<Programme> findByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        List<Programme> programmes = new ArrayList<>();
        String sql = """
            SELECT p.*, 
                   u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            WHERE p.date_programme BETWEEN ? AND ?
            ORDER BY p.date_programme ASC, p.heure_debut ASC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(dateDebut));
            stmt.setString(2, DateConverter.localDateToString(dateFin));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    public List<Programme> findByPeriodeEtUtilisateur(LocalDate dateDebut, LocalDate dateFin, Long utilisateurId) {
        List<Programme> programmes = new ArrayList<>();
        String sql = """
            SELECT p.*, 
                   u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            WHERE p.date_programme BETWEEN ? AND ? 
            AND p.utilisateur_id = ?
            ORDER BY p.date_programme ASC, p.heure_debut ASC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(dateDebut));
            stmt.setString(2, DateConverter.localDateToString(dateFin));
            stmt.setLong(3, utilisateurId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    public List<Programme> findByUtilisateur(Long utilisateurId) {
        List<Programme> programmes = new ArrayList<>();
        String sql = """
            SELECT p.*, 
                   u.utilisateur_id, u.nom, u.prenom, u.nom_utilisateur, 
                   r.role_id, r.libelle as role_libelle
            FROM programmes p
            INNER JOIN utilisateurs u ON p.utilisateur_id = u.utilisateur_id
            INNER JOIN roles r ON u.role_id = r.role_id
            WHERE p.utilisateur_id = ?
            ORDER BY p.date_programme DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, utilisateurId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                programmes.add(mapResultSetToProgramme(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programmes;
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM programmes WHERE programme_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Méthode utilitaire pour compter les programmes
    public int countByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        String sql = "SELECT COUNT(*) as total FROM programmes WHERE date_programme BETWEEN ? AND ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, DateConverter.localDateToString(dateDebut));
            stmt.setString(2, DateConverter.localDateToString(dateFin));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    
}