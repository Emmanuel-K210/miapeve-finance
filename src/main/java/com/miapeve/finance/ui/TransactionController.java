package com.miapeve.finance.ui;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.dao.DepenseDAO;
import com.miapeve.finance.dao.SemenceDAO;
import com.miapeve.finance.entites.Depense;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.service.impl.FinanceService;
import com.miapeve.finance.utils.UICommun;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class TransactionController implements Initializable {
    
    @FXML private TabPane transactionsTabPane;
    @FXML private TableView<Depense> depensesTable;
    @FXML private TableView<Semence> semencesTable;
    @FXML private Button btnNouvelleDepense;
    @FXML private Button btnNouvelleSemence;
    @FXML private Label totalDepensesLabel;
    @FXML private Label totalSemencesLabel;
    @FXML private Label soldeNetLabel;
    @FXML private Label totalTransactionsLabel;
    
    // Filtres
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> typeFilter;
    
    // Colonnes pour la table des dépenses
    @FXML private TableColumn<Depense, String> colDepenseLibelle;
    @FXML private TableColumn<Depense, Double> colDepenseMontant;
    @FXML private TableColumn<Depense, String> colDepenseCompte;
    @FXML private TableColumn<Depense, String> colDepenseDate;
    
    // Colonnes pour la table des semences
    @FXML private TableColumn<Semence, String> colSemenceType;
    @FXML private TableColumn<Semence, Double> colSemenceMontant;
    @FXML private TableColumn<Semence, String> colSemenceProgramme;
    @FXML private TableColumn<Semence, String> colSemenceCompte;
    
    @FXML private Label countDepensesLabel;
    @FXML private Label countSemencesLabel;
    
    private FinanceService financeService;
    private DepenseDAO depenseDAO;
    private SemenceDAO semenceDAO;
    private ObservableList<Depense> depensesList;
    private ObservableList<Semence> semencesList;
    private Node view;
    private MainController mainController;
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        financeService = new FinanceService();
        depenseDAO = new DepenseDAO();
        semenceDAO = new SemenceDAO();
        depensesList = FXCollections.observableArrayList();
        semencesList = FXCollections.observableArrayList();
        
        initializeFilters();
        initializeTables();
        setupEventHandlers();
        loadData();
    }
    
    private void initializeFilters() {
        // Types pour le filtre
        ObservableList<String> types = FXCollections.observableArrayList(
            "Tous les types", "Dépense", "Semence"
        );
        if (typeFilter != null) {
            typeFilter.setItems(types);
            typeFilter.setValue("Tous les types");
        }
        
        // Date par défaut
        if (dateFilter != null) {
            dateFilter.setValue(null);
        }
    }
    
    private void initializeTables() {
        try {
            // Configuration table dépenses
            if (colDepenseLibelle != null) {
                colDepenseLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
            }
            
            if (colDepenseMontant != null) {
                colDepenseMontant.setCellValueFactory(cellData -> {
                    Depense depense = cellData.getValue();
                    Double montantValue = depense.getMontant() != null ? 
                        depense.getMontant().doubleValue() : 0.0;
                    return new javafx.beans.property.SimpleDoubleProperty(montantValue).asObject();
                });
                
                colDepenseMontant.setCellFactory(column -> new javafx.scene.control.TableCell<Depense, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(String.format("%,.0f F CFA", item));
                            setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 600;");
                        }
                    }
                });
            }
            
            if (colDepenseCompte != null) {
                colDepenseCompte.setCellValueFactory(cellData -> {
                    if (cellData.getValue().getCompte() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getCompte().getTypeCompte()
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                });
            }
            
            if (colDepenseDate != null) {
                colDepenseDate.setCellValueFactory(cellData -> {
                    if (cellData.getValue().getDateDepense() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getDateDepense().toString()
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                });
            }
            
            // Configuration table semences
            if (colSemenceType != null) {
                colSemenceType.setCellValueFactory(cellData -> {
                    if (cellData.getValue().getTypeSemence() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getTypeSemence().name()
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                });
            }
            
            if (colSemenceMontant != null) {
                colSemenceMontant.setCellValueFactory(cellData -> {
                    Semence semence = cellData.getValue();
                    Double montantValue = semence.getMontant() != null ? 
                        semence.getMontant().doubleValue() : 0.0;
                    return new javafx.beans.property.SimpleDoubleProperty(montantValue).asObject();
                });
                
                colSemenceMontant.setCellFactory(column -> new javafx.scene.control.TableCell<Semence, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(String.format("%,.0f F CFA", item));
                            setStyle("-fx-text-fill: #10B981; -fx-font-weight: 600;");
                        }
                    }
                });
            }
            
            if (colSemenceProgramme != null) {
                colSemenceProgramme.setCellValueFactory(cellData -> {
                    if (cellData.getValue().getProgramme() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getProgramme().getTypeProgramme()
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                });
            }
            
            if (colSemenceCompte != null) {
                colSemenceCompte.setCellValueFactory(cellData -> {
                    if (cellData.getValue().getCompte() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getCompte().getTypeCompte()
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                });
            }
            
            // Appliquer les données aux tables
            if (depensesTable != null) {
                depensesTable.setItems(depensesList);
            }
            
            if (semencesTable != null) {
                semencesTable.setItems(semencesList);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupEventHandlers() {
        // Double-clic pour voir les détails
        if (depensesTable != null) {
            depensesTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !depensesTable.getSelectionModel().isEmpty()) {
                    Depense selected = depensesTable.getSelectionModel().getSelectedItem();
                    UICommun.showInfo("Détails Dépense", 
                        "Libellé: " + selected.getLibelle() + "\n" +
                        "Montant: " + String.format("%,.0f", selected.getMontant()) + " F CFA\n" +
                        "Date: " + selected.getDateDepense()
                    );
                }
            });
        }
        
        if (semencesTable != null) {
            semencesTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !semencesTable.getSelectionModel().isEmpty()) {
                    Semence selected = semencesTable.getSelectionModel().getSelectedItem();
                    UICommun.showInfo("Détails Semence", 
                        "Type: " + selected.getTypeSemence() + "\n" +
                        "Montant: " + String.format("%,.0f", selected.getMontant()) + " F CFA\n" +
                        "Programme: " + (selected.getProgramme() != null ? 
                            selected.getProgramme().getTypeProgramme() : "N/A")
                    );
                }
            });
        }
        
        // Filtres
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        
        if (dateFilter != null) {
            dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        
        if (typeFilter != null) {
            typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }
    
    private void applyFilters() {
        // TODO: Implémenter le filtrage
        // Pour l'instant on recharge juste les données
        loadData();
    }
    
    public void loadData() {
        try {
            // Charger les dépenses
            List<Depense> depenses = depenseDAO.findAll();
            depensesList.setAll(depenses);
            
            // Charger les semences
            List<Semence> semences = semenceDAO.findAll();
            semencesList.setAll(semences);
            
            updateStats();
            updateCounters();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement transactions: " + e.getMessage());
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les transactions.");
        }
    }
    
    private void updateStats() {
        try {
            double totalDepenses = depensesList.stream()
                .mapToDouble(d -> d.getMontant() != null ? d.getMontant().doubleValue() : 0.0)
                .sum();
            
            double totalSemences = semencesList.stream()
                .mapToDouble(s -> s.getMontant() != null ? s.getMontant().doubleValue() : 0.0)
                .sum();
            
            double soldeNet = totalSemences - totalDepenses;
            int totalTransactions = depensesList.size() + semencesList.size();
            
            if (totalDepensesLabel != null) {
                totalDepensesLabel.setText(String.format("%,.0f F CFA", totalDepenses));
            }
            
            if (totalSemencesLabel != null) {
                totalSemencesLabel.setText(String.format("%,.0f F CFA", totalSemences));
            }
            
            if (soldeNetLabel != null) {
                soldeNetLabel.setText(String.format("%,.0f F CFA", soldeNet));
                // Colorer selon le signe
                if (soldeNet < 0) {
                    soldeNetLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 700; -fx-font-size: 22px;");
                } else {
                    soldeNetLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 700; -fx-font-size: 22px;");
                }
            }
            
            if (totalTransactionsLabel != null) {
                totalTransactionsLabel.setText(String.valueOf(totalTransactions));
            }
            
        } catch (Exception e) {
            System.err.println("Erreur mise à jour statistiques: " + e.getMessage());
        }
    }
    
    private void updateCounters() {
        if (countDepensesLabel != null) {
            countDepensesLabel.setText(depensesList.size() + " dépense(s)");
        }
        if (countSemencesLabel != null) {
            countSemencesLabel.setText(semencesList.size() + " semence(s)");
        }
    }
    
    @FXML
    private void handleNouvelleDepense() {
        if (mainController != null) {
            mainController.loadNouvelleDepense();
        }
    }
    
    @FXML
    private void handleNouvelleSemence() {
        if (mainController != null) {
            mainController.loadNouvelleSemence();
        }
    }
    
    @FXML
    private void handleReinitialiserFiltres() {
        if (searchField != null) searchField.clear();
        if (dateFilter != null) dateFilter.setValue(null);
        if (typeFilter != null) typeFilter.setValue("Tous les types");
        applyFilters();
    }
    
    @FXML
    private void handleExporter() {
        UICommun.showInfo("Export", "Fonctionnalité en développement");
    }
    
    public void refreshData() {
        loadData();
    }
    
    public Node getView() {
        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/transactions.fxml"));
                loader.setController(this);
                view = loader.load();
            } catch (Exception e) {
                System.err.println("Erreur chargement vue transactions: " + e.getMessage());
                e.printStackTrace();
                view = createFallbackView();
            }
        }
        return view;
    }
    
    private Node createFallbackView() {
        VBox fallback = new VBox();
        fallback.setStyle("-fx-padding: 20; -fx-spacing: 10;");
        
        Label title = new Label("Transactions");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label message = new Label("Interface temporairement indisponible");
        message.setStyle("-fx-text-fill: red;");
        
        Button refreshBtn = new Button("Réessayer");
        refreshBtn.setOnAction(e -> refreshData());
        
        fallback.getChildren().addAll(title, message, refreshBtn);
        return fallback;
    }
}