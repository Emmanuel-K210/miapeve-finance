package com.miapeve.finance.enums;

public enum TypeTransaction {
    CREDIT("CREDIT"),
    DEBIT("DEBIT"),
	TRANSFERT("TRANSFERT");
    
    private String libelle;
    
    TypeTransaction(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    @Override
    public String toString() {
        return libelle;
    }
    
    /**
     * Convertit une chaîne en TypeTransaction
     * @param value la valeur à convertir
     * @return le TypeTransaction correspondant
     * @throws IllegalArgumentException si la valeur n'est pas reconnue
     */
    public static TypeTransaction fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        for (TypeTransaction type : TypeTransaction.values()) {
            if (type.name().equalsIgnoreCase(value) || type.getLibelle().equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Type de transaction non reconnu: " + value);
    }
    
    /**
     * Vérifie si c'est une transaction d'entrée (semence)
     */
    public boolean isEntree() {
        return this == CREDIT;
    }
    
    /**
     * Vérifie si c'est une transaction de sortie (dépense)
     */
    public boolean isSortie() {
        return this == DEBIT;
    }
    
    /**
     * Retourne tous les types sous forme de tableau de chaînes
     */
    public static String[] getLibelles() {
        TypeTransaction[] values = values();
        String[] libelles = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            libelles[i] = values[i].getLibelle();
        }
        return libelles;
    }
    
    /**
     * Retourne la couleur associée au type (pour l'UI)
     */
    public String getCouleur() {
        switch (this) {
            case CREDIT:
                return "#2ecc71"; // Vert
            case DEBIT:
                return "#e74c3c"; // Rouge
            default:
                return "#95a5a6"; // Gris
        }
    }
    
    /**
     * Retourne l'icône associée au type (pour l'UI)
     */
    public String getIcone() {
        switch (this) {
            case CREDIT:
                return "ARROW_UP"; // Flèche vers le haut
            case DEBIT:
                return "ARROW_DOWN"; // Flèche vers le bas
            default:
                return "EXCHANGE"; // Échange
        }
    }
}