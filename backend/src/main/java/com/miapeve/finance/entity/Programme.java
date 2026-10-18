package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "programmes")
public class Programme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "programme_id")
    private Long id;

    @Column(name = "type_programme", nullable = false, length = 100)
    private String typeProgramme;

    @Column(name = "date_programme", nullable = false)
    private LocalDate dateProgramme;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "cree_par", nullable = false)
    private Long creePar;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "modifie_par")
    private Long modifiePar;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "est_archive")
    private boolean estArchive = false;

    @Column(name = "hommes")
    private int hommes = 0;

    @Column(name = "femmes")
    private int femmes = 0;

    @Column(name = "jeunes")
    private int jeunes = 0;

    @Column(name = "enfants")
    private int enfants = 0;

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

    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }

    public Long getModifiePar() { return modifiePar; }
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }

    public boolean isEstArchive() { return estArchive; }
    public void setEstArchive(boolean estArchive) { this.estArchive = estArchive; }

    public int getHommes() { return hommes; }
    public void setHommes(int hommes) { this.hommes = hommes; }

    public int getFemmes() { return femmes; }
    public void setFemmes(int femmes) { this.femmes = femmes; }

    public int getJeunes() { return jeunes; }
    public void setJeunes(int jeunes) { this.jeunes = jeunes; }

    public int getEnfants() { return enfants; }
    public void setEnfants(int enfants) { this.enfants = enfants; }
}
