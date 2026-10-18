async function loadDashboard() {
  const statsGrid = document.getElementById("stats-grid");
  const comptesBody = document.getElementById("dashboard-comptes-body");

  try {
    const summary = await api.getDashboardSummary();

    statsGrid.innerHTML = `
      ${statCard("Solde total", formatMontant(summary.soldeTotal), Icons.wallet(), "primary")}
      ${statCard("Total semences", formatMontant(summary.totalSemences), Icons.sprout(), "success")}
      ${statCard("Total dépenses", formatMontant(summary.totalDepenses), Icons.receipt(), "danger")}
      ${statCard("Comptes actifs", summary.nombreComptes, Icons.bank(), "warning")}
    `;

    comptesBody.innerHTML = summary.comptes.length
      ? summary.comptes
          .map(
            (c) => `
        <tr>
          <td>
            <div style="font-weight:600">${escapeHtml(c.nomCompte)}</div>
            <div style="font-size: var(--font-size-xs); color: var(--color-text-subtle);">${escapeHtml(c.typeCompte)}</div>
          </td>
          <td style="text-align:right; font-weight:700;">${formatMontant(c.solde)}</td>
        </tr>`
          )
          .join("")
      : `<tr><td colspan="2" class="empty-state">Aucun compte pour le moment</td></tr>`;

    renderChart(summary.evolution);
    renderActivity(summary.activitesRecentes);
    renderBudgetWidget(summary.budgetsMoisCourant, "dashboard-budgets-root");
  } catch (err) {
    showToast(err.message, "error");
  }
}

function statCard(label, value, icon, tone) {
  return `
    <div class="stat-card">
      <div class="stat-icon tone-${tone}">${icon}</div>
      <div class="stat-label">${label}</div>
      <div class="stat-value">${value}</div>
    </div>
  `;
}

function renderChart(points) {
  const root = document.getElementById("chart-root");
  if (!points || !points.length) {
    root.innerHTML = `<div class="empty-state">Pas encore de données</div>`;
    return;
  }

  const max = Math.max(1, ...points.flatMap((p) => [p.semences, p.depenses]));

  root.innerHTML = `
    <div class="chart-wrap">
      <div class="chart-bars">
        ${points
          .map((p) => {
            const hSemences = Math.max(2, Math.round((p.semences / max) * 130));
            const hDepenses = Math.max(2, Math.round((p.depenses / max) * 130));
            return `
              <div class="chart-month" title="${escapeHtml(p.label)} — Semences: ${formatMontant(p.semences)}, Dépenses: ${formatMontant(p.depenses)}">
                <div class="chart-month-bars">
                  <div class="chart-bar tone-success" style="height:${hSemences}px"></div>
                  <div class="chart-bar tone-danger" style="height:${hDepenses}px"></div>
                </div>
                <div class="chart-month-label">${escapeHtml(p.label)}</div>
              </div>
            `;
          })
          .join("")}
      </div>
    </div>
  `;
}

function renderActivity(activites) {
  const root = document.getElementById("activity-root");
  if (!activites || !activites.length) {
    root.innerHTML = `<div class="empty-state">Aucune activité récente</div>`;
    return;
  }

  root.innerHTML = `
    <div class="activity-list">
      ${activites
        .map((a) => {
          const tone = a.positif ? "tone-success" : "tone-danger";
          const icon = a.positif ? Icons.arrowUpCircle() : Icons.arrowDownCircle();
          const signe = a.positif ? "+" : "−";
          return `
            <div class="activity-item">
              <div class="activity-icon ${tone}">${icon}</div>
              <div class="activity-main">
                <div class="activity-title">${escapeHtml(a.titre)}</div>
                <div class="activity-meta">${escapeHtml(a.sousTitre || "")}</div>
              </div>
              <div class="activity-amount ${a.positif ? "amount-positive" : "amount-negative"}">${signe} ${formatMontant(a.montant)}</div>
            </div>
          `;
        })
        .join("")}
    </div>
  `;
}

function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = value ?? "";
  return div.innerHTML;
}
