package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(name = "type_transaction", nullable = false, length = 20)
    private String typeTransaction;

    @Column(name = "montant", nullable = false)
    private double montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @Column(name = "description")
    private String description;

    @Column(name = "reference")
    private String reference;

    @Column(name = "cree_par", nullable = false)
    private Long creePar;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(String typeTransaction) { this.typeTransaction = typeTransaction; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }

    public LocalDateTime getDateCreation() { return dateCreation; }
}
