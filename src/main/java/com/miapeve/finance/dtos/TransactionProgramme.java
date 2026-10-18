package com.miapeve.finance.dtos;

public class TransactionProgramme {
	private String mois;
	private String nomService;
	private String dateProgramme;
	private Double reportComptePrincipal;
	private Double reportCompteDesDimes;
	private Double offrand;
	private Double dime;
	private Double autres;
	private Double total;
	private Double sortir;
	private Double dimeDesDimes;
	private Double balanceComptePrincipal;
	private Double balanceCompteDesDimes;
	private Double montantRestantEnCaisse;
	private String typeProgramme;
	private Integer hommes;
	private Integer femmes;
	private Integer enfants;

	private Integer jeunes;

	public Integer getHommes() {
		return hommes;
	}

	public Integer getFemmes() {
		return femmes;
	}

	public Integer getEnfants() {
		return enfants;
	}

	public Integer getJeunes() {
		return jeunes;
	}

	public String getTypeProgramme() {
		return typeProgramme;
	}

	public void setTypeProgramme(String typeProgramme) {
		this.typeProgramme = typeProgramme;
	}

	public TransactionProgramme() {
		// TODO Auto-generated constructor stub
	}

	public TransactionProgramme(TransactionProgrammeBuilder builder) {
		this.autres = builder.autres;
		this.balanceCompteDesDimes = builder.balanceCompteDesDimes;
		this.balanceComptePrincipal = builder.balanceComptePrincipal;
		this.dateProgramme = builder.dateProgramme;
		this.dime = builder.dime;
		this.dimeDesDimes = builder.dimeDesDimes;
		this.mois = builder.mois;
		this.montantRestantEnCaisse = builder.montantRestantEnCaisse;
		this.nomService = builder.nomService;
		this.offrand = builder.offrand;
		this.reportCompteDesDimes = builder.reportCompteDesDimes;
		this.reportComptePrincipal = builder.reportComptePrincipal;
		this.sortir = builder.sortir;
		this.total = builder.total;
		this.typeProgramme = builder.typeProgramme;
		this.hommes = builder.hommes;
		this.femmes = builder.femmes;
		this.jeunes = builder.jeunes;
		this.enfants = builder.enfants;
	}

	public String getMois() {
		return mois;
	}

	public void setMois(String mois) {
		this.mois = mois;
	}

	public String getNomService() {
		return nomService;
	}

	public void setNomService(String nomService) {
		this.nomService = nomService;
	}

	public String getDateProgramme() {
		return dateProgramme;
	}

	public void setDateProgramme(String dateProgramme) {
		this.dateProgramme = dateProgramme;
	}

	public Double getReportComptePrincipal() {
		return reportComptePrincipal;
	}

	public void setReportComptePrincipal(Double reportComptePrincipal) {
		this.reportComptePrincipal = reportComptePrincipal;
	}

	public Double getReportCompteDesDimes() {
		return reportCompteDesDimes;
	}

	public void setReportCompteDesDimes(Double reportCompteDesDimes) {
		this.reportCompteDesDimes = reportCompteDesDimes;
	}

	public Double getOffrand() {
		return offrand;
	}

	public void setOffrand(Double offrand) {
		this.offrand = offrand;
	}

	public Double getDime() {
		return dime;
	}

	public void setDime(Double dime) {
		this.dime = dime;
	}

	public Double getAutres() {
		return autres;
	}

	public void setAutres(Double autres) {
		this.autres = autres;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Double getSortir() {
		return sortir;
	}

	public void setSortir(Double sortir) {
		this.sortir = sortir;
	}

	public Double getDimeDesDimes() {
		return dimeDesDimes;
	}

	public void setDimeDesDimes(Double dimeDesDimes) {
		this.dimeDesDimes = dimeDesDimes;
	}

	public Double getBalanceComptePrincipal() {
		return balanceComptePrincipal;
	}

	public void setBalanceComptePrincipal(Double balanceComptePrincipal) {
		this.balanceComptePrincipal = balanceComptePrincipal;
	}

	public Double getBalanceCompteDesDimes() {
		return balanceCompteDesDimes;
	}

	public void setBalanceCompteDesDimes(Double balanceCompteDesDimes) {
		this.balanceCompteDesDimes = balanceCompteDesDimes;
	}

	public Double getMontantRestantEnCaisse() {
		return montantRestantEnCaisse;
	}

	public void setMontantRestantEnCaisse(Double montantRestantEnCaisse) {
		this.montantRestantEnCaisse = montantRestantEnCaisse;
	}

	public static TransactionProgrammeBuilder builder() {
		return new TransactionProgrammeBuilder();
	}

	public static class TransactionProgrammeBuilder {
		private String mois;
		private String nomService;
		private String dateProgramme;
		private Double reportComptePrincipal;
		private Double reportCompteDesDimes;
		private Double offrand;
		private Double dime;
		private Double autres;
		private Double total;
		private Double sortir;
		private Double dimeDesDimes;
		private Double balanceComptePrincipal;
		private Double balanceCompteDesDimes;
		private Double montantRestantEnCaisse;
		private String typeProgramme;
		private Integer hommes;
		private Integer femmes;
		private Integer enfants;
		private Integer jeunes;

		public TransactionProgrammeBuilder hommes(Integer hommes) {
			this.hommes = hommes;
			return this;
		}

		public TransactionProgrammeBuilder femmes(Integer femmes) {
			this.femmes = femmes;
			return this;
		}

		public TransactionProgrammeBuilder enfants(Integer enfants) {
			this.enfants = enfants;
			return this;
		}

		public TransactionProgrammeBuilder jeunes(Integer jeunes) {
			this.jeunes = jeunes;
			return this;
		}

		public TransactionProgrammeBuilder mois(String mois) {
			this.mois = mois;
			return this;
		}

		public TransactionProgrammeBuilder typeProgramme(String typeProgramme) {
			this.typeProgramme = typeProgramme;
			return this;
		}

		public TransactionProgrammeBuilder nomService(String nomService) {
			this.nomService = nomService;
			return this;
		}

		public TransactionProgrammeBuilder dateProgramme(String dateProgramme) {
			this.dateProgramme = dateProgramme;
			return this;
		}

		public TransactionProgrammeBuilder reportComptePrincipal(Double reportComptePrincipal) {
			this.reportComptePrincipal = reportComptePrincipal;
			return this;
		}

		public TransactionProgrammeBuilder reportCompteDesDimes(Double reportCompteDesDimes) {
			this.reportCompteDesDimes = reportCompteDesDimes;

			return this;
		}

		public TransactionProgrammeBuilder offrand(Double offrand) {
			this.offrand = offrand;
			return this;
		}

		public TransactionProgrammeBuilder dime(Double dime) {
			this.dime = dime;
			return this;
		}

		public TransactionProgrammeBuilder autres(Double autres) {
			this.autres = autres;
			return this;
		}

		public TransactionProgrammeBuilder total(Double total) {
			this.total = total;
			return this;
		}

		public TransactionProgrammeBuilder sortir(Double sortir) {
			this.sortir = sortir;
			return this;
		}

		public TransactionProgrammeBuilder dimeDesDimes(Double dimeDesDimes) {
			this.dimeDesDimes = dimeDesDimes;
			return this;
		}

		public TransactionProgrammeBuilder balanceComptePrincipal(Double balanceComptePrincipal) {
			this.balanceComptePrincipal = balanceComptePrincipal;
			return this;
		}

		public TransactionProgrammeBuilder balanceCompteDesDimes(Double balanceCompteDesDimes) {
			this.balanceCompteDesDimes = balanceCompteDesDimes;
			return this;
		}

		public TransactionProgrammeBuilder montantRestantEnCaisse(Double montantRestantEnCaisse) {
			this.montantRestantEnCaisse = montantRestantEnCaisse;
			return this;
		}

		public TransactionProgramme build() {
			return new TransactionProgramme(this);
		}

	}
}
