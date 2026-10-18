package com.miapeve.finance.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.service.ImpressionService;
import com.miapeve.finance.service.ProgrammeService;
import com.miapeve.finance.service.impl.ImpressionServiceImpl;
import com.miapeve.finance.service.impl.ProgrammeServiceImpl;
import com.miapeve.finance.utils.UICommun;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ImpressionController implements Initializable {

    // Onglets
    @FXML private TabPane rapportsTabPane;
    
    // Table Programme
    @FXML private TableView<Programme> programmeTable;
    @FXML private TableColumn<Programme, String> programmeTypeCol;
    @FXML private TableColumn<Programme, LocalDate> programmeDateCol;
   
    
    // Dates
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    
    // ComboBox mois/années
    @FXML private ComboBox<String> moisCombo;
    @FXML private ComboBox<Integer> anneeCombo;
    @FXML private ComboBox<String> moisComptableCombo;
    @FXML private ComboBox<Integer> anneeComptableCombo;
    
    // Boutons de génération
    @FXML private Button genererProgrammeBtn;
    @FXML private Button genererMensuelBtn;
    @FXML private Button genererTransactionsBtn;
    @FXML private Button genererComptabiliteBtn;
    
    // Boutons communs
    @FXML private Button selectionnerDossierBtn;
    @FXML private Button visualiserBtn;
    
    // Progression et statut
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Label dossierLabel;
    
    // Services
    private ImpressionService impressionService;
    private ProgrammeService programmeService;
    
    // Variables
    private File dossierDestination;
    private MainController mainController;
    private ObservableList<Programme> programmesList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        impressionService = new ImpressionServiceImpl();
        programmeService = new ProgrammeServiceImpl();
        
        initializeDatePickers();
        initializeMoisAnnees();
        initializeProgrammeTable();
        loadProgrammes();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    private void initializeDatePickers() {
        // Définir les valeurs par défaut
        LocalDate aujourdhui = LocalDate.now();
        dateDebutPicker.setValue(aujourdhui.withDayOfMonth(1));
        dateFinPicker.setValue(aujourdhui);
        
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebutPicker.getValue() != null) {
                if (newVal.isBefore(dateDebutPicker.getValue())) {
                    dateFinPicker.setValue(oldVal);
                    UICommun.showAlert(AlertType.WARNING, "Date invalide", 
                        "La date de fin ne peut pas être avant la date de début.");
                }
            }
        });
    }
    
    private void initializeMoisAnnees() {
        String[] mois = {
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };
        
        // Initialiser tous les combobox de mois
        moisCombo.getItems().addAll(mois);
        moisComptableCombo.getItems().addAll(mois);
        
        // Définir les valeurs par défaut
        int moisCourant = LocalDate.now().getMonthValue() - 1;
        moisCombo.setValue(mois[moisCourant]);
        moisComptableCombo.setValue(mois[moisCourant]);
        
        // Initialiser les années
        int anneeCourante = LocalDate.now().getYear();
        for (int i = anneeCourante; i >= anneeCourante - 4; i--) {
            anneeCombo.getItems().add(i);
            anneeComptableCombo.getItems().add(i);
        }
        
        // Définir les valeurs par défaut
        anneeCombo.setValue(anneeCourante);
        anneeComptableCombo.setValue(anneeCourante);
    }
    
    private void initializeProgrammeTable() {
        programmeTypeCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTypeProgramme()));
        
        programmeDateCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDateProgramme()));
        
        programmeTable.setItems(programmesList);
        programmeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void loadProgrammes() {
        try {
            programmesList.clear();
            programmesList.addAll(programmeService.findAll());
            
            if (programmesList.isEmpty()) {
                statusLabel.setText("Aucun programme trouvé pour ce mois");
            } else {
                statusLabel.setText(programmesList.size() + " programme(s) trouvé(s)");
            }
        } catch (Exception e) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", 
                "Impossible de charger les programmes: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSelectionnerDossier() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner le dossier de destination");
        
        if (dossierDestination != null) {
            directoryChooser.setInitialDirectory(dossierDestination);
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        Stage stage = (Stage) selectionnerDossierBtn.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        
        if (selectedDirectory != null) {
            dossierDestination = selectedDirectory;
            dossierLabel.setText(dossierDestination.getAbsolutePath());
            
            // Activer le bouton de visualisation si un dossier est sélectionné
            visualiserBtn.setDisable(false);
        }
    }
    
    @FXML
    private void handleGenererProgramme() {
        if (dossierDestination == null) {
            UICommun.showAlert(AlertType.WARNING, "Dossier manquant", 
                "Veuillez sélectionner un dossier de destination.");
            return;
        }
        
        Programme selected = programmeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UICommun.showAlert(AlertType.WARNING, "Sélection manquante", 
                "Veuillez sélectionner un programme dans le tableau.");
            return;
        }
        imprimerRapportProgramme(selected);
    }

    @FXML
    private void handleGenererMensuel() {
        if (dossierDestination == null) {
            UICommun.showAlert(AlertType.WARNING, "Dossier manquant", 
                "Veuillez sélectionner un dossier de destination.");
            return;
        }
        
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();
        
        if (debut == null || fin == null) {
            UICommun.showAlert(AlertType.WARNING, "Période invalide", 
                "Veuillez sélectionner une période valide.");
            return;
        }
        
        if (debut.isAfter(fin)) {
            UICommun.showAlert(AlertType.WARNING, "Période invalide", 
                "La date de début ne peut pas être après la date de fin.");
            return;
        }
        
        imprimerRapportMensuel(debut, fin);
    }

    @FXML
    private void handleGenererTransactions() {
        if (dossierDestination == null) {
            UICommun.showAlert(AlertType.WARNING, "Dossier manquant", 
                "Veuillez sélectionner un dossier de destination.");
            return;
        }
        
        String mois = moisCombo.getValue();
        Integer annee = anneeCombo.getValue();
        
        if (mois == null || annee == null) {
            UICommun.showAlert(AlertType.WARNING, "Période invalide", 
                "Veuillez sélectionner un mois et une année.");
            return;
        }
        
        // Convertir mois en LocalDate
        Map<String, Integer> moisMap = new HashMap<>();
        String[] moisNoms = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                             "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        for (int i = 0; i < moisNoms.length; i++) {
            moisMap.put(moisNoms[i], i + 1);
        }
        
        int moisNum = moisMap.get(mois);
        YearMonth yearMonth = YearMonth.of(annee, moisNum);
        LocalDate debut = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();
        
        imprimerTransactions(debut, fin);
    }

    @FXML
    private void handleGenererComptabilite() {
        if (dossierDestination == null) {
            UICommun.showAlert(AlertType.WARNING, "Dossier manquant", 
                "Veuillez sélectionner un dossier de destination.");
            return;
        }
        
        String mois = moisComptableCombo.getValue();
        Integer annee = anneeComptableCombo.getValue();
        
        if (mois == null || annee == null) {
            UICommun.showAlert(AlertType.WARNING, "Période invalide", 
                "Veuillez sélectionner un mois et une année.");
            return;
        }
        imprimerComptabilite(mois, annee);
    }
    
    @FXML
    private void handleVisualiser() {
        if (dossierDestination != null && dossierDestination.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(dossierDestination);
                statusLabel.setText("Dossier ouvert avec succès");
            } catch (IOException e) {
                UICommun.showAlert(AlertType.ERROR, "Erreur", 
                    "Impossible d'ouvrir le dossier: " + e.getMessage());
            }
        } else {
            UICommun.showAlert(AlertType.WARNING, "Dossier non sélectionné", 
                "Veuillez d'abord sélectionner un dossier.");
        }
    }
    
    @FXML
    private void handleActualiser() {
        loadProgrammes();
        statusLabel.setText("Liste des programmes actualisée");
    }
    
    @FXML
    private void handleOuvrirDossier() {
        if (dossierDestination != null && dossierDestination.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(dossierDestination);
            } catch (IOException e) {
                UICommun.showAlert(AlertType.ERROR, "Erreur", 
                    "Impossible d'ouvrir le dossier: " + e.getMessage());
            }
        } else {
            UICommun.showAlert(AlertType.WARNING, "Dossier non sélectionné", 
                "Veuillez d'abord sélectionner un dossier.");
        }
    }
    
    private void imprimerRapportProgramme(Programme programme) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Génération du rapport de programme...");
                updateProgress(0.1, 1);
                
                try {
                    impressionService.printProgramme(programme, dossierDestination.getAbsolutePath());
                    updateProgress(1.0, 1);
                    updateMessage("Rapport généré avec succès!");
                    
                } catch (Exception e) {
                    updateMessage("Erreur lors de la génération: " + e.getMessage());
                    throw e;
                }
                return null;
            }
        };
        
        executerTacheImpression(task, "Rapport de programme");
    }
    
    private void imprimerRapportMensuel(LocalDate debut, LocalDate fin) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Génération du rapport mensuel...");
                updateProgress(0.1, 1);
                
                try {
                    impressionService.printGestionFinancierMensuel(debut, fin, dossierDestination.getAbsolutePath());
                    updateProgress(1.0, 1);
                    updateMessage("Rapport mensuel généré avec succès!");
                    
                } catch (Exception e) {
                    updateMessage("Erreur lors de la génération: " + e.getMessage());
                    throw e;
                }
                return null;
            }
        };
        
        executerTacheImpression(task, "Rapport mensuel");
    }
    
    private void imprimerTransactions(LocalDate debut, LocalDate fin) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Génération du rapport des transactions...");
                updateProgress(0.1, 1);
                
                try {
                    impressionService.printTransactionMensuel(debut, fin, dossierDestination.getAbsolutePath());
                    updateProgress(1.0, 1);
                    updateMessage("Rapport des transactions généré avec succès!");
                    
                } catch (Exception e) {
                    updateMessage("Erreur lors de la génération: " + e.getMessage());
                    throw e;
                }
                return null;
            }
        };
        
        executerTacheImpression(task, "Rapport des transactions");
    }
    
    private void imprimerComptabilite(String mois, int annee) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Génération du rapport de comptabilité...");
                updateProgress(0.1, 1);
                
                try {
                    Map<String, Integer> moisMap = new HashMap<>();
                    String[] moisNoms = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                                         "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
                    for (int i = 0; i < moisNoms.length; i++) {
                        moisMap.put(moisNoms[i], i + 1);
                    }
                    
                    int moisNum = moisMap.get(mois);
                    YearMonth yearMonth = YearMonth.of(annee, moisNum);
                    LocalDate debut = yearMonth.atDay(1);
                    LocalDate fin = yearMonth.atEndOfMonth();
                    
                    impressionService.printComptabiliteMensuel(debut, fin, dossierDestination.getAbsolutePath());
                    updateProgress(1.0, 1);
                    updateMessage("Rapport de comptabilité généré avec succès!");
                    
                } catch (Exception e) {
                    updateMessage("Erreur lors de la génération: " + e.getMessage());
                    throw e;
                }
                return null;
            }
        };
        
        executerTacheImpression(task, "Rapport de comptabilité");
    }
    
    private void executerTacheImpression(Task<Void> task, String typeRapport) {
        // Désactiver les boutons pendant la génération
        disableButtonsDuringGeneration(true);
        
        // Lier la progression
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        
        task.setOnSucceeded(e -> {
            unbindProgress();
            disableButtonsDuringGeneration(false);
            
            UICommun.showAlert(AlertType.INFORMATION, "Succès", 
                typeRapport + " généré avec succès!\n" +
                "Fichier enregistré dans: " + dossierDestination.getAbsolutePath());
            
            progressBar.setProgress(0);
            statusLabel.setText("Prêt pour une nouvelle impression");
        });
        
        task.setOnFailed(e -> {
            unbindProgress();
            disableButtonsDuringGeneration(false);
            
            Throwable cause = task.getException();
            UICommun.showAlert(AlertType.ERROR, "Erreur d'impression", 
                "Échec de la génération du rapport:\n" + cause.getMessage());
            
            progressBar.setProgress(0);
            statusLabel.setText("Erreur - Veuillez réessayer");
        });
        
        new Thread(task).start();
    }
    
    private void disableButtonsDuringGeneration(boolean disable) {
        // Désactiver tous les boutons de génération
        genererProgrammeBtn.setDisable(disable);
        genererMensuelBtn.setDisable(disable);
        genererTransactionsBtn.setDisable(disable);
        genererComptabiliteBtn.setDisable(disable);
        selectionnerDossierBtn.setDisable(disable);
        visualiserBtn.setDisable(disable);
        
        // Désactiver les contrôles d'entrée
        programmeTable.setDisable(disable);
        dateDebutPicker.setDisable(disable);
        dateFinPicker.setDisable(disable);
        moisCombo.setDisable(disable);
        anneeCombo.setDisable(disable);
        moisComptableCombo.setDisable(disable);
        anneeComptableCombo.setDisable(disable);
        
        // Désactiver la navigation des onglets
        rapportsTabPane.setDisable(disable);
    }
    
    private void unbindProgress() {
        if (progressBar.progressProperty().isBound()) {
            progressBar.progressProperty().unbind();
        }
        if (statusLabel.textProperty().isBound()) {
            statusLabel.textProperty().unbind();
        }
    }
    
    @FXML
    private void handleAide() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Aide - Impression");
        alert.setHeaderText("Guide d'utilisation de l'impression");
        alert.setContentText(
            "1. Sélectionnez le type de rapport dans les onglets\n" +
            "2. Choisissez le programme ou la période selon le type\n" +
            "3. Sélectionnez un dossier de destination\n" +
            "4. Cliquez sur le bouton correspondant pour générer le PDF\n\n" +
            "Les rapports sont enregistrés dans le dossier sélectionné.\n" +
            "Le nom du fichier inclut la date et le type de rapport."
        );
        alert.showAndWait();
    }
}