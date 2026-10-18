package com.miapeve.finance.utils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

import com.miapeve.finance.entites.Utilisateur;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

public class UICommun {
    
    // ==================== CONSTANTES ====================
    
    public static final String URLStyle = "/static/css/app.css";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // ==================== ALERTES MODERNES ====================
    
    /**
     * Affiche une alerte moderne alignée sur le design system.
     */
    public static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dialog-modern");
        
        // Appliquer la classe CSS spécifique au type
        String typeClass = getStyleClassForType(type);
        if (typeClass != null && !typeClass.isEmpty()) {
            dialogPane.getStyleClass().add("dialog-" + typeClass);
        }
        
        // Charger le CSS
        loadCss(dialogPane);
        
        // Styliser les boutons
        stylizeButtons(dialogPane, type);
        
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte de confirmation moderne.
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, message, "Confirmer", "Annuler");
    }
    
    /**
     * Affiche une alerte de confirmation moderne avec textes personnalisés.
     */
    public static boolean showConfirmation(String title, String message, String confirmText, String cancelText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dialog-modern");
        dialogPane.getStyleClass().add("dialog-confirmation");
        
        loadCss(dialogPane);
        
        // Personnaliser les boutons
        ButtonType confirmButton = new ButtonType(confirmText, ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelText, ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        // Styliser les boutons
        stylizeButtons(dialogPane, AlertType.CONFIRMATION);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
    
    /**
     * Affiche une alerte de suppression avec un message de confirmation.
     */
    public static boolean confirmDelete(String entityName, String details) {
        String message = String.format("Voulez-vous vraiment supprimer %s : %s ?", entityName, details);
        return showConfirmation("Confirmation de suppression", message, "Supprimer", "Annuler");
    }
    
    /**
     * Affiche une alerte de succès standardisée.
     */
    public static void showSuccess(String operation, String details) {
        String message = String.format("%s effectué avec succès.\n%s", operation, details);
        showAlert(AlertType.INFORMATION, "Succès", message);
    }
    
    /**
     * Affiche une alerte d'erreur standardisée.
     */
    public static void showError(String operation, String error) {
        String message = String.format("Erreur lors de %s :\n%s", operation, error);
        showAlert(AlertType.ERROR, "Erreur", message);
    }
    
    /**
     * Affiche une alerte d'avertissement standardisée.
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }
    
    /**
     * Affiche une alerte d'information standardisée.
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }
    
    // ==================== MÉTHODES PRIVÉES POUR LES ALERTES ====================
    
    private static String getStyleClassForType(AlertType type) {
        switch (type) {
            case ERROR:   return "danger";
            case WARNING: return "warning";
            case INFORMATION: return "info";
            case CONFIRMATION: return "confirmation";
            default: return "";
        }
    }
    
    private static void loadCss(DialogPane dialogPane) {
        try {
            URL cssUrl = UICommun.class.getResource(URLStyle);
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("⚠️ Fichier CSS non trouvé: " + URLStyle);
            }
        } catch (Exception e) {
            System.err.println("⚠️ CSS d'alerte non chargé: " + e.getMessage());
        }
    }
    
    private static void stylizeButtons(DialogPane dialogPane, AlertType type) {
        for (ButtonType buttonType : dialogPane.getButtonTypes()) {
            Node buttonNode = dialogPane.lookupButton(buttonType);
            if (buttonNode instanceof Button) {
                Button button = (Button) buttonNode;
                button.getStyleClass().add("dialog-button");
                
                // Style selon le type de bouton
                if (buttonType.getButtonData() == ButtonData.CANCEL_CLOSE) {
                    button.getStyleClass().add("btn-outline");
                } else if (buttonType.getButtonData() == ButtonData.OK_DONE) {
                    if (type == AlertType.ERROR || type == AlertType.WARNING) {
                        button.getStyleClass().add("btn-danger");
                    } else {
                        button.getStyleClass().add("btn-primary");
                    }
                } else if (buttonType.getButtonData() == ButtonData.YES) {
                    button.getStyleClass().add("btn-danger");
                } else if (buttonType.getButtonData() == ButtonData.NO) {
                    button.getStyleClass().add("btn-outline");
                }
            }
        }
    }
    
    // ==================== MÉTHODES SÉCURISÉES ====================
    
    /**
     * Exécute une opération avec gestion d'erreur sécurisée.
     */
    public static void executeSafe(Runnable operation, String operationName) {
        try {
            operation.run();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de " + operationName + ": " + e.getMessage());
            e.printStackTrace();
            showError(operationName, e.getMessage());
        }
    }
    
    /**
     * Exécute une opération avec gestion d'erreur et callback.
     */
    public static <T> void executeSafe(Consumer<T> operation, T parameter, String operationName) {
        try {
            operation.accept(parameter);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de " + operationName + ": " + e.getMessage());
            e.printStackTrace();
            showError(operationName, e.getMessage());
        }
    }
    
    // ==================== PERMISSIONS ====================
    
    /**
     * Vérifie si l'utilisateur a les permissions d'administration.
     */
    public static boolean hasAdminPermission(Utilisateur utilisateur) {
        return utilisateur != null && 
               utilisateur.getRole() != null && 
               "ADMIN".equalsIgnoreCase(utilisateur.getRole().getLibelle());
    }
    
    /**
     * Vérifie si l'utilisateur a les permissions pour gérer les utilisateurs.
     */
    public static boolean hasUserManagementPermission(Utilisateur utilisateur) {
        return hasAdminPermission(utilisateur);
    }
    
    // ==================== FORMATAGE ====================
    
    /**
     * Formate une date au format dd/MM/yyyy.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Formate un montant en F CFA.
     */
    public static String formatMontant(Double montant) {
        if (montant == null) return "0 F CFA";
        return String.format("%,.0f F CFA", montant);
    }
    
    /**
     * Formate un montant avec couleur (positif/negatif).
     */
    public static String formatMontantWithColor(Double montant) {
        if (montant == null) return "0 F CFA";
        String formatted = String.format("%,.0f F CFA", Math.abs(montant));
        if (montant < 0) {
            return "-" + formatted;
        }
        return "+" + formatted;
    }
    
    /**
     * Vérifie si une chaîne est null ou vide.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Vérifie si une valeur est null ou négative.
     */
    public static boolean isNullOrNegative(Integer value) {
        return value == null || value < 0;
    }
    
    /**
     * Vérifie si une valeur est null ou négative.
     */
    public static boolean isNullOrNegative(Double value) {
        return value == null || value < 0;
    }
    
    // ==================== COMPOSANTS UI ====================
    
    /**
     * Crée un séparateur horizontal avec un label.
     */
    public static Region createDivider() {
        Region divider = new Region();
        divider.setStyle("-fx-background-color: -border-color; -fx-pref-height: 1; -fx-max-height: 1;");
        return divider;
    }
    
    /**
     * Crée une étiquette pour un champ obligatoire.
     */
    public static Label createRequiredLabel(String text) {
        Label label = new Label(text + " *");
        label.getStyleClass().add("form-label-required");
        return label;
    }
    
    /**
     * Crée une étiquette pour un champ.
     */
    public static Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }
    
    // ==================== ANCIENNES MÉTHODES (conservées pour compatibilité) ====================
    
    private static void setupAlertStyles(Alert alert) {
        try {
            URL cssUrl = UICommun.class.getResource(URLStyle);
            if (cssUrl != null) {
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
                dialogPane.setStyle("-fx-pref-width: 400px; -fx-pref-height: 200px;");
            } else {
                System.err.println("⚠️ Fichier CSS non trouvé: " + URLStyle);
            }
        } catch (Exception e) {
            System.err.println("⚠️ CSS d'alerte non chargé: " + e.getMessage());
        }
    }
    
    /**
     * Méthode utilitaire pour afficher une boîte de dialogue de confirmation.
     * @deprecated Utilisez showConfirmation() à la place.
     */
    @Deprecated
    public static boolean showConfirmationDialog(String title, String message) {
        return showConfirmation(title, message);
    }
    
    /**
     * Méthode utilitaire pour afficher une boîte de dialogue d'erreur.
     * @deprecated Utilisez showError() à la place.
     */
    @Deprecated
    public static void showErrorDialog(String title, String message) {
        showError(title, message);
    }
    
    /**
     * Méthode utilitaire pour afficher une boîte de dialogue d'information.
     * @deprecated Utilisez showInfo() à la place.
     */
    @Deprecated
    public static void showInfoDialog(String title, String message) {
        showInfo(title, message);
    }
    
    /**
     * Méthode utilitaire pour afficher une boîte de dialogue d'avertissement.
     * @deprecated Utilisez showWarning() à la place.
     */
    @Deprecated
    public static void showWarningDialog(String title, String message) {
        showWarning(title, message);
    }
}