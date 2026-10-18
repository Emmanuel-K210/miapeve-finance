package com.miapeve.finance.service.impl;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.miapeve.finance.dao.TransactionDAO;
import com.miapeve.finance.entites.Depense;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.entites.Transaction;
import com.miapeve.finance.enums.TypeCompte;
import com.miapeve.finance.enums.TypeTransaction;
import com.miapeve.finance.service.ImpressionService;
import com.miapeve.finance.utils.DateConverter;

public class ImpressionServiceImpl implements ImpressionService {

    private final FinanceService financeService;
    private final TransactionDAO transactionDAO;

    private static final String FCFA = "F CFA";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_LONG = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH);

    public ImpressionServiceImpl() {
        financeService = new FinanceService();
        transactionDAO = new TransactionDAO();
    }

    @Override
    public void printProgramme(Programme programme, String parentDir) {
        String fileName = parentDir + "\\PROGRAMME_" + 
                programme.getTypeProgramme().replace(" ", "_") + "_" + 
                programme.getDateProgramme().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";
        
        System.out.println("📄 Génération du PDF: " + fileName);
        
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            // Configuration du document
            pdfDoc.setDefaultPageSize(PageSize.A4);
            document.setMargins(50, 50, 50, 50);
            
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.setFont(font);
            
            // ========== EN-TÊTE ==========
            Paragraph header = new Paragraph("MIAPEVE")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(header);
            
            // ========== TITRE DU PROGRAMME ==========
            Paragraph titreProgramme = new Paragraph(programme.getTypeProgramme())
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(titreProgramme);
            
            // ========== INFORMATIONS DU PROGRAMME ==========
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(80))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginBottom(20);
            
            // Date du programme
            infoTable.addCell(createCell("Date:", true).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell(
                programme.getDateProgramme().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)), 
                false
            ).setBorder(Border.NO_BORDER));
            
            // Heure
            infoTable.addCell(createCell("Heure:", true).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell(
                programme.getHeureDebut() != null ? programme.getHeureDebut().toString()+"-"+programme.getHeureFin().toString() : "Non spécifiée", 
                false
            ).setBorder(Border.NO_BORDER));
            
            // Prédicateur
            infoTable.addCell(createCell("Utilisateur Responsable:", true).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell(
                programme.getUtilisateur() != null ? programme.getUtilisateur().getPrenom() + " " + programme.getUtilisateur().getNom() : " ", 
                false
            ).setBorder(Border.NO_BORDER));
            
            document.add(infoTable);
            
            Table semencesByPrograme = new Table(UnitValue.createPercentArray(new float[] {30,30,40}))
            		.setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            String[] tableSemencesHeaderName= {"Type","Montant","Compte"};
            
            var semences = financeService.getSemencesByProgramme(programme.getId());
            for(String semenceName : tableSemencesHeaderName) {
            	semencesByPrograme.addHeaderCell(createHeaderCell(semenceName));
            }
            
            semences.forEach(semence->{
            	semencesByPrograme.addCell(createCell(semence.getTypeSemence().name(), false));
            	semencesByPrograme.addCell(createCell(semence.getMontant().toString(), false));
            	semencesByPrograme.addCell(createCell(semence.getCompte().getTypeCompte(), false));
            }
            );
            document.add(semencesByPrograme);
            
            Table denombrementTable = new Table(UnitValue.createPercentArray(new float[]{25f,25f,25f,25f}))
            		.setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            		
            String[] denombrements = {"Hommes","Femmes","Jeunes","Enfants"};
            
            for(String personnes : denombrements) {
            	denombrementTable.addHeaderCell(createHeaderCell(personnes));
            }
            
            denombrementTable.addCell(createCell(programme.getHommes().toString(), false));
            denombrementTable.addCell(createCell(programme.getFemmes().toString(), false));
            denombrementTable.addCell(createCell(programme.getJeunes().toString(), false));
            denombrementTable.addCell(createCell(programme.getEnfants().toString(), false));            
            document.add(denombrementTable);
            // ========== PIED DE PAGE ==========
            Paragraph footer = new Paragraph("Document généré le: " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(font)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30)
                    .setFontColor(DeviceGray.GRAY);
            document.add(footer);
            
            System.out.println("✅ Programme exporté en PDF: " + fileName);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF du programme: " + e.getMessage(), e);
        }
    }

    
    @Override
    public void printGestionFinancierMensuel(LocalDate debut, LocalDate fin, String parentDir) {
        String fileName = parentDir + String.format("\\GESTION_FINANCIERE_%s_%s.pdf",
                debut.format(DateTimeFormatter.ofPattern("MMyyyy")),
                fin.format(DateTimeFormatter.ofPattern("MMyyyy")));
        
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);
            
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.setFont(font);
            
            // En-tête
            Paragraph title = new Paragraph("RAPPORT DE GESTION FINANCIÈRE MENSUELLE")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);
            
            Paragraph periode = new Paragraph(String.format("Période: %s au %s",
                    debut.format(DATE_FORMATTER),
                    fin.format(DATE_FORMATTER)))
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(periode);
            
            // Synthèse financière
            Paragraph syntheseTitle = new Paragraph("SYNTHÈSE FINANCIÈRE")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginBottom(15);
            document.add(syntheseTitle);
            
            // Calcul des totaux
            double totalSemences = financeService.getTotalSemencesPeriode(debut, fin);
            double totalDepenses = financeService.getTotalDepensesPeriode(debut, fin);
            double soldePrincipal = financeService.getSoldeCompte(TypeCompte.PRINCIPAL.name());
            double soldeDimes = financeService.getSoldeCompte(TypeCompte.DIMES.name());
            double soldeTotal = soldePrincipal + soldeDimes;
            
            Table syntheseTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            syntheseTable.addCell(createCell("Total des semences:", true));
            syntheseTable.addCell(createCell(String.format("%,.0f %s", totalSemences, FCFA), false));
            
            syntheseTable.addCell(createCell("Total des dépenses:", true));
            syntheseTable.addCell(createCell(String.format("%,.0f %s", totalDepenses, FCFA), false));
            
            syntheseTable.addCell(createCell("Solde (Entrées - Sorties):", true));
            syntheseTable.addCell(createCell(String.format("%,.0f %s", (totalSemences - totalDepenses), FCFA), false));
            
            syntheseTable.addCell(createCell("Solde compte principal:", true));
            syntheseTable.addCell(createCell(String.format("%,.0f %s", soldePrincipal, FCFA), false));
            
            syntheseTable.addCell(createCell("Solde compte dîmes:", true));
            syntheseTable.addCell(createCell(String.format("%,.0f %s", soldeDimes, FCFA), false));
            
            syntheseTable.addCell(createCell("SOLDE TOTAL:", true));
            syntheseTable.addCell(createCell(String.format("%,.0f %s", soldeTotal, FCFA), true));
            
            document.add(syntheseTable);
            
            // Détail des semences
            Paragraph semencesTitle = new Paragraph("DÉTAIL DES SEMENCES")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginBottom(10);
            document.add(semencesTitle);
            
            List<Semence> semences = financeService.getSemenceByPeriode(debut, fin);
            
            // CORRECTION : Créer un tableau pour les semences (variable non utilisée)
            if (semences != null && !semences.isEmpty()) {
                Table semencesTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginBottom(20);
                
                semencesTable.addHeaderCell(createHeaderCell("DATE"));
                semencesTable.addHeaderCell(createHeaderCell("MONTANT"));
                
                for (Semence semence : semences) {
                    semencesTable.addCell(createCell(
                            semence.getDateCreation().format(DATE_FORMATTER), false));
                    semencesTable.addCell(createCell(
                            String.format("%,.0f %s", semence.getMontant().doubleValue(), FCFA), false));
                }
                
                document.add(semencesTable);
            }
            
            // Détail des dépenses
            Paragraph depensesTitle = new Paragraph("DÉTAIL DES DÉPENSES")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginBottom(10);
            document.add(depensesTitle);
            
            List<Depense> depenses = financeService.getdepenseByMonth(debut, fin);
            
            // CORRECTION : Cette variable n'était pas utilisée dans le tableau de dépenses
            Map<String, Double> depensesParType = new HashMap<>();
            if (depenses != null && !depenses.isEmpty()) {
                depensesParType = depenses.stream()
                        .collect(Collectors.groupingBy(
                                Depense::getLibelle,
                                Collectors.summingDouble(d -> d.getMontant().doubleValue())));
            }
            
            Table depensesTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            depensesTable.addHeaderCell(createHeaderCell("CATÉGORIE DE DÉPENSE"));
            depensesTable.addHeaderCell(createHeaderCell("MONTANT"));
            
            // CORRECTION : Utiliser la variable depensesParType qui était déclarée mais non utilisée
            if (!depensesParType.isEmpty()) {
                depensesParType.forEach((categorie, montant) -> {
                    depensesTable.addCell(createCell(categorie, false));
                    depensesTable.addCell(createCell(String.format("%,.0f %s", montant, FCFA), false));
                });
            }
            
            depensesTable.addCell(createCell("TOTAL DÉPENSES", true));
            depensesTable.addCell(createCell(String.format("%,.0f %s", totalDepenses, FCFA), true));
            
            document.add(depensesTable);
            
            // Analyse et commentaires
            Paragraph analyseTitle = new Paragraph("ANALYSE ET COMMENTAIRES")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginBottom(10);
            document.add(analyseTitle);
            
            double ratioDepensesSemences = totalSemences > 0 ? (totalDepenses / totalSemences) * 100 : 0;
            
            // CORRECTION : Variable déclarée mais calcul inutilisé dans l'analyse
            double tauxEpargne = totalSemences > 0 ? ((totalSemences - totalDepenses) / totalSemences) * 100 : 0;
            
            Paragraph analyse = new Paragraph(String.format(
                    "Taux d'épargne: %.1f%%\n" +
                    "Ratio dépenses/semences: %.1f%%\n" +
                    "Solde net: %,.0f %s",
                    tauxEpargne, // CORRECTION : Utiliser la variable calculée
                    ratioDepensesSemences,
                    (totalSemences - totalDepenses),
                    FCFA))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(20);
            document.add(analyse);
            
            // Date de génération
            Paragraph dateGen = new Paragraph(String.format("Rapport généré le: %s",
                    LocalDate.now().format(DATE_FORMATTER_LONG)))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(30);
            document.add(dateGen);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du rapport mensuel: " + e.getMessage(), e);
        }
    }

    @Override
    public void printComptabiliteMensuel(LocalDate debut, LocalDate fin,String parentDir) {
        String fileName = parentDir + String.format("\\COMPTABILITE_MENSUELLE_%s.pdf",
                debut.format(DateTimeFormatter.ofPattern("MMyyyy")));
        
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            pdfDoc.setDefaultPageSize(PageSize.A4);
            document.setMargins(40, 40, 40, 40);
            
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.setFont(font);
            
            // En-tête
            Paragraph title = new Paragraph("JOURNAL DE COMPTABILITÉ MENSUELLE")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);
            
            String mois = DateConverter.convertToMoisFrench(debut);
            Paragraph moisPara = new Paragraph(String.format("Mois de %s %d", mois, debut.getYear()))
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(moisPara);
            
            // Section Compte Principal
            Paragraph principalTitle = new Paragraph("COMPTE PRINCIPAL")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(principalTitle);
            
            double cumulPrincipal = financeService.getMontantByCompte(TypeCompte.PRINCIPAL);
            double depensesPrincipal = financeService.getTotalDepensesByCompte(TypeCompte.PRINCIPAL.name(), debut, fin);
            double balancePrincipal = cumulPrincipal - depensesPrincipal;
            
            Table principalTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            principalTable.addCell(createCell("Cumul du compte principal:", true));
            principalTable.addCell(createCell(String.format("%,.0f %s", cumulPrincipal, FCFA), false));
            
            principalTable.addCell(createCell("Dépenses du mois:", true));
            principalTable.addCell(createCell(String.format("%,.0f %s", depensesPrincipal, FCFA), false));
            
            principalTable.addCell(createCell("Balance du compte:", true));
            principalTable.addCell(createCell(String.format("%,.0f %s", balancePrincipal, FCFA), true));
            
            document.add(principalTable);
            
            // Section Compte des Dîmes
            Paragraph dimesTitle = new Paragraph("COMPTE DES DÎMES")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(dimesTitle);
            
            double cumulDimes = financeService.getMontantByCompte(TypeCompte.DIMES);
            double depensesDimes = financeService.getTotalDepensesByCompte(TypeCompte.DIMES.name(), debut, fin);
            double balanceDimes = cumulDimes - depensesDimes;
            
            Table dimesTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            dimesTable.addCell(createCell("Cumul du compte des dîmes:", true));
            dimesTable.addCell(createCell(String.format("%,.0f %s", cumulDimes, FCFA), false));
            
            dimesTable.addCell(createCell("Dépenses du compte dîmes:", true));
            dimesTable.addCell(createCell(String.format("%,.0f %s", depensesDimes, FCFA), false));
            
            dimesTable.addCell(createCell("Balance du compte dîmes:", true));
            dimesTable.addCell(createCell(String.format("%,.0f %s", balanceDimes, FCFA), true));
            
            document.add(dimesTable);
            
            // Section Récapitulatif
            Paragraph recapTitle = new Paragraph("RÉCAPITULATIF MENSUEL")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(recapTitle);
            
            Table recapTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            recapTable.addCell(createCell("Total des semences du mois:", true));
            recapTable.addCell(createCell(String.format("%,.0f %s", 
                    financeService.getTotalSemencesPeriode(debut, fin), FCFA), false));
            
            recapTable.addCell(createCell("Total des dépenses du mois:", true));
            recapTable.addCell(createCell(String.format("%,.0f %s", 
                    financeService.getTotalDepensesPeriode(debut, fin), FCFA), false));
            
            recapTable.addCell(createCell("Solde net du mois:", true));
            recapTable.addCell(createCell(String.format("%,.0f %s", 
                    financeService.getTotalSemencesPeriode(debut, fin) - 
                    financeService.getTotalDepensesPeriode(debut, fin), FCFA), false));
            
            recapTable.addCell(createCell("Cumul total des comptes:", true));
            recapTable.addCell(createCell(String.format("%,.0f %s", 
                    cumulPrincipal + cumulDimes, FCFA), true));
            
            document.add(recapTable);
            
            // Grand Livre (Transactions journalières)
            Paragraph grandLivreTitle = new Paragraph("GRAND LIVRE DES TRANSACTIONS")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(grandLivreTitle);
            
            List<Transaction> transactions = transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay());
            
            Table grandLivreTable = new Table(UnitValue.createPercentArray(new float[]{15, 30, 25, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            grandLivreTable.addHeaderCell(createHeaderCell("DATE"));
            grandLivreTable.addHeaderCell(createHeaderCell("LIBELLÉ"));
            grandLivreTable.addHeaderCell(createHeaderCell("TYPE"));
            grandLivreTable.addHeaderCell(createHeaderCell("MONTANT"));
            
            for (Transaction t : transactions) {
                grandLivreTable.addCell(createCell(t.getDateCreation().format(DATE_FORMATTER), false));
                grandLivreTable.addCell(createCell(t.getDescription(), false));
                grandLivreTable.addCell(createCell(t.getTypeTransaction().toString(), false));
                grandLivreTable.addCell(createCell(String.format("%,.0f %s", 
                        t.getMontant().doubleValue(), FCFA), false));
            }
            
            document.add(grandLivreTable);
            
            // Signature
            Paragraph signature = new Paragraph("Le Comptable")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(30);
            document.add(signature);
            
            Paragraph ligneSig = new Paragraph("__________________________")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(ligneSig);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération de la comptabilité mensuelle: " + e.getMessage(), e);
        }
    }

    @Override
    public void printTransactionMensuel(LocalDate debut, LocalDate fin,String parentDir) {
        String fileName = parentDir + String.format("\\TRANSACTIONS_MENSUELLES_%s_%s.pdf",
                debut.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                fin.format(DateTimeFormatter.ofPattern("ddMMyyyy")));
        
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20);
            
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.setFont(font);
            
            // En-tête
            Paragraph title = new Paragraph("RELEVÉ DES TRANSACTIONS MENSUELLES")
                    .setFont(fontBold)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);
            
            Paragraph periode = new Paragraph(String.format("Du %s au %s",
                    debut.format(DATE_FORMATTER),
                    fin.format(DATE_FORMATTER)))
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(periode);
            
            // Tableau des transactions
            List<Transaction> transactions = transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay());
            System.err.println("LIST DE TRANSACTION MESSUELLE :"+transactions.size());
            
            Table transactionsTable = new Table(UnitValue.createPercentArray(new float[]{15, 15, 20, 15, 15, 20}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            transactionsTable.addHeaderCell(createHeaderCell("N°"));
            transactionsTable.addHeaderCell(createHeaderCell("DATE"));
            transactionsTable.addHeaderCell(createHeaderCell("LIBELLÉ"));
            transactionsTable.addHeaderCell(createHeaderCell("TYPE"));
            transactionsTable.addHeaderCell(createHeaderCell("ENTRÉE"));
            transactionsTable.addHeaderCell(createHeaderCell("SORTIE"));
            
            int numero = 1;
            double totalEntrees = 0;
            double totalSorties = 0;
            
            for (Transaction t : transactions) {
                transactionsTable.addCell(createCell(String.valueOf(numero++), false));
                transactionsTable.addCell(createCell(t.getDateCreation().format(DATE_FORMATTER), false));
                transactionsTable.addCell(createCell(t.getDescription(), false));
                transactionsTable.addCell(createCell(t.getTypeTransaction().toString(), false));
                
                if (t.getTypeTransaction() == TypeTransaction.CREDIT) {
                    transactionsTable.addCell(createCell(String.format("%,.0f", t.getMontant().doubleValue()), false));
                    transactionsTable.addCell(createCell("-", false));
                    totalEntrees += t.getMontant().doubleValue();
                } else {
                    transactionsTable.addCell(createCell("-", false));
                    transactionsTable.addCell(createCell(String.format("%,.0f", t.getMontant().doubleValue()), false));
                    totalSorties += t.getMontant().doubleValue();
                }
            }
            
            document.add(transactionsTable);
            
            // Totaux
            Table totauxTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            totauxTable.addCell(createCell("TOTAL DES ENTREES:", true));
            totauxTable.addCell(createCell(String.format("%,.0f %s", totalEntrees, FCFA), true));
            
            totauxTable.addCell(createCell("TOTAL DES SORTIES:", true));
            totauxTable.addCell(createCell(String.format("%,.0f %s", totalSorties, FCFA), true));
            
            totauxTable.addCell(createCell("SOLDE NET:", true));
            totauxTable.addCell(createCell(String.format("%,.0f %s", (totalEntrees - totalSorties), FCFA), true));
            
            document.add(totauxTable);
            
            // Statistiques
            Paragraph statsTitle = new Paragraph("STATISTIQUES")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(statsTitle);
            
            long nbTransactions = transactions.size();
            long nbSemences = transactions.stream().filter(t -> t.getTypeTransaction() == TypeTransaction.CREDIT).count();
            long nbDepenses = transactions.stream().filter(t -> t.getTypeTransaction() == TypeTransaction.DEBIT).count();
            
            double moyenneEntree = nbSemences > 0 ? totalEntrees / nbSemences : 0;
            double moyenneSortie = nbDepenses > 0 ? totalSorties / nbDepenses : 0;
            
            Paragraph stats = new Paragraph(String.format(
                    "Nombre total de transactions: %d\n" +
                    "Nombre de semences: %d\n" +
                    "Nombre de dépenses: %d\n" +
                    "Moyenne par entrée: %,.0f %s\n" +
                    "Moyenne par sortie: %,.0f %s",
                    nbTransactions, nbSemences, nbDepenses,
                    moyenneEntree, FCFA, moyenneSortie, FCFA))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(20);
            document.add(stats);
            
            // Date d'édition
            Paragraph dateEdition = new Paragraph(String.format("Édité le: %s",
                    LocalDate.now().format(DATE_FORMATTER)))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(dateEdition);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du relevé de transactions: " + e.getMessage(), e);
        }
    }
    
    // Méthodes utilitaires
    private Cell createCell(String text, boolean bold) {
        Cell cell = new Cell();
        Paragraph para = new Paragraph(text);
        
        if (bold) {
            try {
                para.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
            } catch (Exception e) {
                // Fallback
            }
        }
        
        cell.add(para);
        cell.setPadding(5);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        return cell;
    }
    
    private Cell createHeaderCell(String text) {
        Cell cell = new Cell();
        Paragraph para = new Paragraph(text);
        
        try {
            para.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        } catch (Exception e) {
            // Fallback
        }
        
        para.setTextAlignment(TextAlignment.CENTER);
        cell.add(para);
        cell.setPadding(8);
        cell.setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240));
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        return cell;
    }
    
    
    private String checker(Integer pers) {
    	return pers != null ? pers+"" : "N/A"; 
    }
    
    
 
}