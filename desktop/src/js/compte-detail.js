document.getElementById("compte-detail-back").addEventListener("click", () => {
  document.querySelectorAll(".nav-item").forEach((el) => el.classList.remove("active"));
  document.querySelector(".nav-item[data-page='comptes']")?.classList.add("active");
  navigateTo("comptes");
});

async function openCompteDetail(compteId) {
  document.querySelectorAll(".page-content > section").forEach((el) => el.classList.add("hidden"));
  document.querySelectorAll(".nav-item").forEach((el) => el.classList.remove("active"));
  document.getElementById("page-compte-detail").classList.remove("hidden");

  const statCard = document.getElementById("compte-detail-stat");
  const timeline = document.getElementById("compte-detail-timeline");
  statCard.innerHTML = "";
  timeline.innerHTML = `<div class="empty-state">Chargement…</div>`;

  try {
    const [comptes, transactions, semences] = await Promise.all([
      api.getComptes(),
      api.getTransactions(compteId),
      api.getSemences(compteId),
    ]);

    const compte = comptes.find((c) => c.id === compteId);
    if (!compte) {
      showToast("Compte introuvable", "error");
      return;
    }

    document.getElementById("page-title").textContent = compte.nomCompte;
    document.getElementById("page-subtitle").textContent = `Historique complet du compte`;

    statCard.innerHTML = `
      <div class="stat-icon tone-primary">${Icons.bank()}</div>
      <div class="stat-label">${escapeHtml(compte.typeCompte)}</div>
      <div class="stat-value">${formatMontant(compte.solde)}</div>
    `;

    const entries = [
      ...transactions.map((t) => ({
        date: t.dateCreation,
        titre: t.typeTransaction === "CREDIT" ? "Entrée manuelle" : "Sortie manuelle",
        sousTitre: t.description || t.reference || "—",
        montant: t.montant,
        positif: t.typeTransaction === "CREDIT",
      })),
      ...semences.map((s) => ({
        date: s.dateSemence,
        titre: `Semence — ${SEMENCE_TYPE_LABELS[s.typeSemence] || s.typeSemence}`,
        sousTitre: s.programmeLibelle,
        montant: s.montant,
        positif: true,
      })),
    ].sort((a, b) => new Date(b.date) - new Date(a.date));

    timeline.innerHTML = entries.length
      ? `<div class="activity-list">${entries.map(renderCompteDetailEntry).join("")}</div>`
      : `<div class="empty-state">Aucune opération enregistrée pour ce compte</div>`;
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderCompteDetailEntry(entry) {
  const tone = entry.positif ? "tone-success" : "tone-danger";
  const icon = entry.positif ? Icons.arrowUpCircle() : Icons.arrowDownCircle();
  const signe = entry.positif ? "+" : "−";

  return `
    <div class="activity-item">
      <div class="activity-icon ${tone}">${icon}</div>
      <div class="activity-main">
        <div class="activity-title">${escapeHtml(entry.titre)}</div>
        <div class="activity-meta">${escapeHtml(entry.sousTitre || "")} · ${formatDateOnly(entry.date)}</div>
      </div>
      <div class="activity-amount ${entry.positif ? "amount-positive" : "amount-negative"}">${signe} ${formatMontant(entry.montant)}</div>
    </div>
  `;
}
