package com.miapeve.finance.ui;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.repositories.SemenceRepository;
import com.miapeve.finance.repositories.impl.SemenceRepositoryImpl;
import com.miapeve.finance.service.ProgrammeService;
import com.miapeve.finance.service.impl.ProgrammeServiceImpl;
import com.miapeve.finance.utils.UICommun;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class ProgrammeController implements Initializable {
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private TableView<Programme> programmesTable;
    @FXML private TableColumn<Programme, String> colLibelle;
    @FXML private TableColumn<Programme, String> colDate;
    @FXML private TableColumn<Programme, String> colHeure;
    @FXML private TableColumn<Programme, String> colUtilisateur;
    @FXML private TableColumn<Programme, String> colParticipants;
    @FXML private TableColumn<Programme, String> colStatut;
    @FXML private TableColumn<Programme, Void> colActions;
    
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> moisFilter;
    @FXML private Button btnNouveauProgramme;
    @FXML private Button btnExporterProgrammes;
    @FXML private GridPane statsContainer;
    @FXML private Label programmesCountLabel;
    @FXML private ComboBox<String> pageSizeCombo;
    @FXML private Label pageInfoLabel;
    
    // ==================== SERVICES ====================
    
    private ProgrammeService programmeService;
    private SemenceRepository semenceRepository;
    
    // ==================== DONNÉES ====================
    
    private ObservableList<Programme> programmesList;
    private List<Programme> allProgrammes;
    private Node view;
    private MainController mainController;
    
    // Pagination
    private int currentPage = 0;
    private int pageSize = 10;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        programmeService = new ProgrammeServiceImpl();
        semenceRepository = new SemenceRepositoryImpl();
        programmesList = FXCollections.observableArrayList();
        
        initializeTable();
        initializeFilters();
        setupEventHandlers();
        loadData();
    }
    
    // ==================== INITIALISATION DES COMPOSANTS ====================
    
    @SuppressWarnings("unchecked")
    private void initializeTable() {
        // Colonne Libellé
        colLibelle.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTypeProgramme()));
        
        // Colonne Date
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateProgramme() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getDateProgramme().format(formatter));
            }
            return new SimpleStringProperty("");
        });
        
        // Colonne Heure
        colHeure.setCellValueFactory(cellData -> {
            String debut = cellData.getValue().getHeureDebut() != null ? 
                cellData.getValue().getHeureDebut().toString() : "--:--";
            String fin = cellData.getValue().getHeureFin() != null ? 
                cellData.getValue().getHeureFin().toString() : "--:--";
            return new SimpleStringProperty(debut + " - " + fin);
        });
        
        // Colonne Utilisateur
        colUtilisateur.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUtilisateur() != null) {
                String prenom = cellData.getValue().getUtilisateur().getPrenom() != null ? 
                    cellData.getValue().getUtilisateur().getPrenom() : "";
                String nom = cellData.getValue().getUtilisateur().getNom() != null ? 
                    cellData.getValue().getUtilisateur().getNom() : "";
                return new SimpleStringProperty((prenom + " " + nom).trim());
            }
            return new SimpleStringProperty("");
        });
        
        // Colonne Participants
        colParticipants.setCellValueFactory(cellData -> {
            int total = 0;
            total += cellData.getValue().getHommes() != null ? cellData.getValue().getHommes() : 0;
            total += cellData.getValue().getFemmes() != null ? cellData.getValue().getFemmes() : 0;
            total += cellData.getValue().getEnfants() != null ? cellData.getValue().getEnfants() : 0;
            total += cellData.getValue().getJeunes() != null ? cellData.getValue().getJeunes() : 0;
            return new SimpleStringProperty(String.valueOf(total));
        });
        
        // Colonne Statut avec badge
        colStatut.setCellValueFactory(cellData -> {
            String statut = getStatutProgramme(cellData.getValue());
            return new SimpleStringProperty(statut);
        });
        colStatut.setCellFactory(column -> new TableCell<Programme, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    badge.getStyleClass().add("badge-" + item.toLowerCase().replace(" ", ""));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        
        // Colonne Actions
        colActions.setCellFactory(createActionsCellFactory());
        colActions.setPrefWidth(160);
        
        programmesTable.setItems(programmesList);
        
        // Style des lignes
        programmesTable.setRowFactory(tv -> {
            TableRow<Programme> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Programme programme = row.getItem();
                    ouvrirDetailsProgramme(programme);
                }
            });
            return row;
        });
    }
    
    private void initializeFilters() {
        // Mois
        ObservableList<String> mois = FXCollections.observableArrayList(
            "Tous les mois", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        );
        moisFilter.setItems(mois);
        moisFilter.setValue("Tous les mois");
        
        // Date par défaut
        dateFilter.setValue(null);
        
        // Page size
        ObservableList<String> pageSizes = FXCollections.observableArrayList("10", "25", "50", "100");
        pageSizeCombo.setItems(pageSizes);
        pageSizeCombo.setValue("10");
        pageSizeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = Integer.parseInt(newVal);
                currentPage = 0;
                applyFilters();
            }
        });
    }
    
    private void setupEventHandlers() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            applyFilters();
        });
        
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            applyFilters();
        });
        
        moisFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            applyFilters();
        });
    }
    
    // ==================== GESTION DU STATUT ====================
    
    private String getStatutProgramme(Programme programme) {
        if (programme.getDateProgramme() == null) {
            return "Planifié";
        }
        
        LocalDate today = LocalDate.now();
        LocalDate dateProg = programme.getDateProgramme();
        
        if (dateProg.isBefore(today)) {
            return "Terminé";
        } else if (dateProg.equals(today)) {
            return "Aujourd'hui";
        } else {
            return "Planifié";
        }
    }
    
    // ==================== CHARGEMENT DES DONNÉES ====================
    
    public void loadData() {
        try {
            allProgrammes = programmeService.findAll();
            applyFilters();
            updateStats();
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement programmes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        LocalDate selectedDate = dateFilter.getValue();
        String selectedMois = moisFilter.getValue();
        
        ObservableList<Programme> filteredList = FXCollections.observableArrayList();
        
        for (Programme programme : allProgrammes) {
            boolean matchesSearch = searchText.isEmpty() ||
                programme.getTypeProgramme().toLowerCase().contains(searchText) ||
                (programme.getUtilisateur() != null && 
                 (programme.getUtilisateur().getPrenom() != null && 
                  programme.getUtilisateur().getPrenom().toLowerCase().contains(searchText)) ||
                 (programme.getUtilisateur().getNom() != null && 
                  programme.getUtilisateur().getNom().toLowerCase().contains(searchText)));
            
            boolean matchesDate = selectedDate == null || 
                (programme.getDateProgramme() != null && 
                 programme.getDateProgramme().equals(selectedDate));
            
            boolean matchesMois = selectedMois == null || 
                selectedMois.equals("Tous les mois") ||
                (programme.getDateProgramme() != null && 
                 programme.getDateProgramme().getMonthValue() == getMoisIndex(selectedMois));
            
            if (matchesSearch && matchesDate && matchesMois) {
                filteredList.add(programme);
            }
        }
        
        int total = filteredList.size();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, total);
        
        ObservableList<Programme> pageList = FXCollections.observableArrayList();
        if (start < total) {
            pageList.addAll(filteredList.subList(start, end));
        }
        
        programmesList.setAll(pageList);
        programmesCountLabel.setText(total + " programme" + (total > 1 ? "s" : ""));
        
        int totalPages = (int) Math.ceil((double) total / pageSize);
        pageInfoLabel.setText((currentPage + 1) + " / " + (totalPages > 0 ? totalPages : 1));
    }
    
    private int getMoisIndex(String mois) {
        String[] moisArray = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                             "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        for (int i = 0; i < moisArray.length; i++) {
            if (moisArray[i].equals(mois)) {
                return i + 1;
            }
        }
        return -1;
    }
    
 // ==================== STATISTIQUES ====================

    private void updateStats() {
        statsContainer.getChildren().clear();
        
        try {
            int total = allProgrammes != null ? allProgrammes.size() : 0;
            int ceMois = programmeService.listeDesProgrammesMoisEnCours() != null ? 
                programmeService.listeDesProgrammesMoisEnCours().size() : 0;
            int aujourdhui = programmeService.listeDesProgrammesParMois(LocalDate.now()) != null ? 
                programmeService.listeDesProgrammesParMois(LocalDate.now()).size() : 0;
            double totalSemences = programmeService.calculerTotalSemencesMoisEnCours();
            int nbSemences = programmeService.compterSemencesMoisEnCours();
            
            // Ajout des cartes de statistiques
            addStatCard(FontAwesomeIcon.CALENDAR, "Total", String.valueOf(total), "primary", "Programmes");
            addStatCard(FontAwesomeIcon.CALENDAR_ALT, "Ce Mois", String.valueOf(ceMois), "success", "Programmes");
            addStatCard(FontAwesomeIcon.BELL, "Aujourd'hui", String.valueOf(aujourdhui), "warning", "Programmes");
            addStatCard(FontAwesomeIcon.LEAF, "Semences", String.valueOf(nbSemences), "purple", "Distribuées");
            addStatCard(FontAwesomeIcon.MONEY, "Montant", String.format("%,.0f", totalSemences), "danger", "F CFA");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur stats: " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            addStatCard(FontAwesomeIcon.CALENDAR, "Total", "0", "primary", "Programmes");
            addStatCard(FontAwesomeIcon.CALENDAR_ALT, "Ce Mois", "0", "success", "Programmes");
            addStatCard(FontAwesomeIcon.BELL, "Aujourd'hui", "0", "warning", "Programmes");
            addStatCard(FontAwesomeIcon.LEAF, "Semences", "0", "purple", "Distribuées");
            addStatCard(FontAwesomeIcon.MONEY, "Montant", "0", "danger", "F CFA");
        }
    }

    private void addStatCard(FontAwesomeIcon icon, String label, String value, String colorClass, String subtitle) {
        // Carte principale - SANS ARRONDIS
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E5E7EB; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 14 18 14 18; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 2); " +
            "-fx-min-width: 160; " +
            "-fx-pref-width: 180; " +
            "-fx-min-height: 105; " +
            "-fx-pref-height: 115;"
        );
        
        // Bordure colorée à gauche
        String borderColor = getIconHexColor(colorClass);
        card.setStyle(card.getStyle() + 
            "-fx-border-color: " + borderColor + " #E5E7EB #E5E7EB #E5E7EB; " +
            "-fx-border-width: 0 0 0 3;"
        );
        
        // Ligne supérieure : Icône + Valeur
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Icône avec fond - SANS ARRONDIS
        StackPane iconWrapper = new StackPane();
        iconWrapper.setStyle(
            "-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; " +
            "-fx-alignment: center; " +
            "-fx-background-color: " + borderColor + "20;"
        );
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(16);
        iconView.setFill(Color.web(borderColor));
        iconWrapper.getChildren().add(iconView);
        
        // Valeur
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: " + borderColor + ";"
        );
        
        topRow.getChildren().addAll(iconWrapper, valueLabel);
        
        // Label principal
        Label labelLabel = new Label(label);
        labelLabel.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #6B7280;"
        );
        
        // Sous-titre
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #9CA3AF;"
        );
        
        card.getChildren().addAll(topRow, labelLabel, subtitleLabel);
        
        // Ajout à la grille
        int colCount = statsContainer.getChildren().size();
        statsContainer.add(card, colCount % 5, colCount / 5);
    }

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
    // ==================== PAGINATION ====================
    
    @FXML
    private void handlePagePrecedente() {
        if (currentPage > 0) {
            currentPage--;
            applyFilters();
        }
    }
    
    @FXML
    private void handlePageSuivante() {
        int total = allProgrammes != null ? allProgrammes.size() : 0;
        int totalPages = (int) Math.ceil((double) total / pageSize);
        if (currentPage < totalPages - 1) {
            currentPage++;
            applyFilters();
        }
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void handleNouveauProgramme(ActionEvent event) {
        ouvrirFormulaireProgramme(null);
    }
    
    @FXML
    private void handleEditerProgramme(ActionEvent event) {
        Programme selected = programmesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ouvrirFormulaireProgramme(selected);
        } else {
            UICommun.showWarning("Sélection requise", "Veuillez sélectionner un programme à éditer");
        }
    }
    
    @FXML
    private void handleSupprimerProgramme(ActionEvent event) {
        Programme selected = programmesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (UICommun.confirmDelete("programme", selected.getTypeProgramme())) {
                boolean success = programmeService.supprimer(selected.getId());
                if (success) {
                    loadData();
                    UICommun.showSuccess("Suppression", "Programme supprimé avec succès");
                } else {
                    UICommun.showError("Suppression", "Impossible de supprimer le programme");
                }
            }
        } else {
            UICommun.showWarning("Sélection requise", "Veuillez sélectionner un programme à supprimer");
        }
    }
    
    @FXML
    private void handleReinitialiserFiltres(ActionEvent event) {
        searchField.clear();
        dateFilter.setValue(null);
        moisFilter.setValue("Tous les mois");
        currentPage = 0;
        applyFilters();
    }
    
    @FXML
    private void handleExporterProgrammes(ActionEvent event) {
        UICommun.showInfo("Export", "Fonctionnalité en développement");
    }
    
    // ==================== FORMULAIRE PROGRAMME ====================
    
    public void ouvrirFormulaireProgramme(Programme programme) {
        try {
            System.out.println("📝 Ouverture formulaire programme...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/programme_form.fxml"));
            VBox formRoot = loader.load();
            ProgrammeFormController controller = loader.getController();
            
            controller.setMainController(mainController);
            
            if (mainController != null && mainController.getUtilisateurCourant() != null) {
                controller.setUtilisateurConnecte(mainController.getUtilisateurCourant());
            }
            
            if (programme != null) {
                controller.setProgramme(programme);
            }
            
            if (mainController != null) {
                mainController.switchContent(formRoot, 
                    programme == null ? "Nouveau Programme" : "Modifier Programme");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture formulaire: " + e.getMessage());
            e.printStackTrace();
            UICommun.showError("Ouverture formulaire", e.getMessage());
        }
    }
    
    // ==================== DÉTAILS PROGRAMME - REDESIGN COMPLET ====================
    
    private void ouvrirDetailsProgramme(Programme programme) {
        try {
            System.out.println("🔍 Ouverture détails programme: " + programme.getTypeProgramme());
            
            List<Semence> semences = semenceRepository.findByProgramme(programme.getId());
            programme.setSemences(semences);
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle("Détails du Programme");
            
            DialogPane dialogPane = new DialogPane();
            dialogPane.getStyleClass().add("dialog-modern");
            dialogPane.getStyleClass().add("dialog-detail");
            
            try {
                String cssPath = getClass().getResource("/static/css/app.css").toExternalForm();
                if (cssPath != null) {
                    dialogPane.getStylesheets().add(cssPath);
                }
            } catch (Exception e) {
                System.err.println("⚠️ CSS non trouvé");
            }
            
            VBox content = createDetailContent(programme);
            dialogPane.setContent(content);
            
            ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().add(closeButton);
            
            dialog.setDialogPane(dialogPane);
            dialog.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur détails programme: " + e.getMessage());
            e.printStackTrace();
            UICommun.showError("Détails programme", e.getMessage());
        }
    }
    
    private VBox createDetailContent(Programme programme) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle(
            "-fx-background-color: -bg-app; " +
            "-fx-background-radius: -radius-lg;"
        );
        
        // ===== CARTE PRINCIPALE =====
        VBox mainCard = new VBox(20);
        mainCard.setStyle(
            "-fx-background-color: -bg-surface; " +
            "-fx-background-radius: -radius-lg; " +
            "-fx-border-radius: -radius-lg; " +
            "-fx-border-color: -border-color; " +
            "-fx-border-width: 1; " +
            "-fx-effect: -shadow-md; " +
            "-fx-padding: 24;"
        );
        
        // ===== EN-TÊTE =====
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Icône du programme avec fond
        StackPane iconWrapper = new StackPane();
        iconWrapper.setStyle(
            "-fx-min-width: 48; -fx-min-height: 48; -fx-max-width: 48; -fx-max-height: 48; " +
            "-fx-background-color: -primary-muted; -fx-background-radius: -radius-md; " +
            "-fx-effect: -shadow-sm;"
        );
        FontAwesomeIconView headerIcon = new FontAwesomeIconView(FontAwesomeIcon.CALENDAR);
        headerIcon.setGlyphSize(24);
        headerIcon.setStyle(headerIcon.getStyle() + "-fx-fill: -primary;");
        iconWrapper.getChildren().add(headerIcon);
        
        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(programme.getTypeProgramme());
        titleLabel.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: -text-primary; " +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        // Statut avec badge
        String statut = getStatutProgramme(programme);
        Label statutBadge = new Label(statut);
        statutBadge.getStyleClass().addAll("badge", "badge-" + statut.toLowerCase().replace(" ", ""));
        statutBadge.setStyle("-fx-effect: -shadow-sm;");
        
        HBox titleRow = new HBox(12, titleLabel, statutBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label subtitleLabel = new Label("Programme " + (programme.getId() != null ? "N°" + programme.getId() : ""));
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -text-secondary;");
        
        titleBox.getChildren().addAll(titleRow, subtitleLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(iconWrapper, titleBox, spacer);
        mainCard.getChildren().add(header);
        
        // ===== SÉPARATEUR =====
        Region separator1 = new Region();
        separator1.setStyle(
            "-fx-background-color: -border-color; " +
            "-fx-pref-height: 1; -fx-max-height: 1; " +
            "-fx-padding: 0;"
        );
        mainCard.getChildren().add(separator1);
        
        // ===== GRILLE D'INFORMATIONS =====
        VBox infoSection = new VBox(12);
        infoSection.setStyle("-fx-padding: 4 0 0 0;");
        
        Label infoTitle = new Label("Informations générales");
        infoTitle.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: -text-primary; " +
            "-fx-padding: 0 0 4 0;"
        );
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(24);
        infoGrid.setVgap(12);
        infoGrid.setStyle("-fx-padding: 0;");
        
        // Date
        addDetailRowWithIcon(infoGrid, 0, FontAwesomeIcon.CALENDAR, "Date", 
            programme.getDateProgramme() != null ? 
            programme.getDateProgramme().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
        
        // Heure
        addDetailRowWithIcon(infoGrid, 1, FontAwesomeIcon.BELL, "Heure", 
            (programme.getHeureDebut() != null ? programme.getHeureDebut().toString() : "--:--") + 
            " - " + 
            (programme.getHeureFin() != null ? programme.getHeureFin().toString() : "--:--"));
        
        // Responsable
        addDetailRowWithIcon(infoGrid, 2, FontAwesomeIcon.USER, "Responsable", 
            programme.getUtilisateur() != null ? 
            programme.getUtilisateur().getPrenom() + " " + programme.getUtilisateur().getNom() : "—");
        
        infoSection.getChildren().addAll(infoTitle, infoGrid);
        mainCard.getChildren().add(infoSection);
        
        // ===== SÉPARATEUR =====
        Region separator2 = new Region();
        separator2.setStyle(
            "-fx-background-color: -border-color; " +
            "-fx-pref-height: 1; -fx-max-height: 1; " +
            "-fx-padding: 0;"
        );
        mainCard.getChildren().add(separator2);
        
        // ===== PARTICIPANTS =====
        VBox participantsSection = new VBox(10);
        participantsSection.setStyle("-fx-padding: 0;");
        
        Label participantsTitle = new Label("Participants");
        participantsTitle.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: -text-primary; " +
            "-fx-padding: 0 0 4 0;"
        );
        
        HBox participantsBox = new HBox(12);
        participantsBox.setAlignment(Pos.CENTER_LEFT);
        participantsBox.setStyle("-fx-padding: 0;");
        
        int hommes = programme.getHommes() != null ? programme.getHommes() : 0;
        int femmes = programme.getFemmes() != null ? programme.getFemmes() : 0;
        int enfants = programme.getEnfants() != null ? programme.getEnfants() : 0;
        int jeunes = programme.getJeunes() != null ? programme.getJeunes() : 0;
        int total = hommes + femmes + enfants + jeunes;
        
        participantsBox.getChildren().addAll(
            createParticipantTile(FontAwesomeIcon.MALE, "Hommes", String.valueOf(hommes)),
            createParticipantTile(FontAwesomeIcon.FEMALE, "Femmes", String.valueOf(femmes)),
            createParticipantTile(FontAwesomeIcon.CHILD, "Enfants", String.valueOf(enfants)),
            createParticipantTile(FontAwesomeIcon.GRADUATION_CAP, "Jeunes", String.valueOf(jeunes)),
            createParticipantTile(FontAwesomeIcon.USERS, "Total", String.valueOf(total), true)
        );
        
        participantsSection.getChildren().addAll(participantsTitle, participantsBox);
        mainCard.getChildren().add(participantsSection);
        
        // ===== SÉPARATEUR =====
        Region separator3 = new Region();
        separator3.setStyle(
            "-fx-background-color: -border-color; " +
            "-fx-pref-height: 1; -fx-max-height: 1; " +
            "-fx-padding: 0;"
        );
        mainCard.getChildren().add(separator3);
        
        // ===== SEMENCES =====
        VBox semencesSection = new VBox(10);
        semencesSection.setStyle("-fx-padding: 0;");
        
        HBox semencesHeader = new HBox(10);
        semencesHeader.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView semenceIcon = new FontAwesomeIconView(FontAwesomeIcon.LEAF);
        semenceIcon.setGlyphSize(16);
        semenceIcon.setStyle(semenceIcon.getStyle() + "-fx-fill: -success;");
        
        Label semencesTitle = new Label("Semences associées");
        semencesTitle.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: -text-primary; " +
            "-fx-padding: 0 0 4 0;"
        );
        
        Region semenceSpacer = new Region();
        HBox.setHgrow(semenceSpacer, javafx.scene.layout.Priority.ALWAYS);
        
        double totalSemences = 0;
        if (programme.getSemences() != null && !programme.getSemences().isEmpty()) {
            totalSemences = programme.getSemences().stream()
                .mapToDouble(s -> s.getMontant() != null ? s.getMontant().doubleValue() : 0)
                .sum();
        }
        
        Label semencesCount = new Label((programme.getSemences() != null ? programme.getSemences().size() : 0) + " semence(s)");
        semencesCount.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: -text-secondary;");
        
        Label semencesTotal = new Label(String.format("Total: %,.0f F CFA", totalSemences));
        semencesTotal.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: -success; " +
            "-fx-background-color: -success-muted; -fx-padding: 2 12 2 12; " +
            "-fx-background-radius: -radius-pill;"
        );
        
        semencesHeader.getChildren().addAll(semenceIcon, semencesTitle, semenceSpacer, semencesCount, semencesTotal);
        
        // Liste des semences
        VBox semencesList = new VBox(6);
        semencesList.setStyle("-fx-padding: 4 0 0 0;");
        
        if (programme.getSemences() != null && !programme.getSemences().isEmpty()) {
            for (Semence s : programme.getSemences()) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle(
                    "-fx-background-color: -bg-subtle; -fx-background-radius: -radius-sm; " +
                    "-fx-padding: 8 14 8 14; -fx-spacing: 10; " +
                    "-fx-border-color: -border-color; -fx-border-width: 1; " +
                    "-fx-border-radius: -radius-sm; -fx-effect: -shadow-sm;"
                );
                
                FontAwesomeIconView dotIcon = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE);
                dotIcon.setGlyphSize(8);
                dotIcon.setStyle(dotIcon.getStyle() + "-fx-fill: -primary;");
                
                Label typeLabel = new Label(s.getTypeSemence().name());
                typeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: -text-primary;");
                
                Region spacerItem = new Region();
                HBox.setHgrow(spacerItem, javafx.scene.layout.Priority.ALWAYS);
                
                Label montantLabel = new Label(String.format("%,.0f F CFA", s.getMontant().doubleValue()));
                montantLabel.setStyle(
                    "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: -text-primary; " +
                    "-fx-padding: 0 8 0 0;"
                );
                
                Label compteLabel = new Label(s.getCompte() != null ? s.getCompte().getTypeCompte() : "—");
                compteLabel.setStyle(
                    "-fx-font-size: 11px; -fx-font-weight: 400; -fx-text-fill: -text-tertiary; " +
                    "-fx-padding: 2 10 2 10; -fx-background-color: -bg-surface; " +
                    "-fx-background-radius: -radius-pill; -fx-border-color: -border-color; " +
                    "-fx-border-width: 1; -fx-border-radius: -radius-pill;"
                );
                
                item.getChildren().addAll(dotIcon, typeLabel, spacerItem, montantLabel, compteLabel);
                semencesList.getChildren().add(item);
            }
        } else {
            Label noSemences = new Label("Aucune semence associée à ce programme");
            noSemences.setStyle(
                "-fx-text-fill: -text-tertiary; -fx-font-style: italic; " +
                "-fx-padding: 12 0 0 0; -fx-font-size: 13px;"
            );
            semencesList.getChildren().add(noSemences);
        }
        
        semencesSection.getChildren().addAll(semencesHeader, semencesList);
        mainCard.getChildren().add(semencesSection);
        
        // ===== ASSEMBLAGE FINAL =====
        content.getChildren().add(mainCard);
        
        return content;
    }
    
    private void addDetailRowWithIcon(GridPane grid, int row, FontAwesomeIcon icon, String label, String value) {
        int col = 0;
        
        // Icône
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(14);
        iconView.setStyle(iconView.getStyle() + "-fx-fill: -text-tertiary;");
        grid.add(iconView, col++, row);
        
        // Label
        Label keyLabel = new Label(label);
        keyLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: -text-secondary; -fx-font-size: 13px;");
        grid.add(keyLabel, col++, row);
        
        // Valeur
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 13px;");
        grid.add(valueLabel, col++, row);
    }
    
    private VBox createParticipantTile(FontAwesomeIcon icon, String label, String value) {
        return createParticipantTile(icon, label, value, false);
    }
    
    private VBox createParticipantTile(FontAwesomeIcon icon, String label, String value, boolean isTotal) {
        VBox tile = new VBox(4);
        tile.setAlignment(Pos.CENTER);
        tile.setStyle(
            "-fx-background-color: " + (isTotal ? "-primary-muted" : "-bg-subtle") + "; " +
            "-fx-background-radius: -radius-sm; -fx-padding: 8 14 8 14; -fx-min-width: 70;"
        );
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(16);
        iconView.setStyle(iconView.getStyle() + "-fx-fill: " + (isTotal ? "-primary" : "-text-tertiary") + ";");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: " + (isTotal ? "20px" : "16px") + "; " +
            "-fx-font-weight: 700; -fx-text-fill: " + (isTotal ? "-primary" : "-text-primary") + ";"
        );
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 500; -fx-text-fill: -text-secondary;");
        
        tile.getChildren().addAll(iconView, valueLabel, labelLabel);
        return tile;
    }
    
    // ==================== ACTIONS TABLE ====================
    
    private Callback<TableColumn<Programme, Void>, TableCell<Programme, Void>> createActionsCellFactory() {
        return param -> new TableCell<Programme, Void>() {
            private final HBox buttons = new HBox(4);
            
            {
                // Bouton Voir avec icône
                Button viewBtn = createTableButton(FontAwesomeIcon.EYE, "Voir les détails", "primary");
                viewBtn.setOnAction(e -> {
                    Programme p = getTableView().getItems().get(getIndex());
                    ouvrirDetailsProgramme(p);
                });
                
                // Bouton Éditer avec icône
                Button editBtn = createTableButton(FontAwesomeIcon.EDIT, "Modifier", "warning");
                editBtn.setOnAction(e -> {
                    Programme p = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireProgramme(p);
                });
                
                // Bouton Supprimer avec icône
                Button deleteBtn = createTableButton(FontAwesomeIcon.TRASH, "Supprimer", "danger");
                deleteBtn.setOnAction(e -> {
                    Programme p = getTableView().getItems().get(getIndex());
                    if (UICommun.confirmDelete("programme", p.getTypeProgramme())) {
                        boolean success = programmeService.supprimer(p.getId());
                        if (success) {
                            loadData();
                            UICommun.showSuccess("Suppression", "Programme supprimé avec succès");
                        } else {
                            UICommun.showError("Suppression", "Impossible de supprimer le programme");
                        }
                    }
                });
                
                buttons.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                buttons.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
    }
    
    private Button createTableButton(FontAwesomeIcon icon, String tooltip, String colorClass) {
        Button btn = new Button();
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-padding: 4 6 4 6; " +
            "-fx-cursor: hand; " +
            "-fx-min-width: 28; " +
            "-fx-min-height: 28;"
        );
        btn.setTooltip(new Tooltip(tooltip));
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(14);
        iconView.getStyleClass().add("glyph-icon");
        
        String color = "#6B7280";
        switch (colorClass) {
            case "primary": color = "#4F46E5"; break;
            case "warning": color = "#F59E0B"; break;
            case "danger": color = "#EF4444"; break;
            case "success": color = "#10B981"; break;
        }
        iconView.setFill(Color.web(color));
        
        btn.setGraphic(iconView);
        
        // Ajout du style hover - SANS ARRONDIS
        btn.setOnMouseEntered(e -> {
            String bgColor = "";
            switch (colorClass) {
                case "primary": bgColor = "#EEF2FF"; break;
                case "warning": bgColor = "#FEF3C7"; break;
                case "danger": bgColor = "#FEE2E2"; break;
                case "success": bgColor = "#D1FAE5"; break;
            }
            btn.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-padding: 4 6 4 6; " +
                "-fx-cursor: hand; " +
                "-fx-min-width: 28; " +
                "-fx-min-height: 28;"
            );
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-padding: 4 6 4 6; " +
                "-fx-cursor: hand; " +
                "-fx-min-width: 28; " +
                "-fx-min-height: 28;"
            );
        });
        
        return btn;
    }
    
    // ==================== GETTERS / SETTERS ====================
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    public Node getView() {
        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/programmes.fxml"));
                view = loader.load();
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement vue programmes: " + e.getMessage());
                e.printStackTrace();
                view = createFallbackView();
            }
        }
        return view;
    }
    
    private Node createFallbackView() {
        VBox fallback = new VBox(20);
        fallback.setAlignment(Pos.CENTER);
        fallback.setPadding(new Insets(40));
        fallback.setStyle("-fx-background-color: -bg-app;");
        
        Label title = new Label("Module Programmes");
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