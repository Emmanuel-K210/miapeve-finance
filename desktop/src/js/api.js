const API_PORT_CANDIDATES = [8787, 8788, 8789, 8790, 8791, 8792, 8793, 8794, 8795, 8800];
let resolvedApiBaseUrl = null;

function getApiBaseUrlCandidates() {
  const extraPort = window.MIAPEVE_BACKEND_PORT;
  const ports = [];

  if (extraPort) ports.push(Number(extraPort));
  for (const port of API_PORT_CANDIDATES) {
    if (!ports.includes(port)) ports.push(port);
  }

  return ports.map((port) => `http://127.0.0.1:${port}/api`);
}

async function discoverApiBaseUrl() {
  if (resolvedApiBaseUrl) return resolvedApiBaseUrl;

  const candidates = getApiBaseUrlCandidates();
  for (const baseUrl of candidates) {
    try {
      const healthUrl = `${baseUrl.replace(/\/api$/, "")}/health`;
      const response = await fetch(healthUrl, { method: "GET", cache: "no-store" });
      if (response.ok) {
        resolvedApiBaseUrl = baseUrl;
        return baseUrl;
      }
    } catch {
      // continue to next candidate
    }
  }

  resolvedApiBaseUrl = "http://127.0.0.1:8787/api";
  return resolvedApiBaseUrl;
}

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
  const apiBaseUrl = await discoverApiBaseUrl();
  const headers = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch (err) {
    const detail = err && err.message ? ` [${err.message}]` : "";
    throw new Error(
      `Impossible de contacter le service de l'application${detail}. Le backend n'a peut-être pas démarré correctement (consultez backend.log dans le dossier de données de l'application).`
    );
  }

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

  getSemences: (compteId) => apiRequest(compteId ? `/semences?compteId=${compteId}` : "/semences"),
  createSemence: (data) => apiRequest("/semences", { method: "POST", body: data }),
  deleteSemence: (id) => apiRequest(`/semences/${id}`, { method: "DELETE" }),

  getBudgets: () => apiRequest("/budgets"),
  createBudget: (data) => apiRequest("/budgets", { method: "POST", body: data }),
  deleteBudget: (id) => apiRequest(`/budgets/${id}`, { method: "DELETE" }),

  getDashboardSummary: () => apiRequest("/dashboard/summary"),

  getDepenses: (compteId) => apiRequest(compteId ? `/depenses?compteId=${compteId}` : "/depenses"),
  createDepense: (data) => apiRequest("/depenses", { method: "POST", body: data }),
  deleteDepense: (id) => apiRequest(`/depenses/${id}`, { method: "DELETE" }),

  changeOwnPassword: (motDePasseActuel, nouveauMotDePasse) =>
    apiRequest("/auth/me/password", { method: "PUT", body: { motDePasseActuel, nouveauMotDePasse } }),

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

  getAuditLog: () => apiRequest("/audit/entries"),

  downloadBackup: () => downloadFile("/parametres/backup", "miapeve-finance-backup.db"),
  restoreBackup: (file) => {
    const formData = new FormData();
    formData.append("fichier", file);
    return apiRequestMultipart("/parametres/restore", formData);
  },
};

async function downloadFile(path, fallbackName) {
  const apiBaseUrl = await discoverApiBaseUrl();
  const token = getToken();
  let response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
  } catch {
    throw new Error("Impossible de contacter le service de l'application.");
  }

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    throw new Error(payload.message || `Erreur ${response.status}`);
  }

  const disposition = response.headers.get("Content-Disposition") || "";
  const match = disposition.match(/filename="?([^"]+)"?/);
  const filename = match ? match[1] : fallbackName;

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

async function apiRequestMultipart(path, formData) {
  const apiBaseUrl = await discoverApiBaseUrl();
  const token = getToken();
  const headers = token ? { Authorization: `Bearer ${token}` } : {};

  let response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, { method: "POST", headers, body: formData });
  } catch {
    throw new Error("Impossible de contacter le service de l'application.");
  }

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    throw new Error(payload.message || `Erreur ${response.status}`);
  }
  return response.json().catch(() => null);
}

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
