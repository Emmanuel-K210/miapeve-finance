package com.miapeve.finance.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";

    /**
     * Hache un mot de passe avec un salt aléatoire
     */
    public static String hashPassword(String password) {
        try {
            // Générer un salt aléatoire
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hacher le mot de passe avec le salt
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());

            // Combiner salt et mot de passe haché
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme de hachage non disponible", e);
        }
    }

    /**
     * Vérifie si un mot de passe correspond au hash stocké
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Décoder le hash stocké
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // Extraire le salt (premiers SALT_LENGTH bytes)
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // Hacher le mot de passe fourni avec le salt extrait
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());

            // Comparer avec le hash stocké (derniers bytes)
            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashedPassword[i] != combined[i + SALT_LENGTH]) {
                    return false;
                }
            }
            return true;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme de hachage non disponible", e);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Format de hash invalide: " + e.getMessage());
            return false;
        }
    }

    /**
     * Version simple de hachage (pour compatibilité avec l'existant)
     */
    public static String hashSimple(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hachage", e);
        }
    }

    /**
     * Vérification simple (pour compatibilité avec l'existant)
     */
    public static boolean verifySimple(String password, String storedHash) {
        String hashedInput = hashSimple(password);
        return hashedInput.equals(storedHash);
    }
}