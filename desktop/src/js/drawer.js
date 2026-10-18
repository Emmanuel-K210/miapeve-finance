/**
 * Panneau latéral réutilisable pour les formulaires de création/édition.
 * Remplace les anciennes modales centrées, jugées peu pratiques.
 */
let drawerRoot = null;

function ensureDrawerRoot() {
  if (!drawerRoot) {
    drawerRoot = document.getElementById("drawer-root");
  }
  return drawerRoot;
}

function openDrawer({ title, subtitle, bodyHtml, footerHtml, formId }) {
  const root = ensureDrawerRoot();
  root.innerHTML = `
    <div class="drawer-overlay" id="drawer-overlay">
      <div class="drawer" role="dialog" aria-modal="true">
        <div class="drawer-header">
          <div class="drawer-header-text">
            <h3>${title}</h3>
            ${subtitle ? `<p>${subtitle}</p>` : ""}
          </div>
          <button type="button" class="drawer-close" id="drawer-close-btn">
            Fermer ${Icons.chevronRight()}
          </button>
        </div>
        <form id="${formId}">
          <div class="drawer-body">${bodyHtml}</div>
          <div class="drawer-footer">${footerHtml}</div>
        </form>
      </div>
    </div>
  `;

  document.getElementById("drawer-close-btn").addEventListener("click", closeDrawer);
  document.getElementById("drawer-overlay").addEventListener("mousedown", (e) => {
    if (e.target.id === "drawer-overlay") closeDrawer();
  });
  document.addEventListener("keydown", drawerEscHandler);

  return document.getElementById(formId);
}

function closeDrawer() {
  const root = ensureDrawerRoot();
  root.innerHTML = "";
  document.removeEventListener("keydown", drawerEscHandler);
}

function drawerEscHandler(e) {
  if (e.key === "Escape") closeDrawer();
}

const QUICK_COMPTE_TYPES = ["CAISSE", "BANQUE", "MOBILE MONEY", "EPARGNE"];

/**
 * Bloc réutilisable "créer un compte à la volée" pour les formulaires qui
 * réfèrent un compte (transaction, semence, budget) : l'utilisateur n'est
 * pas limité aux comptes déjà créés, il peut saisir le sien directement.
 */
function quickCreateCompteBlock(selectId) {
  return `
    <div class="inline-action-row">
      <label style="margin:0;">Compte</label>
      <button type="button" class="link-btn" id="qc-compte-toggle">+ créer un compte</button>
    </div>
    <div class="field field-full" style="margin-top: var(--space-2);">
      <select class="input" id="${selectId}" required></select>
    </div>
    <div class="quick-panel hidden" id="qc-compte-panel">
      <div class="form-grid">
        <div class="field field-full">
          <label>Nom du compte</label>
          <input class="input" id="qc-compte-nom" placeholder="ex. Caisse Jeunesse" />
        </div>
        <div class="field field-full">
          <label>Type de compte</label>
          <input class="input" id="qc-compte-type" list="qc-compte-type-suggestions" placeholder="CAISSE, BANQUE, ..." />
          <datalist id="qc-compte-type-suggestions">
            ${QUICK_COMPTE_TYPES.map((t) => `<option value="${t}"></option>`).join("")}
          </datalist>
        </div>
      </div>
      <button type="button" class="btn btn-secondary btn-block" id="qc-compte-create">Créer ce compte</button>
    </div>
  `;
}

function renderCompteSelectOptions(comptes) {
  if (!comptes.length) {
    return `<option value="">Aucun compte — créez-en un</option>`;
  }
  return comptes.map((c) => `<option value="${c.id}">${escapeHtml(c.nomCompte)}</option>`).join("");
}

/**
 * Branche les interactions du bloc ci-dessus.
 * getCache/setCache lisent et mettent à jour le tableau de comptes en
 * mémoire du module appelant (ex. transactionsComptesCache).
 */
function wireQuickCreateCompte({ selectId, getCache, setCache }) {
  const select = document.getElementById(selectId);
  select.innerHTML = renderCompteSelectOptions(getCache());

  document.getElementById("qc-compte-toggle").addEventListener("click", () => {
    document.getElementById("qc-compte-panel").classList.toggle("hidden");
  });

  document.getElementById("qc-compte-create").addEventListener("click", async () => {
    const nomCompte = document.getElementById("qc-compte-nom").value.trim();
    const typeCompte = document.getElementById("qc-compte-type").value.trim().toUpperCase();

    if (!nomCompte || !typeCompte) {
      showToast("Indiquez un nom et un type de compte", "error");
      return;
    }

    try {
      const compte = await api.createCompte({ nomCompte, typeCompte, solde: 0 });
      const cache = getCache();
      cache.push(compte);
      setCache(cache);
      select.innerHTML = renderCompteSelectOptions(cache);
      select.value = compte.id;
      document.getElementById("qc-compte-panel").classList.add("hidden");
      document.getElementById("qc-compte-nom").value = "";
      document.getElementById("qc-compte-type").value = "";
      showToast("Compte créé");
    } catch (err) {
      showToast(err.message, "error");
    }
  });
}
