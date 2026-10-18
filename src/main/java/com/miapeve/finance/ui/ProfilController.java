package com.miapeve.finance.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.UtilisateurService;
import com.miapeve.finance.service.impl.UtilisateurServiceImpl;
import com.miapeve.finance.utils.UICommun;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProfilController implements Initializable {

    @FXML private Label labelTitre;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldNomUtilisateur;
    @FXML private Label labelRole;
    @FXML private Label labelDateCreation;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnAnnuler;

    private UtilisateurService utilisateurService;
    private Utilisateur utilisateur;
    private MainController mainController;
    private Stage stage;

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        peuplerFormulaire();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurService = new UtilisateurServiceImpl();
    }

    private void peuplerFormulaire() {
        if (utilisateur != null) {
            labelTitre.setText("Profil de " + utilisateur.getPrenom() + " " + utilisateur.getNom());
            fieldNom.setText(utilisateur.getNom());
            fieldPrenom.setText(utilisateur.getPrenom());
            fieldNomUtilisateur.setText(utilisateur.getNomUtilisateur());
            labelRole.setText(utilisateur.getRole() != null ? utilisateur.getRole().getLibelle() : "Non défini");
            labelDateCreation.setText(utilisateur.getDateCreation() != null ? 
                utilisateur.getDateCreation().toString() : "Non disponible");
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (validateForm()) {
            try {
                utilisateur.setNom(fieldNom.getText().trim());
                utilisateur.setPrenom(fieldPrenom.getText().trim());
                utilisateur.setNomUtilisateur(fieldNomUtilisateur.getText().trim());

                Utilisateur utilisateurModifie = utilisateurService.enregistrer(utilisateur);
                
                if (utilisateurModifie != null) {
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", "Profil mis à jour avec succès");
                    
                    // Mettre à jour l'utilisateur courant
                    if (mainController != null) {
                        mainController.setUtilisateurCourant(utilisateurModifie);
                        // Rafraîchir les paramètres si nécessaire
                        mainController.refreshParametres();
                    }
                    
                    if (stage != null) {
                        stage.close();
                    }
                } else {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du profil");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Erreur sauvegarde profil: " + e.getMessage());
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        if (stage != null) {
            stage.close();
        }
    }

    private boolean validateForm() {
        if (fieldNom.getText().trim().isEmpty()) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            fieldNom.requestFocus();
            return false;
        }
        if (fieldPrenom.getText().trim().isEmpty()) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Le prénom est obligatoire");
            fieldPrenom.requestFocus();
            return false;
        }
        if (fieldNomUtilisateur.getText().trim().isEmpty()) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Le nom d'utilisateur est obligatoire");
            fieldNomUtilisateur.requestFocus();
            return false;
        }
        return true;
    }
}