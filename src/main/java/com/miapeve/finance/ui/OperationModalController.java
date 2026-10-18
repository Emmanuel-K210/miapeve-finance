package com.miapeve.finance.ui;

import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.impl.FinanceService;
import com.miapeve.finance.utils.UICommun;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class OperationModalController {

    // ==================== FXML COMPONENTS ====================
    
    @FXML private StackPane iconWrapper;
    @FXML private FontAwesomeIconView operationIcon;
    @FXML private Label titreLabel;
    @FXML private Label sousTitreLabel;
    @FXML private Label soldeActuelLabel;
    @FXML private TextField montantField;
    @FXML private TextField descriptionField;
    @FXML private Label messageLabel;
    @FXML private Button validerButton;
    
    // ==================== DONNÉES ====================
    
    private Compte compte;
    private String typeOperation; // "CREDIT" ou "DEBIT"
    private CompteController compteController;
    private Stage stage;
    private FinanceService financeService;
    
    // ==================== INITIALIZATION ====================
    
    @FXML
    public void initialize() {
        financeService = new FinanceService();
        
        // Validation du montant (uniquement des chiffres)
        montantField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                montantField.setText(oldVal);
            }
        });
        
        // Touche Entrée pour valider
        montantField.setOnAction(e -> handleValider());
        descriptionField.setOnAction(e -> handleValider());
    }
    
    // ==================== SETTERS ====================
    
    public void setCompte(Compte compte) {
        this.compte = compte;
        if (compte != null) {
            sousTitreLabel.setText("Compte: " + compte.getTypeCompte());
            double solde = compte.getSolde() != null ? compte.getSolde().doubleValue() : 0.0;
            soldeActuelLabel.setText(String.format("%,.0f F CFA", solde));
        }
    }
    
    public void setTypeOperation(String typeOperation) {
        this.typeOperation = typeOperation;
        if ("CREDIT".equals(typeOperation)) {
            titreLabel.setText("Créditer le compte");
            iconWrapper.getStyleClass().add("success");
            operationIcon.setGlyphName("ARROW_UP");
            validerButton.setText("Créditer");
            validerButton.getStyleClass().add("btn-success");
        } else {
            titreLabel.setText("Débiter le compte");
            iconWrapper.getStyleClass().add("danger");
            operationIcon.setGlyphName("ARROW_DOWN");
            validerButton.setText("Débiter");
            validerButton.getStyleClass().add("btn-danger");
        }
    }
    
    public void setCompteController(CompteController compteController) {
        this.compteController = compteController;
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void handleValider() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        
        // Vérifier le montant
        String montantStr = montantField.getText();
        if (montantStr == null || montantStr.trim().isEmpty()) {
            showError("Veuillez saisir un montant");
            montantField.requestFocus();
            return;
        }
        
        double montant;
        try {
            montant = Double.parseDouble(montantStr.trim());
        } catch (NumberFormatException e) {
            showError("Montant invalide");
            montantField.requestFocus();
            return;
        }
        
        if (montant <= 0) {
            showError("Le montant doit être supérieur à 0");
            montantField.requestFocus();
            return;
        }
        
        // Vérifier le solde pour un débit
        if ("DEBIT".equals(typeOperation)) {
            double solde = compte.getSolde() != null ? compte.getSolde().doubleValue() : 0.0;
            if (solde < montant) {
                showError("Solde insuffisant. Solde actuel: " + String.format("%,.0f F CFA", solde));
                montantField.requestFocus();
                return;
            }
        }
        
        // ✅ Récupérer l'utilisateur connecté (OBJET Utilisateur, pas Long)
        Utilisateur utilisateur = null;
        if (compteController != null && compteController.getMainController() != null) {
            utilisateur = compteController.getMainController().getUtilisateurCourant();
        }
        
        if (utilisateur == null) {
            showError("Utilisateur non connecté. Veuillez vous reconnecter.");
            return;
        }
        
        // ✅ Effectuer l'opération avec l'objet Utilisateur
        boolean success;
        if ("CREDIT".equals(typeOperation)) {
            success = financeService.crediterCompte(compte.getCompteId(), montant, utilisateur);
        } else {
            success = financeService.debiterCompte(compte.getCompteId(), montant, utilisateur);
        }
        
        if (success) {
            // Rafraîchir la liste
            if (compteController != null) {
                compteController.refreshData();
                compteController.notifyComptesChanged();
            }
            
            // Message de succès
            String msg = String.format("Opération %s de %,.0f F CFA effectuée avec succès.", 
                typeOperation.toLowerCase(), montant);
            UICommun.showSuccess("Succès", msg);
            
            // Fermer le modal
            stage.close();
        } else {
            showError("Erreur lors de l'opération. Veuillez réessayer.");
        }
    }
    
    @FXML
    private void handleAnnuler() {
        stage.close();
    }
    
    // ==================== MÉTHODES PRIVÉES ====================
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: -danger; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}