let depensesComptesCache = [];

document.getElementById("new-depense-btn").addEventListener("click", () => openDepenseDrawer());

async function loadDepenses() {
  const body = document.getElementById("depenses-body");
  document.getElementById("new-depense-btn").classList.toggle("hidden", !canManage("DEPENSES"));

  try {
    const [depenses, comptes] = await Promise.all([api.getDepenses(), api.getComptes()]);
    depensesComptesCache = comptes;

    body.innerHTML = depenses.length
      ? depenses.map(renderDepenseRow).join("")
      : `<tr><td colspan="5" class="empty-state">Aucune dépense enregistrée</td></tr>`;

    body.querySelectorAll("[data-delete-depense]").forEach((btn) =>
      btn.addEventListener("click", () => deleteDepense(Number(btn.dataset.deleteDepense)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderDepenseRow(d) {
  return `
    <tr>
      <td>${formatDateOnly(d.dateDepense)}</td>
      <td>${escapeHtml(d.libelle)}</td>
      <td>${escapeHtml(d.nomCompte)}</td>
      <td style="text-align:right; font-weight:700;" class="amount-negative">− ${formatMontant(d.montant)}</td>
      <td style="text-align:right">
        ${canManage("DEPENSES") ? `<button class="btn btn-ghost" data-delete-depense="${d.id}">Supprimer</button>` : ""}
      </td>
    </tr>
  `;
}

function openDepenseDrawer() {
  if (!depensesComptesCache.length) {
    showToast("Créez d'abord un compte (page Comptes)", "error");
    return;
  }

  const form = openDrawer({
    title: "Nouvelle dépense",
    subtitle: "Une sortie d'argent débitée d'un compte",
    formId: "depense-form",
    bodyHtml: `
      <div class="field">
        <label>Libellé</label>
        <input class="input" id="dp-libelle" required placeholder="ex. Facture électricité" autofocus />
      </div>

      <div class="field">
        <label>Montant (FCFA)</label>
        <input class="input" id="dp-montant" type="number" min="1" step="1" required />
      </div>

      <div class="field">
        <label>Compte à débiter</label>
        <select class="input" id="dp-compte" required>${renderCompteSelectOptions(depensesComptesCache)}</select>
      </div>

      <div class="field">
        <label>Date</label>
        <input class="input" id="dp-date" type="date" required value="${todayIso()}" />
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="dp-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">Enregistrer</button>
    `,
  });

  document.getElementById("dp-cancel").addEventListener("click", closeDrawer);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      libelle: document.getElementById("dp-libelle").value.trim(),
      montant: Number(document.getElementById("dp-montant").value),
      dateDepense: document.getElementById("dp-date").value,
      compteId: Number(document.getElementById("dp-compte").value),
    };

    try {
      await api.createDepense(payload);
      showToast("Dépense enregistrée");
      closeDrawer();
      loadDepenses();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

async function deleteDepense(id) {
  if (!confirm("Supprimer cette dépense ? Le solde du compte sera recrédité.")) return;
  try {
    await api.deleteDepense(id);
    showToast("Dépense supprimée");
    loadDepenses();
  } catch (err) {
    showToast(err.message, "error");
  }
}
