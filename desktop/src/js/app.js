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
  budgets: "Budgets",
  comptes: "Comptes",
  transactions: "Transactions",
  semences: "Semences",
  depenses: "Dépenses",
  programmes: "Programmes",
  utilisateurs: "Utilisateurs",
  roles: "Rôles et permissions",
  audit: "Journal d'activité",
  parametres: "Paramètres",
  soon: "À venir",
};

const PAGE_SUBTITLES = {
  dashboard: "Vue d'ensemble des finances de l'église",
  budgets: "Suivez vos dépenses par rapport aux montants alloués",
  comptes: "Caisses, banques et portefeuilles de l'église",
  transactions: "Historique des entrées et sorties manuelles",
  semences: "Dîmes, offrandes et contributions des fidèles",
  depenses: "Sorties d'argent débitées d'un compte",
  programmes: "Cultes, collectes et évènements de l'église",
  utilisateurs: "Comptes d'accès à l'application",
  roles: "Définissez qui peut voir et faire quoi",
  audit: "Qui a fait quoi, et quand",
  parametres: "Votre compte et vos préférences",
  soon: "Ce module arrive dans une prochaine mise à jour",
};

document.getElementById("topbar-print-btn").addEventListener("click", () => printDashboardReport());
document.getElementById("topbar-etat-financier-btn").addEventListener("click", () => openEtatFinancierDrawer());

document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    document.querySelectorAll(".nav-item").forEach((el) => el.classList.remove("active"));
    item.classList.add("active");
    navigateTo(item.dataset.page);
  });
});

function navigateTo(page) {
  document.querySelectorAll(".page-content > section").forEach((el) => el.classList.add("hidden"));
  document.getElementById(`page-${page}`).classList.remove("hidden");
  document.getElementById("page-title").textContent = PAGE_TITLES[page] || "";
  document.getElementById("page-subtitle").textContent = PAGE_SUBTITLES[page] || "";
  document.getElementById("topbar-print-btn").classList.toggle("hidden", page !== "dashboard");
  document.getElementById("topbar-etat-financier-btn").classList.toggle("hidden", page !== "dashboard");

  if (page === "dashboard") loadDashboard();
  if (page === "budgets") loadBudgets();
  if (page === "comptes") loadComptes();
  if (page === "transactions") loadTransactions();
  if (page === "semences") loadSemences();
  if (page === "depenses") loadDepenses();
  if (page === "programmes") loadProgrammes();
  if (page === "utilisateurs") loadUtilisateurs();
  if (page === "roles") loadRoles();
  if (page === "audit") loadAuditLog();
  if (page === "parametres") loadParametres();
}

/**
 * Masque les entrées de menu dont l'utilisateur n'a pas la permission de
 * consultation — l'affichage s'adapte entièrement aux permissions du rôle,
 * rien n'est câblé en dur par rôle.
 */
function applyPermissionVisibility() {
  document.querySelectorAll(".nav-item[data-module]").forEach((item) => {
    const module = item.dataset.module;
    item.classList.toggle("hidden", !canView(module));
  });

  // Masque aussi une étiquette de section si plus aucun de ses items n'est visible
  document.querySelectorAll(".nav-section-label").forEach((label) => {
    let sibling = label.nextElementSibling;
    let hasVisible = false;
    while (sibling && !sibling.classList.contains("nav-section-label")) {
      if (!sibling.classList.contains("hidden")) hasVisible = true;
      sibling = sibling.nextElementSibling;
    }
    label.classList.toggle("hidden", !hasVisible);
  });
}

applyPermissionVisibility();

document.addEventListener("DOMContentLoaded", () => {
  const firstVisible = document.querySelector(".nav-item:not(.hidden)");
  const startPage = firstVisible ? firstVisible.dataset.page : "dashboard";
  document.querySelectorAll(".nav-item").forEach((el) => el.classList.remove("active"));
  if (firstVisible) firstVisible.classList.add("active");
  navigateTo(startPage);
});
