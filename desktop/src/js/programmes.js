document.getElementById("new-programme-btn").addEventListener("click", () => openProgrammeDrawer());

async function loadProgrammes() {
  const body = document.getElementById("programmes-body");
  document.getElementById("new-programme-btn").classList.toggle("hidden", !canManage("PROGRAMMES"));
  try {
    const programmes = await api.getProgrammes();

    body.innerHTML = programmes.length
      ? programmes.map(renderProgrammeRow).join("")
      : `<tr><td colspan="5" class="empty-state">Aucun programme. Créez-en un pour y rattacher des semences.</td></tr>`;

    body.querySelectorAll("[data-delete-programme]").forEach((btn) =>
      btn.addEventListener("click", () => deleteProgramme(Number(btn.dataset.deleteProgramme)))
    );
    body.querySelectorAll("[data-add-semence]").forEach((btn) =>
      btn.addEventListener("click", () => openSemenceDrawerForProgramme(Number(btn.dataset.addSemence)))
    );
    body.querySelectorAll("[data-print-programme]").forEach((btn) =>
      btn.addEventListener("click", () => printProgrammeReceipt(Number(btn.dataset.printProgramme)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderProgrammeRow(p) {
  return `
    <tr>
      <td><span class="badge badge-neutral">${escapeHtml(p.typeProgramme)}</span></td>
      <td>${formatDateOnly(p.dateProgramme)}</td>
      <td>${formatHeure(p.heureDebut)}</td>
      <td>${formatHeure(p.heureFin)}</td>
      <td style="text-align:right">
        ${canManage("SEMENCES") ? `<button class="btn btn-ghost" data-add-semence="${p.id}">Ajouter une semence</button>` : ""}
        <button class="btn btn-ghost" data-print-programme="${p.id}">Imprimer</button>
        ${canManage("PROGRAMMES") ? `<button class="btn btn-ghost" data-delete-programme="${p.id}">Supprimer</button>` : ""}
      </td>
    </tr>
  `;
}

/**
 * Comme dans l'ancienne version : on peut saisir les semences collectées
 * directement pendant la création du programme (une ligne par semence,
 * ajoutée dans une petite liste avant validation), plutôt que de devoir
 * créer le programme puis revenir les ajouter une par une.
 */
async function openProgrammeDrawer() {
  const lignesSemences = [];
  const peutAjouterSemences = canManage("SEMENCES");
  let comptesDisponibles = [];

  if (peutAjouterSemences) {
    try {
      comptesDisponibles = await api.getComptes();
    } catch (err) {
      comptesDisponibles = [];
    }
  }

  const form = openDrawer({
    title: "Nouveau programme",
    subtitle: "Un culte, une collecte ou un évènement de l'église",
    formId: "programme-form",
    bodyHtml: `
      <div class="field">
        <label>Type de programme</label>
        <select class="input" id="pr-type">
          ${PROGRAMME_TYPES.map((t) => `<option value="${t}">${t}</option>`).join("")}
        </select>
      </div>
      <div class="field">
        <label>Date</label>
        <input class="input" id="pr-date" type="date" required value="${todayIso()}" />
      </div>
      <div style="display:flex; gap: var(--space-3);">
        <div class="field" style="flex:1">
          <label>Heure début</label>
          <input class="input" id="pr-debut" type="time" value="09:00" required />
        </div>
        <div class="field" style="flex:1">
          <label>Heure fin</label>
          <input class="input" id="pr-fin" type="time" value="11:00" required />
        </div>
      </div>

      ${
        peutAjouterSemences && comptesDisponibles.length
          ? `
        <div class="form-section-title">Semences collectées (optionnel)</div>
        <div class="form-grid">
          <div class="field">
            <label>Type</label>
            <select class="input" id="ls-type">
              ${Object.entries(SEMENCE_TYPE_LABELS).map(([v, l]) => `<option value="${v}">${l}</option>`).join("")}
            </select>
          </div>
          <div class="field">
            <label>Montant</label>
            <input class="input" id="ls-montant" type="number" min="1" step="1" placeholder="FCFA" />
          </div>
          <div class="field field-full">
            <label>Compte</label>
            <select class="input" id="ls-compte">${renderCompteSelectOptions(comptesDisponibles)}</select>
          </div>
        </div>
        <button type="button" class="btn btn-secondary btn-block" id="ls-ajouter">Ajouter cette semence</button>

        <div id="ls-liste" style="margin-top: var(--space-3);"></div>
        <div class="inline-action-row" style="margin-top: var(--space-2); font-weight:700;">
          <span>Total</span>
          <span id="ls-total">0 FCFA</span>
        </div>
      `
          : ""
      }
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="pr-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">Créer le programme</button>
    `,
  });

  document.getElementById("pr-cancel").addEventListener("click", closeDrawer);

  function renderLignes() {
    const liste = document.getElementById("ls-liste");
    if (!liste) return;

    liste.innerHTML = lignesSemences.length
      ? lignesSemences
          .map(
            (l, i) => `
        <div class="budget-row-compact">
          <div class="budget-row-top">
            <span>${escapeHtml(SEMENCE_TYPE_LABELS[l.typeSemence])}</span>
            <span>${formatMontant(l.montant)}</span>
          </div>
          <div class="inline-action-row">
            <span style="font-size: var(--font-size-xs); color: var(--color-text-subtle);">${escapeHtml(l.nomCompte)}</span>
            <button type="button" class="btn btn-ghost" data-remove-ligne="${i}">Retirer</button>
          </div>
        </div>
      `
          )
          .join("")
      : "";

    liste.querySelectorAll("[data-remove-ligne]").forEach((btn) =>
      btn.addEventListener("click", () => {
        lignesSemences.splice(Number(btn.dataset.removeLigne), 1);
        renderLignes();
      })
    );

    const total = lignesSemences.reduce((sum, l) => sum + l.montant, 0);
    document.getElementById("ls-total").textContent = formatMontant(total);
  }

  const ajouterBtn = document.getElementById("ls-ajouter");
  if (ajouterBtn) {
    ajouterBtn.addEventListener("click", () => {
      const typeSemence = document.getElementById("ls-type").value;
      const montant = Number(document.getElementById("ls-montant").value);
      const compteId = Number(document.getElementById("ls-compte").value);
      const compte = comptesDisponibles.find((c) => c.id === compteId);

      if (!montant || montant <= 0) {
        showToast("Indiquez un montant valide", "error");
        return;
      }
      if (!compte) {
        showToast("Sélectionnez un compte", "error");
        return;
      }

      lignesSemences.push({ typeSemence, montant, compteId, nomCompte: compte.nomCompte });
      document.getElementById("ls-montant").value = "";
      document.getElementById("ls-montant").focus();
      renderLignes();
    });
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      typeProgramme: document.getElementById("pr-type").value,
      dateProgramme: document.getElementById("pr-date").value,
      heureDebut: document.getElementById("pr-debut").value,
      heureFin: document.getElementById("pr-fin").value,
    };

    try {
      const programme = await api.createProgramme(payload);

      for (const ligne of lignesSemences) {
        await api.createSemence({
          typeSemence: ligne.typeSemence,
          montant: ligne.montant,
          dateSemence: payload.dateProgramme,
          description: "",
          compteId: ligne.compteId,
          programmeId: programme.id,
        });
      }

      showToast(lignesSemences.length ? "Programme et semences enregistrés" : "Programme créé");
      closeDrawer();
      loadProgrammes();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

async function deleteProgramme(id) {
  if (!confirm("Supprimer ce programme ?")) return;
  try {
    await api.deleteProgramme(id);
    showToast("Programme supprimé");
    loadProgrammes();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function formatHeure(value) {
  if (!value) return "—";
  return value.slice(0, 5);
}
