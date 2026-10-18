const SEMENCE_TYPE_LABELS = {
  DIMES: "Dîmes",
  OFFRANDE: "Offrande",
  LIBERALE: "Offrande libérale",
  PREMICE: "Prémices",
  AUTRES: "Autres",
};

const PROGRAMME_TYPES = [
  "COMMANDER AU MOIS",
  "COMMANDER À LA SAISON",
  "CULTE DU JOUR DU SEIGNEUR",
  "COMMANDER À L'ANNÉE",
  "CULTE D'ACTION DE GRACE",
];

let semencesComptesCache = [];
let semencesProgrammesCache = [];

document.getElementById("new-semence-btn").addEventListener("click", () => openSemenceModal());
document.getElementById("semence-type-filter").addEventListener("change", loadSemences);

async function loadSemences() {
  const body = document.getElementById("semences-body");

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
        <button class="btn btn-ghost" data-delete-semence="${s.id}">Supprimer</button>
      </td>
    </tr>
  `;
}

function openSemenceModal() {
  const root = document.getElementById("semence-modal-root");

  root.innerHTML = `
    <div class="modal-overlay" id="semence-modal-overlay">
      <form class="modal" id="semence-form">
        <div class="modal-header">
          <h3>Nouvelle semence</h3>
        </div>

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

        <div class="field">
          <label>Date</label>
          <input class="input" id="sem-date" type="date" required value="${todayIso()}" />
        </div>

        <div class="field">
          <label>Compte à créditer</label>
          <select class="input" id="sem-compte" required>
            ${semencesComptesCache.map((c) => `<option value="${c.id}">${escapeHtml(c.nomCompte)}</option>`).join("")}
          </select>
        </div>

        <div class="field">
          <label>Programme</label>
          <div style="display:flex; gap: var(--space-2);">
            <select class="input" id="sem-programme" required style="flex:1">
              ${renderProgrammeOptions()}
            </select>
            <button type="button" class="btn btn-secondary" id="sem-new-programme-toggle">Nouveau</button>
          </div>
        </div>

        <div class="field hidden" id="sem-new-programme-fields">
          <div class="card" style="background: var(--color-surface-muted); box-shadow:none; padding: var(--space-4);">
            <div class="field">
              <label>Type de programme</label>
              <select class="input" id="np-type">
                ${PROGRAMME_TYPES.map((t) => `<option value="${t}">${t}</option>`).join("")}
              </select>
            </div>
            <div class="field">
              <label>Date du programme</label>
              <input class="input" id="np-date" type="date" value="${todayIso()}" />
            </div>
            <div style="display:flex; gap: var(--space-3);">
              <div class="field" style="flex:1">
                <label>Heure début</label>
                <input class="input" id="np-debut" type="time" value="09:00" />
              </div>
              <div class="field" style="flex:1">
                <label>Heure fin</label>
                <input class="input" id="np-fin" type="time" value="11:00" />
              </div>
            </div>
            <button type="button" class="btn btn-primary btn-block" id="np-create">Créer ce programme</button>
          </div>
        </div>

        <div class="field">
          <label>Description</label>
          <input class="input" id="sem-description" placeholder="Optionnel" />
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="sem-cancel">Annuler</button>
          <button type="submit" class="btn btn-primary">Enregistrer</button>
        </div>
      </form>
    </div>
  `;

  document.getElementById("sem-cancel").addEventListener("click", closeSemenceModal);
  document.getElementById("semence-modal-overlay").addEventListener("click", (e) => {
    if (e.target.id === "semence-modal-overlay") closeSemenceModal();
  });

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

  document.getElementById("semence-form").addEventListener("submit", async (e) => {
    e.preventDefault();

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
      closeSemenceModal();
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

function closeSemenceModal() {
  document.getElementById("semence-modal-root").innerHTML = "";
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
