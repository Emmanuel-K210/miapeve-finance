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
  const match = disposition.match(/filename="?([^\"]+)"?/);
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
