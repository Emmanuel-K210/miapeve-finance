async function loadDashboard() {
  const statsGrid = document.getElementById("stats-grid");
  const comptesBody = document.getElementById("dashboard-comptes-body");

  try {
    const summary = await api.getDashboardSummary();

    statsGrid.innerHTML = `
      ${statCard("Solde total", formatMontant(summary.soldeTotal), "💰", "primary")}
      ${statCard("Total semences", formatMontant(summary.totalSemences), "🌱", "success")}
      ${statCard("Total dépenses", formatMontant(summary.totalDepenses), "🧾", "danger")}
      ${statCard("Comptes actifs", summary.nombreComptes, "🏦", "warning")}
    `;

    comptesBody.innerHTML = summary.comptes.length
      ? summary.comptes
          .map(
            (c) => `
        <tr>
          <td>${escapeHtml(c.nomCompte)}</td>
          <td><span class="badge badge-neutral">${escapeHtml(c.typeCompte)}</span></td>
          <td>${formatMontant(c.solde)}</td>
        </tr>`
          )
          .join("")
      : `<tr><td colspan="3" class="empty-state">Aucun compte pour le moment</td></tr>`;
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

function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = value ?? "";
  return div.innerHTML;
}
