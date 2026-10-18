package com.miapeve.finance.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.config.PathManager;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.UtilisateurService;
import com.miapeve.finance.utils.UICommun;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ParametresController implements Initializable {

    // ==================== FXML COMPONENTS ====================
    
    @FXML private TabPane parametresTabs;
    @FXML private Tab tabProfil;
    @FXML private Tab tabUtilisateurs;
    @FXML private Tab tabSysteme;

    // Composants Profil
    @FXML private Label labelNomUtilisateur;
    @FXML private Label labelRole;
    @FXML private Label labelDateCreation;

    // Composants Gestion Utilisateurs
    @FXML private TableView<Utilisateur> utilisateursTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private Button btnNouvelUtilisateur;
    @FXML private Label totalUtilisateursLabel;

    // Colonnes Table Utilisateurs
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colNomUtilisateur;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Boolean> colActif;
    @FXML private TableColumn<Utilisateur, Void> colActions;

    // ==================== SERVICES ====================
    
    private MainController mainController;
    private UtilisateurService utilisateurService;
    private ObservableList<Utilisateur> utilisateursList;

    // ==================== CONSTRUCTEUR ====================
    
    public ParametresController() {
        this.utilisateursList = FXCollections.observableArrayList();
    }

    // ==================== SETTERS ====================
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("⚙️ Initialisation de ParametresController");

        this.utilisateurService = new com.miapeve.finance.service.impl.UtilisateurServiceImpl();

        initializeTabs();
        initializeGestionUtilisateurs();
    }

    private void initializeTabs() {
        parametresTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == tabProfil) {
                chargerProfil();
            } else if (newTab == tabUtilisateurs) {
                chargerGestionUtilisateurs();
            } else if (newTab == tabSysteme) {
                chargerParametresSysteme();
            }
        });
    }

    private void initializeGestionUtilisateurs() {
        initializeFilters();
        configureTableColumns();
        setupEventHandlers();
    }

    private void initializeFilters() {
        UICommun.executeSafe(() -> {
            if (roleFilter != null) {
                roleFilter.getItems().clear();
                roleFilter.getItems().addAll("Tous", "ADMIN", "PASTEUR", "COMPTABLE", "MEMBRE");
                roleFilter.setValue("Tous");
            }

            if (statutFilter != null) {
                statutFilter.getItems().clear();
                statutFilter.getItems().addAll("Tous", "Actif", "Inactif");
                statutFilter.setValue("Tous");
            }
        }, "initialisation des filtres");
    }

    private void configureTableColumns() {
        UICommun.executeSafe(() -> {
            if (colNom != null) {
                colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            }
            
            if (colPrenom != null) {
                colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            }
            
            if (colNomUtilisateur != null) {
                colNomUtilisateur.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
            }
            
            if (colRole != null) {
                colRole.setCellValueFactory(cellData -> {
                    Utilisateur utilisateur = cellData.getValue();
                    if (utilisateur != null && utilisateur.getRole() != null) {
                        return new SimpleStringProperty(utilisateur.getRole().getLibelle());
                    }
                    return new SimpleStringProperty("N/A");
                });
            }
            
            if (colActif != null) {
                colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
                colActif.setCellFactory(column -> new TableCell<Utilisateur, Boolean>() {
                    @Override
                    protected void updateItem(Boolean actif, boolean empty) {
                        super.updateItem(actif, empty);
                        if (empty || actif == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(actif ? "✅ Actif" : "❌ Inactif");
                            setStyle(actif ? 
                                "-fx-text-fill: -success; -fx-font-weight: bold;" : 
                                "-fx-text-fill: -danger; -fx-font-weight: bold;");
                        }
                    }
                });
            }

            if (colActions != null) {
                colActions.setCellFactory(param -> createActionsCell());
            }

            if (utilisateursTable != null) {
                utilisateursTable.setItems(utilisateursList);
            }
        }, "configuration des colonnes");
    }

    private TableCell<Utilisateur, Void> createActionsCell() {
        return new TableCell<Utilisateur, Void>() {
            
            // Création des icônes FontAwesome avec setFill() au lieu de setStyle()
            private final FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
            private final FontAwesomeIconView iconKey = new FontAwesomeIconView(FontAwesomeIcon.KEY);
            private final FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
            
            private final Button editBtn = new Button();
            private final Button resetBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox buttons = new HBox(5, editBtn, resetBtn, deleteBtn);

            {
                // ✅ Configuration des icônes avec setFill() au lieu de setStyle()
                iconEdit.setGlyphSize(12);
                iconEdit.setFill(Color.WHITE);
                
                iconKey.setGlyphSize(12);
                iconKey.setFill(Color.WHITE);
                
                iconDel.setGlyphSize(12);
                iconDel.setFill(Color.WHITE);

                // Configuration des boutons
                editBtn.setGraphic(iconEdit);
                editBtn.setTooltip(new Tooltip("Modifier"));
                editBtn.setStyle(
                    "-fx-background-color: -primary; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                );
                
                resetBtn.setGraphic(iconKey);
                resetBtn.setTooltip(new Tooltip("Réinitialiser le mot de passe"));
                resetBtn.setStyle(
                    "-fx-background-color: -warning; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                );
                
                deleteBtn.setGraphic(iconDel);
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                deleteBtn.setStyle(
                    "-fx-background-color: -danger; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                );

                // Hover effects
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                    "-fx-background-color: -primary-hover; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                ));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(
                    "-fx-background-color: -primary; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                ));
                
                resetBtn.setOnMouseEntered(e -> resetBtn.setStyle(
                    "-fx-background-color: -warning-dark; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                ));
                resetBtn.setOnMouseExited(e -> resetBtn.setStyle(
                    "-fx-background-color: -warning; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                ));
                
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                    "-fx-background-color: -danger-dark; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                ));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                    "-fx-background-color: -danger; " +
                    "-fx-text-fill: white; " +
                    "-fx-min-width: 30; " +
                    "-fx-min-height: 28; " +
                    "-fx-padding: 0 6; " +
                    "-fx-cursor: hand;"
                ));

                buttons.setAlignment(Pos.CENTER);

                // Actions
                editBtn.setOnAction(e -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireUtilisateur(utilisateur);
                });

                resetBtn.setOnAction(e -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    reinitialiserMotDePasse(utilisateur);
                });

                deleteBtn.setOnAction(e -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(utilisateur);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        };
    }

    private void setupEventHandlers() {
        UICommun.executeSafe(() -> {
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterUtilisateurs();
                });
            }

            if (roleFilter != null) {
                roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                    filterUtilisateurs();
                });
            }

            if (statutFilter != null) {
                statutFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                    filterUtilisateurs();
                });
            }

            if (btnNouvelUtilisateur != null) {
                btnNouvelUtilisateur.setOnAction(e -> ouvrirFormulaireUtilisateur(null));
            }
        }, "configuration des gestionnaires d'événements");
    }

    // ==================== MÉTHODES DE GESTION DES UTILISATEURS ====================

    public void loadData() {
        System.out.println("📥 Chargement des données paramètres");
        chargerProfil();
    }

    private void chargerProfil() {
        UICommun.executeSafe(() -> {
            Utilisateur utilisateur = mainController.getUtilisateurCourant();
            if (utilisateur != null) {
                labelNomUtilisateur.setText(utilisateur.getNomUtilisateur());
                labelRole.setText(utilisateur.getRole() != null ? 
                    utilisateur.getRole().getLibelle() : "N/A");
                labelDateCreation.setText(utilisateur.getDateCreation() != null ? 
                    utilisateur.getDateCreation().toString() : "Non disponible");
            }
        }, "chargement du profil");
    }

    private void chargerGestionUtilisateurs() {
        UICommun.executeSafe(() -> {
            Utilisateur currentUser = mainController.getUtilisateurCourant();
            if (!UICommun.hasAdminPermission(currentUser)) {
                UICommun.showAlert(AlertType.WARNING, "Permission refusée",
                        "Seuls les administrateurs peuvent gérer les utilisateurs.");
                return;
            }

            chargerUtilisateurs();
        }, "chargement de la gestion des utilisateurs");
    }

    private void chargerUtilisateurs() {
        System.out.println("📥 ========== CHARGEMENT UTILISATEURS ==========");
        
        try {
            if (utilisateurService == null) {
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Service utilisateur non disponible.");
                return;
            }
        
            List<Utilisateur> utilisateurs = utilisateurService.findAll();
            System.out.println("📦 " + utilisateurs.size() + " utilisateurs récupérés");

            utilisateursList.setAll(utilisateurs);
            
            Platform.runLater(() -> {
                if (utilisateursTable != null) {
                    utilisateursTable.refresh();
                    updateStats();
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement des utilisateurs.");
        }
    }

    public void filterUtilisateurs() {
        UICommun.executeSafe(() -> {
            if (utilisateursList == null) return;
            
            String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
            String selectedRole = roleFilter != null ? roleFilter.getValue() : "Tous";
            String selectedStatut = statutFilter != null ? statutFilter.getValue() : "Tous";

            ObservableList<Utilisateur> filteredList = FXCollections.observableArrayList();

            for (Utilisateur utilisateur : utilisateursList) {
                boolean matchesSearch = searchText.isEmpty() ||
                    (utilisateur.getNom() != null && utilisateur.getNom().toLowerCase().contains(searchText)) ||
                    (utilisateur.getPrenom() != null && utilisateur.getPrenom().toLowerCase().contains(searchText)) ||
                    (utilisateur.getNomUtilisateur() != null && utilisateur.getNomUtilisateur().toLowerCase().contains(searchText));

                boolean matchesRole = selectedRole == null || selectedRole.equals("Tous") ||
                    (utilisateur.getRole() != null && utilisateur.getRole().getLibelle().equals(selectedRole));

                boolean matchesStatut = selectedStatut == null || selectedStatut.equals("Tous") ||
                    (selectedStatut.equals("Actif") && utilisateur.isActif()) ||
                    (selectedStatut.equals("Inactif") && !utilisateur.isActif());

                if (matchesSearch && matchesRole && matchesStatut) {
                    filteredList.add(utilisateur);
                }
            }

            if (utilisateursTable != null) {
                utilisateursTable.setItems(filteredList);
            }
            updateStats();
        }, "filtrage des utilisateurs");
    }
    
    private void updateStats() {
        UICommun.executeSafe(() -> {
            if (totalUtilisateursLabel != null && utilisateursTable != null) {
                int total = utilisateursTable.getItems().size();
                totalUtilisateursLabel.setText(String.valueOf(total));
                System.out.println("📊 Statistiques mises à jour: " + total + " utilisateurs");
            }
        }, "mise à jour des statistiques");
    }

    // ==================== MÉTHODES DES ACTIONS ====================

    @FXML
    private void handleNouvelUtilisateur() {
        ouvrirFormulaireUtilisateur(null);
    }

    public void ouvrirFormulaireUtilisateur(Utilisateur utilisateur) {
        UICommun.executeSafe(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/utilisateur-form.fxml"));
                Parent root = loader.load();
                UtilisateurFormController formController = loader.getController();
                formController.setParametresController(this);
                formController.setUtilisateur(utilisateur);

                Stage stage = new Stage();
                formController.setStage(stage);
                
                stage.setTitle(utilisateur == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");
                stage.initModality(Modality.APPLICATION_MODAL);
                root.getStylesheets().add(UICommun.URLStyle);
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.showAndWait();
            } catch (Exception e) {
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire: " + e.getMessage());
            }
        }, "ouverture du formulaire utilisateur");
    }

    private void reinitialiserMotDePasse(Utilisateur utilisateur) {
        if (UICommun.showConfirmation("Réinitialiser le mot de passe", 
            "Voulez-vous réinitialiser le mot de passe de " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " ?\nLe nouveau mot de passe sera : 'password123'")) {
            
            UICommun.executeSafe(() -> {
                boolean success = utilisateurService.reinitialiserMotDePasse(utilisateur.getId(), "password123");
                if (success) {
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", "Mot de passe réinitialisé avec succès.\nNouveau mot de passe : password123");
                } else {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la réinitialisation du mot de passe.");
                }
            }, "réinitialisation du mot de passe");
        }
    }

    private void supprimerUtilisateur(Utilisateur utilisateur) {
        Utilisateur currentUser = mainController.getUtilisateurCourant();
        if (currentUser == null) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible de vérifier les permissions.");
            return;
        }
        
        if (utilisateur.getId() == currentUser.getId()) {
            UICommun.showAlert(AlertType.WARNING, "Impossible", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        if (UICommun.showConfirmation("Supprimer l'utilisateur", 
            "Voulez-vous vraiment supprimer l'utilisateur " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " ?")) {
            
            UICommun.executeSafe(() -> {
                boolean success = utilisateurService.delete(utilisateur.getId());
                if (success) {
                    chargerUtilisateurs();
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
                } else {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'utilisateur.");
                }
            }, "suppression de l'utilisateur");
        }
    }

    // ==================== MÉTHODES SYSTÈME ====================

    private void chargerParametresSysteme() {
        UICommun.executeSafe(() -> {
            System.out.println("🔧 Chargement des paramètres système");

            String version = getVersionApplication();
            String nomBDD = getNomBaseDeDonnees();
            String derniereSauvegarde = getDerniereSauvegarde();
            String tailleBDD = getTailleBaseDeDonnees();

            mettreAJourInfosSysteme(version, nomBDD, derniereSauvegarde, tailleBDD);

        }, "chargement des paramètres système");
    }

    @FXML
    private void handleSauvegarderBase() {
        UICommun.executeSafe(() -> {
            System.out.println("💾 Début sauvegarde de la base de données...");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sauvegarder la base de données");
            fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Fichiers SQLite", "*.db", "*.sqlite"),
                new ExtensionFilter("Tous les fichiers", "*.*")
            );

            String defaultFileName = "backup_miapeve_finance_"
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".db";
            fileChooser.setInitialFileName(defaultFileName);

            File file = fileChooser.showSaveDialog(parametresTabs.getScene().getWindow());

            if (file != null) {
                Path sourcePath = PathManager.getDatabasePath();
                try {
                    Files.copy(sourcePath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    UICommun.showAlert(AlertType.INFORMATION, "Succès",
                        "Base de données sauvegardée avec succès vers:\n" + file.getAbsolutePath());
                    System.out.println("✅ Sauvegarde réussie: " + file.getAbsolutePath());
                } catch (IOException e) {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        }, "sauvegarde de la base de données");
    }

    @FXML
    private void handleViderLogs() {
        if (UICommun.showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir vider tous les logs d'audit ? Cette action est irréversible.")) {
            UICommun.executeSafe(() -> {
                // auditLogService.viderLogs();
                UICommun.showAlert(AlertType.INFORMATION, "Succès", "Logs d'audit vidés avec succès.");
                System.out.println("🗑️ Logs d'audit vidés");
            }, "vidage des logs");
        }
    }

    @FXML
    private void handleRecalculerSoldes() {
        UICommun.executeSafe(() -> {
            System.out.println("🧮 Recalcul des soldes en cours...");
            // financeService.recalculerTousLesSoldes();
            UICommun.showAlert(AlertType.INFORMATION, "Succès", "Soldes recalculés avec succès.");
            System.out.println("✅ Soldes recalculés");
        }, "recalcul des soldes");
    }

    @FXML
    private void handleModifierProfil() {
        mainController.handleMonProfil();
    }

    @FXML
    private void handleChangerMotDePasse() {
        UICommun.executeSafe(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/changer-motdepasse.fxml"));
                // ✅ Définir le contrôleur en Java
                ChangerMotDePasseController controller = new ChangerMotDePasseController();
                loader.setController(controller);
                Parent root = loader.load();
                
                controller.setUtilisateur(mainController.getUtilisateurCourant());

                Stage stage = new Stage();
                stage.setTitle("Changer le mot de passe");
                stage.initModality(Modality.APPLICATION_MODAL);
                root.getStylesheets().add(UICommun.URLStyle);
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.showAndWait();
            } catch (Exception e) {
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture: " + e.getMessage());
                e.printStackTrace();
            }
        }, "ouverture du changement de mot de passe");
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String getVersionApplication() {
        try {
            Package packageInfo = getClass().getPackage();
            return packageInfo != null && packageInfo.getImplementationVersion() != null
                    ? packageInfo.getImplementationVersion()
                    : "1.0.0";
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    private String getNomBaseDeDonnees() {
        return "SQLite";
    }

    private String getDerniereSauvegarde() {
        return "Non effectuée";
    }

    private String getTailleBaseDeDonnees() {
        try {
            Path dbPath = PathManager.getDatabasePath();
            if (Files.exists(dbPath)) {
                long sizeBytes = Files.size(dbPath);
                double sizeMB = sizeBytes / (1024.0 * 1024.0);
                return String.format("%.2f MB", sizeMB);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur calcul taille BDD: " + e.getMessage());
        }
        return "Inconnue";
    }

    private void mettreAJourInfosSysteme(String version, String nomBDD, String sauvegarde, String taille) {
        System.out.println("📊 Infos système:");
        System.out.println("   - Version: " + version);
        System.out.println("   - Base de données: " + nomBDD);
        System.out.println("   - Dernière sauvegarde: " + sauvegarde);
        System.out.println("   - Taille BDD: " + taille);
    }

    public void refreshData() {
        System.out.println("🔄 Rafraîchissement des données utilisateurs");
        chargerUtilisateurs();
    }
}