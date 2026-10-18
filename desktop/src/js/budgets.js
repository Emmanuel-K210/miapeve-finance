const MOIS_LABELS = [
  "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
  "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre",
];

let budgetsComptesCache = [];

document.getElementById("new-budget-btn").addEventListener("click", () => openBudgetDrawer());

async function loadBudgets() {
  const root = document.getElementById("budgets-root");
  try {
    const [budgets, comptes] = await Promise.all([api.getBudgets(), api.getComptes()]);
    budgetsComptesCache = comptes;

    root.innerHTML = budgets.length
      ? budgets.map(renderBudgetCard).join("")
      : `<div class="card"><div class="empty-state">Aucun budget. Créez-en un pour suivre vos dépenses par compte et par mois.</div></div>`;

    root.querySelectorAll("[data-delete-budget]").forEach((btn) =>
      btn.addEventListener("click", () => deleteBudget(Number(btn.dataset.deleteBudget)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderBudgetCard(b) {
  const { pct, tone, toneLabel } = budgetProgress(b);
  return `
    <div class="budget-card">
      <div class="budget-card-header">
        <div>
          <div class="budget-name">${escapeHtml(b.libelle)}</div>
          <div class="budget-period">${escapeHtml(b.nomCompte)} · ${MOIS_LABELS[b.mois - 1]} ${b.annee}</div>
        </div>
        <span class="badge badge-${tone === "tone-over" ? "danger" : tone === "tone-warning" ? "neutral" : "success"}">${toneLabel}</span>
      </div>
      <div class="progress-track">
        <div class="progress-fill ${tone}" style="width:${Math.min(pct, 100)}%"></div>
      </div>
      <div class="budget-figures">
        <span class="spent">${formatMontant(b.montantConsomme)}</span>
        <span class="limit">sur ${formatMontant(b.montantPrevu)}</span>
      </div>
      <div class="page-toolbar" style="margin: var(--space-3) 0 0;">
        <div></div>
        <button class="btn btn-ghost" data-delete-budget="${b.id}">Supprimer</button>
      </div>
    </div>
  `;
}

function budgetProgress(b) {
  const pct = b.montantPrevu > 0 ? (b.montantConsomme / b.montantPrevu) * 100 : 0;
  if (pct >= 100) return { pct, tone: "tone-over", toneLabel: "Dépassé" };
  if (pct >= 80) return { pct, tone: "tone-warning", toneLabel: "À surveiller" };
  return { pct, tone: "tone-ok", toneLabel: "Dans la limite" };
}

function openBudgetDrawer() {
  const now = new Date();

  const form = openDrawer({
    title: "Nouveau budget",
    subtitle: "Fixez une limite de dépense mensuelle pour un compte",
    formId: "budget-form",
    bodyHtml: `
      <div class="form-section-title">Budget</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Nom du budget</label>
          <input class="input" id="bg-libelle" required placeholder="ex. Entretien du temple" />
        </div>
      </div>

      ${quickCreateCompteBlock("bg-compte")}

      <div class="form-section-title">Montant et période</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Montant alloué (FCFA)</label>
          <input class="input" id="bg-montant" type="number" min="1" step="1" required />
        </div>
        <div class="field">
          <label>Mois</label>
          <select class="input" id="bg-mois">
            ${MOIS_LABELS.map((label, i) => `<option value="${i + 1}" ${i + 1 === now.getMonth() + 1 ? "selected" : ""}>${label}</option>`).join("")}
          </select>
        </div>
        <div class="field">
          <label>Année</label>
          <input class="input" id="bg-annee" type="number" min="2000" value="${now.getFullYear()}" />
        </div>
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="bg-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">Créer le budget</button>
    `,
  });

  document.getElementById("bg-cancel").addEventListener("click", closeDrawer);

  wireQuickCreateCompte({
    selectId: "bg-compte",
    getCache: () => budgetsComptesCache,
    setCache: (next) => (budgetsComptesCache = next),
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const compteId = document.getElementById("bg-compte").value;
    if (!compteId) {
      showToast("Sélectionnez ou créez un compte", "error");
      return;
    }

    const payload = {
      libelle: document.getElementById("bg-libelle").value.trim(),
      compteId: Number(compteId),
      montantPrevu: Number(document.getElementById("bg-montant").value),
      mois: Number(document.getElementById("bg-mois").value),
      annee: Number(document.getElementById("bg-annee").value),
    };

    try {
      await api.createBudget(payload);
      showToast("Budget créé");
      closeDrawer();
      loadBudgets();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

async function deleteBudget(id) {
  if (!confirm("Supprimer ce budget ?")) return;
  try {
    await api.deleteBudget(id);
    showToast("Budget supprimé");
    loadBudgets();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderBudgetWidget(budgets, targetId) {
  const root = document.getElementById(targetId);
  if (!budgets.length) {
    root.innerHTML = `<div class="empty-state" style="padding: var(--space-5) 0;">Aucun budget ce mois-ci</div>`;
    return;
  }

  root.innerHTML = budgets
    .slice(0, 4)
    .map((b) => {
      const { pct, tone } = budgetProgress(b);
      return `
        <div class="budget-row-compact">
          <div class="budget-row-top">
            <span>${escapeHtml(b.libelle)}</span>
            <span class="budget-pct">${Math.round(pct)}%</span>
          </div>
          <div class="progress-track" style="margin: 2px 0;">
            <div class="progress-fill ${tone}" style="width:${Math.min(pct, 100)}%"></div>
          </div>
        </div>
      `;
    })
    .join("");
}
