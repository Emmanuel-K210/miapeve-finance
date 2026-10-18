package com.miapeve.finance.service;

import com.miapeve.finance.dto.TransactionDTO;
import com.miapeve.finance.dto.TransactionRequest;
import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.entity.Transaction;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CompteRepository compteRepository;

    public TransactionService(TransactionRepository transactionRepository, CompteRepository compteRepository) {
        this.transactionRepository = transactionRepository;
        this.compteRepository = compteRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> findAll() {
        return transactionRepository.findAllByOrderByDateCreationDesc().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> findByCompte(Long compteId) {
        return transactionRepository.findByCompteIdOrderByDateCreationDesc(compteId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public TransactionDTO create(TransactionRequest request, Long utilisateurId) {
        Compte compte = compteRepository.findById(request.compteId())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + request.compteId()));

        boolean isCredit = "CREDIT".equals(request.typeTransaction());
        double nouveauSolde = isCredit ? compte.getSolde() + request.montant() : compte.getSolde() - request.montant();

        if (!isCredit && nouveauSolde < 0) {
            throw new IllegalArgumentException("Solde insuffisant sur le compte '" + compte.getNomCompte() + "'");
        }

        compte.setSolde(nouveauSolde);
        compteRepository.save(compte);

        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(request.typeTransaction());
        transaction.setMontant(request.montant());
        transaction.setCompte(compte);
        transaction.setDescription(request.description());
        transaction.setReference(request.reference());
        transaction.setCreePar(utilisateurId);

        return toDTO(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable: " + id));

        Compte compte = transaction.getCompte();
        boolean isCredit = "CREDIT".equals(transaction.getTypeTransaction());
        double soldeRestaure = isCredit ? compte.getSolde() - transaction.getMontant() : compte.getSolde() + transaction.getMontant();
        compte.setSolde(soldeRestaure);
        compteRepository.save(compte);

        transactionRepository.delete(transaction);
    }

    private TransactionDTO toDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getTypeTransaction(),
                transaction.getMontant(),
                transaction.getCompte().getId(),
                transaction.getCompte().getNomCompte(),
                transaction.getDescription(),
                transaction.getReference(),
                transaction.getDateCreation()
        );
    }
}
