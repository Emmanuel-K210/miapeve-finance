package com.miapeve.finance.controller;

import com.miapeve.finance.dto.BudgetDTO;
import com.miapeve.finance.dto.BudgetRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetDTO> findAll(@RequestParam(required = false) Boolean currentMonth) {
        return Boolean.TRUE.equals(currentMonth) ? budgetService.findForCurrentMonth() : budgetService.findAll();
    }

    @PostMapping
    public BudgetDTO create(@Valid @RequestBody BudgetRequest request, HttpServletRequest httpRequest) {
        Long utilisateurId = (Long) httpRequest.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return budgetService.create(request, utilisateurId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }
}
