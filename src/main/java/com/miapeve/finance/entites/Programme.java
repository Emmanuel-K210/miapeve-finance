package com.miapeve.finance.entites;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Programme {
    private Long id;
    private String typeProgramme;
    private LocalDate dateProgramme;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Utilisateur utilisateur;
    private Integer hommes;
    private Integer femmes;
    private Integer enfants;
    private Integer jeunes;
    private List<Semence> semences = new ArrayList<>();
    private Boolean isCustom = false;
    private Boolean isArchive = false;
    private Long creePar; // ✅ Audit
    private LocalDateTime dateCreation; // ✅ Audit
    private Long modifiePar; // ✅ Audit
    private LocalDateTime dateModification; // ✅ Audit

    // Constructeurs
    public Programme() {
        this.dateCreation = LocalDateTime.now();
    }
    
    public Integer getHommes() {
		return hommes;
	}

	public void setHommes(Integer hommes) {
		this.hommes = hommes;
	}

	public Integer getFemmes() {
		return femmes;
	}

	public void setFemmes(Integer femmes) {
		this.femmes = femmes;
	}

	public Integer getEnfants() {
		return enfants;
	}

	public void setEnfants(Integer enfants) {
		this.enfants = enfants;
	}

	public Integer getJeunes() {
		return jeunes;
	}

	public void setJeunes(Integer jeunes) {
		this.jeunes = jeunes;
	}

	public Programme(Long id, String typeProgramme, LocalDate dateProgramme, LocalTime heureDebut, LocalTime heureFin) {
        this.id = id;
        this.typeProgramme = typeProgramme;
        this.dateProgramme = dateProgramme;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.dateCreation = LocalDateTime.now();
    }

    public Programme(ProgrammeBuilder builder) {
        this.id = builder.id;
        this.typeProgramme = builder.typeProgramme;
        this.dateProgramme = builder.dateProgramme;
        this.heureDebut = builder.heureDebut;
        this.heureFin = builder.heureFin;
        this.utilisateur = builder.utilisateur;
        this.isCustom = builder.isCustom;
        this.creePar = builder.creePar;
        this.modifiePar = builder.modifierPar;
        this.dateCreation = builder.dateCreation != null ? builder.dateCreation : LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeProgramme() { return typeProgramme; }
    public void setTypeProgramme(String typeProgramme) { this.typeProgramme = typeProgramme; }

    public LocalDate getDateProgramme() { return dateProgramme; }
    public void setDateProgramme(LocalDate dateProgramme) { this.dateProgramme = dateProgramme; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public List<Semence> getSemences() { return semences; }
    public void setSemences(List<Semence> semences) { this.semences = semences; }

    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }

    public Boolean getIsArchive() { return isArchive; }
    public void setIsArchive(Boolean isArchive) { this.isArchive = isArchive; }

    public Long getCreePar() { return creePar; } // ✅ Audit
    public void setCreePar(Long creePar) { this.creePar = creePar; }

    public LocalDateTime getDateCreation() { return dateCreation; } // ✅ Audit
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Long getModifiePar() { return modifiePar; } // ✅ Audit
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }

    public LocalDateTime getDateModification() { return dateModification; } // ✅ Audit
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    // Builder Pattern
    public static ProgrammeBuilder builder() {
        return new ProgrammeBuilder();
    }

    public static class ProgrammeBuilder {
        private Long id;
        private String typeProgramme;
        private LocalDate dateProgramme;
        private LocalTime heureDebut;
        private LocalTime heureFin;
        private Utilisateur utilisateur;
        private Boolean isCustom = false;
        private Long creePar;
        private LocalDateTime dateCreation;
        private Long modifierPar;

        public ProgrammeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProgrammeBuilder typeProgramme(String typeProgramme) {
            this.typeProgramme = typeProgramme;
            return this;
        }

        public ProgrammeBuilder dateProgramme(LocalDate dateProgramme) {
            this.dateProgramme = dateProgramme;
            return this;
        }

        public ProgrammeBuilder heureDebut(LocalTime heureDebut) {
            this.heureDebut = heureDebut;
            return this;
        }

        public ProgrammeBuilder heureFin(LocalTime heureFin) {
            this.heureFin = heureFin;
            return this;
        }


        public ProgrammeBuilder utilisateur(Utilisateur utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public ProgrammeBuilder isCustom(Boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        public ProgrammeBuilder creePar(Long creePar) {
            this.creePar = creePar;
            return this;
        }
        
        public ProgrammeBuilder modifierPar(Long modifierPar) {
            this.modifierPar = modifierPar;
            return this;
        }

        public ProgrammeBuilder dateCreation(LocalDateTime dateCreation) {
            this.dateCreation = dateCreation;
            return this;
        }

        public Programme build() {
            return new Programme(this);
        }
    }
}