package com.miapeve.finance.ui;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.dao.DepenseDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Depense;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

public class DepenseController implements CompteChangeListener {

	@FXML
	private TextField montantField;
	@FXML
	private TextField libelleField;
	@FXML
	private ComboBox<Compte> compteCombo;
	@FXML
	private Button validerButton;
	@FXML
	private Button annulerButton;
	@FXML
	private Label messageLabel;

	private DepenseDAO depenseDAO;
	private CompteDAO compteDAO;
	private Parent view;
	private final MainController mainController;

	public DepenseController(MainController mainController) {
		this.depenseDAO = new DepenseDAO();
		this.compteDAO = new CompteDAO();
		this.mainController = mainController;
		
		// S'enregistrer comme observer
		if (mainController != null) {
			mainController.addCompteChangeListener(this);
		}
	}

	@FXML
	public void initialize() {
		System.out.println("💰 Initialisation de DepenseController");
		
		try {
			refreshComptes();
			setupEventHandlers();
		} catch (SQLException e) {
			showError("Erreur lors du chargement des comptes: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Implémentation de l'interface CompteChangeListener
	@Override
	public void onComptesChanged() throws SQLException {
		System.out.println("🔄 DepenseController: notification de changement de comptes");
		refreshComptes();
	}
	
	// Méthode publique pour rafraîchir les comptes
	public void refreshComptes() throws SQLException {
		if (compteCombo != null) {
			List<Compte> comptes = compteDAO.findAll();
			Compte compteSelectionne = compteCombo.getValue(); // Sauvegarder la sélection
			
			compteCombo.setItems(FXCollections.observableArrayList(comptes));
			
			// Restaurer la sélection si elle existe encore dans la nouvelle liste
			if (compteSelectionne != null) {
				compteCombo.getItems().stream()
					.filter(c -> c.getCompteId().equals(compteSelectionne.getCompteId()))
					.findFirst()
					.ifPresent(c -> compteCombo.setValue(c));
			}
			
			// Configurer l'affichage
			compteCombo.setCellFactory(param -> new ListCell<Compte>() {
				@Override
				protected void updateItem(Compte item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText("");
					} else {
						setText(String.format("%s (%,.0f FCFA)", 
							item.getTypeCompte(), 
							item.getSolde().doubleValue()));
					}
				}
			});
			
			compteCombo.setButtonCell(new ListCell<Compte>() {
				@Override
				protected void updateItem(Compte item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText("Sélectionner un compte");
					} else {
						setText(String.format("%s (%,.0f FCFA)", 
							item.getTypeCompte(), 
							item.getSolde().doubleValue()));
					}
				}
			});
			
			System.out.println("🔄 Comptes rafraîchis dans DepenseController: " + comptes.size() + " comptes");
		}
	}

	private void setupEventHandlers() {
		if (validerButton != null) {
			validerButton.setOnAction(e -> {
				try {
					validerDepense();
				} catch (SQLException e1) {
					showError("Erreur lors de l'enregistrement: " + e1.getMessage());
					e1.printStackTrace();
				}
			});
		}
		
		if (annulerButton != null) {
			annulerButton.setOnAction(e -> annuler());
		}

		// Validation en temps réel
		if (montantField != null) {
			montantField.textProperty().addListener((obs, oldVal, newVal) -> {
				if (!newVal.matches("\\d*(\\.\\d*)?")) {
					montantField.setText(oldVal);
				}
			});
		}
	}

	@FXML
	private void validerDepense() throws SQLException {
		System.out.println("✅ Validation dépense demandée");
		
		try {
			if (validateForm()) {
				Depense depense = new Depense();
				depense.setMontant(new BigDecimal(montantField.getText()));
				depense.setLibelle(libelleField.getText());
				depense.setCompte(compteCombo.getValue());
				depense.setCreePar(mainController.getUtilisateurCourant().getId());
				
				// Enregistrer la dépense
				depenseDAO.save(depense);
				
				// Notifier tous les observers du changement de comptes
				if (mainController != null) {
					mainController.notifyComptesChanged();
				}
				
				showSuccess("Dépense enregistrée avec succès !");
				clearForm();
			}
		} catch (Exception e) {
			showError("Erreur: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void annuler() {
		System.out.println("❌ Annulation dépense");
		clearForm();
	}

	private boolean validateForm() {
		// Vérifier montant
		if (montantField.getText().isEmpty()) {
			showError("Veuillez saisir le montant");
			return false;
		}

		// Vérifier libellé
		if (libelleField.getText().isEmpty()) {
			showError("Veuillez saisir le libellé");
			return false;
		}

		// Vérifier compte
		if (compteCombo.getValue() == null) {
			showError("Veuillez sélectionner un compte");
			return false;
		}

		// Vérifier format montant
		try {
			double montant = Double.parseDouble(montantField.getText());
			if (montant <= 0) {
				showError("Le montant doit être positif");
				return false;
			}
		} catch (NumberFormatException e) {
			showError("Le montant doit être un nombre valide");
			return false;
		}

		// Vérifier solde du compte
		try {
			double montant = Double.parseDouble(montantField.getText());
			Compte compte = compteCombo.getValue();
			if (montant > compte.getSolde().doubleValue()) {
				showError("Solde insuffisant sur le compte " + compte.getTypeCompte());
				return false;
			}
		} catch (NumberFormatException e) {
			showError("Erreur de format du montant");
			return false;
		}

		return true;
	}

	private void clearForm() {
		if (montantField != null) montantField.clear();
		if (libelleField != null) libelleField.clear();
		if (compteCombo != null) compteCombo.setValue(null);
		if (messageLabel != null) messageLabel.setText("");
	}

	private void showSuccess(String message) {
		if (messageLabel != null) {
			messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
			messageLabel.setText(message);
		}
		System.out.println("✅ " + message);
	}

	private void showError(String message) {
		if (messageLabel != null) {
			messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
			messageLabel.setText(message);
		}
		System.err.println("❌ " + message);
	}
	
	// Méthode appelée quand le formulaire devient visible
	public void onShow() {
		try {
			refreshComptes();
			System.out.println("👁️  Formulaire Depense affiché - données rafraîchies");
		} catch (SQLException e) {
			showError("Erreur lors du rafraîchissement des comptes: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Parent getView() {
		if (view == null) {
			try {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("/template/fxml/depense-form.fxml"));
				loader.setController(this);
				view = loader.load();
				System.out.println("✅ Vue dépense chargée avec succès");
			} catch (IOException e) {
				System.err.println("❌ Erreur chargement depense-form.fxml: " + e.getMessage());
				throw new RuntimeException("Erreur lors du chargement de depense-form.fxml", e);
			}
		}
		return view;
	}
}