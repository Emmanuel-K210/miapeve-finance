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

/**
 * Permet d'ajouter une semence depuis un programme (comme dans l'ancienne
 * version) : charge les caches si besoin puis ouvre le drawer avec le
 * programme déjà connu (pas besoin de le resélectionner).
 */
async function openSemenceDrawerForProgramme(programmeId) {
  await ensureSemenceRefCaches();
  openSemenceDrawer(programmeId);
}

async function ensureSemenceRefCaches() {
  if (!semencesComptesCache.length) semencesComptesCache = await api.getComptes();
  if (!semencesProgrammesCache.length) semencesProgrammesCache = await api.getProgrammes();
}

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

/**
 * Formulaire volontairement simple (type, montant, programme, compte) —
 * fidèle à l'ancien formulaire de semence. Si aucun programme ou compte
 * n'existe encore, on invite à en créer un depuis sa propre page plutôt
 * que d'alourdir ce formulaire avec des panneaux de création imbriqués.
 */
function openSemenceDrawer(preselectedProgrammeId) {
  if (!semencesComptesCache.length) {
    showToast("Créez d'abord un compte (page Comptes)", "error");
    return;
  }
  if (!semencesProgrammesCache.length) {
    showToast("Créez d'abord un programme (page Programmes)", "error");
    return;
  }

  const programmeConnu = preselectedProgrammeId
    ? semencesProgrammesCache.find((p) => p.id === preselectedProgrammeId)
    : null;

  const form = openDrawer({
    title: "Nouvelle semence",
    subtitle: programmeConnu
      ? `Pour « ${programmeConnu.typeProgramme} » — ${formatDateOnly(programmeConnu.dateProgramme)}`
      : "Dîme, offrande ou autre contribution",
    formId: "semence-form",
    bodyHtml: `
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
        <input class="input" id="sem-montant" type="number" min="1" step="1" required autofocus />
      </div>

      ${
        programmeConnu
          ? ""
          : `<div class="field">
               <label>Programme</label>
               <select class="input" id="sem-programme" required>${renderProgrammeOptions()}</select>
             </div>`
      }

      <div class="field">
        <label>Compte</label>
        <select class="input" id="sem-compte" required>${renderCompteSelectOptions(semencesComptesCache)}</select>
      </div>

      <div class="field">
        <label>Date</label>
        <input class="input" id="sem-date" type="date" required value="${todayIso()}" />
      </div>

      <div class="field">
        <label>Description (optionnel)</label>
        <input class="input" id="sem-description" placeholder="ex. Culte du dimanche matin" />
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="sem-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">Enregistrer</button>
    `,
  });

  document.getElementById("sem-cancel").addEventListener("click", closeDrawer);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const programmeId = programmeConnu ? programmeConnu.id : Number(document.getElementById("sem-programme").value);

    const payload = {
      typeSemence: document.getElementById("sem-type").value,
      montant: Number(document.getElementById("sem-montant").value),
      dateSemence: document.getElementById("sem-date").value,
      description: document.getElementById("sem-description").value.trim(),
      compteId: Number(document.getElementById("sem-compte").value),
      programmeId,
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
