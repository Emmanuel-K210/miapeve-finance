package com.miapeve.finance.ui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.service.UtilisateurService;
import com.miapeve.finance.utils.UICommun;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UtilisateurController implements Initializable {

	@FXML private TableView<Utilisateur> utilisateursTable;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colNomUtilisateur;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Boolean> colActif;
    @FXML private TableColumn<Utilisateur, Void> colActions;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private Button btnNouvelUtilisateur;
    @FXML private Label totalUtilisateursLabel;
    
    private UtilisateurService utilisateurService;
    private ObservableList<Utilisateur> utilisateursList;
    private MainController mainController;

    public UtilisateurController() {
        this.utilisateursList = FXCollections.observableArrayList();
    }

    public void setUtilisateurService(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
        System.out.println("✅ UtilisateurService injecté");
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setNouvelUtilisateurButton(Button button) {
        this.btnNouvelUtilisateur = button;
    }
    
    public ObservableList<Utilisateur> getUtilisateursList() {
        if (utilisateursList == null) {
            utilisateursList = FXCollections.observableArrayList();
        }
        return utilisateursList;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 Initialisation de UtilisateurController");
        
        if (utilisateursList == null) {
            utilisateursList = FXCollections.observableArrayList();
        }
        
        // CORRECTION : Configurer les colonnes FXML
        configureTableColumns();
        initializeFilters();
        setupEventHandlers();
        
        System.out.println("✅ UtilisateurController initialisé");
    }

    private void configureTableColumns() {
        System.out.println("🎯 Configuration des colonnes FXML...");
        
        try {
            // CORRECTION : Colonne Nom
            if (colNom != null) {
                colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
                System.out.println("✅ Colonne Nom configurée - Factory: " + (colNom.getCellValueFactory() != null));
            } else {
                System.err.println("❌ Colonne colNom est null");
            }
            
            // CORRECTION : Colonne Prénom
            if (colPrenom != null) {
                colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
                System.out.println("✅ Colonne Prénom configurée - Factory: " + (colPrenom.getCellValueFactory() != null));
            } else {
                System.err.println("❌ Colonne colPrenom est null");
            }
            
            // CORRECTION : Colonne Nom d'utilisateur
            if (colNomUtilisateur != null) {
                colNomUtilisateur.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
                System.out.println("✅ Colonne Nom Utilisateur configurée - Factory: " + (colNomUtilisateur.getCellValueFactory() != null));
            } else {
                System.err.println("❌ Colonne colNomUtilisateur est null");
            }
            
            // CORRECTION : Colonne Rôle
            if (colRole != null) {
                colRole.setCellValueFactory(cellData -> {
                    Utilisateur utilisateur = cellData.getValue();
                    if (utilisateur != null && utilisateur.getRole() != null) {
                        return new SimpleStringProperty(utilisateur.getRole().getLibelle());
                    }
                    return new SimpleStringProperty("N/A");
                });
                System.out.println("✅ Colonne Rôle configurée - Factory: " + (colRole.getCellValueFactory() != null));
            } else {
                System.err.println("❌ Colonne colRole est null");
            }
            
            // CORRECTION : Colonne Statut
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
                                "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : 
                                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                });
                System.out.println("✅ Colonne Statut configurée - Factory: " + (colActif.getCellValueFactory() != null));
            } else {
                System.err.println("❌ Colonne colActif est null");
            }

            // CORRECTION : Colonne Actions
            if (colActions != null) {
                colActions.setCellFactory(param -> new TableCell<Utilisateur, Void>() {
                    private final Button editBtn = new Button("✏️");
                    private final Button deleteBtn = new Button("🗑️");
                    private final Button resetBtn = new Button("🔑");
                    private final HBox buttons = new HBox(5, editBtn, resetBtn, deleteBtn);

                    {
                        // Style des boutons
                        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-min-width: 30; -fx-max-height: 25;");
                        resetBtn.setStyle("-fx-background-color: #f39c12; -fx-text-follow: white; -fx-min-width: 30; -fx-max-height: 25;");
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-min-width: 30; -fx-max-height: 25;");

                        // Actions
                        editBtn.setOnAction(e -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            if (utilisateur != null) {
                                ouvrirFormulaireUtilisateur(utilisateur);
                            }
                        });

                        resetBtn.setOnAction(e -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            if (utilisateur != null) {
                                reinitialiserMotDePasse(utilisateur);
                            }
                        });

                        deleteBtn.setOnAction(e -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            if (utilisateur != null) {
                                supprimerUtilisateur(utilisateur);
                            }
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
                });
                System.out.println("✅ Colonne Actions configurée");
            } else {
                System.err.println("❌ Colonne colActions est null");
            }

            // CORRECTION : Lier la table à la liste
            if (utilisateursTable != null) {
                utilisateursTable.setItems(getUtilisateursList());
                System.out.println("✅ Table liée à utilisateursList");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur configuration colonnes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeFilters() {
        System.out.println("🎛️ Initialisation des filtres...");
        
        try {
            if (roleFilter != null) {
                roleFilter.getItems().addAll("Tous", "ADMIN", "PASTEUR", "COMPTABLE", "MEMBRE");
                roleFilter.setValue("Tous");
            }
            
            if (statutFilter != null) {
                statutFilter.getItems().addAll("Tous", "Actif", "Inactif");
                statutFilter.setValue("Tous");
            }
            
            System.out.println("✅ Filtres initialisés");
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation filtres: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        System.out.println("🎮 Configuration des gestionnaires d'événements...");
        
        try {
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
            
            System.out.println("✅ Gestionnaires d'événements configurés");
        } catch (Exception e) {
            System.err.println("❌ Erreur configuration gestionnaires d'événements: " + e.getMessage());
        }
    }

    public void loadData() {
        System.out.println("📥 ========== CHARGEMENT UTILISATEURS ==========");
        
        try {
            if (utilisateurService == null) {
                System.err.println("❌ Service utilisateur non disponible");
                return;
            }
        
            // Charger les données
            List<Utilisateur> utilisateurs = utilisateurService.findAll();
            System.out.println("📦 " + utilisateurs.size() + " utilisateurs récupérés");

            // Mettre à jour la liste observable
            getUtilisateursList().setAll(utilisateurs);
            
            // CORRECTION : Forcer la mise à jour dans le thread JavaFX
            Platform.runLater(() -> {
                if (utilisateursTable != null) {
                    utilisateursTable.refresh();
                    System.out.println("✅ Table rafraîchie dans JavaFX Thread");
                    debugTableState();
                }
            });
            
            updateStats();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

   

   
    public void filterUtilisateurs() {
        try {
            if (utilisateursList == null) {
                System.err.println("❌ utilisateursList est null dans filterUtilisateurs");
                return;
            }
            
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
            
        } catch (Exception e) {
            System.err.println("❌ Erreur filtrage utilisateurs: " + e.getMessage());
        }
    }
    
    private void updateStats() {
        try {
            if (totalUtilisateursLabel != null && utilisateursTable != null) {
                int total = utilisateursTable.getItems().size();
                totalUtilisateursLabel.setText(String.valueOf(total));
                System.out.println("📊 Statistiques mises à jour: " + total + " utilisateurs");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour statistiques: " + e.getMessage());
        }
    }
    
    public void setTableView(TableView<Utilisateur> tableView) {
        this.utilisateursTable = tableView;
        System.out.println("✅ TableView injecté: " + (tableView != null));
    }
    
    public void setSearchField(TextField searchField) {
        this.searchField = searchField;
        System.out.println("✅ SearchField injecté: " + (searchField != null));
    }
    
    public void setRoleFilter(ComboBox<String> roleFilter) {
        this.roleFilter = roleFilter;
        System.out.println("✅ RoleFilter injecté: " + (roleFilter != null));
    }
    
    public void setStatutFilter(ComboBox<String> statutFilter) {
        this.statutFilter = statutFilter;
        System.out.println("✅ StatutFilter injecté: " + (statutFilter != null));
    }
    
    public void setTotalUtilisateursLabel(Label totalUtilisateursLabel) {
        this.totalUtilisateursLabel = totalUtilisateursLabel;
        System.out.println("✅ TotalUtilisateursLabel injecté: " + (totalUtilisateursLabel != null));
    }

    public void ouvrirFormulaireUtilisateur(Utilisateur utilisateur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/utilisateur-form.fxml"));
            Parent root = loader.load();
            UtilisateurFormController formController = loader.getController();
            formController.setUtilisateurController(this);
            formController.setUtilisateur(utilisateur);

            Stage stage = new Stage();
            formController.setStage(stage);
            
            stage.setTitle(utilisateur == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);
            root.getStylesheets().add("/static/css/styles.css");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture formulaire utilisateur: " + e.getMessage());
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    private void reinitialiserMotDePasse(Utilisateur utilisateur) {
        if (UICommun.showConfirmation("Réinitialiser le mot de passe", 
            "Voulez-vous réinitialiser le mot de passe de " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " ?\nLe nouveau mot de passe sera : 'password123'")) {
            try {
                boolean success = utilisateurService.reinitialiserMotDePasse(utilisateur.getId(), "password123");
                if (success) {
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", "Mot de passe réinitialisé avec succès.\nNouveau mot de passe : password123");
                } else {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la réinitialisation du mot de passe.");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur réinitialisation mot de passe: " + e.getMessage());
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    private void supprimerUtilisateur(Utilisateur utilisateur) {
        if (mainController == null || mainController.getUtilisateurCourant() == null) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible de vérifier les permissions.");
            return;
        }
        
        Utilisateur currentUser = mainController.getUtilisateurCourant();
        if (utilisateur.getId() == currentUser.getId()) {
            UICommun.showAlert(AlertType.WARNING, "Impossible", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        if (UICommun.showConfirmation("Supprimer l'utilisateur", 
            "Voulez-vous vraiment supprimer l'utilisateur " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " ?")) {
            try {
                boolean success = utilisateurService.delete(utilisateur.getId());
                if (success) {
                    loadData();
                    UICommun.showAlert(AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
                } else {
                    UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'utilisateur.");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur suppression utilisateur: " + e.getMessage());
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    private void debugTableState() {
        System.out.println("🐛 DEBUG TABLE STATE:");
        System.out.println("   - Table: " + (utilisateursTable != null));
        System.out.println("   - Items dans table: " + (utilisateursTable != null && utilisateursTable.getItems() != null ? utilisateursTable.getItems().size() : "null"));
        System.out.println("   - Liste observable: " + (utilisateursList != null ? utilisateursList.size() : "null"));
        
        if (utilisateursTable != null) {
            System.out.println("   - Nombre de colonnes: " + utilisateursTable.getColumns().size());
            for (TableColumn<Utilisateur, ?> col : utilisateursTable.getColumns()) {
                System.out.println("     - " + col.getText() + ": " + 
                    (col.getCellValueFactory() != null ? "FACTORY OK" : "FACTORY NULL"));
            }
        }
    }
    
    public void refreshData() {
        System.out.println("🔄 Rafraîchissement des données utilisateurs");
        loadData();
    }
}