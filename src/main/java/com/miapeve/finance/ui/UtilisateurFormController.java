package com.miapeve.finance.ui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Role;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.RoleService;
import com.miapeve.finance.service.UtilisateurService;
import com.miapeve.finance.service.impl.RoleServiceImpl;
import com.miapeve.finance.service.impl.UtilisateurServiceImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UtilisateurFormController implements Initializable {

    @FXML private Label titreLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField nomUtilisateurField;
    @FXML private PasswordField motDePasseField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private CheckBox actifCheckBox;
    @FXML private Button validerButton;
    @FXML private Button annulerButton;
    @FXML private Label messageLabel;

    private UtilisateurService utilisateurService;
    private RoleService roleService;
    private Utilisateur utilisateur;
    private UtilisateurController utilisateurController;
    private Stage stage;

    public void setUtilisateurController(UtilisateurController utilisateurController) {
        this.utilisateurController = utilisateurController;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        peuplerFormulaire();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurService = new UtilisateurServiceImpl();
        roleService = new RoleServiceImpl();
        chargerRoles();
    }
    
  

    private void chargerRoles() {
        try {
            List<Role> roles = roleService.findAll();
            ObservableList<Role> rolesList = FXCollections.observableArrayList(roles);
            roleCombo.setItems(rolesList);

            roleCombo.setCellFactory(param -> new javafx.scene.control.ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getLibelle() + " - " + item.getDescription());
                    }
                }
            });

            roleCombo.setButtonCell(new javafx.scene.control.ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un rôle");
                    } else {
                        setText(item.getLibelle());
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement des rôles: " + e.getMessage());
        }
    }

    private void peuplerFormulaire() {
        if (utilisateur == null) {
            // Mode création
            titreLabel.setText("Nouvel Utilisateur");
            validerButton.setText("Créer");
            actifCheckBox.setSelected(true);
            motDePasseField.setVisible(true);
        } else {
            // Mode édition
            titreLabel.setText("Modifier l'Utilisateur");
            validerButton.setText("Modifier");
            nomField.setText(utilisateur.getNom());
            prenomField.setText(utilisateur.getPrenom());
            nomUtilisateurField.setText(utilisateur.getNomUtilisateur());
            roleCombo.setValue(utilisateur.getRole());
            actifCheckBox.setSelected(utilisateur.isActif());
            motDePasseField.setVisible(false);
        }
    }

    @FXML
    private void handleValider() {
        if (validateForm()) {
            try {
                Utilisateur utilisateurToSave = (utilisateur != null) ? utilisateur : new Utilisateur();

                // Mettre à jour les informations
                utilisateurToSave.setNom(nomField.getText());
                utilisateurToSave.setPrenom(prenomField.getText());
                utilisateurToSave.setNomUtilisateur(nomUtilisateurField.getText());
                utilisateurToSave.setRole(roleCombo.getValue());
                utilisateurToSave.setActif(actifCheckBox.isSelected());

                Utilisateur savedUtilisateur;
                if (utilisateur == null) {
                    // Création avec mot de passe
                    savedUtilisateur = utilisateurService.enregistrer(utilisateurToSave, motDePasseField.getText());
                } else {
                    // Édition sans changer le mot de passe
                    savedUtilisateur = utilisateurService.enregistrer(utilisateurToSave);
                }

                if (savedUtilisateur != null) {
                    showSuccess("Utilisateur " + (utilisateur != null ? "modifié" : "créé") + " avec succès !");
                    
                    // Rafraîchir la liste
                    if (utilisateurController != null) {
                        utilisateurController.refreshData();
                    }
                    
                    stage.close();
                } else {
                    showError("Erreur lors de la sauvegarde de l'utilisateur.");
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur sauvegarde utilisateur: " + e.getMessage());
                showError("Erreur: " + e.getMessage());
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
        messageLabel.setText("");

        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire.");
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire.");
            return false;
        }

        if (nomUtilisateurField.getText().trim().isEmpty()) {
            showError("Le nom d'utilisateur est obligatoire.");
            return false;
        }

        if (utilisateur == null && motDePasseField.getText().isEmpty()) {
            showError("Le mot de passe est obligatoire pour la création.");
            return false;
        }

        if (utilisateur == null && motDePasseField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }

        if (roleCombo.getValue() == null) {
            showError("Veuillez sélectionner un rôle.");
            return false;
        }

        return true;
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

	public void setParametresController(ParametresController parametresController) {
		
		
	}
}