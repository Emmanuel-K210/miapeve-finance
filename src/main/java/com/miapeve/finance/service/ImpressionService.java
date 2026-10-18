package com.miapeve.finance.service;

import java.time.LocalDate;
import com.miapeve.finance.entites.Programme;

public interface ImpressionService {
    
    /**
     * Génère un rapport PDF pour un programme spécifique
     * @param programme Le programme à imprimer
     */
    void printProgramme(Programme programme,String parentDir);
    
    /**
     * Génère un rapport de gestion financière mensuelle
     * @param debut Date de début de la période
     * @param fin Date de fin de la période
     */
    void printGestionFinancierMensuel(LocalDate debut, LocalDate fin,String parentDir);
    
    /**
     * Génère un journal de comptabilité mensuelle
     * @param debut Date de début de la période
     * @param fin Date de fin de la période
     */
    void printComptabiliteMensuel(LocalDate debut, LocalDate fin,String parentDir);
    
    /**
     * Génère un relevé des transactions mensuelles
     * @param debut Date de début de la période
     * @param fin Date de fin de la période
     */
    void printTransactionMensuel(LocalDate debut, LocalDate fin,String parentDir);
}