package com.miapeve.finance.repositories.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.miapeve.finance.dao.TransactionDAO;
import com.miapeve.finance.dtos.TransactionProgramme;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Transaction;
import com.miapeve.finance.enums.TypeCompte;
import com.miapeve.finance.enums.TypeSemence;
import com.miapeve.finance.enums.TypeTransaction;
import com.miapeve.finance.service.impl.FinanceService;
import com.miapeve.finance.utils.DateConverter;

public class ImpressionFacadeService {

	private final FinanceService financeService;
	private final TransactionDAO dao;

	public ImpressionFacadeService() {
		financeService = new FinanceService();
		dao = new TransactionDAO();
	}

	public TransactionProgramme buildTransactionProgramme(Programme programme) {

		try {
			LocalDate dateProgramme = programme.getDateProgramme();
            LocalDateTime dateTimeFinJournee = dateProgramme.atTime(23, 59, 59);
            Compte compte1 = getCompte(TypeCompte.PRINCIPAL);
            Compte compte2 = getCompte(TypeCompte.DIMES);
			Double reportComptePrincipal = getCumulCompte(compte1,dateTimeFinJournee,TypeTransaction.CREDIT).doubleValue();
			Double reportCompteDesDimes = getCumulCompte(compte2,dateTimeFinJournee,TypeTransaction.CREDIT).doubleValue();
			
			Double offrand = getCumulSemenceByProgrammAndTypeSemence(programme, TypeSemence.OFFRANDE).doubleValue();
			Double dime = getCumulSemenceByProgrammAndTypeSemence(programme, TypeSemence.DIMES).doubleValue();
			Double autre = getCumulSemenceByProgrammAutre(programme).doubleValue();
			Double total = offrand + dime + autre;
			
			Double balanceComptePrincipal = getBalanceCompte(TypeCompte.PRINCIPAL,dateTimeFinJournee).doubleValue();
	        Double balanceCompteDesDimes =getBalanceCompte(TypeCompte.DIMES,dateTimeFinJournee).doubleValue();
			Double montantRestantCaisse = balanceComptePrincipal + balanceCompteDesDimes;
	          
			return TransactionProgramme.builder()
					.mois(DateConverter.convertToMoisFrench(dateProgramme).toUpperCase())
					.dateProgramme(DateConverter.formatToStringForPrint(dateProgramme))
					.typeProgramme(programme.getTypeProgramme())
					.autres(autre)
					.offrand(offrand)
					.dime(dime)
					.total(total)
					.reportComptePrincipal(reportComptePrincipal)
					.reportCompteDesDimes(reportCompteDesDimes)
					.hommes(programme.getHommes())
					.femmes(programme.getFemmes())
					.jeunes(programme.getJeunes())
					.enfants(programme.getEnfants())
					.balanceCompteDesDimes(balanceCompteDesDimes)
					.balanceComptePrincipal(balanceComptePrincipal)
					.montantRestantEnCaisse(montantRestantCaisse)
					.build();
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		return TransactionProgramme.builder().build();
	}
	
	
	private BigDecimal getCumulCompte(Compte compte,LocalDateTime date,TypeTransaction type){
		return dao.getTotalByCompteAndTypeBeforeDate(compte.getCompteId(),type,date);
	}
	
	
	private BigDecimal getCumulSemenceByProgrammAndTypeSemence(Programme p, TypeSemence type) throws Exception {
		if (p == null)
			throw new Exception("Programme non trouvé");
		return financeService.getSemencesByProgramme(p.getId())
				.stream()
				.filter(s -> s.getTypeSemence().equals(type))
				.map(s -> s.getMontant())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	 private BigDecimal getCumulSemenceByProgrammAutre(Programme p) throws Exception {
	        if (p == null)
	            throw new Exception("Programme non trouvé");
	        return financeService.getSemencesByProgramme(p.getId())
	                .stream()
	                .filter(s -> !s.getTypeSemence().equals(TypeSemence.DIMES) && 
	                            !s.getTypeSemence().equals(TypeSemence.OFFRANDE))
	                .map(s -> s.getMontant())
	                .reduce(BigDecimal.ZERO, BigDecimal::add);
	    }
	 
	 private Compte getCompte(TypeCompte compte) {
		 return financeService.findCompteByType(compte);
	 }
	 
	 private BigDecimal getBalanceCompte(TypeCompte type, LocalDateTime date) {
		    Compte compte = financeService.findCompteByType(type);

		    BigDecimal credit = dao.getTotalByCompteAndTypeBeforeDate(
		            compte.getCompteId(), TypeTransaction.CREDIT, date);

		    BigDecimal debit = dao.getTotalByCompteAndTypeBeforeDate(
		            compte.getCompteId(), TypeTransaction.DEBIT, date);

		    return credit.subtract(debit);
		}

}
