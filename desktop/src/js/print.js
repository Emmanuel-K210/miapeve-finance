/**
 * Impression (reçus, relevés, rapports) via l'impression native du
 * navigateur : on remplit une zone cachée (#print-area), on déclenche
 * window.print(), le CSS @media print masque tout le reste. Fonctionne
 * aussi bien pour imprimer sur papier que pour « Enregistrer en PDF »
 * depuis la boîte de dialogue d'impression — y compris dans la coquille
 * Tauri.
 */
function triggerPrint(title, bodyHtml) {
  const area = document.getElementById("print-area");
  area.innerHTML = `
    <div class="print-header">
      <div class="print-brand">MIAPEVE FINANCE</div>
      <div class="print-title">${title}</div>
    </div>
    ${bodyHtml}
    <div class="print-footer">
      Document généré le ${new Intl.DateTimeFormat("fr-FR", { dateStyle: "long", timeStyle: "short" }).format(new Date())}
    </div>
  `;
  window.print();
}

async function printProgrammeReceipt(programmeId) {
  try {
    const [programmes, semences] = await Promise.all([api.getProgrammes(), api.getSemences()]);
    const programme = programmes.find((p) => p.id === programmeId);
    if (!programme) {
      showToast("Programme introuvable", "error");
      return;
    }
    const semencesDuProgramme = semences.filter((s) => s.programmeId === programmeId);
    const total = semencesDuProgramme.reduce((sum, s) => sum + s.montant, 0);

    const rows = semencesDuProgramme.length
      ? semencesDuProgramme
          .map(
            (s) => `
        <tr>
          <td>${escapeHtml(SEMENCE_TYPE_LABELS[s.typeSemence] || s.typeSemence)}</td>
          <td>${escapeHtml(s.nomCompte)}</td>
          <td>${formatMontant(s.montant)}</td>
        </tr>`
          )
          .join("")
      : `<tr><td colspan="3">Aucune semence enregistrée pour ce programme</td></tr>`;

    triggerPrint(
      "Reçu de programme",
      `
        <table class="print-meta">
          <tr><td>Type de programme</td><td>${escapeHtml(programme.typeProgramme)}</td></tr>
          <tr><td>Date</td><td>${formatDateOnly(programme.dateProgramme)}</td></tr>
          <tr><td>Horaire</td><td>${formatHeure(programme.heureDebut)} — ${formatHeure(programme.heureFin)}</td></tr>
        </table>
        <table class="print-table">
          <thead><tr><th>Type de semence</th><th>Compte</th><th>Montant</th></tr></thead>
          <tbody>${rows}</tbody>
          <tfoot>
            <tr class="print-total-row"><td colspan="2">Total</td><td>${formatMontant(total)}</td></tr>
          </tfoot>
        </table>
      `
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function printCompteStatement(compteId) {
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

    const entries = [
      ...transactions.map((t) => ({
        date: t.dateCreation,
        libelle: t.typeTransaction === "CREDIT" ? "Entrée manuelle" : "Sortie manuelle",
        detail: t.description || t.reference || "—",
        montant: t.montant,
        positif: t.typeTransaction === "CREDIT",
      })),
      ...semences.map((s) => ({
        date: s.dateSemence,
        libelle: `Semence — ${SEMENCE_TYPE_LABELS[s.typeSemence] || s.typeSemence}`,
        detail: s.programmeLibelle,
        montant: s.montant,
        positif: true,
      })),
    ].sort((a, b) => new Date(a.date) - new Date(b.date));

    const rows = entries.length
      ? entries
          .map(
            (e) => `
        <tr>
          <td>${formatDateOnly(e.date)}</td>
          <td>${escapeHtml(e.libelle)}</td>
          <td>${escapeHtml(e.detail || "—")}</td>
          <td>${e.positif ? "+" : "−"} ${formatMontant(e.montant)}</td>
        </tr>`
          )
          .join("")
      : `<tr><td colspan="4">Aucune opération enregistrée</td></tr>`;

    triggerPrint(
      "Relevé de compte",
      `
        <table class="print-meta">
          <tr><td>Compte</td><td>${escapeHtml(compte.nomCompte)} (${escapeHtml(compte.typeCompte)})</td></tr>
          <tr><td>Solde actuel</td><td>${formatMontant(compte.solde)}</td></tr>
        </table>
        <table class="print-table">
          <thead><tr><th>Date</th><th>Opération</th><th>Détail</th><th>Montant</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      `
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function printDashboardReport() {
  try {
    const summary = await api.getDashboardSummary();

    const comptesRows = summary.comptes
      .map((c) => `<tr><td>${escapeHtml(c.nomCompte)}</td><td>${escapeHtml(c.typeCompte)}</td><td>${formatMontant(c.solde)}</td></tr>`)
      .join("");

    triggerPrint(
      "Synthèse financière",
      `
        <table class="print-meta">
          <tr><td>Solde total</td><td>${formatMontant(summary.soldeTotal)}</td></tr>
          <tr><td>Total semences</td><td>${formatMontant(summary.totalSemences)}</td></tr>
          <tr><td>Total dépenses</td><td>${formatMontant(summary.totalDepenses)}</td></tr>
          <tr><td>Comptes actifs</td><td>${summary.nombreComptes}</td></tr>
        </table>
        <table class="print-table">
          <thead><tr><th>Compte</th><th>Type</th><th>Solde</th></tr></thead>
          <tbody>${comptesRows}</tbody>
        </table>
      `
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}
