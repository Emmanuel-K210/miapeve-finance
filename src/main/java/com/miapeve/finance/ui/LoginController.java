package com.miapeve.finance.ui;

import java.io.IOException;

import com.miapeve.finance.service.impl.AuthenticationService;
import com.miapeve.finance.utils.UICommun;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginController {

    @FXML private TextField nomUtilisateurField;
    @FXML private PasswordField motDePasseField;

    // --- Ajouts maquette v2 : bascule affichage du mot de passe ---
    @FXML private TextField motDePasseVisibleField;
    @FXML private Button togglePasswordButton;
    private boolean motDePasseVisible = false;

    private AuthenticationService authService;
    private Stage stage;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        System.out.println("🔐 Initialisation de LoginController");
        nomUtilisateurField.requestFocus();
        setupEnterKeyHandler();
        setupPasswordToggleSync();
    }

    private void setupEnterKeyHandler() {
        motDePasseField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        motDePasseVisibleField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        nomUtilisateurField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                motDePasseField.requestFocus();
            }
        });
    }

    /**
     * Les deux champs (PasswordField masqué + TextField visible) partagent
     * le même texte en permanence ; seule leur visibilité bascule.
     * Aucune logique métier ici, purement présentationnel.
     */
    private void setupPasswordToggleSync() {
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());
    }

    @FXML
    private void handleTogglePassword() {
        motDePasseVisible = !motDePasseVisible;

        motDePasseField.setVisible(!motDePasseVisible);
        motDePasseField.setManaged(!motDePasseVisible);

        motDePasseVisibleField.setVisible(motDePasseVisible);
        motDePasseVisibleField.setManaged(motDePasseVisible);

        togglePasswordButton.setText(motDePasseVisible ? "\uD83D\uDE48" : "\uD83D\uDC41");

        // Rendre le focus au champ actif pour ne pas perdre le curseur
        if (motDePasseVisible) {
            motDePasseVisibleField.requestFocus();
            motDePasseVisibleField.end();
        } else {
            motDePasseField.requestFocus();
            motDePasseField.end();
        }
    }

    @FXML
    private void handleLogin() {
        String nomUtilisateur = nomUtilisateurField.getText().trim();
        String motDePasse = motDePasseField.getText();

        System.out.println("🔐 Tentative de connexion: " + nomUtilisateur);

        if (nomUtilisateur.isEmpty() || motDePasse.isEmpty()) {
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            if (authService.authentifier(nomUtilisateur, motDePasse)) {
                nomUtilisateurField.setDisable(true);
                motDePasseField.setDisable(true);
                motDePasseVisibleField.setDisable(true);

                ouvrirApplicationPrincipale();
            } else {
                UICommun.showAlert(AlertType.ERROR, "Erreur", "Nom d'utilisateur ou mot de passe incorrect");
                motDePasseField.clear();
                motDePasseField.requestFocus();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la connexion");
            reactiverChampsConnexion();
        }
    }

    @FXML
    private void handleQuitter() {
        System.out.println("👋 Fermeture de l'application");
        System.exit(0);
    }

    private void ouvrirApplicationPrincipale() {
        try {
            afficherEcranChargement();

            new Thread(() -> {
                try {
                    Thread.sleep(2000);

                    Platform.runLater(() -> {
                        try {
                            chargerApplicationPrincipale();
                        } catch (Exception e) {
                            System.err.println("❌ Erreur chargement application: " + e.getMessage());
                            e.printStackTrace();
                            fermerEcranChargement();
                            UICommun.showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'application");
                            reactiverChampsConnexion();
                        }
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        fermerEcranChargement();
                        UICommun.showAlert(AlertType.ERROR, "Erreur", "Chargement interrompu");
                        reactiverChampsConnexion();
                    });
                }
            }).start();

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture application principale: " + e.getMessage());
            e.printStackTrace();
            UICommun.showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'application principale");
            reactiverChampsConnexion();
        }
    }

    private void afficherEcranChargement() {
        try {
            System.out.println("🔄 Affichage écran de chargement...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/loading.fxml"));
            Parent loadingRoot = loader.load();

            LoadingController loadingController = loader.getController();
            if (loadingController != null) {
                loadingController.setMessage("Bienvenue Frère,Soeur dans le seigneur et que le seigneur vous benisse");
                loadingController.setSubMessage("Patienter un moment");
            } else {
                System.err.println("❌ LoadingController est null");
            }

            Stage loadingStage = new Stage();
            loadingStage.setTitle("Chargement");
            loadingStage.initStyle(StageStyle.UNDECORATED);

            Scene loadingScene = new Scene(loadingRoot, 400, 300);
            loadingScene.getStylesheets().add(getClass().getResource("/static/css/app.css").toExternalForm());
            loadingStage.setScene(loadingScene);
            loadingStage.setResizable(false);
            loadingStage.centerOnScreen();

            this.stage = loadingStage;

            loadingStage.show();
            System.out.println("✅ Écran de chargement affiché");

        } catch (IOException e) {
            System.err.println("❌ Erreur affichage écran chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fermerEcranChargement() {
        if (stage != null) {
            stage.close();
            System.out.println("✅ Écran de chargement fermé");
        }
    }

    private void chargerApplicationPrincipale() {
        try {
            System.out.println("🚀 Chargement de l'application principale...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/main.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUtilisateurCourant(authService.getUtilisateurCourant());

            Stage mainStage = new Stage();
            mainStage.setTitle("Miapeve Finance - " + authService.getUtilisateurCourant().getNomUtilisateur());

            Scene mainScene = new Scene(root, 1200, 800);
            mainScene.getStylesheets().add(getClass().getResource(UICommun.URLStyle).toExternalForm());
            mainStage.setScene(mainScene);
            mainStage.setMaximized(true);

            fermerEcranChargement();
            fermerFenetreLogin();

            mainStage.show();

            System.out.println("✅ Application principale chargée avec succès");

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement application principale: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur chargement application", e);
        }
    }

    private void fermerFenetreLogin() {
        try {
            if (nomUtilisateurField != null && nomUtilisateurField.getScene() != null) {
                Stage loginStage = (Stage) nomUtilisateurField.getScene().getWindow();
                loginStage.close();
                System.out.println("✅ Fenêtre login fermée");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de fermer la fenêtre login: " + e.getMessage());
        }
    }

    private void reactiverChampsConnexion() {
        Platform.runLater(() -> {
            if (nomUtilisateurField != null) nomUtilisateurField.setDisable(false);
            if (motDePasseField != null) motDePasseField.setDisable(false);
            if (motDePasseVisibleField != null) motDePasseVisibleField.setDisable(false);
        });
    }

}
