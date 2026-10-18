let parametresWired = false;

function loadParametres() {
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
}
