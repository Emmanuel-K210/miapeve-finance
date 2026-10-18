package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "semences")
public class Semence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semence_id")
    private Long id;

    @Column(name = "type_semence", nullable = false, length = 100)
    private String typeSemence;

    @Column(name = "montant", nullable = false)
    private double montant;

    @Column(name = "date_semence", nullable = false)
    private LocalDate dateSemence;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @Column(name = "cree_par", nullable = false)
    private Long creePar;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeSemence() { return typeSemence; }
    public void setTypeSemence(String typeSemence) { this.typeSemence = typeSemence; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDate getDateSemence() { return dateSemence; }
    public void setDateSemence(LocalDate dateSemence) { this.dateSemence = dateSemence; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }

    public LocalDateTime getDateCreation() { return dateCreation; }
}
