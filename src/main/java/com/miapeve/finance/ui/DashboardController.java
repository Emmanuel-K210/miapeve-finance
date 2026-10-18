package com.miapeve.finance.ui;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.service.ProgrammeService;
import com.miapeve.finance.service.impl.FinanceService;
import com.miapeve.finance.service.impl.ProgrammeServiceImpl;
import com.miapeve.finance.utils.UICommun;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController implements Initializable {

    // ==================== FXML COMPONENTS ====================
    
    @FXML 
    private VBox recentProgrammes;
    
    @FXML 
    private HBox chartContainer;
    
    @FXML 
    private HBox pieChartContainer;
    
    @FXML 
    private Label totalSoldeLabel;
    
    @FXML 
    private Label totalDepensesLabel;
    
    @FXML 
    private Label totalSemencesLabel;
    
    @FXML 
    private Label programmesCountLabel;
    
    @FXML
    private Label welcomeSubtitleLabel;
    
    @FXML 
    private HBox comptesMouvementesContainer;
    
    @FXML 
    private VBox comptesMouvementesVide;
    
    @FXML 
    private Label comptesMouvementesCountLabel;

    // ==================== SERVICES ====================
    
    private FinanceService financeService;
    private ProgrammeService programmeService;

    // ==================== GRAPHIQUES ====================
    
    private PieChart depensesPieChart;
    private BarChart<String, Number> activiteBarChart;

    // ==================== RÉFÉRENCE ====================
    
    private MainController mainController;

    // ==================== SETTERS ====================
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        System.out.println("✅ MainController défini dans DashboardController");
    }

    public MainController getMainController() {
        return mainController;
    }

    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        financeService = new FinanceService();
        programmeService = new ProgrammeServiceImpl();
        initializeCharts();
        System.out.println("🔄 DashboardController.initialize() appelé");
    }

    @FXML
    public void loadData() {
        System.out.println("📊 DashboardController.loadData() appelé");
        
        updateWelcomeSubtitle();

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            updateStats();
            loadRecentProgrammes();
            updateCharts();
            loadComptesMouvementes();
        });
        pause.play();
    }

    private void updateWelcomeSubtitle() {
        if (welcomeSubtitleLabel == null) return;
        if (mainController != null && mainController.getUtilisateurCourant() != null) {
            String prenom = mainController.getUtilisateurCourant().getPrenom();
            String nom = mainController.getUtilisateurCourant().getNom();
            String nomComplet = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
            welcomeSubtitleLabel.setText("Bienvenue, " + nomComplet.trim() + " ! Voici un aperçu de votre situation financière.");
        } else {
            welcomeSubtitleLabel.setText("Voici un aperçu de votre situation financière.");
        }
    }

    public void refreshData() {
        loadData();
    }

    // ==================== STATISTIQUES ====================
    
    private void updateStats() {
        try {
            Double soldeTotal = financeService.getSoldeTotal();
            Double depensesTotal = financeService.getTotalDepenses();
            Double semencesTotal = financeService.getTotalSemences();
            int programmesCount = programmeService.listeDesProgrammesMoisEnCours().size();

            totalSoldeLabel.setText(formatMontant(soldeTotal));
            totalDepensesLabel.setText(formatMontant(depensesTotal));
            totalSemencesLabel.setText(formatMontant(semencesTotal));
            programmesCountLabel.setText(String.valueOf(programmesCount));

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour stats: " + e.getMessage());
            e.printStackTrace();
            totalSoldeLabel.setText("0 F CFA");
            totalDepensesLabel.setText("0 F CFA");
            totalSemencesLabel.setText("0 F CFA");
            programmesCountLabel.setText("0");
        }
    }

    private String formatMontant(Double valeur) {
        if (valeur == null) return "0 F CFA";
        return String.format("%,.0f F CFA", valeur);
    }

    // ==================== PROGRAMMES RÉCENTS ====================
    
    private void loadRecentProgrammes() {
        recentProgrammes.getChildren().clear();

        try {
            List<Programme> programmesRecents = programmeService.findProgrammesRecents(3);

            if (programmesRecents == null || programmesRecents.isEmpty()) {
                Label aucunProgramme = new Label("Aucun programme récent");
                aucunProgramme.setStyle("-fx-text-fill: -text-tertiary; -fx-font-style: italic; -fx-padding: 10;");
                recentProgrammes.getChildren().add(aucunProgramme);
            } else {
                for (Programme programme : programmesRecents) {
                    Node card = createModernProgrammeCard(programme);
                    if (card != null) {
                        recentProgrammes.getChildren().add(card);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement programmes récents: " + e.getMessage());
            Label erreurLabel = new Label("Erreur de chargement");
            erreurLabel.setStyle("-fx-text-fill: -danger; -fx-padding: 10;");
            recentProgrammes.getChildren().add(erreurLabel);
        }
    }

    /**
     * Crée une carte de programme récent avec style moderne.
     */
    private Node createModernProgrammeCard(Programme programme) {
        VBox card = new VBox(8);
        card.getStyleClass().add("dash-programme-item");
        
        // En-tête : date + statut
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Date
        Label dateLabel = new Label();
        dateLabel.getStyleClass().add("dash-programme-item-date");
        if (programme.getDateProgramme() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateLabel.setText(programme.getDateProgramme().format(formatter));
        } else {
            dateLabel.setText("Date inconnue");
        }
        
        // Statut
        String statut = getStatutProgramme(programme);
        Label statusBadge = new Label(statut);
        String statusClass = "dash-programme-item-status-" + statut.toLowerCase()
            .replace(" ", "")
            .replace("'", "")
            .replace("ô", "o")
            .replace("é", "e");
        statusBadge.getStyleClass().addAll("dash-programme-item-status", statusClass);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(dateLabel, spacer, statusBadge);
        
        // Titre
        Label titleLabel = new Label(programme.getTypeProgramme() != null ? 
            programme.getTypeProgramme() : "Programme");
        titleLabel.getStyleClass().add("dash-programme-item-title");
        titleLabel.setWrapText(true);
        
        // Footer : actions
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("dash-programme-item-footer");
        
        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("dash-btn-item-outline");
        viewBtn.setOnAction(e -> {
            if (mainController != null) {
                mainController.loadProgrammes();
            }
        });
        
        Button editBtn = new Button("Éditer");
        editBtn.getStyleClass().add("dash-btn-item-primary");
        editBtn.setOnAction(e -> {
            if (mainController != null) {
                mainController.loadProgrammes();
            }
        });
        
        footer.getChildren().addAll(viewBtn, editBtn);
        
        card.getChildren().addAll(header, titleLabel, footer);
        return card;
    }

    /**
     * Retourne le statut d'un programme.
     */
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

    // ==================== COMPTES MOUVEMENTÉS ====================
    
    private void loadComptesMouvementes() {
        comptesMouvementesContainer.getChildren().clear();

        try {
            List<Compte> comptes = financeService.getComptesMouvementes();

            if (comptes == null || comptes.isEmpty()) {
                comptesMouvementesVide.setVisible(true);
                comptesMouvementesVide.setManaged(true);
                comptesMouvementesContainer.setVisible(false);
                comptesMouvementesContainer.setManaged(false);
                comptesMouvementesCountLabel.setText("");
            } else {
                comptesMouvementesVide.setVisible(false);
                comptesMouvementesVide.setManaged(false);
                comptesMouvementesContainer.setVisible(true);
                comptesMouvementesContainer.setManaged(true);
                comptesMouvementesCountLabel.setText(comptes.size() + " compte(s)");

                for (Compte compte : comptes) {
                    Node card = createCompteCard(compte);
                    if (card != null) {
                        comptesMouvementesContainer.getChildren().add(card);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement comptes mouvementés: " + e.getMessage());
            Label errLabel = new Label("Erreur de chargement");
            errLabel.setStyle("-fx-text-fill: -danger; -fx-padding: 10;");
            comptesMouvementesContainer.getChildren().add(errLabel);
        }
    }

    private Node createCompteCard(Compte compte) {
        VBox card = new VBox(8);
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        card.setStyle(
            "-fx-background-color: -bg-surface; " +
            "-fx-background-radius: -radius-sm; " +
            "-fx-border-radius: -radius-sm; " +
            "-fx-border-color: -border-color; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 14; " +
            "-fx-effect: -shadow-sm; " +
            "-fx-cursor: hand;"
        );
        
        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: -bg-surface; " +
                "-fx-background-radius: -radius-sm; " +
                "-fx-border-radius: -radius-sm; " +
                "-fx-border-color: -primary; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 14; " +
                "-fx-effect: -shadow-md; " +
                "-fx-cursor: hand; " +
                "-fx-translate-y: -1px;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: -bg-surface; " +
                "-fx-background-radius: -radius-sm; " +
                "-fx-border-radius: -radius-sm; " +
                "-fx-border-color: -border-color; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 14; " +
                "-fx-effect: -shadow-sm; " +
                "-fx-cursor: hand;"
            );
        });
        
        String type = compte.getTypeCompte() != null ? compte.getTypeCompte().toUpperCase() : "COMPTE";
        Label typeLabel = new Label(type);
        typeLabel.setStyle(
            "-fx-font-size: 10px; " +
            "-fx-font-weight: 700; " +
            "-fx-text-fill: -text-tertiary; " +
            "-fx-letter-spacing: 0.5px;"
        );
        
        String nom = compte.getNomCompte() != null ? compte.getNomCompte() : "—";
        Label nomLabel = new Label(nom);
        nomLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-text-fill: -text-primary;"
        );
        nomLabel.setWrapText(true);
        
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: -border-color;");
        
        double solde = compte.getSolde() != null ? compte.getSolde().doubleValue() : 0.0;
        Label soldeLabel = new Label("Solde");
        soldeLabel.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: -text-secondary;"
        );
        
        Label soldeValue = new Label(String.format("%,.0f F CFA", solde));
        String soldeColor = solde < 0 ? "-danger" : solde > 0 ? "-success" : "-text-tertiary";
        soldeValue.setStyle(
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 700; " +
            "-fx-text-fill: " + soldeColor + ";"
        );
        
        HBox soldeBox = new HBox(6);
        soldeBox.setAlignment(Pos.CENTER_LEFT);
        soldeBox.getChildren().addAll(soldeLabel, soldeValue);
        
        card.getChildren().addAll(typeLabel, nomLabel, sep, soldeBox);
        
        card.setOnMouseClicked(event -> {
            if (mainController != null) {
                mainController.naviguerVersComptes();
            }
        });
        
        return card;
    }

    // ==================== ACTIONS RAPIDES ====================
    
    @FXML
    private void handleNouveauProgramme() {
        System.out.println("📅 Nouveau Programme");
        if (mainController != null) {
            mainController.loadProgrammes();
        }
    }

    @FXML
    private void loadProgrammes() {
        System.out.println("📅 Chargement des programmes...");
        if (mainController != null) {
            mainController.loadProgrammes();
        }
    }

    @FXML
    private void handleNouvelleDepense() {
        System.out.println("💰 Nouvelle Dépense");
        if (mainController != null) {
            mainController.loadNouvelleDepense();
        }
    }

    @FXML
    private void handleNouvelleSemence() {
        System.out.println("🌱 Nouvelle Semence");
        if (mainController != null) {
            mainController.loadNouvelleSemence();
        }
    }

    @FXML
    private void handleRapportMensuel() {
        System.out.println("📈 Rapport mensuel");
        UICommun.showInfo("Rapport mensuel", "Génération du rapport en cours...");
    }

    // ==================== GRAPHIQUES ====================
    
    private void initializeCharts() {
        try {
            // === Pie Chart ===
            depensesPieChart = new PieChart();
            depensesPieChart.getStyleClass().add("dash-pie-chart");
            depensesPieChart.setLabelsVisible(true);
            depensesPieChart.setLegendVisible(true);
            depensesPieChart.setPrefSize(350, 200);
            depensesPieChart.setTitle("Répartition des dépenses");
            depensesPieChart.setLabelLineLength(10);
            depensesPieChart.setStartAngle(90);
            
            // === Bar Chart ===
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Jours");
            xAxis.setStyle("-fx-tick-label-font-size: 10px;");
            
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Montant (F CFA)");
            yAxis.setStyle("-fx-tick-label-font-size: 10px;");
            
            activiteBarChart = new BarChart<>(xAxis, yAxis);
            activiteBarChart.getStyleClass().add("dash-bar-chart");
            activiteBarChart.setLegendVisible(true);
            activiteBarChart.setPrefSize(500, 200);
            activiteBarChart.setAnimated(false);
            activiteBarChart.setTitle("Évolution des flux");
            activiteBarChart.setBarGap(4);
            activiteBarChart.setCategoryGap(8);
            
            // Charger le CSS
            try {
                String cssPath = getClass().getResource(UICommun.URLStyle).toExternalForm();
                if (cssPath != null) {
                    activiteBarChart.getStylesheets().add(cssPath);
                    depensesPieChart.getStylesheets().add(cssPath);
                }
            } catch (Exception e) {
                System.err.println("⚠️ CSS des graphiques non chargé: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation graphiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCharts() {
        try {
            updatePieChart();
            updateBarChart();
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour graphiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePieChart() {
        try {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            
            var depenses = financeService.getDepenseMonthCurrent();
            boolean hasData = false;
            
            if (depenses != null && !depenses.isEmpty()) {
                for (var dp : depenses) {
                    double montant = dp.getMontant() != null ? dp.getMontant().doubleValue() : 0.0;
                    if (montant > 0) {
                        PieChart.Data data = new PieChart.Data(
                            dp.getLibelle() + " (" + String.format("%.0f", montant) + " F CFA)", 
                            montant
                        );
                        pieData.add(data);
                        hasData = true;
                    }
                }
            }
            
            if (!hasData) {
                // ✅ Ajouter une donnée vide sans essayer de styliser le node
                PieChart.Data noData = new PieChart.Data("Aucune dépense", 1);
                pieData.add(noData);
            }
            
            depensesPieChart.setData(pieData);
            
            // ✅ Appliquer le style uniquement si on a des données
            if (hasData && !pieData.isEmpty()) {
                // Styliser les segments avec un délai
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(100));
                pause.setOnFinished(e -> {
                    for (PieChart.Data data : pieData) {
                        Node node = data.getNode();
                        if (node != null) {
                            node.setStyle("-fx-border-color: -bg-surface; -fx-border-width: 2;");
                        }
                    }
                });
                pause.play();
            }
            
            if (pieChartContainer != null) {
                pieChartContainer.getChildren().clear();
                pieChartContainer.getChildren().add(depensesPieChart);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBarChart() {
        try {
            activiteBarChart.getData().clear();

            Map<LocalDate, Double> montantsParJour = new TreeMap<>();
            var transactions = financeService.getTransactionParMoisEnCours();

            if (transactions != null) {
                transactions.forEach(transaction -> {
                    if (transaction.getDateCreation() != null) {
                        LocalDate date = transaction.getDateCreation().toLocalDate();
                        double montant = transaction.getMontant() != null ? transaction.getMontant().doubleValue() : 0.0;
                        montantsParJour.merge(date, montant, Double::sum);
                    }
                });
            }

            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenus");
            
            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Dépenses");

            if (montantsParJour.isEmpty()) {
                // Aucune donnée
                XYChart.Data<String, Number> emptyData = new XYChart.Data<>("Aucune donnée", 0);
                revenueSeries.getData().add(emptyData);
                activiteBarChart.getData().add(revenueSeries);
            } else {
                montantsParJour.forEach((date, montant) -> {
                    String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH) 
                                 + " " + date.getDayOfMonth();
                    
                    if (montant >= 0) {
                        revenueSeries.getData().add(new XYChart.Data<>(label, montant));
                        expenseSeries.getData().add(new XYChart.Data<>(label, 0));
                    } else {
                        expenseSeries.getData().add(new XYChart.Data<>(label, Math.abs(montant)));
                        revenueSeries.getData().add(new XYChart.Data<>(label, 0));
                    }
                });

                if (!revenueSeries.getData().isEmpty()) {
                    activiteBarChart.getData().add(revenueSeries);
                }
                if (!expenseSeries.getData().isEmpty()) {
                    activiteBarChart.getData().add(expenseSeries);
                }
            }

            if (chartContainer != null) {
                chartContainer.getChildren().clear();
                chartContainer.getChildren().add(activiteBarChart);
            }

            Platform.runLater(() -> {
                customizeBarColors();
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour bar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Personnalise les couleurs des barres du graphique.
     */
    private void customizeBarColors() {
        try {
            if (activiteBarChart.getData().isEmpty()) {
                return;
            }

            for (XYChart.Series<String, Number> series : activiteBarChart.getData()) {
                String styleClass = series.getName().equals("Revenus") 
                    ? "-fx-bar-fill: -primary; -fx-background-radius: 3 3 0 0;" 
                    : "-fx-bar-fill: -success; -fx-background-radius: 3 3 0 0;";
                
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(50));
                        pause.setOnFinished(e -> {
                            node.setStyle(styleClass);
                            node.setOnMouseEntered(me -> {
                                String hoverStyle = series.getName().equals("Revenus") 
                                    ? "-fx-bar-fill: -primary-hover; -fx-effect: dropshadow(gaussian, rgba(68, 87, 217, 0.3), 8, 0, 0, 2);"
                                    : "-fx-bar-fill: -success-dark; -fx-effect: dropshadow(gaussian, rgba(22, 163, 74, 0.3), 8, 0, 0, 2);";
                                node.setStyle(hoverStyle);
                            });
                            node.setOnMouseExited(me -> {
                                node.setStyle(styleClass);
                            });
                        });
                        pause.play();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur personnalisation couleurs barres: " + e.getMessage());
            e.printStackTrace();
        }
    }
}