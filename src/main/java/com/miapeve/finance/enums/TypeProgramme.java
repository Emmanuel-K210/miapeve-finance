package com.miapeve.finance.enums;

import java.util.List;
import java.util.stream.Stream;

public enum TypeProgramme {
	
	COMMANDER_AU_MOIS("COMMANDER AU MOIS"),
	COMMANDER_LA_SAISON("COMMANDER À LA SAISON"),
	CULTE_DU_DIMANCHE("CULTE DU JOUR DU SEIGNEUR"),
	COMMANDER_ANNEE("COMMANDER À L'ANNÉE"),
	CULTE_ACTION_GRACE("CULTE D'ACTION DE GRACE");
	
	private String nom;
	
	private TypeProgramme(String nom) {
		this.nom = nom;
	}
	
	public String getNom() {
		return this.nom;
	}
	
	public static List<String> findAll(){
		return Stream.of(values())
				.map(type->type.getNom())
				.toList();
	}
	
	public static TypeProgramme of(String nom) {
		return Stream.of(values())
				.filter(tp->tp.getNom().equals(nom))
				.findFirst()
				.orElseThrow();
	}
	
}
