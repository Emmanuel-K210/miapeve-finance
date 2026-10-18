const THEME_KEY = "miapeve_theme";

function getStoredTheme() {
  try {
    return localStorage.getItem(THEME_KEY);
  } catch (e) {
    return null;
  }
}

function isDarkActive() {
  const stored = getStoredTheme();
  if (stored) return stored === "dark";
  return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
}

function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);
  try {
    localStorage.setItem(THEME_KEY, theme);
  } catch (e) {}
  updateThemeToggleUI();
}

function updateThemeToggleUI() {
  const icon = document.getElementById("theme-toggle-icon");
  const label = document.getElementById("theme-toggle-label");
  if (!icon || !label) return;

  const dark = isDarkActive();
  icon.innerHTML = dark ? Icons.sun() : Icons.moon();
  label.textContent = dark ? "Mode clair" : "Mode sombre";
}

const themeToggleBtn = document.getElementById("theme-toggle");
if (themeToggleBtn) {
  themeToggleBtn.addEventListener("click", () => {
    applyTheme(isDarkActive() ? "light" : "dark");
  });
  updateThemeToggleUI();
}
