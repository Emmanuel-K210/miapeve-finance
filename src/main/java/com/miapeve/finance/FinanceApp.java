package com.miapeve.finance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.miapeve.finance.config.DatabaseManager;

public class FinanceApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser la base de données
            DatabaseManager.initializeDatabase();
            System.out.println("✅ Base de données initialisée");
            
            // Charger l'interface principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/login.fxml"));
            Parent root = loader.load();
            System.out.println("✅ Main FXML chargé");
            
            Scene scene = new Scene(root, 1300, 820);
            scene.setFill(Color.TRANSPARENT);
            primaryStage.setResizable(false); 
            primaryStage.centerOnScreen();
            primaryStage.initStyle(StageStyle.UNDECORATED);
            // Appliquer le CSS - CORRECTION ICI
            scene.getStylesheets().add(getClass().getResource("/static/css/app.css").toExternalForm());
            Image logo = new Image(getClass().getResourceAsStream("/static/images/logo.png"));
	        primaryStage.getIcons().add(logo);
            System.out.println("✅ CSS chargé");
            
            // Configurer la fenêtre
            primaryStage.setTitle("Miapeve Finance - Gestion des Finances de l'Église");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            System.out.println("✅ Application démarrée avec succès!");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE AU DÉMARRAGE: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}