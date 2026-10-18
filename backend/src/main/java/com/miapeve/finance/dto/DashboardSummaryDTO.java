package com.miapeve.finance.dto;

import java.util.List;

public record DashboardSummaryDTO(
        double soldeTotal,
        double totalSemences,
        double totalDepenses,
        int nombreComptes,
        int nombreProgrammes,
        List<CompteDTO> comptes,
        List<MonthlyPointDTO> evolution,
        List<ActivityDTO> activitesRecentes,
        List<BudgetDTO> budgetsMoisCourant
) {
}
