async function loadAuditLog() {
  const body = document.getElementById("audit-body");
  try {
    const entries = await api.getAuditLog();
    body.innerHTML = entries.length
      ? entries
          .map(
            (entry) => `
        <tr>
          <td>${formatDate(entry.dateCreation)}</td>
          <td>${escapeHtml(entry.nomUtilisateur || "—")}</td>
          <td><span class="badge badge-neutral">${escapeHtml(entry.module)}</span></td>
          <td>${escapeHtml(entry.methode)}</td>
          <td>${escapeHtml(entry.chemin)}</td>
          <td>${entry.statut}</td>
        </tr>`
          )
          .join("")
      : `<tr><td colspan="6" class="empty-state">Aucune activité enregistrée pour le moment.</td></tr>`;
  } catch (err) {
    showToast(err.message, "error");
  }
}
