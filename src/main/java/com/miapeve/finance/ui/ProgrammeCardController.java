package com.miapeve.finance.ui;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProgrammeCardController implements Initializable {
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML 
    private Label dateLabel;
    
    @FXML 
    private Label statutLabel;
    
    @FXML 
    private Label titreLabel;
    
    @FXML 
    private Label heureLabel;
    
    @FXML 
    private Label utilisateurLabel;
    
    @FXML 
    private Label participantsLabel;
    
    @FXML 
    private Button btnVoir;
    
    @FXML 
    private Button btnEditer;
    
    @FXML 
    private VBox cardContainer;
    
    // ==================== ÉTAT ====================
    
    private Programme programme;
    private MainController mainController;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupCardHoverEffect();
    }
    
    private void setupEventHandlers() {
        btnVoir.setOnAction(e -> handleVoir());
        btnEditer.setOnAction(e -> handleEditer());
    }
    
    private void setupCardHoverEffect() {
        cardContainer.setOnMouseEntered(e -> {
            cardContainer.getStyleClass().add("programme-card-hover");
        });
        
        cardContainer.setOnMouseExited(e -> {
            cardContainer.getStyleClass().remove("programme-card-hover");
        });
    }
    
    // ==================== SETTERS ====================
    
    public void setProgramme(Programme programme) {
        this.programme = programme;
        updateUI();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    // ==================== MISE À JOUR UI ====================
    
    private void updateUI() {
        if (programme == null) {
            return;
        }
        
        // === Date formatée ===
        if (programme.getDateProgramme() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateLabel.setText(programme.getDateProgramme().format(formatter));
        } else {
            dateLabel.setText("Date non définie");
        }
        
        // === Titre ===
        String type = programme.getTypeProgramme();
        titreLabel.setText(type != null && !type.isEmpty() ? type : "Programme");
        
        // === Heure ===
        String heureDebut = programme.getHeureDebut() != null ? programme.getHeureDebut().toString() : "--:--";
        String heureFin = programme.getHeureFin() != null ? programme.getHeureFin().toString() : "--:--";
        heureLabel.setText(heureDebut + " - " + heureFin);
        
        // === Utilisateur ===
        Utilisateur user = programme.getUtilisateur();
        if (user != null) {
            String prenom = user.getPrenom() != null ? user.getPrenom() : "";
            String nom = user.getNom() != null ? user.getNom() : "";
            utilisateurLabel.setText((prenom + " " + nom).trim());
        } else {
            utilisateurLabel.setText("Sans responsable");
        }
        
        // === Participants ===
        int total = 0;
        total += programme.getHommes() != null ? programme.getHommes() : 0;
        total += programme.getFemmes() != null ? programme.getFemmes() : 0;
        total += programme.getEnfants() != null ? programme.getEnfants() : 0;
        total += programme.getJeunes() != null ? programme.getJeunes() : 0;
        participantsLabel.setText(total + " participants");
        
        // === Statut ===
        updateStatut();
    }
    
    private void updateStatut() {
        // Déterminer le statut en fonction de la date
        String statut;
        String styleClass;
        
        if (programme.getDateProgramme() != null) {
            LocalDate today = LocalDate.now();
            LocalDate dateProg = programme.getDateProgramme();
            
            if (dateProg.isBefore(today)) {
                statut = "Terminé";
                styleClass = "badge-info";
            } else if (dateProg.equals(today)) {
                statut = "Aujourd'hui";
                styleClass = "badge-warning";
            } else {
                statut = "Planifié";
                styleClass = "badge-success";
            }
        } else {
            statut = "Planifié";
            styleClass = "badge-success";
        }
        
        statutLabel.setText(statut);
        statutLabel.getStyleClass().add(styleClass);
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void handleVoir() {
        System.out.println("👁️ Voir programme: " + programme.getTypeProgramme());
        
        if (mainController != null) {
            // Naviguer vers la vue des programmes
            mainController.loadProgrammes();
        }
    }
    
    @FXML
    private void handleEditer() {
        System.out.println("✏️ Éditer programme: " + programme.getTypeProgramme());
        
        // TODO: Ouvrir le formulaire d'édition du programme
        // Si vous avez un formulaire d'édition, l'appeler ici
    }
    
    // ==================== UTILITAIRES ====================
    
    public Node getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/template/fxml/programme-card.fxml"));
            return loader.load();
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement programme-card: " + e.getMessage());
            e.printStackTrace();
            return createFallbackView();
        }
    }
    
    private Node createFallbackView() {
        VBox fallback = new VBox(8);
        fallback.setStyle("-fx-background-color: -bg-surface; -fx-padding: 14; -fx-background-radius: -radius-sm; -fx-effect: -shadow-sm;");
        fallback.getChildren().add(new Label("⚠️ Erreur de chargement"));
        return fallback;
    }
}