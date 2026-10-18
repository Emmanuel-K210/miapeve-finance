package com.miapeve.finance.ui;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.utils.UICommun;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainController implements Initializable {

	// ==================== COMPOSANTS FXML ====================

	@FXML
	private BorderPane mainContainer;

	@FXML
	private VBox sidebar;

	@FXML
	private StackPane contentArea;

	@FXML
	private Label appTitle;

	// === Boutons de navigation (pour la gestion de l'état actif) ===
	@FXML
	private Button btnDashboard;

	@FXML
	private Button btnProgrammes;

	@FXML
	private Button btnComptes;

	@FXML
	private Button btnTransactions;

	@FXML
	private Button btnParametres;

	@FXML
	private Button btnImpression;

	// === Bloc profil (toolbar) ===
	@FXML
	private Label userInitialsLabel;

	@FXML
	private Label userNameLabel;

	@FXML
	private Label userRoleLabel;

	@FXML
	private Label notificationBadge;

	// Bouton actif courant
	private Button currentActiveButton;

	// ==================== CONTROLEURS ====================

	private DashboardController dashboardController;
	private ProgrammeController programmeController;
	private CompteController compteController;
	private TransactionController transactionController;
	private DepenseController depenseController;
	private SemenceController semenceController;
	private ParametresController parametresController;
	private ImpressionController impressionController;

	// ==================== VUES ====================

	private Node dashboardView;
	private Node programmeView;
	private Node compteView;
	private Node transactionView;
	private Node depenseView;
	private Node semenceView;
	private Node parametresView;
	private Node impressionView;

	// ==================== ÉTAT ====================

	private String currentView;
	private Utilisateur utilisateurCourant;

	// ==================== OBSERVERS ====================

	private List<CompteChangeListener> compteChangeListeners = new ArrayList<>();

	// ==================== CONSTRUCTEUR ====================

	public MainController() {
		// Initialisation par défaut
	}

	// ==================== GETTERS ====================

	public Node getProgrammeView() {
		return this.programmeView;
	}

	public String getCurrentView() {
		return currentView;
	}

	public Utilisateur getUtilisateurCourant() {
		return this.utilisateurCourant;
	}

	public DashboardController getDashboardController() {
		return dashboardController;
	}

	public ProgrammeController getProgrammeController() {
		return programmeController;
	}

	public CompteController getCompteController() {
		return compteController;
	}

	public TransactionController getTransactionController() {
		return transactionController;
	}

	public DepenseController getDepenseController() {
		return depenseController;
	}

	public SemenceController getSemenceController() {
		return semenceController;
	}

	public ParametresController getParametresController() {
		return parametresController;
	}

	// Ajouter cette méthode pour gérer la sauvegarde depuis le formulaire
	public void sauvegarderProgramme(ProgrammeFormController formController) {
		// La sauvegarde est déjà gérée dans ProgrammeController
		// Cette méthode permet juste de revenir à la liste
		loadProgrammes();
	}

	public ImpressionController getImpressionController() {
		return impressionController;
	}

	// ==================== SETTERS ====================

	public void setUtilisateurCourant(Utilisateur utilisateur) {
		this.utilisateurCourant = utilisateur;
		mettreAJourInterfaceUtilisateur();
	}

	// ==================== GESTION DES OBSERVERS ====================

	public void addCompteChangeListener(CompteChangeListener listener) {
		if (!compteChangeListeners.contains(listener)) {
			compteChangeListeners.add(listener);
			System.out.println("👂 Ajout d'un listener comptes: " + listener.getClass().getSimpleName());
		}
	}

	public void removeCompteChangeListener(CompteChangeListener listener) {
		compteChangeListeners.remove(listener);
		System.out.println("👂 Suppression d'un listener comptes: " + listener.getClass().getSimpleName());
	}

	public void notifyComptesChanged() {
		System.out.println("🔄 Notification changement comptes à " + compteChangeListeners.size() + " listeners");

		for (CompteChangeListener listener : compteChangeListeners) {
			try {
				Platform.runLater(() -> {
					try {
						listener.onComptesChanged();
					} catch (SQLException e) {
						System.err.println("❌ Erreur dans listener: " + e.getMessage());
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				System.err.println("❌ Erreur notification listener: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// Rafraîchir aussi le dashboard
		if (dashboardController != null) {
			Platform.runLater(() -> {
				dashboardController.loadData();
			});
		}
	}

	public void refreshAllControllers() {
		System.out.println("🔄 Rafraîchissement de tous les contrôleurs");
		notifyComptesChanged();
	}

	// ==================== INTERFACE UTILISATEUR ====================

	private void mettreAJourInterfaceUtilisateur() {
		if (utilisateurCourant != null) {
			String role = utilisateurCourant.getRole().getLibelle();
			System.out
					.println("👤 Utilisateur connecté: " + utilisateurCourant.getNomUtilisateur() + " (" + role + ")");

			if (appTitle != null) {
				appTitle.setText("Bienvenue, " + utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
			}

			String prenom = utilisateurCourant.getPrenom() != null ? utilisateurCourant.getPrenom() : "";
			String nom = utilisateurCourant.getNom() != null ? utilisateurCourant.getNom() : "";

			if (userNameLabel != null) {
				userNameLabel.setText((prenom + " " + nom).trim());
			}
			if (userRoleLabel != null) {
				userRoleLabel.setText(role);
			}
			if (userInitialsLabel != null) {
				String initiales = ((prenom.isEmpty() ? "" : prenom.substring(0, 1))
						+ (nom.isEmpty() ? "" : nom.substring(0, 1))).toUpperCase();
				userInitialsLabel.setText(initiales.isEmpty() ? "?" : initiales);
			}
		}
	}

	// ==================== GESTION DE L'ÉTAT ACTIF ====================

	/**
	 * Met à jour le bouton de navigation actif.
	 * 
	 * @param activeButton Le bouton à marquer comme actif
	 */
	private void setActiveButton(Button activeButton) {
		// Désactiver le précédent
		if (currentActiveButton != null) {
			currentActiveButton.getStyleClass().remove("active");
		}

		// Activer le nouveau
		if (activeButton != null) {
			activeButton.getStyleClass().add("active");
			currentActiveButton = activeButton;
		}
	}

	// ==================== NAVIGATION PRINCIPALE ====================

	@FXML
	private void loadDashboard() {
		System.out.println("📊 Chargement du dashboard...");
		if (dashboardView != null && dashboardController != null) {
			setActiveButton(btnDashboard);
			switchContent(dashboardView, "Tableau de Bord");
			dashboardController.loadData();
		} else {
			showErrorContent("Dashboard non disponible");
		}
	}

	@FXML
	void loadProgrammes() {
		System.out.println("📅 Chargement des programmes...");
		if (programmeView != null && programmeController != null) {
			setActiveButton(btnProgrammes);
			switchContent(programmeView, "Gestion des Programmes");
			programmeController.loadData();
		} else {
			showErrorContent("Module Programmes non disponible");
		}
	}

	@FXML
	private void loadComptes() {
		System.out.println("💰 Chargement des comptes...");
		if (compteView != null && compteController != null) {
			setActiveButton(btnComptes);
			switchContent(compteView, "Gestion des Comptes");
			try {
				compteController.loadData();
			} catch (SQLException e) {
				System.err.println("❌ Erreur chargement comptes: " + e.getMessage());
				e.printStackTrace();
				UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les comptes: " + e.getMessage());
			}
		} else {
			showErrorContent("Module Comptes non disponible");
		}
	}

	@FXML
	private void loadTransactions() {
		System.out.println("💸 Chargement des transactions...");
		if (transactionView != null && transactionController != null) {
			setActiveButton(btnTransactions);
			switchContent(transactionView, "Transactions");
			transactionController.loadData();
		} else {
			showErrorContent("Module Transactions non disponible");
		}
	}

	@FXML
	private void loadParametres() {
		System.out.println("⚙️  Chargement des paramètres...");
		if (parametresView != null && parametresController != null) {
			setActiveButton(btnParametres);
			switchContent(parametresView, "Paramètres");
			parametresController.loadData();
		} else {
			showErrorContent("Module Paramètres non disponible");
		}
	}

	@FXML
	private void loadImpression() {
		System.out.println("🖨️  Chargement impression...");
		if (impressionView != null && impressionController != null) {
			setActiveButton(btnImpression);
			switchContent(impressionView, "Impression des Rapports");
		} else {
			showErrorContent("Module Impression non disponible");
		}
	}

	// ==================== NAVIGATION VERS LES FORMULAIRES ====================

	public void naviguerVersComptes() {
		if (compteView != null && compteController != null) {
			setActiveButton(btnComptes);
			switchContent(compteView, "Gestion des Comptes");
			compteController.refreshData();
		} else {
			showErrorContent("Module Comptes non disponible");
		}
	}

	public void loadNouvelleDepense() {
		System.out.println("💰 Chargement du formulaire de dépense...");
		if (depenseView != null) {
			switchContent(depenseView, "Nouvelle Dépense");
			if (depenseController != null) {
				depenseController.onShow();
			}
		} else {
			showErrorContent("Formulaire de dépense non disponible");
		}
	}

	public void loadNouvelleSemence() {
		System.out.println("🌱 Chargement du formulaire de semence...");
		if (semenceView != null) {
			switchContent(semenceView, "Nouvelle Semence");
			if (semenceController != null) {
				semenceController.onShow();
			}
		} else {
			showErrorContent("Formulaire de semence non disponible");
		}
	}

	// ==================== MENU ACTIONS ====================

	@FXML
	private void handleDeconnexion() {
		try {
			System.out.println("🚪 Déconnexion demandée");
			Stage stage = (Stage) mainContainer.getScene().getWindow();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/login.fxml"));
			Parent root = loader.load();
			root.getStylesheets().add(UICommun.URLStyle);
			Scene scene = new Scene(root, 1300, 820);
			stage.setScene(scene);
			stage.centerOnScreen();
			System.out.println("✅ Déconnexion réussie");
		} catch (Exception e) {
			System.err.println("❌ Erreur lors de la déconnexion: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	void handleMonProfil() {
		System.out.println("👤 Ouverture du profil");
		ouvrirProfilUtilisateur();
	}

	@FXML
	private void handleAPropos() {
		System.out.println("ℹ️ À propos");
		UICommun.showAlert(AlertType.INFORMATION, "À propos", "Miapeve Finance v2.0.0\n\n"
				+ "Gestion financière intelligente\n" + "© 2026 Miapeve Finance - Tous droits réservés");
	}

	@FXML
	private void handleDocumentation() {
		System.out.println("📚 Documentation");
		// TODO: Ouvrir la documentation ou un lien
		UICommun.showAlert(AlertType.INFORMATION, "Documentation", "La documentation sera disponible prochainement.");
	}

	// ==================== PROFIL UTILISATEUR ====================

	private void ouvrirProfilUtilisateur() {
		try {
			System.out.println("👤 Ouverture du profil utilisateur...");
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/profile.fxml"));
			Parent root = loader.load();

			ProfilController profilController = loader.getController();
			profilController.setUtilisateur(utilisateurCourant);
			profilController.setMainController(this);

			Stage stage = new Stage();
			profilController.setStage(stage);

			stage.setTitle("Mon Profil");
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initStyle(StageStyle.UNDECORATED);
			root.getStylesheets().add(UICommun.URLStyle);
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.centerOnScreen();
			stage.showAndWait();

		} catch (Exception e) {
			System.err.println("❌ Erreur ouverture profil: " + e.getMessage());
			e.printStackTrace();
			UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil.");
		}
	}

	// ==================== CHANGEMENT DE CONTENU ====================

	/**
	 * Change le contenu de la zone principale avec une animation de fondu.
	 * 
	 * @param content Le nouveau contenu à afficher
	 * @param title   Le titre à afficher dans la barre d'outils
	 */
	public void switchContent(Node content, String title) {
		if (content == null) {
			System.err.println("❌ Contenu null pour: " + title);
			showErrorContent("Contenu non disponible: " + title);
			return;
		}

		appTitle.setText(title);
		currentView = title;
		System.out.println("🔄 Changement de vue: " + title);

		// Animation de transition
		FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setOnFinished(e -> {
			contentArea.getChildren().clear();
			contentArea.getChildren().add(content);

			FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentArea);
			fadeIn.setFromValue(0.0);
			fadeIn.setToValue(1.0);
			fadeIn.play();
		});
		fadeOut.play();
	}

	/**
	 * Affiche un message d'erreur dans la zone de contenu.
	 * 
	 * @param message Le message d'erreur
	 */
	public void showErrorContent(String message) {
		Label errorLabel = new Label(message);
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-padding: 20;");
		contentArea.getChildren().clear();
		contentArea.getChildren().add(errorLabel);
		System.err.println("❌ " + message);
	}

	// ==================== INITIALISATION ====================

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeUI();
	}

	/**
	 * Initialise toutes les vues et les contrôleurs. Cette méthode est appelée une
	 * fois au démarrage.
	 */
	private void initializeUI() {
		try {
			System.out.println("📁 Chargement des vues...");

			// 1. Initialiser les contrôleurs de formulaire
			System.out.println("➡️  Initialisation des contrôleurs de formulaire...");
			this.depenseController = new DepenseController(this);
			this.semenceController = new SemenceController(this);
			this.depenseView = depenseController.getView();
			this.semenceView = semenceController.getView();

			// Enregistrer les contrôleurs comme observers
			addCompteChangeListener(depenseController);
			addCompteChangeListener(semenceController);
			System.out.println("✅ Contrôleurs de formulaire initialisés et enregistrés comme observers");

			// 2. Charger Dashboard
			System.out.println("➡️  Chargement dashboard...");
			FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/template/fxml/dashboard.fxml"));
			this.dashboardView = dashboardLoader.load();
			this.dashboardController = dashboardLoader.getController();
			this.dashboardController.setMainController(this);
			System.out.println("✅ Dashboard chargé");

			// 3. Charger Programmes
			System.out.println("➡️  Chargement programmes...");
			FXMLLoader programmeLoader = new FXMLLoader(getClass().getResource("/template/fxml/programmes.fxml"));
			this.programmeView = programmeLoader.load();
			this.programmeController = programmeLoader.getController();
			this.programmeController.setMainController(this);
			System.out.println("✅ Programmes chargé");

			// 4. Charger Comptes
			System.out.println("➡️  Chargement comptes...");
			FXMLLoader compteLoader = new FXMLLoader(getClass().getResource("/template/fxml/comptes.fxml"));
			this.compteView = compteLoader.load();
			this.compteController = compteLoader.getController();
			this.compteController.setMainController(this);
			addCompteChangeListener(compteController);
			System.out.println("✅ Comptes chargé et enregistré comme observer");

			// 5. Charger Transactions
			System.out.println("➡️  Chargement transactions...");
			FXMLLoader transactionLoader = new FXMLLoader(getClass().getResource("/template/fxml/transactions.fxml"));
			this.transactionView = transactionLoader.load();
			this.transactionController = transactionLoader.getController();
			this.transactionController.setMainController(this);
			System.out.println("✅ Transactions chargé");

			// 6. Charger Paramètres
			System.out.println("➡️  Chargement paramètres...");
			FXMLLoader parametresLoader = new FXMLLoader(getClass().getResource("/template/fxml/parametres.fxml"));
			this.parametresView = parametresLoader.load();
			this.parametresController = parametresLoader.getController();
			this.parametresController.setMainController(this);
			System.out.println("✅ Paramètres chargé");

			// 7. Charger Impression
			System.out.println("➡️  Chargement impression...");
			FXMLLoader impressionLoader = new FXMLLoader(getClass().getResource("/template/fxml/impression.fxml"));
			this.impressionView = impressionLoader.load();
			this.impressionController = impressionLoader.getController();
			this.impressionController.setMainController(this);
			System.out.println("✅ Impression chargé");

			System.out.println("🎉 Toutes les vues sont chargées!");

			// Charger le dashboard par défaut (active automatiquement btnDashboard)
			loadDashboard();

		} catch (Exception e) {
			System.err.println("❌ Erreur lors du chargement des vues: " + e.getMessage());
			e.printStackTrace();
			showErrorContent("Erreur de chargement: " + e.getMessage());
		}
	}

	// Dans MainController.java

	/**
	 * Affiche l'écran de chargement
	 */
	public void showLoadingScreen(String message, String subMessage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/loading.fxml"));
			Parent loadingRoot = loader.load();
			LoadingController loadingController = loader.getController();

			if (message != null) {
				loadingController.setMessage(message);
			}
			if (subMessage != null) {
				loadingController.setSubMessage(subMessage);
			}

			Stage loadingStage = new Stage();
			loadingStage.initStyle(StageStyle.UNDECORATED);
			loadingStage.initModality(Modality.APPLICATION_MODAL);
			loadingStage.setScene(new Scene(loadingRoot));
			loadingStage.setResizable(false);
			loadingStage.centerOnScreen();

			// Stocker la référence
			this.loadingStage = loadingStage;
			this.loadingController = loadingController;

			loadingStage.show();

		} catch (Exception e) {
			System.err.println("❌ Erreur affichage écran de chargement: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Met à jour la progression de l'écran de chargement
	 */
	public void updateLoadingProgress(double progress) {
		if (loadingController != null) {
			loadingController.updateProgress(progress);
		}
	}

	/**
	 * Met à jour le message de l'écran de chargement
	 */
	public void updateLoadingMessage(String message, String subMessage) {
		if (loadingController != null) {
			if (message != null) {
				loadingController.setMessage(message);
			}
			if (subMessage != null) {
				loadingController.setSubMessage(subMessage);
			}
		}
	}

	/**
	 * Ferme l'écran de chargement
	 */
	public void closeLoadingScreen() {
		if (loadingStage != null) {
			loadingStage.close();
			loadingStage = null;
			loadingController = null;
		}
	}

	// Ajouter les attributs
	private Stage loadingStage;
	private LoadingController loadingController;

	/**
	 * Rafraîchit les données des paramètres si l'onglet est ouvert
	 */
	public void refreshParametres() {
		if (parametresController != null) {
			parametresController.refreshData();
			System.out.println("🔄 Paramètres rafraîchis");
		}
	}

	/**
	 * Met à jour les informations utilisateur dans la sidebar
	 */
	public void updateUserInfo() {
		if (utilisateurCourant != null) {
			// Mettre à jour le nom dans la sidebar
			if (userNameLabel != null) {
				userNameLabel.setText(utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
			}
			// Mettre à jour le rôle
			if (userRoleLabel != null) {
				userRoleLabel
						.setText(utilisateurCourant.getRole() != null ? utilisateurCourant.getRole().getLibelle() : "");
			}
			// Mettre à jour les initiales
			if (userInitialsLabel != null) {
				String prenom = utilisateurCourant.getPrenom() != null ? utilisateurCourant.getPrenom() : "";
				String nom = utilisateurCourant.getNom() != null ? utilisateurCourant.getNom() : "";
				String initiales = "";
				if (!prenom.isEmpty()) {
					initiales += prenom.charAt(0);
				}
				if (!nom.isEmpty()) {
					initiales += nom.charAt(0);
				}
				userInitialsLabel.setText(initiales.toUpperCase());
			}
		}
	}

}