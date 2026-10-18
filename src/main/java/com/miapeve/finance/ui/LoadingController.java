package com.miapeve.finance.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

public class LoadingController implements Initializable {
    
    @FXML
    private ProgressIndicator loadingSpinner;
    
    @FXML
    private ProgressBar loadingProgress;
    
    @FXML
    private Label loadingText;
    
    @FXML
    private Label loadingSubtext;
    
    @FXML
    private Label loadingTitle;
    
    @FXML
    private Label loadingPercent;
    
    private Timeline progressTimeline;
    private double currentProgress = 0.0;
    private boolean isIndeterminate = true;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 LoadingController initialisé");
        loadingSpinner.setProgress(-1.0); // Mode indéterminé
    }
    
    /**
     * Définit le message principal
     */
    public void setMessage(String message) {
        if (loadingText != null) {
            Platform.runLater(() -> loadingText.setText(message));
        }
    }
    
    /**
     * Définit le message secondaire
     */
    public void setSubMessage(String subMessage) {
        if (loadingSubtext != null) {
            Platform.runLater(() -> loadingSubtext.setText(subMessage));
        }
    }
    
    /**
     * Définit le titre
     */
    public void setTitle(String title) {
        if (loadingTitle != null) {
            Platform.runLater(() -> loadingTitle.setText(title));
        }
    }
    
    /**
     * Passe en mode progression déterminée
     */
    public void setDeterminate() {
        isIndeterminate = false;
        Platform.runLater(() -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            loadingProgress.setVisible(true);
            loadingProgress.setManaged(true);
            loadingPercent.setVisible(true);
            loadingPercent.setManaged(true);
        });
    }
    
    /**
     * Met à jour la progression (0.0 à 1.0)
     */
    public void updateProgress(double progress) {
        if (!isIndeterminate) {
            currentProgress = Math.min(1.0, Math.max(0.0, progress));
            Platform.runLater(() -> {
                loadingProgress.setProgress(currentProgress);
                loadingPercent.setText(String.format("%.0f%%", currentProgress * 100));
            });
        }
    }
    
    /**
     * Simule une progression avec animation
     */
    public void startSimulatedProgress() {
        setDeterminate();
        setMessage("Chargement en cours...");
        
        if (progressTimeline != null) {
            progressTimeline.stop();
        }
        
        progressTimeline = new Timeline(
            new KeyFrame(Duration.millis(50), e -> {
                currentProgress += 0.01;
                if (currentProgress >= 1.0) {
                    currentProgress = 1.0;
                    progressTimeline.stop();
                }
                updateProgress(currentProgress);
            })
        );
        progressTimeline.setCycleCount(Timeline.INDEFINITE);
        progressTimeline.play();
    }
    
    /**
     * Arrête la simulation de progression
     */
    public void stopSimulatedProgress() {
        if (progressTimeline != null) {
            progressTimeline.stop();
            progressTimeline = null;
        }
        updateProgress(1.0);
    }
    
    /**
     * Passe en mode indéterminé (spinner)
     */
    public void setIndeterminate() {
        isIndeterminate = true;
        Platform.runLater(() -> {
            loadingSpinner.setVisible(true);
            loadingSpinner.setManaged(true);
            loadingSpinner.setProgress(-1.0);
            loadingProgress.setVisible(false);
            loadingProgress.setManaged(false);
            loadingPercent.setVisible(false);
            loadingPercent.setManaged(false);
        });
    }
    
    /**
     * Définit le texte du pourcentage
     */
    public void setPercentText(String text) {
        if (loadingPercent != null) {
            Platform.runLater(() -> loadingPercent.setText(text));
        }
    }
    
    /**
     * Ferme l'écran de chargement
     */
    public void close() {
        if (progressTimeline != null) {
            progressTimeline.stop();
            progressTimeline = null;
        }
        // La fermeture est gérée par l'appelant
    }
}