package com.miapeve.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Paths;

@SpringBootApplication
public class FinanceBackendApplication {
    public static void main(String[] args) {
        ensureDatabaseDirectoryExists();
        SpringApplication.run(FinanceBackendApplication.class, args);
    }

    private static void ensureDatabaseDirectoryExists() {
        String dbPath = System.getenv().getOrDefault("MIAPEVE_DB_PATH", "./data/miapeve_finance.db");
        File parent = Paths.get(dbPath).toAbsolutePath().getParent().toFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
    }
}
