let transactionsComptesCache = [];

document.getElementById("new-transaction-btn").addEventListener("click", () => openTransactionModal());
document.getElementById("transaction-compte-filter").addEventListener("change", loadTransactions);

async function loadTransactions() {
  const body = document.getElementById("transactions-body");
  const filterSelect = document.getElementById("transaction-compte-filter");

  try {
    if (transactionsComptesCache.length === 0) {
      transactionsComptesCache = await api.getComptes();
      fillCompteOptions(filterSelect, transactionsComptesCache, "Tous les comptes");
    }

    const compteId = filterSelect.value || undefined;
    const transactions = await api.getTransactions(compteId);

    body.innerHTML = transactions.length
      ? transactions.map(renderTransactionRow).join("")
      : `<tr><td colspan="7" class="empty-state">Aucune transaction pour le moment</td></tr>`;

    body.querySelectorAll("[data-delete-tx]").forEach((btn) =>
      btn.addEventListener("click", () => deleteTransaction(Number(btn.dataset.deleteTx)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderTransactionRow(t) {
  const isCredit = t.typeTransaction === "CREDIT";
  const icon = isCredit ? Icons.arrowUpCircle() : Icons.arrowDownCircle();
  const tone = isCredit ? "tone-success" : "tone-danger";
  const signe = isCredit ? "+" : "−";

  return `
    <tr>
      <td><span class="${tone}" style="display:inline-flex">${icon}</span></td>
      <td>${formatDate(t.dateCreation)}</td>
      <td>${escapeHtml(t.nomCompte)}</td>
      <td>${escapeHtml(t.description || "—")}</td>
      <td>${escapeHtml(t.reference || "—")}</td>
      <td style="text-align:right; font-weight:700;" class="${isCredit ? "amount-positive" : "amount-negative"}">
        ${signe} ${formatMontant(t.montant)}
      </td>
      <td style="text-align:right">
        <button class="btn btn-ghost" data-delete-tx="${t.id}">Supprimer</button>
      </td>
    </tr>
  `;
}

function openTransactionModal() {
  const root = document.getElementById("transaction-modal-root");
  const compteOptions = transactionsComptesCache
    .map((c) => `<option value="${c.id}">${escapeHtml(c.nomCompte)}</option>`)
    .join("");

  root.innerHTML = `
    <div class="modal-overlay" id="transaction-modal-overlay">
      <form class="modal" id="transaction-form">
        <div class="modal-header">
          <h3>Nouvelle transaction</h3>
        </div>

        <div class="field">
          <label>Compte</label>
          <select class="input" id="tx-compte" required>${compteOptions}</select>
        </div>

        <div class="field">
          <label>Type</label>
          <div class="type-toggle">
            <label class="type-option">
              <input type="radio" name="tx-type" value="CREDIT" checked />
              <span>${Icons.arrowUpCircle()} Entrée (crédit)</span>
            </label>
            <label class="type-option">
              <input type="radio" name="tx-type" value="DEBIT" />
              <span>${Icons.arrowDownCircle()} Sortie (débit)</span>
            </label>
          </div>
        </div>

        <div class="field">
          <label>Montant (FCFA)</label>
          <input class="input" id="tx-montant" type="number" min="1" step="1" required />
        </div>

        <div class="field">
          <label>Description</label>
          <input class="input" id="tx-description" placeholder="Motif de la transaction" />
        </div>

        <div class="field">
          <label>Référence</label>
          <input class="input" id="tx-reference" placeholder="Optionnel" />
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="tx-cancel">Annuler</button>
          <button type="submit" class="btn btn-primary">Enregistrer</button>
        </div>
      </form>
    </div>
  `;

  document.getElementById("tx-cancel").addEventListener("click", closeTransactionModal);
  document.getElementById("transaction-modal-overlay").addEventListener("click", (e) => {
    if (e.target.id === "transaction-modal-overlay") closeTransactionModal();
  });

  document.getElementById("transaction-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      typeTransaction: document.querySelector("input[name='tx-type']:checked").value,
      montant: Number(document.getElementById("tx-montant").value),
      compteId: Number(document.getElementById("tx-compte").value),
      description: document.getElementById("tx-description").value.trim(),
      reference: document.getElementById("tx-reference").value.trim(),
    };

    try {
      await api.createTransaction(payload);
      showToast("Transaction enregistrée");
      closeTransactionModal();
      transactionsComptesCache = [];
      loadTransactions();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

function closeTransactionModal() {
  document.getElementById("transaction-modal-root").innerHTML = "";
}

async function deleteTransaction(id) {
  if (!confirm("Supprimer cette transaction ? Le solde du compte sera recalculé.")) return;
  try {
    await api.deleteTransaction(id);
    showToast("Transaction supprimée");
    transactionsComptesCache = [];
    loadTransactions();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function fillCompteOptions(select, comptes, placeholder) {
  const current = select.value;
  select.innerHTML =
    `<option value="">${placeholder}</option>` +
    comptes.map((c) => `<option value="${c.id}">${escapeHtml(c.nomCompte)}</option>`).join("");
  select.value = current;
}

function formatDate(isoString) {
  if (!isoString) return "—";
  const date = new Date(isoString);
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "short", timeStyle: "short" }).format(date);
}
