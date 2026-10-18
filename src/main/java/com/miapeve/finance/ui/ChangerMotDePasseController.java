package com.miapeve.finance.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.UtilisateurService;
import com.miapeve.finance.service.impl.UtilisateurServiceImpl;
import com.miapeve.finance.utils.UICommun; // AJOUT

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangerMotDePasseController implements Initializable {

    @FXML private Label labelUtilisateur;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;
    @FXML private Label messageLabel;

    private UtilisateurService utilisateurService;
    private Utilisateur utilisateur;
    private Stage stage;

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        updateInterface();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurService = new UtilisateurServiceImpl();
        setupEventHandlers();
    }

    private void updateInterface() {
        if (utilisateur != null) {
            labelUtilisateur.setText("Changer le mot de passe de " + utilisateur.getPrenom() + " " + utilisateur.getNom());
        }
    }

    private void setupEventHandlers() {
        // Validation en temps réel
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validerNouveauMotDePasse();
        });

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validerConfirmationMotDePasse();
        });

        // CORRECTION : Activer/désactiver le bouton valider en fonction de la validation
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateBoutonValider();
        });

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateBoutonValider();
        });

        currentPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateBoutonValider();
        });
    }

    private void updateBoutonValider() {
        boolean champsRemplis = !currentPasswordField.getText().isEmpty() &&
                               !newPasswordField.getText().isEmpty() &&
                               !confirmPasswordField.getText().isEmpty();
        
        boolean motsDePasseValides = newPasswordField.getText().equals(confirmPasswordField.getText()) &&
                                    newPasswordField.getText().length() >= 6;

        btnValider.setDisable(!(champsRemplis && motsDePasseValides));
    }

    @FXML
    private void handleValider() {
        if (validateForm()) {
            try {
                // Vérifier l'ancien mot de passe
                boolean passwordCorrect = utilisateurService.verifierMotDePasse(
                    utilisateur.getNomUtilisateur(), 
                    currentPasswordField.getText()
                );

                if (!passwordCorrect) {
                    showError("Le mot de passe actuel est incorrect.");
                    return;
                }

                // Vérifier que le nouveau mot de passe est différent de l'ancien
                if (currentPasswordField.getText().equals(newPasswordField.getText())) {
                    showError("Le nouveau mot de passe doit être différent de l'ancien.");
                    return;
                }

                // Changer le mot de passe
                boolean success = utilisateurService.changerMotDePasse(
                    utilisateur.getId(), 
                    newPasswordField.getText()
                );

                if (success) {
                    // CORRECTION : Utiliser UICommun au lieu de showSuccess
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", "Mot de passe changé avec succès !");
                    stage.close();
                } else {
                    showError("Erreur lors du changement de mot de passe.");
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur changement mot de passe: " + e.getMessage());
                // CORRECTION : Utiliser UICommun pour les erreurs critiques
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors du changement de mot de passe: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        // CORRECTION : Demander confirmation avant annulation
        if (champsModifies()) {
            if (UICommun.showConfirmation("Annuler", 
                "Des modifications ont été effectuées. Voulez-vous vraiment annuler ?")) {
                stage.close();
            }
        } else {
            stage.close();
        }
    }

    private boolean champsModifies() {
        return !currentPasswordField.getText().isEmpty() ||
               !newPasswordField.getText().isEmpty() ||
               !confirmPasswordField.getText().isEmpty();
    }

    private boolean validateForm() {
        messageLabel.setText("");

        // Vérifier mot de passe actuel
        if (currentPasswordField.getText().isEmpty()) {
            showError("Veuillez saisir votre mot de passe actuel.");
            return false;
        }

        // Vérifier nouveau mot de passe
        if (newPasswordField.getText().isEmpty()) {
            showError("Veuillez saisir le nouveau mot de passe.");
            return false;
        }

        if (newPasswordField.getText().length() < 6) {
            showError("Le nouveau mot de passe doit contenir au moins 6 caractères.");
            return false;
        }

        // Vérifier confirmation
        if (confirmPasswordField.getText().isEmpty()) {
            showError("Veuillez confirmer le nouveau mot de passe.");
            return false;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            return false;
        }

        return true;
    }

    private void validerNouveauMotDePasse() {
        String password = newPasswordField.getText();
        if (password.length() > 0 && password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
        } else if (password.length() >= 6) {
            clearError();
        }
    }

    private void validerConfirmationMotDePasse() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (confirmPassword.length() > 0 && !newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
        } else if (confirmPassword.length() > 0 && newPassword.equals(confirmPassword)) {
            clearError();
        }
    }

   
    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void clearError() {
        messageLabel.setStyle("");
        messageLabel.setText("");
    }


}