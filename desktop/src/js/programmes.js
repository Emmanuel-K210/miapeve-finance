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
        ${canManage("PROGRAMMES") ? `<button class="btn btn-ghost" data-delete-programme="${p.id}">Supprimer</button>` : ""}
      </td>
    </tr>
  `;
}

function openProgrammeDrawer() {
  const form = openDrawer({
    title: "Nouveau programme",
    subtitle: "Un culte, une collecte ou un évènement de l'église",
    formId: "programme-form",
    bodyHtml: `
      <div class="form-section-title">Programme</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Type de programme</label>
          <select class="input" id="pr-type">
            ${PROGRAMME_TYPES.map((t) => `<option value="${t}">${t}</option>`).join("")}
          </select>
        </div>
        <div class="field field-full">
          <label>Date</label>
          <input class="input" id="pr-date" type="date" required value="${todayIso()}" />
        </div>
        <div class="field">
          <label>Heure début</label>
          <input class="input" id="pr-debut" type="time" value="09:00" required />
        </div>
        <div class="field">
          <label>Heure fin</label>
          <input class="input" id="pr-fin" type="time" value="11:00" required />
        </div>
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="pr-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">Créer le programme</button>
    `,
  });

  document.getElementById("pr-cancel").addEventListener("click", closeDrawer);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      typeProgramme: document.getElementById("pr-type").value,
      dateProgramme: document.getElementById("pr-date").value,
      heureDebut: document.getElementById("pr-debut").value,
      heureFin: document.getElementById("pr-fin").value,
    };

    try {
      await api.createProgramme(payload);
      showToast("Programme créé");
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
