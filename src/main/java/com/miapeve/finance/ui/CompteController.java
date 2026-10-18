package com.miapeve.finance.ui;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.impl.FinanceService;
import com.miapeve.finance.utils.UICommun;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CompteController implements Initializable, CompteChangeListener {
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private TableView<Compte> comptesTable;
    @FXML private TableColumn<Compte, String> colNom;
    @FXML private TableColumn<Compte, String> colType;
    @FXML private TableColumn<Compte, Double> colSolde;
    
    @FXML private GridPane statsContainer;          // ✅ NOUVEAU : conteneur des cartes stats
    @FXML private Label comptesCountLabel;          // ✅ NOUVEAU : label pour le nombre de comptes
    @FXML private Button btnNouveauCompte;
    @FXML private Button btnCrediter;
    @FXML private Button btnDebiter;
    @FXML private Button btnTransferer;
 
    // ==================== SERVICES ====================
    
    private FinanceService financeService;
    private CompteDAO compteDAO;
    private ObservableList<Compte> comptesList;
    private Node view;
    private MainController mainController;
    
    // ==================== SETTERS ====================
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        if (mainController != null) {
            mainController.addCompteChangeListener(this);
            System.out.println("👂 CompteController enregistré comme observer");
        }
    }
    
    public MainController getMainController() {
        return this.mainController;
    }
    
    // ==================== COMPTE CHANGE LISTENER ====================
    
    @Override
    public void onComptesChanged() throws SQLException {
        System.out.println("🔄 CompteController: notification de changement de comptes reçue");
        loadData();
    }
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        financeService = new FinanceService();
        compteDAO = new CompteDAO();
        comptesList = FXCollections.observableArrayList();
        
        System.out.println("🏦 Initialisation de CompteController");
        
        initializeTable();
        setupEventHandlers();
        loadStatsCards(); // ✅ Charger les cartes de stats au démarrage
    }
    
    // ==================== INITIALISATION TABLE ====================
    
    private void initializeTable() {
        try {
            if (colNom != null) {
                colNom.setCellValueFactory(new PropertyValueFactory<>("nomCompte"));
                colNom.setCellFactory(column -> new TableCell<Compte, String>() {
                    @Override
                    protected void updateItem(String nom, boolean empty) {
                        super.updateItem(nom, empty);
                        if (empty || nom == null) {
                            setText(null);
                        } else {
                            setText(nom);
                            setStyle("-fx-font-weight: bold; -fx-text-fill: -text-primary;");
                        }
                    }
                });
            }
            
            if (colType != null) {
                colType.setCellValueFactory(new PropertyValueFactory<>("typeCompte"));
                colType.setCellFactory(column -> new TableCell<Compte, String>() {
                    @Override
                    protected void updateItem(String type, boolean empty) {
                        super.updateItem(type, empty);
                        if (empty || type == null) {
                            setText(null);
                        } else {
                            setText(type);
                            switch (type.toUpperCase()) {
                                case "CAISSE":
                                    setStyle("-fx-text-fill: -success; -fx-font-weight: bold;");
                                    break;
                                case "BANQUE":
                                    setStyle("-fx-text-fill: -primary; -fx-font-weight: bold;");
                                    break;
                                case "EPARGNE":
                                    setStyle("-fx-text-fill: -purple; -fx-font-weight: bold;");
                                    break;
                                default:
                                    setStyle("-fx-text-fill: -text-secondary;");
                            }
                        }
                    }
                });
            }
            
            if (colSolde != null) {
                colSolde.setCellValueFactory(cellData -> {
                    Compte compte = cellData.getValue();
                    Double soldeValue = compte.getSolde() != null ? compte.getSolde().doubleValue() : 0.0;
                    return new javafx.beans.property.SimpleDoubleProperty(soldeValue).asObject();
                });
                
                colSolde.setCellFactory(column -> new TableCell<Compte, Double>() {
                    @Override
                    protected void updateItem(Double solde, boolean empty) {
                        super.updateItem(solde, empty);
                        if (empty || solde == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(String.format("%,.0f F CFA", solde));
                            if (solde < 0) {
                                setStyle("-fx-text-fill: -danger; -fx-font-weight: bold;");
                            } else if (solde > 0) {
                                setStyle("-fx-text-fill: -success; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: -text-tertiary; -fx-font-weight: bold;");
                            }
                        }
                    }
                });
            }
            
            if (comptesTable != null) {
                comptesTable.setItems(comptesList);
                
                comptesTable.setRowFactory(tv -> {
                    javafx.scene.control.TableRow<Compte> row = new javafx.scene.control.TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            Compte compte = row.getItem();
                            System.out.println("👆 Double-clic sur compte: " + compte.getNomCompte());
                            ouvrirModalCompte(compte);
                        }
                    });
                    return row;
                });
            }
            
            System.out.println("✅ Table des comptes initialisée avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== STATISTIQUES ====================
    
    /**
     * Charge et affiche les cartes de statistiques dans le GridPane.
     * Similaire à la méthode addStatCard() de ProgrammeController.
     */
    private void loadStatsCards() {
        if (statsContainer == null) {
            System.err.println("❌ statsContainer est null - vérifier le FXML");
            return;
        }
        
        statsContainer.getChildren().clear();
        
        try {
            List<Compte> comptes = compteDAO.findAll();
            
            double totalSolde = 0;
            int nbComptes = comptes.size();
            int nbPositifs = 0;
            int nbNegatifs = 0;
            double plusGrandSolde = 0;
            
            for (Compte c : comptes) {
                double solde = c.getSolde() != null ? c.getSolde().doubleValue() : 0.0;
                totalSolde += solde;
                if (solde >= 0) nbPositifs++;
                else nbNegatifs++;
                if (Math.abs(solde) > plusGrandSolde) plusGrandSolde = Math.abs(solde);
            }
            
            // Ajouter les cartes
            addStatCard(FontAwesomeIcon.CALCULATOR, "Solde Total", 
                String.format("%,.0f F CFA", totalSolde), "primary", "Tous comptes");
            addStatCard(FontAwesomeIcon.BANK, "Nombre de Comptes", 
                String.valueOf(nbComptes), "purple", "Comptes actifs");
            addStatCard(FontAwesomeIcon.ARROW_UP, "Comptes Positifs", 
                String.valueOf(nbPositifs), "success", "Solde > 0");
            addStatCard(FontAwesomeIcon.ARROW_DOWN, "Comptes Négatifs", 
                String.valueOf(nbNegatifs), "danger", "Solde < 0");
            
            // Mettre à jour le label du nombre de comptes
            if (comptesCountLabel != null) {
                comptesCountLabel.setText(nbComptes + " compte(s)");
            }
            
            System.out.println("📊 Statistiques mises à jour");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement stats: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ajoute une carte de statistique dans le GridPane.
     * Version auto-suffisante : ne dépend pas de classes CSS externes.
     * Utilise les variables du design system via setStyle().
     */
    private void addStatCard(FontAwesomeIcon icon, String label, String value, String colorClass, String subtitle) {
        
        // Récupérer les couleurs depuis les variables CSS via lookup
        String borderColor = getIconHexColor(colorClass);
        String mutedColor = getMutedColor(colorClass);
        
        // === Carte principale ===
        VBox card = new VBox(6);
        card.setPrefWidth(190);
        card.setMinWidth(160);
        card.setPrefHeight(100);
        card.setMinHeight(85);
        card.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
        card.setStyle(
            "-fx-background-color: -bg-surface; " +
            "-fx-background-radius: -radius-md; " +
            "-fx-border-radius: -radius-md; " +
            "-fx-border-color: -border-color; " +
            "-fx-border-width: 1; " +
            "-fx-effect: -shadow-sm; " +
            "-fx-border-width: 0 0 0 3; " +
            "-fx-border-color: " + borderColor + ";"
        );
        
        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: -bg-surface; " +
                "-fx-background-radius: -radius-md; " +
                "-fx-border-radius: -radius-md; " +
                "-fx-effect: -shadow-md; " +
                "-fx-border-width: 0 0 0 3; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-translate-y: -1px;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: -bg-surface; " +
                "-fx-background-radius: -radius-md; " +
                "-fx-border-radius: -radius-md; " +
                "-fx-effect: -shadow-sm; " +
                "-fx-border-width: 0 0 0 3; " +
                "-fx-border-color: " + borderColor + ";"
            );
        });
        
        // === Ligne supérieure : Icône + Valeur ===
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Icône avec fond coloré (opacité 20%)
        StackPane iconWrapper = new StackPane();
        iconWrapper.setMinSize(40, 40);
        iconWrapper.setMaxSize(40, 40);
        iconWrapper.setStyle(
            "-fx-background-color: " + mutedColor + "; " +
            "-fx-background-radius: -radius-sm; " +
            "-fx-alignment: center;"
        );
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(18);
        iconView.setFill(Color.web(borderColor));
        iconWrapper.getChildren().add(iconView);
        
        // Valeur
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: 700; " +
            "-fx-text-fill: " + borderColor + ";"
        );
        // Tronquer si trop long
        if (value.length() > 18) {
            valueLabel.setText(value.substring(0, 15) + "...");
        }
        
        topRow.getChildren().addAll(iconWrapper, valueLabel);
        
        // === Label principal ===
        Label labelLabel = new Label(label);
        labelLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 500; " +
            "-fx-text-fill: -text-secondary;"
        );
        // Tronquer si trop long
        if (label.length() > 20) {
            labelLabel.setText(label.substring(0, 18) + "…");
        }
        labelLabel.setTooltip(new javafx.scene.control.Tooltip(label));
        
        // === Sous-titre ===
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: -text-tertiary;"
        );
        if (subtitle.length() > 20) {
            subtitleLabel.setText(subtitle.substring(0, 18) + "…");
        }
        
        card.getChildren().addAll(topRow, labelLabel, subtitleLabel);
        
        // Ajouter la carte à la grille
        int colCount = statsContainer.getChildren().size();
        statsContainer.add(card, colCount % 4, colCount / 4);
    }

    /**
     * Retourne la couleur hexadécimale pour la classe donnée.
     */
    private String getIconHexColor(String colorClass) {
        switch (colorClass) {
            case "primary": return "#4F46E5";
            case "success": return "#10B981";
            case "warning": return "#F59E0B";
            case "danger": return "#EF4444";
            case "purple": return "#8B5CF6";
            default: return "#4F46E5";
        }
    }

    /**
     * Retourne la couleur "muted" (avec opacité) pour la classe donnée.
     * Utilise les variables CSS du design system.
     */
    private String getMutedColor(String colorClass) {
        switch (colorClass) {
            case "primary": return "-primary-muted";
            case "success": return "-success-muted";
            case "warning": return "-warning-muted";
            case "danger": return "-danger-muted";
            case "purple": return "-purple-muted";
            default: return "-primary-muted";
        }
    }
    
    // ==================== CHARGEMENT DES DONNÉES ====================
    
    public void loadData() throws SQLException {
        List<Compte> comptes = compteDAO.findAll();
        comptesList.setAll(comptes);
        loadStatsCards(); // ✅ Rafraîchir les stats
        System.out.println("✅ Données comptes chargées: " + comptes.size() + " comptes");
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void handleNouveauCompte() {
        System.out.println("➕ Nouveau compte demandé");
        ouvrirModalCompte(null);
    }
    
    @FXML
    private void handleCrediter() {
        Compte selected = comptesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ouvrirOperationDialog(selected, "CREDIT");
        } else {
            UICommun.showAlert(AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un compte à créditer.");
        }
    }
    
    @FXML
    private void handleDebiter() {
        Compte selected = comptesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ouvrirOperationDialog(selected, "DEBIT");
        } else {
            UICommun.showAlert(AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un compte à débiter.");
        }
    }
    
    @FXML
    private void handleTransferer() {
        Compte selected = comptesTable.getSelectionModel().getSelectedItem();
        ouvrirModalTransfert(selected);
    }
    
    // ==================== MODAUX ====================
    
    private void ouvrirModalCompte(Compte compte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/compte-modal.fxml"));
            Parent root = loader.load();
            
            CompteModalController modalController = loader.getController();
            modalController.setCompteController(this);
            modalController.setCompte(compte);
            
            Stage modalStage = new Stage();
            root.getStylesheets().add(UICommun.URLStyle);
            modalController.setStage(modalStage);
            
            modalStage.setTitle(compte == null ? "Nouveau Compte" : "Modifier le Compte");
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            
            modalStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture modal compte: " + e.getMessage());
            e.printStackTrace();
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire");
        }
    }
    
    private void ouvrirModalTransfert(Compte compteSource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/transfert-modal.fxml"));
            Parent root = loader.load();
            
            TransfertModalController modalController = loader.getController();
            modalController.setCompteController(this);
            
            if (compteSource != null) {
                modalController.setCompteSource(compteSource);
            }
            
            Stage modalStage = new Stage();
            root.getStylesheets().add(UICommun.URLStyle);
            modalController.setStage(modalStage);
            
            modalStage.setTitle("Transfert entre comptes");
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            
            modalStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture modal transfert: " + e.getMessage());
            e.printStackTrace();
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de transfert");
        }
    }
    
    private void ouvrirOperationDialog(Compte compte, String typeOperation) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.getDialogPane().getStylesheets().add(UICommun.URLStyle);
        dialog.setTitle(typeOperation.equals("CREDIT") ? "Créditer le compte" : "Débiter le compte");
        dialog.setHeaderText(String.format("Compte: %s (Solde: %,.0f F CFA)", 
            compte.getTypeCompte(), compte.getSolde()));
        dialog.setContentText("Montant (F CFA):");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Double montant = Double.parseDouble(result.get());
                
                if (montant <= 0) {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Le montant doit être positif.");
                    return;
                }
                
                Utilisateur utilisateur = mainController != null ? 
                    mainController.getUtilisateurCourant() : null;
                
                if (utilisateur == null) {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Utilisateur non connecté");
                    return;
                }
                
                boolean success;
                if (typeOperation.equals("CREDIT")) {
                    success = financeService.crediterCompte(compte.getCompteId(), montant, utilisateur);
                } else {
                    if (compte.getSolde().doubleValue() < montant) {
                        UICommun.showAlert(AlertType.ERROR, "Erreur", "Solde insuffisant.");
                        return;
                    }
                    success = financeService.debiterCompte(compte.getCompteId(), montant, utilisateur);
                }
                
                if (success) {
                    try {
                        loadData();
                        notifyComptesChanged();
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur chargement comptes: " + e.getMessage());
                        e.printStackTrace();
                        UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les comptes.");
                    }
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", 
                        String.format("Opération %s de %,.0f F CFA effectuée avec succès.", 
                        typeOperation.toLowerCase(), montant));
                } else {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'opération.");
                }
            } catch (NumberFormatException e) {
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
            }
        }
    }
    
    // ==================== NOTIFICATIONS ====================
    
    void notifyComptesChanged() {
        if (mainController != null) {
            System.out.println("🔔 CompteController notifie MainController du changement de comptes");
            mainController.notifyComptesChanged();
        }
    }
    
    public void afterCompteOperation() {
        try {
            System.out.println("🔄 CompteController: rafraîchissement après opération");
            loadData();
            notifyComptesChanged();
        } catch (SQLException e) {
            System.err.println("❌ Erreur rafraîchissement comptes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== GESTION DES VUES ====================
    
    private void setupEventHandlers() {
        if (btnCrediter != null) {
            btnCrediter.setOnAction(e -> handleCrediter());
        }
        
        if (btnDebiter != null) {
            btnDebiter.setOnAction(e -> handleDebiter());
        }
        
        if (btnNouveauCompte != null) {
            btnNouveauCompte.setOnAction(e -> handleNouveauCompte());
        }
        
        if (btnTransferer != null) {
            btnTransferer.setOnAction(e -> handleTransferer());
        }
    }
    
    public void refreshData() {
        try {
            loadData();
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement comptes: " + e.getMessage());
            e.printStackTrace();
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les comptes.");
        }
    }
    
    public Node getView() {
        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/comptes.fxml"));
                loader.setController(this);
                view = loader.load();
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement vue comptes: " + e.getMessage());
                e.printStackTrace();
                view = createFallbackView();
            }
        }
        return view;
    }
    
    private Node createFallbackView() {
        VBox fallback = new VBox(20);
        fallback.setAlignment(Pos.CENTER);
        fallback.setPadding(new javafx.geometry.Insets(40));
        fallback.setStyle("-fx-background-color: -bg-app;");
        
        Label title = new Label("Module Comptes");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: -text-primary;");
        
        Label message = new Label("Le chargement du module a échoué");
        message.setStyle("-fx-text-fill: -text-secondary;");
        
        Button retryBtn = new Button("Réessayer");
        retryBtn.getStyleClass().add("btn-primary");
        retryBtn.setOnAction(e -> {
            view = null;
            getView();
        });
        
        fallback.getChildren().addAll(title, message, retryBtn);
        return fallback;
    }
}