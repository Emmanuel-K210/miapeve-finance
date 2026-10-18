const SEMENCE_TYPE_LABELS = {
  DIMES: "Dîmes",
  OFFRANDE: "Offrande",
  LIBERALE: "Offrande libérale",
  PREMICE: "Prémices",
  AUTRES: "Autres",
};

let semencesComptesCache = [];
let semencesProgrammesCache = [];

document.getElementById("new-semence-btn").addEventListener("click", () => openSemenceDrawer());
document.getElementById("semence-type-filter").addEventListener("change", loadSemences);

async function loadSemences() {
  const body = document.getElementById("semences-body");
  document.getElementById("new-semence-btn").classList.toggle("hidden", !canManage("SEMENCES"));

  try {
    const [semences, comptes, programmes] = await Promise.all([
      api.getSemences(),
      api.getComptes(),
      api.getProgrammes(),
    ]);
    semencesComptesCache = comptes;
    semencesProgrammesCache = programmes;

    const typeFilter = document.getElementById("semence-type-filter").value;
    const filtered = typeFilter ? semences.filter((s) => s.typeSemence === typeFilter) : semences;

    body.innerHTML = filtered.length
      ? filtered.map(renderSemenceRow).join("")
      : `<tr><td colspan="7" class="empty-state">Aucune semence enregistrée</td></tr>`;

    body.querySelectorAll("[data-delete-semence]").forEach((btn) =>
      btn.addEventListener("click", () => deleteSemence(Number(btn.dataset.deleteSemence)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderSemenceRow(s) {
  return `
    <tr>
      <td>${formatDateOnly(s.dateSemence)}</td>
      <td><span class="badge badge-neutral">${escapeHtml(SEMENCE_TYPE_LABELS[s.typeSemence] || s.typeSemence)}</span></td>
      <td>${escapeHtml(s.programmeLibelle)}</td>
      <td>${escapeHtml(s.nomCompte)}</td>
      <td>${escapeHtml(s.description || "—")}</td>
      <td style="text-align:right; font-weight:700;" class="amount-positive">+ ${formatMontant(s.montant)}</td>
      <td style="text-align:right">
        ${canManage("SEMENCES") ? `<button class="btn btn-ghost" data-delete-semence="${s.id}">Supprimer</button>` : ""}
      </td>
    </tr>
  `;
}

function openSemenceDrawer() {
  const form = openDrawer({
    title: "Nouvelle semence",
    subtitle: "Dîme, offrande ou autre contribution rattachée à un programme",
    formId: "semence-form",
    bodyHtml: `
      <div class="form-section-title">Type et montant</div>
      <div class="form-grid">
        <div class="field">
          <label>Type</label>
          <select class="input" id="sem-type" required>
            ${Object.entries(SEMENCE_TYPE_LABELS)
              .map(([value, label]) => `<option value="${value}">${label}</option>`)
              .join("")}
          </select>
        </div>
        <div class="field">
          <label>Montant (FCFA)</label>
          <input class="input" id="sem-montant" type="number" min="1" step="1" required />
        </div>
        <div class="field field-full">
          <label>Date</label>
          <input class="input" id="sem-date" type="date" required value="${todayIso()}" />
        </div>
      </div>

      <div class="form-section-title">Compte à créditer</div>
      ${quickCreateCompteBlock("sem-compte")}

      <div class="form-section-title">Programme</div>
      <div class="inline-action-row">
        <label style="margin:0;">Associer à un programme existant ou en créer un</label>
        <button type="button" class="link-btn" id="sem-new-programme-toggle">+ créer un programme</button>
      </div>
      <div class="field field-full" style="margin-top: var(--space-2);">
        <select class="input" id="sem-programme" required>
          ${renderProgrammeOptions()}
        </select>
      </div>

      <div class="quick-panel hidden" id="sem-new-programme-fields">
        <div class="form-grid">
          <div class="field field-full">
            <label>Type de programme</label>
            <select class="input" id="np-type">
              ${PROGRAMME_TYPES.map((t) => `<option value="${t}">${t}</option>`).join("")}
            </select>
          </div>
          <div class="field">
            <label>Date du programme</label>
            <input class="input" id="np-date" type="date" value="${todayIso()}" />
          </div>
          <div></div>
          <div class="field">
            <label>Heure début</label>
            <input class="input" id="np-debut" type="time" value="09:00" />
          </div>
          <div class="field">
            <label>Heure fin</label>
            <input class="input" id="np-fin" type="time" value="11:00" />
          </div>
        </div>
        <button type="button" class="btn btn-secondary btn-block" id="np-create">Créer ce programme</button>
      </div>

      <div class="form-section-title">Description</div>
      <div class="form-grid">
        <div class="field field-full">
          <input class="input" id="sem-description" placeholder="Optionnel" />
        </div>
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="sem-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">Enregistrer</button>
    `,
  });

  document.getElementById("sem-cancel").addEventListener("click", closeDrawer);

  document.getElementById("sem-new-programme-toggle").addEventListener("click", () => {
    document.getElementById("sem-new-programme-fields").classList.toggle("hidden");
  });

  document.getElementById("np-create").addEventListener("click", async () => {
    const payload = {
      typeProgramme: document.getElementById("np-type").value,
      dateProgramme: document.getElementById("np-date").value,
      heureDebut: document.getElementById("np-debut").value,
      heureFin: document.getElementById("np-fin").value,
    };
    try {
      const programme = await api.createProgramme(payload);
      semencesProgrammesCache.push(programme);
      const select = document.getElementById("sem-programme");
      select.innerHTML = renderProgrammeOptions();
      select.value = programme.id;
      document.getElementById("sem-new-programme-fields").classList.add("hidden");
      showToast("Programme créé");
    } catch (err) {
      showToast(err.message, "error");
    }
  });

  wireQuickCreateCompte({
    selectId: "sem-compte",
    getCache: () => semencesComptesCache,
    setCache: (next) => (semencesComptesCache = next),
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const compteId = document.getElementById("sem-compte").value;
    if (!compteId) {
      showToast("Sélectionnez ou créez un compte", "error");
      return;
    }

    if (!semencesProgrammesCache.length) {
      showToast("Créez d'abord un programme pour cette semence", "error");
      return;
    }

    const payload = {
      typeSemence: document.getElementById("sem-type").value,
      montant: Number(document.getElementById("sem-montant").value),
      dateSemence: document.getElementById("sem-date").value,
      description: document.getElementById("sem-description").value.trim(),
      compteId: Number(document.getElementById("sem-compte").value),
      programmeId: Number(document.getElementById("sem-programme").value),
    };

    try {
      await api.createSemence(payload);
      showToast("Semence enregistrée");
      closeDrawer();
      loadSemences();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

function renderProgrammeOptions() {
  if (!semencesProgrammesCache.length) {
    return `<option value="">Aucun programme — créez-en un</option>`;
  }
  return semencesProgrammesCache
    .map((p) => `<option value="${p.id}">${escapeHtml(p.typeProgramme)} — ${formatDateOnly(p.dateProgramme)}</option>`)
    .join("");
}

async function deleteSemence(id) {
  if (!confirm("Supprimer cette semence ? Le solde du compte sera recalculé.")) return;
  try {
    await api.deleteSemence(id);
    showToast("Semence supprimée");
    loadSemences();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function formatDateOnly(value) {
  if (!value) return "—";
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "medium" }).format(new Date(value));
}
