requireAuth();

const user = getCurrentUser();
if (user) {
  document.getElementById("user-name").textContent = `${user.prenom} ${user.nom}`;
  document.getElementById("user-role").textContent = user.role;
  document.getElementById("user-avatar").textContent = (user.prenom?.[0] || "?").toUpperCase();
}

document.getElementById("logout-btn").addEventListener("click", async () => {
  try {
    await api.logout();
  } catch (e) {
    // session déjà invalide côté serveur, on continue la déconnexion locale
  }
  clearSession();
  window.location.href = "login.html";
});

const PAGE_TITLES = {
  dashboard: "Tableau de bord",
  comptes: "Comptes",
  soon: "À venir",
};

document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => navigateTo(item.dataset.page));
});

function navigateTo(page) {
  document.querySelectorAll(".nav-item").forEach((el) => el.classList.toggle("active", el.dataset.page === page));
  document.querySelectorAll(".page-content > section").forEach((el) => el.classList.add("hidden"));
  document.getElementById(`page-${page}`).classList.remove("hidden");
  document.getElementById("page-title").textContent = PAGE_TITLES[page] || "";

  if (page === "dashboard") loadDashboard();
  if (page === "comptes") loadComptes();
}

document.addEventListener("DOMContentLoaded", () => navigateTo("dashboard"));
