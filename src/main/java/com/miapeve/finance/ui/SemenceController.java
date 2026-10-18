package com.miapeve.finance.ui;

import com.miapeve.finance.dao.SemenceDAO;
import com.miapeve.finance.dao.ProgrammeDAO;
import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.enums.TypeSemence;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SemenceController implements CompteChangeListener {
    
    @FXML private ComboBox<TypeSemence> typeSemenceCombo;
    @FXML private TextField montantField;
    @FXML private ComboBox<Programme> programmeCombo;
    @FXML private ComboBox<Compte> compteCombo;
    @FXML private Button validerButton;
    @FXML private Button annulerButton;
    @FXML private Label messageLabel;
    
    private SemenceDAO semenceDAO;
    private ProgrammeDAO programmeDAO;
    private CompteDAO compteDAO;
    private Parent view;
    private final MainController mainController;
    
    public SemenceController(MainController mainController) {
        this.semenceDAO = new SemenceDAO();
        this.programmeDAO = new ProgrammeDAO();
        this.compteDAO = new CompteDAO();
        this.mainController = mainController;
        
        // S'enregistrer comme observer
        if (mainController != null) {
            mainController.addCompteChangeListener(this);
        }
    }
    
    @FXML
    public void initialize() {
        System.out.println("🌱 Initialisation de SemenceController");
        
        try {
            loadTypeSemences();
            loadProgrammes();
            refreshComptes(); // Utiliser refreshComptes
            setupEventHandlers();
        } catch (SQLException e) {
            showError("Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Implémentation de l'interface CompteChangeListener
    @Override
    public void onComptesChanged() throws SQLException {
        System.out.println("🔄 SemenceController: notification de changement de comptes");
        refreshComptes();
    }
    
    private void loadTypeSemences() {
        if (typeSemenceCombo != null) {
            typeSemenceCombo.setItems(FXCollections.observableArrayList(TypeSemence.values()));
            typeSemenceCombo.setCellFactory(param -> new ListCell<TypeSemence>() {
                @Override
                protected void updateItem(TypeSemence item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.name());
                    }
                }
            });
            
            // Définir la cellule du bouton
            typeSemenceCombo.setButtonCell(new ListCell<TypeSemence>() {
                @Override
                protected void updateItem(TypeSemence item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner le type");
                    } else {
                        setText(item.name());
                    }
                }
            });
        }
    }
    
    private void loadProgrammes() throws SQLException {
        if (programmeCombo != null) {
            List<Programme> programmes = programmeDAO.findAll();
            programmeCombo.setItems(FXCollections.observableArrayList(programmes));
            programmeCombo.setCellFactory(param -> new ListCell<Programme>() {
                @Override
                protected void updateItem(Programme item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getTypeProgramme()!= null ? item.getTypeProgramme() : "N/A");
                    }
                }
            });
            
            programmeCombo.setButtonCell(new ListCell<Programme>() {
                @Override
                protected void updateItem(Programme item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un programme");
                    } else {
                        setText(item.getTypeProgramme() != null ? item.getTypeProgramme() : "N/A");
                    }
                }
            });
        }
    }
    
    // Méthode publique pour rafraîchir les comptes
    public void refreshComptes() throws SQLException {
        if (compteCombo != null) {
            List<Compte> comptes = compteDAO.findAll();
            Compte compteSelectionne = compteCombo.getValue(); // Sauvegarder la sélection
            
            compteCombo.setItems(FXCollections.observableArrayList(comptes));
            
            // Restaurer la sélection si elle existe encore dans la nouvelle liste
            if (compteSelectionne != null) {
                compteCombo.getItems().stream()
                    .filter(c -> c.getCompteId().equals(compteSelectionne.getCompteId()))
                    .findFirst()
                    .ifPresent(c -> compteCombo.setValue(c));
            }
            
            // Configurer l'affichage
            compteCombo.setCellFactory(param -> new ListCell<Compte>() {
                @Override
                protected void updateItem(Compte item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(String.format("%s (%,.0f FCFA)", 
                            item.getTypeCompte(), 
                            item.getSolde().doubleValue()));
                    }
                }
            });
            
            compteCombo.setButtonCell(new ListCell<Compte>() {
                @Override
                protected void updateItem(Compte item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un compte");
                    } else {
                        setText(String.format("%s (%,.0f FCFA)", 
                            item.getTypeCompte(), 
                            item.getSolde().doubleValue()));
                    }
                }
            });
            
            System.out.println("🔄 Comptes rafraîchis dans SemenceController: " + comptes.size() + " comptes");
        }
    }
    
    private void setupEventHandlers() {
        if (validerButton != null) {
            validerButton.setOnAction(e -> {
                try {
                    validerSemence();
                } catch (SQLException e1) {
                    showError("Erreur lors de l'enregistrement: " + e1.getMessage());
                    e1.printStackTrace();
                }
            });
        }
        
        if (annulerButton != null) {
            annulerButton.setOnAction(e -> annuler());
        }
        
        if (montantField != null) {
            montantField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*(\\.\\d*)?")) {
                    montantField.setText(oldVal);
                }
            });
        }
    }
    
    @FXML
    private void validerSemence() throws SQLException {
        System.out.println("✅ Validation semence demandée");
        
        try {
            if (validateForm()) {
                Semence semence = new Semence();
                semence.setTypeSemence(typeSemenceCombo.getValue());
                semence.setMontant(new java.math.BigDecimal(montantField.getText()));
                semence.setProgramme(programmeCombo.getValue());
                semence.setCompte(compteCombo.getValue());
                semence.setCreePar(mainController.getUtilisateurCourant().getId());
                
                // Enregistrer la semence
                semenceDAO.save(semence);
                
                // Notifier tous les observers du changement de comptes
                if (mainController != null) {
                    mainController.notifyComptesChanged();
                }
                
                showSuccess("Semence enregistrée avec succès !");
                clearForm();
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void annuler() {
        System.out.println("❌ Annulation semence");
        clearForm();
    }
    
    private boolean validateForm() {
        // Vérifier type semence
        if (typeSemenceCombo.getValue() == null) {
            showError("Veuillez sélectionner le type de semence");
            return false;
        }
        
        // Vérifier montant
        if (montantField.getText().isEmpty()) {
            showError("Veuillez saisir le montant");
            return false;
        }
        
        try {
            double montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                showError("Le montant doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Le montant doit être un nombre valide");
            return false;
        }
        
        // Vérifier programme
        if (programmeCombo.getValue() == null) {
            showError("Veuillez sélectionner un programme");
            return false;
        }
        
        // Vérifier compte
        if (compteCombo.getValue() == null) {
            showError("Veuillez sélectionner un compte");
            return false;
        }
        
        // Vérifier solde du compte
        try {
            double montant = Double.parseDouble(montantField.getText());
            Compte compte = compteCombo.getValue();
            if (montant > compte.getSolde().doubleValue()) {
                showError("Solde insuffisant sur le compte " + compte.getTypeCompte());
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Erreur de format du montant");
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        if (typeSemenceCombo != null) typeSemenceCombo.setValue(null);
        if (montantField != null) montantField.clear();
        if (programmeCombo != null) programmeCombo.setValue(null);
        if (compteCombo != null) compteCombo.setValue(null);
        if (messageLabel != null) messageLabel.setText("");
    }
    
    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            messageLabel.setText(message);
        }
        System.out.println("✅ " + message);
    }
    
    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            messageLabel.setText(message);
        }
        System.err.println("❌ " + message);
    }
    
    // Méthode appelée quand le formulaire devient visible
    public void onShow() {
        try {
            refreshComptes();
            System.out.println("👁️  Formulaire Semence affiché - données rafraîchies");
        } catch (SQLException e) {
            showError("Erreur lors du rafraîchissement des comptes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Parent getView() {
        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/template/fxml/semence-form.fxml"));
                loader.setController(this);
                view = loader.load();
                System.out.println("✅ Vue semence chargée avec succès");
            } catch (IOException e) {
                System.err.println("❌ Erreur chargement semence-form.fxml: " + e.getMessage());
                throw new RuntimeException("Erreur lors du chargement de semence-form.fxml", e);
            }
        }
        return view;
    }
}