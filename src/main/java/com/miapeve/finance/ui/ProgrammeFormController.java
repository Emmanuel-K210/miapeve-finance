package com.miapeve.finance.ui;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.enums.TypeProgramme;
import com.miapeve.finance.enums.TypeSemence;
import com.miapeve.finance.repositories.SemenceRepository;
import com.miapeve.finance.repositories.impl.SemenceRepositoryImpl;
import com.miapeve.finance.service.CompteService;
import com.miapeve.finance.service.ProgrammeService;
import com.miapeve.finance.service.impl.CompteServiceImpl;
import com.miapeve.finance.service.impl.ProgrammeServiceImpl;
import com.miapeve.finance.utils.UICommun;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ProgrammeFormController implements Initializable {

    // ==================== FXML COMPONENTS ====================
    
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField customTypeField;
    @FXML private Button toggleCustomBtn;
    @FXML private DatePicker dateField;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private Label dureeLabel;
    @FXML private Label utilisateurConnecteLabel;
    
    // Participants
    @FXML private TextField hommesField;
    @FXML private TextField femmesField;
    @FXML private TextField enfantsField;
    @FXML private TextField jeunesField;
    @FXML private Label totalParticipantsLabel;
    
    // Semences - ligne de saisie rapide
    @FXML private ComboBox<TypeSemence> typeSemenceCombo;
    @FXML private TextField montantSemenceField;
    @FXML private ComboBox<Compte> compteSemenceCombo;
    @FXML private Button btnAjouterLigneSemence;
    
    @FXML private TableView<Semence> semencesTable;
    @FXML private VBox noSemencesContainer;
    @FXML private Label totalSemencesLabel;
    @FXML private Label nombreSemencesLabel;
    
    // Erreur
    @FXML private Label errorLabel;
    
    // ==================== SERVICES ====================
    
    private ProgrammeService programmeService;
    private SemenceRepository semenceRepository;
    private CompteService compteService;
    
    // ==================== DONNÉES ====================
    
    private Programme programme;
    private Utilisateur utilisateurConnecte;
    private ObservableList<Semence> semencesList;
    private ObservableList<String> typesProgrammes;
    private MainController mainController;
    private boolean isEditMode = false;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        programmeService = new ProgrammeServiceImpl();
        semenceRepository = new SemenceRepositoryImpl();
        compteService = new CompteServiceImpl();
        semencesList = FXCollections.observableArrayList();
        typesProgrammes = FXCollections.observableArrayList();
        
        initializeForm();
        initializeDenombrement();
        initializeSemencesTable();
        initializeSemenceInlineForm();
        setupEventHandlers();
        
        Platform.runLater(() -> {
            if (typeComboBox != null) {
                typeComboBox.requestFocus();
            }
        });
    }
    
    private void initializeForm() {
        typesProgrammes.addAll(TypeProgramme.findAll());
        typeComboBox.setItems(typesProgrammes);
        dateField.setValue(LocalDate.now());
        heureDebutField.setText("09:00");
        heureFinField.setText("12:00");
        
        addTimeValidation(heureDebutField);
        addTimeValidation(heureFinField);
        setupDureeCalculation();
    }
    
    private void initializeDenombrement() {
        addNumericValidation(hommesField, femmesField, enfantsField, jeunesField);
        
        hommesField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalParticipants());
        femmesField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalParticipants());
        enfantsField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalParticipants());
        jeunesField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalParticipants());
        
        hommesField.setText("0");
        femmesField.setText("0");
        enfantsField.setText("0");
        jeunesField.setText("0");
    }
    
    @SuppressWarnings("unchecked")
    private void initializeSemencesTable() {
        TableColumn<Semence, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTypeSemence().name()));
        typeCol.setPrefWidth(180);
        
        TableColumn<Semence, String> montantCol = new TableColumn<>("Montant (F CFA)");
        montantCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%,.0f", cellData.getValue().getMontant().doubleValue())));
        montantCol.setPrefWidth(150);
        
        TableColumn<Semence, String> compteCol = new TableColumn<>("Compte");
        compteCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCompte() != null ? 
                cellData.getValue().getCompte().getTypeCompte() : "Non défini"));
        compteCol.setPrefWidth(180);
        
        TableColumn<Semence, Void> actionsCol = new TableColumn<>("");
        actionsCol.setCellFactory(createActionsCellFactory());
        actionsCol.setPrefWidth(60);
        
        semencesTable.getColumns().clear();
        semencesTable.getColumns().addAll(typeCol, montantCol, compteCol, actionsCol);
        semencesTable.setItems(semencesList);
        
        updateSemencesDisplay();
    }
    
    private void initializeSemenceInlineForm() {
        // Types de semences
        typeSemenceCombo.setItems(FXCollections.observableArrayList(TypeSemence.values()));
        if (!typeSemenceCombo.getItems().isEmpty()) {
            typeSemenceCombo.getSelectionModel().selectFirst();
        }
        
        // Comptes
        List<Compte> comptes = compteService.findAll();
        compteSemenceCombo.setItems(FXCollections.observableArrayList(comptes));
        compteSemenceCombo.setConverter(new StringConverter<Compte>() {
            @Override
            public String toString(Compte compte) {
                return compte != null ? compte.getTypeCompte() : "";
            }
            @Override
            public Compte fromString(String string) {
                return null;
            }
        });
        if (!comptes.isEmpty()) {
            compteSemenceCombo.getSelectionModel().selectFirst();
        }
        
        // Validation numérique
        addNumericValidation(montantSemenceField);
        
        // Entrée valide la ligne
        montantSemenceField.setOnAction(e -> handleAjouterSemenceLigne());
    }
    
    private void setupEventHandlers() {
        toggleCustomBtn.setOnAction(e -> toggleCustomType());
    }
    
    private void setupDureeCalculation() {
        heureDebutField.textProperty().addListener((obs, oldVal, newVal) -> updateDuree());
        heureFinField.textProperty().addListener((obs, oldVal, newVal) -> updateDuree());
    }

    // ==================== MÉTHODES DE VALIDATION ====================
    
    
    /**
     * Vérifie si une chaîne est une heure valide au format HH:MM.
     */
    private boolean isValidTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Met à jour l'affichage de la durée en temps réel.
     */
    private void updateDuree() {
        String debutStr = heureDebutField.getText();
        String finStr = heureFinField.getText();
        
        // Vérifier que les deux champs sont remplis
        if (debutStr == null || debutStr.trim().isEmpty() || 
            finStr == null || finStr.trim().isEmpty()) {
            dureeLabel.setText("--");
            dureeLabel.setStyle("-fx-text-fill: -text-tertiary;");
            return;
        }
        
        // Vérifier que les deux heures sont valides
        if (!isValidTime(debutStr) || !isValidTime(finStr)) {
            dureeLabel.setText("--");
            dureeLabel.setStyle("-fx-text-fill: -text-tertiary;");
            return;
        }
        
        try {
            LocalTime debut = LocalTime.parse(debutStr.trim(), TIME_FORMATTER);
            LocalTime fin = LocalTime.parse(finStr.trim(), TIME_FORMATTER);
            
            // Calculer la durée en minutes
            long minutes = calculateDurationInMinutes(debut, fin);
            
            if (minutes > 0) {
                long heures = minutes / 60;
                long mins = minutes % 60;
                
                // Formater la durée
                String dureeText;
                if (heures > 0 && mins > 0) {
                    dureeText = heures + "h" + mins + "min";
                } else if (heures > 0) {
                    dureeText = heures + "h";
                } else {
                    dureeText = mins + "min";
                }
                
                dureeLabel.setText(dureeText);
                dureeLabel.setStyle("-fx-text-fill: -success; -fx-font-weight: 700;");
            } else {
                dureeLabel.setText("--");
                dureeLabel.setStyle("-fx-text-fill: -text-tertiary;");
            }
        } catch (Exception e) {
            dureeLabel.setText("--");
            dureeLabel.setStyle("-fx-text-fill: -text-tertiary;");
        }
    }
    
    // ==================== VALIDATION DU FORMULAIRE ====================
    
    public boolean validateForm() {
        clearError();
        
        // === 1. Type de programme ===
        String typeProgramme = getTypeProgramme();
        if (typeProgramme == null || typeProgramme.trim().isEmpty()) {
            showError("Le type de programme est requis");
            return false;
        }
        
        // === 2. Date ===
        if (dateField.getValue() == null) {
            showError("La date est requise");
            return false;
        }
        
        // === 3. Heure début ===
        String debutStr = heureDebutField.getText();
        if (debutStr == null || debutStr.trim().isEmpty()) {
            showError("L'heure de début est requise");
            return false;
        }
        if (!isValidTime(debutStr)) {
            showError("Format d'heure de début invalide (HH:MM)");
            return false;
        }
        
        // === 4. Heure fin ===
        String finStr = heureFinField.getText();
        if (finStr == null || finStr.trim().isEmpty()) {
            showError("L'heure de fin est requise");
            return false;
        }
        if (!isValidTime(finStr)) {
            showError("Format d'heure de fin invalide (HH:MM)");
            return false;
        }
        
        // === 5. Parsing des heures ===
        LocalTime debut = LocalTime.parse(debutStr.trim(), TIME_FORMATTER);
        LocalTime fin = LocalTime.parse(finStr.trim(), TIME_FORMATTER);
        
        // === 6. Les heures ne peuvent pas être identiques ===
        // Cas particulier : 00:00 → 00:00 = 24h (journée complète)
        // On autorise si l'utilisateur veut un programme de 24h
        if (debut.equals(fin)) {
            // On considère que c'est un programme de 24h
            // On laisse passer, la durée sera calculée comme 24h
            // Mais on affiche un message informatif
            dureeLabel.setText("24h");
            dureeLabel.setStyle("-fx-text-fill: -success;");
            // On ne bloque pas, car un programme de 24h est valide
            // (ex: du 01/01 00:00 au 02/01 00:00)
        }
        
        // === 7. Calcul de la durée ===
        // La fonction calculateDurationInMinutes gère déjà le cas :
        // - 22h → 01h = 3h (passage à minuit)
        // - 08h → 17h = 9h (journée normale)
        // - 00h → 00h = 24h (journée complète)
        long dureeMinutes = calculateDurationInMinutes(debut, fin);
        
        // === 8. Vérification de la durée minimale ===
        if (dureeMinutes < 1) {
            showError("La durée du programme doit être d'au moins 1 minute");
            return false;
        }
        
        // === 9. Vérification de la durée maximale (optionnel) ===
        // On limite à 24h maximum (1440 minutes)
        // Si vous voulez autoriser des programmes plus longs, augmentez cette valeur
        if (dureeMinutes > 1440) {
            showError("La durée d'un programme ne peut pas dépasser 24h");
            return false;
        }
        
        return true;
    }

    /**
     * Calcule la durée entre deux heures, en gérant le passage à minuit.
     * 
     * Exemples :
     * - 22:00 → 01:30 = 3h30 (210 minutes) ✅
     * - 08:00 → 17:00 = 9h (540 minutes) ✅
     * - 00:00 → 00:00 = 24h (1440 minutes) ✅
     * - 23:00 → 06:00 = 7h (420 minutes) ✅
     * - 18:00 → 02:00 = 8h (480 minutes) ✅
     * - 10:00 → 10:00 = 24h (1440 minutes) ✅
     */
    private long calculateDurationInMinutes(LocalTime debut, LocalTime fin) {
        if (debut == null || fin == null) {
            return 0;
        }
        
        // Si les heures sont identiques → 24h (journée complète)
        if (debut.equals(fin)) {
            return 1440; // 24h * 60min
        }
        
        // Calcul de la différence en minutes
        long diff = ChronoUnit.MINUTES.between(debut, fin);
        
        // Si la différence est négative, on ajoute 24h (1440 minutes)
        // Cela gère le passage à minuit (ex: 22h → 01h = -1260 + 1440 = 180min = 3h)
        if (diff < 0) {
            diff += 1440; // Ajouter 24h
        }
        
        return diff;
    }
    
    // ==================== SAUVEGARDE ====================
    
    @FXML
    private void handleSauvegarder() {
        // 1. Valider le formulaire
        if (!validateForm()) {
            return;
        }
        
        // 2. Récupérer les données
        Programme programmeToSave = buildProgrammeFromForm();
        if (programmeToSave == null) {
            return;
        }
        
        try {
            // 3. Sauvegarder le programme
            Programme savedProgramme = programmeService.enregistrer(programmeToSave);
            
            // 4. Sauvegarder les semences associées
            if (!semencesList.isEmpty()) {
                for (Semence semence : semencesList) {
                    semence.setProgramme(savedProgramme);
                    if (utilisateurConnecte != null) {
                        if (semence.getSemenceId() == null) {
                            semence.setCreePar(utilisateurConnecte.getId());
                        } else {
                            semence.setModifiePar(utilisateurConnecte.getId());
                        }
                    }
                    semenceRepository.enregistrer(semence);
                }
            }
            
            // 5. Message de succès
            String message = isEditMode ? 
                "Programme mis à jour avec succès" : 
                "Programme créé avec succès";
            if (!semencesList.isEmpty()) {
                message += "\n🌱 " + semencesList.size() + " semence(s) associée(s)";
            }
            UICommun.showSuccess("Succès", message);
            
            // 6. Revenir à la liste
            if (mainController != null) {
                mainController.loadProgrammes();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
            UICommun.showError("Sauvegarde", e.getMessage());
        }
    }
    
    private Programme buildProgrammeFromForm() {
        Programme p = (this.programme != null) ? this.programme : new Programme();
        
        p.setTypeProgramme(getTypeProgramme());
        p.setDateProgramme(dateField.getValue());
        
        // ✅ Protection contre les heures invalides
        try {
            p.setHeureDebut(LocalTime.parse(heureDebutField.getText().trim(), TIME_FORMATTER));
            p.setHeureFin(LocalTime.parse(heureFinField.getText().trim(), TIME_FORMATTER));
        } catch (DateTimeParseException e) {
            System.err.println("❌ Erreur parsing heure: " + e.getMessage());
            // Utiliser des valeurs par défaut
            p.setHeureDebut(LocalTime.parse("00:00", TIME_FORMATTER));
            p.setHeureFin(LocalTime.parse("00:00", TIME_FORMATTER));
        }
        
        p.setHommes(parseFieldValue(hommesField));
        p.setFemmes(parseFieldValue(femmesField));
        p.setEnfants(parseFieldValue(enfantsField));
        p.setJeunes(parseFieldValue(jeunesField));
        
        if (utilisateurConnecte != null) {
            p.setUtilisateur(utilisateurConnecte);
            if (p.getId() == null) {
                p.setCreePar(utilisateurConnecte.getId());
            } else {
                p.setModifiePar(utilisateurConnecte.getId());
            }
        }
        
        return p;
    }

    // ==================== MÉTHODES PRIVÉES ====================
    
    private void toggleCustomType() {
        boolean isCustom = customTypeField.isVisible();
        
        customTypeField.setVisible(!isCustom);
        customTypeField.setManaged(!isCustom);
        typeComboBox.setVisible(isCustom);
        typeComboBox.setManaged(isCustom);
        toggleCustomBtn.setText(isCustom ? "Personnalisé" : "Liste");
        
        if (!isCustom) {
            customTypeField.requestFocus();
        } else {
            typeComboBox.requestFocus();
        }
    }
    
    private void addNumericValidation(TextField... fields) {
        for (TextField field : fields) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    field.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (newValue.length() > 9) {
                    field.setText(oldValue);
                }
            });
        }
    }
    
    private void addTimeValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[\\d:]*")) {
                field.setText(newValue.replaceAll("[^\\d:]", ""));
            }
            if (newValue.length() == 2 && oldValue.length() == 1 && !newValue.contains(":")) {
                field.setText(newValue + ":");
            }
            if (newValue.length() > 5) {
                field.setText(oldValue);
            }
        });
    }
    
    private void updateTotalParticipants() {
        try {
            int hommes = parseFieldValue(hommesField);
            int femmes = parseFieldValue(femmesField);
            int enfants = parseFieldValue(enfantsField);
            int jeunes = parseFieldValue(jeunesField);
            
            int total = hommes + femmes + enfants + jeunes;
            totalParticipantsLabel.setText(total + " personne" + (total > 1 ? "s" : ""));
            
        } catch (NumberFormatException e) {
            totalParticipantsLabel.setText("0 personnes");
        }
    }
    
    private int parseFieldValue(TextField field) {
        String text = field.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
    
    private String getTypeProgramme() {
        if (customTypeField.isVisible()) {
            return customTypeField.getText();
        } else {
            return typeComboBox.getValue();
        }
    }
    
    private void updateSemencesDisplay() {
        boolean hasSemences = !semencesList.isEmpty();
        
        semencesTable.setVisible(hasSemences);
        semencesTable.setManaged(hasSemences);
        noSemencesContainer.setVisible(!hasSemences);
        noSemencesContainer.setManaged(!hasSemences);
        
        double total = semencesList.stream()
            .mapToDouble(s -> s.getMontant().doubleValue())
            .sum();
        
        totalSemencesLabel.setText(String.format("%,.0f F CFA", total));
        nombreSemencesLabel.setText(semencesList.size() + " semence" + (semencesList.size() > 1 ? "s" : ""));
    }
    
    private Callback<TableColumn<Semence, Void>, TableCell<Semence, Void>> createActionsCellFactory() {
        return param -> new TableCell<Semence, Void>() {
            private final Button deleteBtn = new Button("✕");
            
            {
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -danger; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> {
                    Semence semence = getTableView().getItems().get(getIndex());
                    semencesList.remove(semence);
                    updateSemencesDisplay();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        };
    }
    
    @FXML
    private void handleAjouterSemenceLigne() {
        TypeSemence type = typeSemenceCombo.getValue();
        if (type == null) {
            UICommun.showAlert(AlertType.WARNING, "Semence", "Veuillez sélectionner un type de semence");
            typeSemenceCombo.requestFocus();
            return;
        }
        
        String montantStr = montantSemenceField.getText();
        if (montantStr == null || montantStr.trim().isEmpty()) {
            UICommun.showAlert(AlertType.WARNING, "Semence", "Veuillez saisir un montant");
            montantSemenceField.requestFocus();
            return;
        }
        
        double montant;
        try {
            montant = Double.parseDouble(montantStr.trim());
        } catch (NumberFormatException e) {
            UICommun.showAlert(AlertType.WARNING, "Semence", "Montant invalide");
            montantSemenceField.requestFocus();
            return;
        }
        
        if (montant <= 0) {
            UICommun.showAlert(AlertType.WARNING, "Semence", "Le montant doit être supérieur à 0");
            montantSemenceField.requestFocus();
            return;
        }
        
        Compte compte = compteSemenceCombo.getValue();
        if (compte == null) {
            UICommun.showAlert(AlertType.WARNING, "Semence", "Veuillez sélectionner un compte");
            compteSemenceCombo.requestFocus();
            return;
        }
        
        Semence nouvelleSemence = new Semence();
        nouvelleSemence.setTypeSemence(type);
        nouvelleSemence.setMontant(new BigDecimal(montant));
        nouvelleSemence.setCompte(compte);
        
        semencesList.add(nouvelleSemence);
        updateSemencesDisplay();
        
        montantSemenceField.clear();
        montantSemenceField.requestFocus();
    }
    
    private void showError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    // ==================== MÉTHODES PUBLIQUES ====================
    
    public void setProgramme(Programme programme) {
        this.programme = programme;
        this.isEditMode = programme != null && programme.getId() != null;
        loadProgrammeData();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        if (utilisateur != null) {
            utilisateurConnecteLabel.setText("👤 " + utilisateur.getPrenom() + " " + utilisateur.getNom());
            utilisateurConnecteLabel.setVisible(true);
            utilisateurConnecteLabel.setManaged(true);
        }
    }
    
    public Collection<Semence> getSemences() {
        return semencesList;
    }
    
    private void loadProgrammeData() {
        if (programme == null) {
            formTitle.setText("Nouveau Programme");
            formSubtitle.setText("Renseignez les informations du programme");
            return;
        }
        
        formTitle.setText(isEditMode ? "Modifier le Programme" : "Nouveau Programme");
        formSubtitle.setText(isEditMode ? "Modifiez les informations du programme" : "Renseignez les informations du programme");
        
        // Type
        if (programme.getTypeProgramme() != null) {
            if (typesProgrammes.contains(programme.getTypeProgramme())) {
                typeComboBox.setValue(programme.getTypeProgramme());
            } else {
                customTypeField.setText(programme.getTypeProgramme());
                toggleCustomType();
            }
        }
        
        // Date
        if (programme.getDateProgramme() != null) {
            dateField.setValue(programme.getDateProgramme());
        }
        
        // Heures
        if (programme.getHeureDebut() != null) {
            heureDebutField.setText(programme.getHeureDebut().format(TIME_FORMATTER));
        }
        if (programme.getHeureFin() != null) {
            heureFinField.setText(programme.getHeureFin().format(TIME_FORMATTER));
        }
        
        // Participants
        if (programme.getHommes() != null) hommesField.setText(String.valueOf(programme.getHommes()));
        if (programme.getFemmes() != null) femmesField.setText(String.valueOf(programme.getFemmes()));
        if (programme.getEnfants() != null) enfantsField.setText(String.valueOf(programme.getEnfants()));
        if (programme.getJeunes() != null) jeunesField.setText(String.valueOf(programme.getJeunes()));
        
        // Semences
        if (programme.getSemences() != null && !programme.getSemences().isEmpty()) {
            semencesList.setAll(programme.getSemences());
            updateSemencesDisplay();
        }
        
        updateTotalParticipants();
        updateDuree();
    }
    
    @FXML
    private void handleRetour() {
        if (mainController != null) {
            mainController.loadProgrammes();
        }
    }
}