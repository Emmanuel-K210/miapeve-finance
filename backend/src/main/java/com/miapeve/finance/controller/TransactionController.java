package com.miapeve.finance.controller;

import com.miapeve.finance.dto.TransactionDTO;
import com.miapeve.finance.dto.TransactionRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionDTO> findAll(@RequestParam(required = false) Long compteId) {
        return compteId != null ? transactionService.findByCompte(compteId) : transactionService.findAll();
    }

    @PostMapping
    public TransactionDTO create(@Valid @RequestBody TransactionRequest request, HttpServletRequest httpRequest) {
        Long utilisateurId = (Long) httpRequest.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return transactionService.create(request, utilisateurId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }
}
