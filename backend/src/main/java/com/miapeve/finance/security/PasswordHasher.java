package com.miapeve.finance.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Identique à l'implémentation de l'ancienne application JavaFX :
 * conservé pour rester compatible avec les mots de passe déjà stockés
 * dans la base SQLite existante.
 */
public final class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {
    }

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme de hachage non disponible", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());

            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashedPassword[i] != combined[i + SALT_LENGTH]) {
                    return false;
                }
            }
            return true;
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }
}
