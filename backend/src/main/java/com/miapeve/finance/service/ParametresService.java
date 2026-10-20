package com.miapeve.finance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ParametresService {

    private static final byte[] SQLITE_HEADER = "SQLite format 3\u0000".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final String PENDING_RESTORE_SUFFIX = ".pending-restore";

    private final DataSource dataSource;
    private final String dbPath;

    public ParametresService(DataSource dataSource, @Value("${miapeve.db-path}") String dbPath) {
        this.dataSource = dataSource;
        this.dbPath = dbPath;
    }

    public record Sauvegarde(String nomFichier, byte[] contenu) {
    }

    public Sauvegarde creerSauvegarde() {
        try {
            Path temp = Files.createTempFile("miapeve-backup-", ".db");
            Files.delete(temp);
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                String chemin = temp.toAbsolutePath().toString().replace("'", "''");
                statement.execute("VACUUM INTO '" + chemin + "'");
                byte[] contenu = Files.readAllBytes(temp);
                String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                return new Sauvegarde("miapeve-finance-backup-" + horodatage + ".db", contenu);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Impossible de créer la sauvegarde: " + e.getMessage(), e);
        }
    }

    public void stagerRestauration(MultipartFile fichier) {
        if (fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier de sauvegarde est vide");
        }
        try {
            byte[] entete = fichier.getInputStream().readNBytes(SQLITE_HEADER.length);
            if (!java.util.Arrays.equals(entete, SQLITE_HEADER)) {
                throw new IllegalArgumentException("Ce fichier n'est pas une base de données SQLite valide");
            }

            Path pending = pendingRestorePath();
            Files.copy(fichier.getInputStream(), pending, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer la sauvegarde à restaurer: " + e.getMessage(), e);
        }
    }

    private Path pendingRestorePath() {
        Path db = Paths.get(dbPath);
        return db.resolveSibling(db.getFileName().toString() + PENDING_RESTORE_SUFFIX);
    }
}
