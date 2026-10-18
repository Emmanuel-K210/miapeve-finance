document.getElementById("new-compte-btn").addEventListener("click", () => openCompteDrawer());

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
        openCompteDrawer(compte);
      })
    );
    body.querySelectorAll("[data-delete]").forEach((btn) =>
      btn.addEventListener("click", () => deleteCompte(Number(btn.dataset.delete)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

const COMPTE_TYPE_SUGGESTIONS = ["CAISSE", "BANQUE", "MOBILE MONEY", "EPARGNE"];

function openCompteDrawer(compte) {
  const isEdit = Boolean(compte);

  const form = openDrawer({
    title: isEdit ? "Modifier le compte" : "Nouveau compte",
    subtitle: isEdit ? compte.nomCompte : "Créez une caisse, une banque ou un portefeuille mobile",
    formId: "compte-form",
    bodyHtml: `
      <div class="form-section-title">Identification</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Nom du compte</label>
          <input class="input" id="compte-nom" required value="${isEdit ? escapeHtml(compte.nomCompte) : ""}" placeholder="ex. Caisse Principale" />
        </div>
        <div class="field field-full">
          <label>Type de compte</label>
          <input class="input" id="compte-type" required list="compte-type-suggestions" value="${isEdit ? escapeHtml(compte.typeCompte) : ""}" placeholder="CAISSE, BANQUE, ..." />
          <datalist id="compte-type-suggestions">
            ${COMPTE_TYPE_SUGGESTIONS.map((t) => `<option value="${t}"></option>`).join("")}
          </datalist>
        </div>
      </div>

      <div class="form-section-title">Solde</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Solde ${isEdit ? "actuel" : "initial"} (FCFA)</label>
          <input class="input" id="compte-solde" type="number" step="1" min="0" required value="${isEdit ? compte.solde : 0}" />
        </div>
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="compte-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">${isEdit ? "Enregistrer les modifications" : "Créer le compte"}</button>
    `,
  });

  document.getElementById("compte-cancel").addEventListener("click", closeDrawer);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      nomCompte: document.getElementById("compte-nom").value.trim(),
      typeCompte: document.getElementById("compte-type").value.trim().toUpperCase(),
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
      closeDrawer();
      loadComptes();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
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
