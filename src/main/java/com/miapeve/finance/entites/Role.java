package com.miapeve.finance.entites;

import java.time.LocalDateTime;

public class Role {
    private Long id;
    private String libelle;
    private String description;
    private Long creePar; // ✅ Audit
    private LocalDateTime dateCreation; // ✅ Audit
    private Long modifiePar; // ✅ Audit
    private LocalDateTime dateModification; // ✅ Audit
    
    public Role() {
        this.dateCreation = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
    	return this.libelle;
    }
    
    public Role(Long id, String libelle, String description) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.dateCreation = LocalDateTime.now();
    }
    
    public Role(Long id, String libelle, String description, Long creePar) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.creePar = creePar;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getCreePar() { return creePar; } // ✅ Audit
    public void setCreePar(Long creePar) { this.creePar = creePar; }
    
    public LocalDateTime getDateCreation() { return dateCreation; } // ✅ Audit
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public Long getModifiePar() { return modifiePar; } // ✅ Audit
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }
    
    public LocalDateTime getDateModification() { return dateModification; } // ✅ Audit
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}