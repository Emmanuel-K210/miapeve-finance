let parametresWired = false;

function loadParametres() {
  const user = getCurrentUser();
  if (user) {
    document.getElementById("profile-avatar").textContent = (user.prenom?.[0] || "?").toUpperCase();
    document.getElementById("profile-name").textContent = `${user.prenom} ${user.nom}`;
    document.getElementById("profile-username").textContent = `@${user.nomUtilisateur}`;
    document.getElementById("profile-role").textContent = user.role;
  }

  document.getElementById("backup-card").classList.toggle("hidden", !canView("PARAMETRES"));
  document.getElementById("backup-restore-btn").classList.toggle("hidden", !canManage("PARAMETRES"));

  if (parametresWired) return;
  parametresWired = true;

  document.getElementById("password-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const actuel = document.getElementById("pw-actuel").value;
    const nouveau = document.getElementById("pw-nouveau").value;
    const confirmation = document.getElementById("pw-confirmation").value;

    if (nouveau !== confirmation) {
      showToast("Les deux mots de passe ne correspondent pas", "error");
      return;
    }

    try {
      await api.changeOwnPassword(actuel, nouveau);
      showToast("Mot de passe mis à jour");
      document.getElementById("password-form").reset();
    } catch (err) {
      showToast(err.message, "error");
    }
  });

  document.getElementById("backup-download-btn").addEventListener("click", async () => {
    try {
      await api.downloadBackup();
      showToast("Sauvegarde téléchargée");
    } catch (err) {
      showToast(err.message, "error");
    }
  });

  document.getElementById("backup-restore-btn").addEventListener("click", async () => {
    const input = document.getElementById("backup-file-input");
    const file = input.files[0];
    if (!file) {
      showToast("Choisissez d'abord un fichier de sauvegarde", "error");
      return;
    }
    if (!confirm("Restaurer cette sauvegarde remplacera toutes les données actuelles. Continuer ?")) return;

    try {
      const result = await api.restoreBackup(file);
      showToast(result?.message || "Sauvegarde restaurée");
      input.value = "";
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}
