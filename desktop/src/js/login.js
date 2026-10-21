const form = document.getElementById("login-form");
const errorBox = document.getElementById("login-error");
const submitBtn = document.getElementById("login-submit");

if (getToken()) {
  window.location.replace("app.html");
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  errorBox.classList.add("hidden");
  submitBtn.disabled = true;
  submitBtn.textContent = "Connexion...";

  const nomUtilisateur = document.getElementById("nomUtilisateur").value.trim();
  const motDePasse = document.getElementById("motDePasse").value;

  try {
    const result = await api.login(nomUtilisateur, motDePasse);
    setSession(result.token, result.utilisateur);
    window.location.replace("app.html");
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.classList.remove("hidden");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Se connecter";
  }
});
