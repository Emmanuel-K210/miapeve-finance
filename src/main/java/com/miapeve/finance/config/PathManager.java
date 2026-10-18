package com.miapeve.finance.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    private static final String APP_NAME = "meapeve finance";
    
    public static Path getAppDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            // Windows: C:\Users\Public\Documents\meapeve finance
            return Paths.get(System.getenv("PUBLIC"), "Documents", APP_NAME);
        } else if (os.contains("mac")) {
            // macOS: /Users/Shared/MonApplication
            return Paths.get("/Users/Shared", APP_NAME);
        } else {
            // Linux: /var/lib/MonApplication
            return Paths.get("/var/lib", APP_NAME);
        }
    }
    
    public static Path getDatabasePath() {
        return getAppDataDirectory().resolve("miapeve_finance.db");
    }
    
    public static Path getConfigPath() {
        return getAppDataDirectory().resolve("config.json");
    }
    
    public static Path getBackupDirectory() {
        return getAppDataDirectory().resolve("backups");
    }
    
    public static void ensureDirectoriesExist() {
        try {
            getAppDataDirectory().toFile().mkdirs();
            getBackupDirectory().toFile().mkdirs();
        } catch (Exception e) {
            throw new RuntimeException("Erreur création répertoires: " + e.getMessage(), e);
        }
    }
}