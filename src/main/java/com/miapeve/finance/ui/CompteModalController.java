package com.miapeve.finance.ui;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.enums.TypeCompte;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CompteModalController {
    
    @FXML private Label titreLabel;
    @FXML private ComboBox<TypeCompte> typeCompteCombo;
    @FXML private TextField soldeField;
    @FXML private Button validerButton;
    @FXML private Button annulerButton;
    @FXML private Label messageLabel;
    
    private CompteDAO compteDAO;
    private Compte compte; 
    private Stage stage;
    private CompteController compteController; 
    
    public CompteModalController() {
        this.compteDAO = new CompteDAO();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setCompteController(CompteController compteController) {
        this.compteController = compteController;
    }
    
    public void setCompte(Compte compte) {
        this.compte = compte;
        peuplerFormulaire();
    }
    
    @FXML
    public void initialize() {
        System.out.println("💰 Initialisation de CompteModalController");
        
        // Peupler les types de comptes
        typeCompteCombo.getItems().addAll(TypeCompte.values());
        
        // Définir le texte d'affichage pour les types
        typeCompteCombo.setCellFactory(param -> new javafx.scene.control.ListCell<TypeCompte>() {
            @Override
            protected void updateItem(TypeCompte item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.name());
                }
            }
        });
        
        typeCompteCombo.setButtonCell(new javafx.scene.control.ListCell<TypeCompte>() {
            @Override
            protected void updateItem(TypeCompte item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionner le type");
                } else {
                    setText(item.name());
                }
            }
        });
        
        // Validation du solde
        soldeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                soldeField.setText(oldVal);
            }
        });
    }
    
    private void peuplerFormulaire() {
        if (compte == null) {
            // Mode création
            titreLabel.setText("Nouveau Compte");
            validerButton.setText("Créer");
            soldeField.setText("0"); // Solde par défaut
            System.out.println("🆕 Mode création nouveau compte");
        } else {
            // Mode édition
            titreLabel.setText("Modifier le Compte");
            validerButton.setText("Modifier");
            typeCompteCombo.setValue(TypeCompte.valueOf(compte.getTypeCompte()));
            soldeField.setText(compte.getSolde().toString());
            System.out.println("✏️ Mode édition - Compte: " + compte.getTypeCompte());
        }
    }
    
    @FXML
    private void handleValider() throws SQLException {
        try {
            System.out.println("💾 Tentative de sauvegarde du compte...");
            
            if (validateForm()) {
                // Créer ou récupérer le compte
                Compte compteToSave = (compte != null) ? compte : new Compte();
                
                // Récupérer les valeurs du formulaire
                String typeCompte = typeCompteCombo.getValue().name();
                BigDecimal solde = new BigDecimal(soldeField.getText());
                
                // Générer le nom automatiquement : "Compte " + TYPE_COMPTE
                String nomCompte = "Compte " + typeCompte;
                
                // Mettre à jour le compte
                compteToSave.setTypeCompte(typeCompte);
                compteToSave.setSolde(solde);
                compteToSave.setNomCompte(nomCompte);
                compteToSave.setDateCreation(LocalDateTime.now());
                compteToSave.setCreePar(compteController.getMainController().getUtilisateurCourant().getId());
                compteToSave.setDateModification(LocalDateTime.now());
                compteToSave.setModifiePar(compteController.getMainController().getUtilisateurCourant().getId());
                System.out.println("📦 Compte à sauvegarder:");
                System.out.println("   - Nom: " + nomCompte);
                System.out.println("   - Type: " + typeCompte);
                System.out.println("   - Solde: " + solde + " F CFA");
                System.out.println("   - Mode: " + (compte != null ? "Édition" : "Création"));
                
                // Sauvegarder en base
                var success = compteDAO.save(compteToSave);
                
                if (success!=null) {
                    String message = "Compte " + (compte != null ? "modifié" : "créé") + " avec succès !";
                    showSuccess(message);
                    System.out.println("✅ " + message);
                    
                    // Rafraîchir la liste dans le contrôleur parent
                    if (compteController != null) {
                        compteController.refreshData();
                        System.out.println("🔄 Liste des comptes rafraîchie");
                    }
                    
                    // Fermer le modal
                    stage.close();
                    System.out.println("✅ Modal fermé");
                    
                } else {
                    String errorMessage = "Erreur lors de la sauvegarde du compte";
                    showError(errorMessage);
                    System.err.println("❌ " + errorMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur critique sauvegarde compte: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAnnuler() {
        System.out.println("❌ Annulation création/modification compte");
        stage.close();
    }
    
    private boolean validateForm() {
        messageLabel.setText(""); // Réinitialiser le message
        
        // Vérifier type de compte
        if (typeCompteCombo.getValue() == null) {
            showError("Veuillez sélectionner un type de compte");
            return false;
        }
        
        // Vérifier solde
        if (soldeField.getText().isEmpty()) {
            showError("Veuillez saisir le solde initial");
            return false;
        }
        
        try {
            BigDecimal solde = new BigDecimal(soldeField.getText());
            if (solde.compareTo(BigDecimal.ZERO) < 0) {
                showError("Le solde ne peut pas être négatif");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Le solde doit être un nombre valide");
            return false;
        }
        
        System.out.println("✅ Formulaire validé avec succès");
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
    
}