package com.miapeve.finance.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.miapeve.finance.utils.PasswordHasher;

public class DatabaseManager {
    private static final int CURRENT_DB_VERSION = 1;
    private static String databaseUrl;
    
    private static DatabaseManager instance;
    
    private DatabaseManager() {
        PathManager.ensureDirectoriesExist();
        Path dbPath = PathManager.getDatabasePath();
        DatabaseManager.databaseUrl = "jdbc:sqlite:" + dbPath.toString();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public static void initializeDatabase() {
        System.out.println("Initialisation base de données...");
        getInstance();
        
        try {
            boolean databaseExists = Files.exists(PathManager.getDatabasePath());
            
            if (!databaseExists) {
                createNewDatabase();
            } else {
                checkDatabaseVersion();
            }
            
            System.out.println("Base de données prête!");
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur initialisation BD: " + e.getMessage(), e);
        }
    }
    
    private static void createNewDatabase() throws SQLException {
        System.out.println("Création nouvelle base de données...");
        
        try (Connection conn = getConnection()) {
            // Créer toutes les tables
            createTables(conn);
            
            // Insérer les données par défaut
            insererDonneesParDefaut(conn);
            
            // Définir la version
            setDatabaseVersion(conn, CURRENT_DB_VERSION);
            
            System.out.println("✅ Base de données créée avec succès");
        }
    }
    
    private static void createTables(Connection conn) throws SQLException {
        String[] createTables = {
            // Table db_version
            """
            CREATE TABLE IF NOT EXISTS db_version (
                version INTEGER PRIMARY KEY,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """,
            
            // Table roles
            """
            CREATE TABLE IF NOT EXISTS roles(
               role_id INTEGER PRIMARY KEY AUTOINCREMENT,
               libelle VARCHAR(50) NOT NULL UNIQUE,
               description TEXT,
               cree_par INTEGER,
               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               modifie_par INTEGER,
               date_modification TIMESTAMP NULL,
               FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
                
            // Table utilisateurs 
            """
            CREATE TABLE IF NOT EXISTS utilisateurs (
                utilisateur_id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                prenom TEXT,
                nom_utilisateur TEXT UNIQUE NOT NULL,
                mot_de_passe TEXT NOT NULL,
                role_id INTEGER NOT NULL,
                cree_par INTEGER,
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                modifie_par INTEGER,
                date_modification TIMESTAMP NULL,
                actif BOOLEAN DEFAULT true,
                FOREIGN KEY (role_id) REFERENCES roles(role_id),
                FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
                FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
                
            // Table comptes
            """
            CREATE TABLE IF NOT EXISTS comptes(
               compte_id INTEGER PRIMARY KEY AUTOINCREMENT,
               nom_compte TEXT NOT NULL,
               type_compte VARCHAR(50) NOT NULL,
               solde REAL DEFAULT 0.0,
               cree_par INTEGER NOT NULL,
               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               modifie_par INTEGER,
               date_modification TIMESTAMP NULL,
               FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
            
            // Table programmes
            """
            CREATE TABLE IF NOT EXISTS programmes(
               programme_id INTEGER PRIMARY KEY AUTOINCREMENT,
               type_programme VARCHAR(100) NOT NULL,
               date_programme DATE NOT NULL,
               heure_debut TIME NOT NULL,
               heure_fin TIME NOT NULL,
               cree_par INTEGER NOT NULL,
               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               modifie_par INTEGER,
               date_modification TIMESTAMP NULL,
               est_archive BOOLEAN DEFAULT 0,
               hommes INTEGER DEFAULT 0,
               femmes INTEGER DEFAULT 0,
               jeunes INTEGER DEFAULT 0,
               enfants INTEGER DEFAULT 0,
               utilisateur_id INTEGER NOT NULL,
               FOREIGN KEY(utilisateur_id) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
            
            // Table semences
            """
            CREATE TABLE IF NOT EXISTS semences(
               semence_id INTEGER PRIMARY KEY AUTOINCREMENT,
               type_semence VARCHAR(100) NOT NULL,
               montant REAL NOT NULL,
               date_semence DATE NOT NULL,
               description TEXT,
               programme_id INTEGER NOT NULL,
               compte_id INTEGER NOT NULL,
               cree_par INTEGER NOT NULL,
               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               modifie_par INTEGER,
               date_modification TIMESTAMP NULL,
               FOREIGN KEY(programme_id) REFERENCES programmes(programme_id),
               FOREIGN KEY(compte_id) REFERENCES comptes(compte_id),
               FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
            
            // Table depenses
            """
            CREATE TABLE IF NOT EXISTS depenses(
               depense_id INTEGER PRIMARY KEY AUTOINCREMENT,
               montant REAL NOT NULL,
               libelle VARCHAR(100) NOT NULL,
               date_depense DATE NOT NULL,
               compte_id INTEGER NOT NULL,
               cree_par INTEGER NOT NULL,
               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               modifie_par INTEGER,
               date_modification TIMESTAMP NULL,
               FOREIGN KEY(compte_id) REFERENCES comptes(compte_id),
               FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
            
            // Table transactions (pour l'historique des opérations)
            """
            CREATE TABLE IF NOT EXISTS transactions(
               transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
               type_transaction VARCHAR(20) NOT NULL,
               montant REAL NOT NULL,
               compte_id INTEGER NOT NULL,
               description TEXT,
               reference TEXT,
               cree_par INTEGER NOT NULL,
               date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               modifie_par INTEGER,
               date_modification TIMESTAMP NULL,
               FOREIGN KEY(compte_id) REFERENCES comptes(compte_id),
               FOREIGN KEY (cree_par) REFERENCES utilisateurs(utilisateur_id),
               FOREIGN KEY (modifie_par) REFERENCES utilisateurs(utilisateur_id)
            );
            """,
            
            // Table d'audit (logs des actions importantes)
            """
            CREATE TABLE IF NOT EXISTS audit_logs(
               log_id INTEGER PRIMARY KEY AUTOINCREMENT,
               action_type VARCHAR(50) NOT NULL,
               table_name VARCHAR(50) NOT NULL,
               record_id INTEGER,
               anciennes_valeurs TEXT,
               nouvelles_valeurs TEXT,
               utilisateur_id INTEGER NOT NULL,
               date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
               ip_address VARCHAR(45),
               user_agent TEXT,
               FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(utilisateur_id)
            );
            """
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String createTable : createTables) {
                stmt.executeUpdate(createTable);
            }
            System.out.println("✅ Tables créées avec succès");
        }
    }
    
    private static void insererDonneesParDefaut(Connection conn) throws SQLException {
        // 1. Insérer les rôles (sans audit car c'est l'initialisation)
        insererRoles(conn);
        
        // 2. Insérer l'utilisateur admin
        insererUtilisateurAdmin(conn);
        
        // 3. Insérer un compte par défaut
        insererCompteDefaut(conn);
    }
    
    private static void insererRoles(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO roles 
            (libelle, description) 
            VALUES (?, ?)
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] roles = {
                {"ADMIN", "Administrateur système avec tous les droits"},
                {"PASTEUR", "Pasteur - Accès complet comme l'admin"},
                {"COMPTABLE", "Comptable - Gestion financière uniquement"},
                {"MEMBRE", "Membre standard - Consultation uniquement"}
            };
            
            for (String[] role : roles) {
                pstmt.setString(1, role[0]);
                pstmt.setString(2, role[1]);
                pstmt.executeUpdate();
            }
            System.out.println("✅ Rôles insérés");
        }
    }
    
    private static void insererUtilisateurAdmin(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO utilisateurs 
            (nom, prenom, nom_utilisateur, mot_de_passe, role_id, actif) 
            VALUES (?, ?, ?, ?, (SELECT role_id FROM roles WHERE libelle = ?), ?)
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Utiliser le nouveau PasswordHasher
            String motDePasseHash = PasswordHasher.hashPassword("admin123");
            
            pstmt.setString(1, "Administrateur");
            pstmt.setString(2, "System");
            pstmt.setString(3, "admin");
            pstmt.setString(4, motDePasseHash);
            pstmt.setString(5, "ADMIN");
            pstmt.setBoolean(6, true);
            
            pstmt.executeUpdate();
            
            // Maintenant mettre à jour les champs d'audit avec l'ID de l'admin
            String sqlUpdate = """
                UPDATE utilisateurs 
                SET cree_par = (SELECT utilisateur_id FROM utilisateurs WHERE nom_utilisateur = 'admin'),
                    modifie_par = (SELECT utilisateur_id FROM utilisateurs WHERE nom_utilisateur = 'admin')
                WHERE nom_utilisateur = 'admin'
                """;
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlUpdate);
            }
            
            System.out.println("✅ Utilisateur admin créé avec audit");
            System.out.println("🔑 Identifiants: admin / admin123");
        }
    }
    
   
    
    private static void insererCompteDefaut(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO comptes 
            (nom_compte, type_compte, solde, cree_par) 
            VALUES (?, ?, ?, (SELECT utilisateur_id FROM utilisateurs WHERE nom_utilisateur = 'admin'))
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Caisse Principale");
            pstmt.setString(2, "CAISSE");
            pstmt.setDouble(3, 0.0);
            
            pstmt.executeUpdate();
            System.out.println("✅ Compte par défaut créé avec audit");
        }
    }
    
    private static void setDatabaseVersion(Connection conn, int version) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO db_version (version) VALUES (?)")) {
            pstmt.setInt(1, version);
            pstmt.executeUpdate();
        }
    }
    
    private static void checkDatabaseVersion() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT MAX(version) as version FROM db_version");
            if (rs.next()) {
                int version = rs.getInt("version");
                System.out.println("Version actuelle BD: " + version);
                
                if (version < CURRENT_DB_VERSION) {
                    System.out.println("Mise à jour nécessaire de la version " + version + " à " + CURRENT_DB_VERSION);
                    migrerBaseDeDonnees(version, CURRENT_DB_VERSION);
                } else {
                    System.out.println("Base de données à jour!");
                }
            }
        }
    }
    
    private static void migrerBaseDeDonnees(int versionActuelle, int nouvelleVersion) throws SQLException {
        System.out.println("🔄 Début migration BD de " + versionActuelle + " à " + nouvelleVersion);
        
        try (Connection conn = getConnection()) {
            // Pour l'instant, pas de migration nécessaire puisque tout est créé en v1
            // Mettre à jour la version
            setDatabaseVersion(conn, nouvelleVersion);
            System.out.println("✅ Migration terminée - Version " + nouvelleVersion);
        }
    }
  
    
    public static Connection getConnection() throws SQLException {
        if (databaseUrl == null) {
            getInstance();
        }
        return DriverManager.getConnection(databaseUrl);
    }
    
    // Méthodes utilitaires pour l'audit
    public static void logAudit(String actionType, String tableName, Integer recordId, 
                               String anciennesValeurs, String nouvellesValeurs, 
                               Integer utilisateurId, String ipAddress, String userAgent) {
        String sql = """
            INSERT INTO audit_logs 
            (action_type, table_name, record_id, anciennes_valeurs, nouvelles_valeurs, utilisateur_id, ip_address, user_agent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, actionType);
            pstmt.setString(2, tableName);
            pstmt.setObject(3, recordId);
            pstmt.setString(4, anciennesValeurs);
            pstmt.setString(5, nouvellesValeurs);
            pstmt.setInt(6, utilisateurId);
            pstmt.setString(7, ipAddress);
            pstmt.setString(8, userAgent);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur journalisation audit: " + e.getMessage());
        }
    }
}