document.getElementById("new-compte-btn").addEventListener("click", () => openCompteModal());

async function loadComptes() {
  const body = document.getElementById("comptes-body");
  try {
    const comptes = await api.getComptes();
    body.innerHTML = comptes.length
      ? comptes
          .map(
            (c) => `
        <tr>
          <td>${escapeHtml(c.nomCompte)}</td>
          <td><span class="badge badge-neutral">${escapeHtml(c.typeCompte)}</span></td>
          <td>${formatMontant(c.solde)}</td>
          <td style="text-align:right">
            <button class="btn btn-ghost" data-edit="${c.id}">Modifier</button>
            <button class="btn btn-ghost" data-delete="${c.id}">Supprimer</button>
          </td>
        </tr>`
          )
          .join("")
      : `<tr><td colspan="4" class="empty-state">Aucun compte. Créez-en un pour commencer.</td></tr>`;

    body.querySelectorAll("[data-edit]").forEach((btn) =>
      btn.addEventListener("click", () => {
        const compte = comptes.find((c) => c.id === Number(btn.dataset.edit));
        openCompteModal(compte);
      })
    );
    body.querySelectorAll("[data-delete]").forEach((btn) =>
      btn.addEventListener("click", () => deleteCompte(Number(btn.dataset.delete)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function openCompteModal(compte) {
  const isEdit = Boolean(compte);
  const root = document.getElementById("compte-modal-root");
  root.innerHTML = `
    <div class="modal-overlay" id="compte-modal-overlay">
      <form class="modal" id="compte-form">
        <div class="modal-header">
          <h3>${isEdit ? "Modifier le compte" : "Nouveau compte"}</h3>
        </div>
        <div class="field">
          <label>Nom du compte</label>
          <input class="input" id="compte-nom" required value="${isEdit ? escapeHtml(compte.nomCompte) : ""}" />
        </div>
        <div class="field">
          <label>Type de compte</label>
          <input class="input" id="compte-type" required value="${isEdit ? escapeHtml(compte.typeCompte) : ""}" placeholder="CAISSE, BANQUE, ..." />
        </div>
        <div class="field">
          <label>Solde</label>
          <input class="input" id="compte-solde" type="number" step="0.01" required value="${isEdit ? compte.solde : 0}" />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="compte-cancel">Annuler</button>
          <button type="submit" class="btn btn-primary">${isEdit ? "Enregistrer" : "Créer"}</button>
        </div>
      </form>
    </div>
  `;

  document.getElementById("compte-cancel").addEventListener("click", closeCompteModal);
  document.getElementById("compte-modal-overlay").addEventListener("click", (e) => {
    if (e.target.id === "compte-modal-overlay") closeCompteModal();
  });

  document.getElementById("compte-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      nomCompte: document.getElementById("compte-nom").value.trim(),
      typeCompte: document.getElementById("compte-type").value.trim(),
      solde: Number(document.getElementById("compte-solde").value),
    };

    try {
      if (isEdit) {
        await api.updateCompte(compte.id, payload);
        showToast("Compte mis à jour");
      } else {
        await api.createCompte(payload);
        showToast("Compte créé");
      }
      closeCompteModal();
      loadComptes();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

function closeCompteModal() {
  document.getElementById("compte-modal-root").innerHTML = "";
}

async function deleteCompte(id) {
  if (!confirm("Supprimer ce compte ?")) return;
  try {
    await api.deleteCompte(id);
    showToast("Compte supprimé");
    loadComptes();
  } catch (err) {
    showToast(err.message, "error");
  }
}
