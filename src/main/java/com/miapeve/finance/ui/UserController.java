package com.miapeve.finance.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.impl.AuthenticationService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserController implements Initializable {
    
    @FXML private Label nomUtilisateurLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateCreationLabel;
    
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField ancienMotDePasseField;
    @FXML private PasswordField nouveauMotDePasseField;
    @FXML private PasswordField confirmerMotDePasseField;
    
    private AuthenticationService authService;
    private Utilisateur utilisateurCourant;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authService = new AuthenticationService();
        chargerProfil();
    }
    
    public void setUtilisateurCourant(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        chargerProfil();
    }
    
    private void chargerProfil() {
        if (utilisateurCourant != null) {
            nomUtilisateurLabel.setText(utilisateurCourant.getNomUtilisateur());
            roleLabel.setText(utilisateurCourant.getRole().getLibelle());
            
            nomField.setText(utilisateurCourant.getNom());
            prenomField.setText(utilisateurCourant.getPrenom());
            // emailField.setText(utilisateurCourant.getEmail()); // Si vous ajoutez email plus tard
        }
    }
    
    @FXML
    private void handleModifierProfil() {
        if (validerFormulaireProfil()) {
            // Mettre à jour le profil
            utilisateurCourant.setNom(nomField.getText().trim());
            utilisateurCourant.setPrenom(prenomField.getText().trim());
            
            // TODO: Appeler le service pour sauvegarder
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil modifié avec succès");
        }
    }
    
    @FXML
    private void handleChangerMotDePasse() {
        if (validerFormulaireMotDePasse()) {
            // TODO: Implémenter le changement de mot de passe
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe modifié avec succès");
            clearChampsMotDePasse();
        }
    }
    
    private boolean validerFormulaireProfil() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            return false;
        }
        return true;
    }
    
    private boolean validerFormulaireMotDePasse() {
        if (ancienMotDePasseField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir l'ancien mot de passe");
            return false;
        }
        
        if (nouveauMotDePasseField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir le nouveau mot de passe");
            return false;
        }
        
        if (!nouveauMotDePasseField.getText().equals(confirmerMotDePasseField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas");
            return false;
        }
        
        if (nouveauMotDePasseField.getText().length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }
        
        return true;
    }
    
    private void clearChampsMotDePasse() {
        ancienMotDePasseField.clear();
        nouveauMotDePasseField.clear();
        confirmerMotDePasseField.clear();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}