package com.miapeve.finance.entites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Rapport {
	private LocalDate dateRapport;
	private BigDecimal cumuleAmountMainAccount;
	private List<Depense> depenses;
	private BigDecimal cumuleDepense;
	 
}
