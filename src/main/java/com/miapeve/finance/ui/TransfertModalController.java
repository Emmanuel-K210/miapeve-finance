package com.miapeve.finance.ui;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.impl.FinanceService;
import com.miapeve.finance.utils.UICommun;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TransfertModalController implements Initializable {
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private ComboBox<Compte> compteSourceCombo;
    @FXML private ComboBox<Compte> compteDestCombo;
    @FXML private TextField montantField;
    @FXML private Label soldeSourceLabel;
    @FXML private Label soldeDestLabel;
    @FXML private VBox resumeBox;
    @FXML private Label resumeLabel;
    @FXML private Button btnValider;
    @FXML private Label messageLabel;
    
    // ==================== SERVICES ====================
    
    private CompteDAO compteDAO;
    private FinanceService financeService;
    private CompteController compteController;
    private Stage stage;
    private MainController mainController;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        compteDAO = new CompteDAO();
        financeService = new FinanceService();
        
        configurerComboBoxes();
        configurerListeners();
        chargerComptes();
    }
    
    // ==================== CONFIGURATION ====================
    
    private void configurerComboBoxes() {
        StringConverter<Compte> converter = new StringConverter<Compte>() {
            @Override
            public String toString(Compte compte) {
                if (compte == null) return "";
                return String.format("%s (%s)", compte.getNomCompte(), compte.getTypeCompte());
            }
            
            @Override
            public Compte fromString(String string) {
                return null;
            }
        };
        
        compteSourceCombo.setConverter(converter);
        compteDestCombo.setConverter(converter);
    }
    
    private void configurerListeners() {
        // Listener pour le compte source
        compteSourceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double solde = newVal.getSolde() != null ? newVal.getSolde().doubleValue() : 0.0;
                soldeSourceLabel.setText(String.format("Solde disponible: %,.0f F CFA", solde));
                soldeSourceLabel.setStyle("-fx-text-fill: -success; -fx-font-weight: 600;");
            } else {
                soldeSourceLabel.setText("Solde disponible: -");
                soldeSourceLabel.setStyle("-fx-text-fill: -text-tertiary;");
            }
            validerFormulaire();
        });
        
        // Listener pour le compte destination
        compteDestCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double solde = newVal.getSolde() != null ? newVal.getSolde().doubleValue() : 0.0;
                soldeDestLabel.setText(String.format("Solde actuel: %,.0f F CFA", solde));
                soldeDestLabel.setStyle("-fx-text-fill: -primary; -fx-font-weight: 600;");
            } else {
                soldeDestLabel.setText("Solde actuel: -");
                soldeDestLabel.setStyle("-fx-text-fill: -text-tertiary;");
            }
            validerFormulaire();
        });
        
        // Listener pour le montant
        montantField.textProperty().addListener((obs, oldVal, newVal) -> {
            validerFormulaire();
        });
        
        // Validation du montant (uniquement des chiffres)
        montantField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                montantField.setText(oldVal);
            }
        });
        
        // Touche Entrée pour valider
        montantField.setOnAction(e -> handleValider());
    }
    
    private void chargerComptes() {
        List<Compte> comptes = compteDAO.findAll();
        compteSourceCombo.getItems().setAll(comptes);
        compteDestCombo.getItems().setAll(comptes);
        
        System.out.println("✅ " + comptes.size() + " comptes chargés dans les combos");
    }
    
    // ==================== VALIDATION ====================
    
    private void validerFormulaire() {
        clearMessage();
        
        Compte source = compteSourceCombo.getValue();
        Compte dest = compteDestCombo.getValue();
        String montantStr = montantField.getText().trim();
        
        boolean valide = false;
        
        if (source != null && dest != null && !montantStr.isEmpty()) {
            // Vérifier que les comptes sont différents
            if (source.getCompteId().equals(dest.getCompteId())) {
                resumeBox.setVisible(false);
                resumeBox.setManaged(false);
                btnValider.setDisable(true);
                showError("Les comptes source et destination doivent être différents");
                return;
            }
            
            try {
                double montant = Double.parseDouble(montantStr);
                double soldeSource = source.getSolde() != null ? source.getSolde().doubleValue() : 0.0;
                
                if (montant > 0 && montant <= soldeSource) {
                    // Afficher le résumé
                    resumeLabel.setText(String.format(
                        "De: %s\nVers: %s\nMontant: %,.0f F CFA\n\n" +
                        "Nouveau solde %s: %,.0f F CFA\n" +
                        "Nouveau solde %s: %,.0f F CFA",
                        source.getNomCompte(),
                        dest.getNomCompte(),
                        montant,
                        source.getNomCompte(),
                        soldeSource - montant,
                        dest.getNomCompte(),
                        (dest.getSolde() != null ? dest.getSolde().doubleValue() : 0.0) + montant
                    ));
                    resumeBox.setVisible(true);
                    resumeBox.setManaged(true);
                    valide = true;
                } else {
                    resumeBox.setVisible(false);
                    resumeBox.setManaged(false);
                    if (montant <= 0) {
                        showError("Le montant doit être supérieur à 0");
                    } else {
                        showError("Solde insuffisant. Solde disponible: " + String.format("%,.0f F CFA", soldeSource));
                    }
                }
            } catch (NumberFormatException e) {
                resumeBox.setVisible(false);
                resumeBox.setManaged(false);
                showError("Veuillez saisir un montant valide");
            }
        } else {
            resumeBox.setVisible(false);
            resumeBox.setManaged(false);
        }
        
        btnValider.setDisable(!valide);
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void handleValider() {
        clearMessage();
        
        Compte source = compteSourceCombo.getValue();
        Compte dest = compteDestCombo.getValue();
        String montantStr = montantField.getText().trim();
        
        // Validation finale
        if (source == null || dest == null || montantStr.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }
        
        if (source.getCompteId().equals(dest.getCompteId())) {
            showError("Veuillez sélectionner deux comptes différents");
            return;
        }
        
        try {
            double montant = Double.parseDouble(montantStr);
            
            if (montant <= 0) {
                showError("Le montant doit être positif");
                return;
            }
            
            double soldeSource = source.getSolde() != null ? source.getSolde().doubleValue() : 0.0;
            if (montant > soldeSource) {
                showError(String.format("Solde insuffisant. Solde disponible: %,.0f F CFA", soldeSource));
                return;
            }
            
            // ✅ Récupérer l'utilisateur connecté (OBJET Utilisateur)
            Utilisateur utilisateur = null;
            if (mainController != null) {
                utilisateur = mainController.getUtilisateurCourant();
            }
            
            if (utilisateur == null) {
                showError("Utilisateur non connecté. Veuillez vous reconnecter.");
                return;
            }
            
            // Confirmation
            if (!UICommun.showConfirmation("Confirmer le transfert", 
                String.format("Êtes-vous sûr de vouloir transférer %,.0f F CFA de %s vers %s ?",
                    montant, source.getNomCompte(), dest.getNomCompte()))) {
                return;
            }
            
            // Effectuer le transfert
            BigDecimal montantBD = BigDecimal.valueOf(montant);
            
            boolean success = financeService.transfert(
                dest.getCompteId(), 
                source.getCompteId(), 
                montantBD, 
                utilisateur.getId()
            );
            
            if (success) {
                UICommun.showSuccess("Transfert", 
                    String.format("Transfert de %,.0f F CFA effectué avec succès.", montant));
                
                if (compteController != null) {
                    compteController.afterCompteOperation();
                }
                
                fermerModal();
            } else {
                showError("Une erreur est survenue lors du transfert");
            }
            
        } catch (NumberFormatException e) {
            showError("Veuillez entrer un montant valide");
        } catch (Exception e) {
            System.err.println("❌ Erreur transfert: " + e.getMessage());
            e.printStackTrace();
            showError("Une erreur est survenue: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAnnuler() {
        fermerModal();
    }
    
    private void fermerModal() {
        if (stage != null) {
            stage.close();
        }
    }
    
    // ==================== MÉTHODES PRIVÉES ====================
    
    private void showError(String message) {
        messageLabel.setText("⚠️ " + message);
        messageLabel.setStyle("-fx-text-fill: -danger; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    private void clearMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
    
    // ==================== SETTERS ====================
    
    public void setCompteController(CompteController compteController) {
        this.compteController = compteController;
        if (compteController != null) {
            this.mainController = compteController.getMainController();
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setCompteSource(Compte compte) {
        compteSourceCombo.setValue(compte);
    }
}