const API_BASE_URL = "http://127.0.0.1:8787/api";

function getToken() {
  return localStorage.getItem("miapeve_token");
}

function setSession(token, utilisateur) {
  localStorage.setItem("miapeve_token", token);
  localStorage.setItem("miapeve_user", JSON.stringify(utilisateur));
}

function clearSession() {
  localStorage.removeItem("miapeve_token");
  localStorage.removeItem("miapeve_user");
}

function getCurrentUser() {
  const raw = localStorage.getItem("miapeve_user");
  return raw ? JSON.parse(raw) : null;
}

async function apiRequest(path, { method = "GET", body } = {}) {
  const headers = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401) {
    clearSession();
    window.location.href = "login.html";
    throw new Error("Session expirée");
  }

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    throw new Error(payload.message || `Erreur ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json().catch(() => null);
}

const api = {
  login: (nomUtilisateur, motDePasse) =>
    apiRequest("/auth/login", { method: "POST", body: { nomUtilisateur, motDePasse } }),
  logout: () => apiRequest("/auth/logout", { method: "POST" }),
  me: () => apiRequest("/auth/me"),

  getComptes: () => apiRequest("/comptes"),
  createCompte: (data) => apiRequest("/comptes", { method: "POST", body: data }),
  updateCompte: (id, data) => apiRequest(`/comptes/${id}`, { method: "PUT", body: data }),
  deleteCompte: (id) => apiRequest(`/comptes/${id}`, { method: "DELETE" }),

  getTransactions: (compteId) => apiRequest(compteId ? `/transactions?compteId=${compteId}` : "/transactions"),
  createTransaction: (data) => apiRequest("/transactions", { method: "POST", body: data }),
  deleteTransaction: (id) => apiRequest(`/transactions/${id}`, { method: "DELETE" }),

  getProgrammes: () => apiRequest("/programmes"),
  createProgramme: (data) => apiRequest("/programmes", { method: "POST", body: data }),
  deleteProgramme: (id) => apiRequest(`/programmes/${id}`, { method: "DELETE" }),

  getSemences: () => apiRequest("/semences"),
  createSemence: (data) => apiRequest("/semences", { method: "POST", body: data }),
  deleteSemence: (id) => apiRequest(`/semences/${id}`, { method: "DELETE" }),

  getBudgets: () => apiRequest("/budgets"),
  createBudget: (data) => apiRequest("/budgets", { method: "POST", body: data }),
  deleteBudget: (id) => apiRequest(`/budgets/${id}`, { method: "DELETE" }),

  getDashboardSummary: () => apiRequest("/dashboard/summary"),

  getUtilisateurs: () => apiRequest("/utilisateurs"),
  createUtilisateur: (data) => apiRequest("/utilisateurs", { method: "POST", body: data }),
  updateUtilisateur: (id, data) => apiRequest(`/utilisateurs/${id}`, { method: "PUT", body: data }),
  resetPassword: (id, nouveauMotDePasse) => apiRequest(`/utilisateurs/${id}/mot-de-passe`, { method: "PUT", body: { nouveauMotDePasse } }),
  deleteUtilisateur: (id) => apiRequest(`/utilisateurs/${id}`, { method: "DELETE" }),

  getRoles: () => apiRequest("/roles"),
  getRolesCatalog: () => apiRequest("/roles/catalog"),
  createRole: (data) => apiRequest("/roles", { method: "POST", body: data }),
  updateRole: (id, data) => apiRequest(`/roles/${id}`, { method: "PUT", body: data }),
  deleteRole: (id) => apiRequest(`/roles/${id}`, { method: "DELETE" }),
};

function requireAuth() {
  if (!getToken()) {
    window.location.href = "login.html";
  }
}

function hasPermission(module, action) {
  const user = getCurrentUser();
  if (!user || !user.permissions) return false;
  return user.permissions.includes(`${module}_${action}`);
}

function canView(module) {
  return hasPermission(module, "VIEW");
}

function canManage(module) {
  return hasPermission(module, "MANAGE");
}

function formatMontant(value) {
  return new Intl.NumberFormat("fr-FR", { minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(value || 0) + " FCFA";
}

function showToast(message, type = "success") {
  const container = document.getElementById("toast-container");
  if (!container) return;
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}
