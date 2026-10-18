let utilisateursRolesCache = [];

document.getElementById("new-utilisateur-btn").addEventListener("click", () => openUtilisateurDrawer());

async function loadUtilisateurs() {
  const body = document.getElementById("utilisateurs-body");
  document.getElementById("new-utilisateur-btn").classList.toggle("hidden", !canManage("UTILISATEURS"));
  try {
    const [utilisateurs, roles] = await Promise.all([api.getUtilisateurs(), api.getRoles()]);
    utilisateursRolesCache = roles;
    const moi = getCurrentUser();

    body.innerHTML = utilisateurs
      .map((u) => renderUtilisateurRow(u, moi))
      .join("");

    body.querySelectorAll("[data-edit-user]").forEach((btn) =>
      btn.addEventListener("click", () => {
        const utilisateur = utilisateurs.find((u) => u.id === Number(btn.dataset.editUser));
        openUtilisateurDrawer(utilisateur);
      })
    );
    body.querySelectorAll("[data-reset-password]").forEach((btn) =>
      btn.addEventListener("click", () => resetPasswordPrompt(Number(btn.dataset.resetPassword)))
    );
    body.querySelectorAll("[data-delete-user]").forEach((btn) =>
      btn.addEventListener("click", () => deleteUtilisateur(Number(btn.dataset.deleteUser)))
    );
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderUtilisateurRow(u, moi) {
  const isSelf = moi && moi.id === u.id;
  const canEdit = canManage("UTILISATEURS");

  return `
    <tr>
      <td>
        <div style="font-weight:600">${escapeHtml(u.prenom)} ${escapeHtml(u.nom)}</div>
        ${isSelf ? `<div style="font-size: var(--font-size-xs); color: var(--color-text-subtle);">Vous</div>` : ""}
      </td>
      <td>${escapeHtml(u.nomUtilisateur)}</td>
      <td><span class="badge badge-neutral">${escapeHtml(u.role)}</span></td>
      <td>${u.actif ? `<span class="badge badge-success">Actif</span>` : `<span class="badge badge-danger">Désactivé</span>`}</td>
      <td style="text-align:right">
        ${
          canEdit
            ? `
          <button class="btn btn-ghost" data-edit-user="${u.id}">Modifier</button>
          <button class="btn btn-ghost" data-reset-password="${u.id}">Mot de passe</button>
          ${!isSelf ? `<button class="btn btn-ghost" data-delete-user="${u.id}">Supprimer</button>` : ""}
        `
            : ""
        }
      </td>
    </tr>
  `;
}

function openUtilisateurDrawer(utilisateur) {
  const isEdit = Boolean(utilisateur);

  const form = openDrawer({
    title: isEdit ? "Modifier l'utilisateur" : "Nouvel utilisateur",
    subtitle: isEdit ? `${utilisateur.prenom} ${utilisateur.nom}` : "Donnez accès à l'application à un membre de l'équipe",
    formId: "utilisateur-form",
    bodyHtml: `
      <div class="form-section-title">Identité</div>
      <div class="form-grid">
        <div class="field">
          <label>Prénom</label>
          <input class="input" id="u-prenom" required value="${isEdit ? escapeHtml(utilisateur.prenom) : ""}" />
        </div>
        <div class="field">
          <label>Nom</label>
          <input class="input" id="u-nom" required value="${isEdit ? escapeHtml(utilisateur.nom) : ""}" />
        </div>
        <div class="field field-full">
          <label>Nom d'utilisateur</label>
          <input class="input" id="u-nomutilisateur" required value="${isEdit ? escapeHtml(utilisateur.nomUtilisateur) : ""}" ${isEdit ? "disabled" : ""} />
        </div>
        ${
          !isEdit
            ? `
          <div class="field field-full">
            <label>Mot de passe initial</label>
            <input class="input" id="u-motdepasse" type="password" required minlength="4" />
          </div>`
            : ""
        }
      </div>

      <div class="form-section-title">Rôle et statut</div>
      <div class="form-grid">
        <div class="field field-full">
          <label>Rôle</label>
          <select class="input" id="u-role" required>
            ${utilisateursRolesCache.map((r) => `<option value="${r.id}" ${isEdit && utilisateur.roleId === r.id ? "selected" : ""}>${escapeHtml(r.libelle)}</option>`).join("")}
          </select>
        </div>
        <div class="field field-full">
          <label class="permission-toggle" style="font-size: var(--font-size-sm);">
            <input type="checkbox" id="u-actif" ${!isEdit || utilisateur.actif ? "checked" : ""} /> Compte actif
          </label>
        </div>
      </div>
    `,
    footerHtml: `
      <button type="button" class="btn btn-secondary" id="u-cancel">Annuler</button>
      <button type="submit" class="btn btn-primary">${isEdit ? "Enregistrer" : "Créer l'utilisateur"}</button>
    `,
  });

  document.getElementById("u-cancel").addEventListener("click", closeDrawer);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      nom: document.getElementById("u-nom").value.trim(),
      prenom: document.getElementById("u-prenom").value.trim(),
      nomUtilisateur: document.getElementById("u-nomutilisateur").value.trim(),
      motDePasse: isEdit ? "" : document.getElementById("u-motdepasse").value,
      roleId: Number(document.getElementById("u-role").value),
      actif: document.getElementById("u-actif").checked,
    };

    try {
      if (isEdit) {
        await api.updateUtilisateur(utilisateur.id, payload);
        showToast("Utilisateur mis à jour");
      } else {
        await api.createUtilisateur(payload);
        showToast("Utilisateur créé");
      }
      closeDrawer();
      loadUtilisateurs();
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}

async function resetPasswordPrompt(id) {
  const nouveau = prompt("Nouveau mot de passe pour cet utilisateur (4 caractères minimum) :");
  if (!nouveau) return;
  try {
    await api.resetPassword(id, nouveau);
    showToast("Mot de passe réinitialisé");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function deleteUtilisateur(id) {
  if (!confirm("Supprimer cet utilisateur ?")) return;
  try {
    await api.deleteUtilisateur(id);
    showToast("Utilisateur supprimé");
    loadUtilisateurs();
  } catch (err) {
    showToast(err.message, "error");
  }
}
