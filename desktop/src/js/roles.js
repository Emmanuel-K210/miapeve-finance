let rolesCatalogCache = [];

document.getElementById("new-role-btn").addEventListener("click", () => openRoleDrawer());

async function loadRoles() {
  const root = document.getElementById("roles-root");
  document.getElementById("new-role-btn").classList.toggle("hidden", !canManage("ROLES"));
  try {
    const [roles, catalog] = await Promise.all([api.getRoles(), api.getRolesCatalog()]);
    rolesCatalogCache = catalog;

    root.innerHTML = roles.map((r) => renderRoleCard(r)).join("");

    root.querySelectorAll("[data-edit-role]").forEach((btn) =>
      btn.addEventListener("click", () => {
        const role = roles.find((r) => r.id === Number(btn.dataset.editRole));
        openRoleDrawer(role);
      })
    );
    root.querySelectorAll("[data-delete-role]").forEach((btn) =>
      btn.addEventListener("click", () => deleteRole(Number(btn.dataset.deleteRole)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderRoleCard(role) {
  const canEdit = canManage("ROLES");
  return `
    <div class="role-card">
      <div class="role-card-header">
        <div>
          <div class="role-name">${escapeHtml(role.libelle)}</div>
          <div class="role-description">${escapeHtml(role.description || "")}</div>
        </div>
        <span class="badge badge-neutral">${role.nombreUtilisateurs} utilisateur${role.nombreUtilisateurs > 1 ? "s" : ""}</span>
      </div>
      <div class="role-permission-count">${role.permissions.length} permission${role.permissions.length > 1 ? "s" : ""} accordée${role.permissions.length > 1 ? "s" : ""}</div>
      ${
        canEdit
          ? `
        <div class="page-toolbar" style="margin: var(--space-2) 0 0;">
          <div></div>
          <div style="display:flex; gap: var(--space-2);">
            <button class="btn btn-ghost" data-edit-role="${role.id}">Modifier</button>
            <button class="btn btn-ghost" data-delete-role="${role.id}">Supprimer</button>
          </div>
        </div>`
          : ""
      }
    </div>
  `;
}

function openRoleDrawer(role) {
  const isEdit = Boolean(role);
  const permissions = new Set(isEdit ? role.permissions : []);

  const form = openDrawer({
    title: isEdit ? "Modifier le rôle" : "Nouveau rôle",
    subtitle: "Cochez les modules que ce rôle peut consulter ou gérer",
    formId: "role-form",
    bodyHtml: `
      <div class="form-section-title">Identification</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Nom du rôle</label>
          <input class="input" id="role-libelle" required value="${isEdit ? escapeHtml(role.libelle) : ""}" placeholder="ex. SECRETAIRE" />
        </div>
        <div class="field field-full">
          <label>Description</label>
          <input class="input" id="role-description" value="${isEdit ? escapeHtml(role.description || "") : ""}" placeholder="Optionnel" />
        </div>
      </div>

      <div class="form-section-title">Permissions</div>
      <div class="permission-matrix">
        ${rolesCatalogCache
          .map(
            (m) => `
          <div class="permission-row">
            <span class="permission-module-label">${escapeHtml(m.libelle)}</span>
            <div class="permission-toggles">
              <label class="permission-toggle">
                <input type="checkbox" data-perm="${m.viewKey}" ${permissions.has(m.viewKey) ? "checked" : ""} /> Voir
              </label>
              <label class="permission-toggle">
                <input type="checkbox" data-perm="${m.manageKey}" ${permissions.has(m.manageKey) ? "checked" : ""} /> Gérer
              </label>
            </div>
          </div>
        `
          )
          .join("")}
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="role-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">${isEdit ? "Enregistrer" : "Créer le rôle"}</button>
    `,
  });

  document.getElementById("role-cancel").addEventListener("click", closeDrawer);

  // Cocher "Gérer" implique automatiquement "Voir" (gérer sans pouvoir voir n'a pas de sens)
  form.querySelectorAll("input[data-perm$='_MANAGE']").forEach((manageBox) => {
    manageBox.addEventListener("change", () => {
      if (manageBox.checked) {
        const viewKey = manageBox.dataset.perm.replace("_MANAGE", "_VIEW");
        const viewBox = form.querySelector(`input[data-perm='${viewKey}']`);
        if (viewBox) viewBox.checked = true;
      }
    });
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const selected = Array.from(form.querySelectorAll("input[data-perm]:checked")).map((el) => el.dataset.perm);

    const payload = {
      libelle: document.getElementById("role-libelle").value.trim(),
      description: document.getElementById("role-description").value.trim(),
      permissions: selected,
    };

    try {
      if (isEdit) {
        await api.updateRole(role.id, payload);
        showToast("Rôle mis à jour");
      } else {
        await api.createRole(payload);
        showToast("Rôle créé");
      }
      closeDrawer();
      loadRoles();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

async function deleteRole(id) {
  if (!confirm("Supprimer ce rôle ?")) return;
  try {
    await api.deleteRole(id);
    showToast("Rôle supprimé");
    loadRoles();
  } catch (err) {
    showToast(err.message, "error");
  }
}
