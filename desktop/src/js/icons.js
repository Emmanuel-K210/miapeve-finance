/**
 * Icônes SVG minimalistes (traits fins, style ligne) pour remplacer tout
 * emoji dans l'interface. Chaque fonction retourne un <svg> prêt à insérer.
 */
const Icons = {
  home: () => svg(`<path d="M4 11.5 12 4l8 7.5"/><path d="M6 10v9a1 1 0 0 0 1 1h3v-6h4v6h3a1 1 0 0 0 1-1v-9"/>`),
  bank: () => svg(`<path d="M3 10 12 4l9 6"/><path d="M5 10v9M19 10v9M9 10v9M15 10v9"/><path d="M3 19h18"/>`),
  swap: () => svg(`<path d="M4 8h13l-3-3"/><path d="M20 16H7l3 3"/>`),
  calendar: () => svg(`<rect x="4" y="5" width="16" height="15" rx="2"/><path d="M4 9h16M8 3v4M16 3v4"/>`),
  sprout: () => svg(`<path d="M12 21V10"/><path d="M12 10C12 10 7 10.5 7 5.5C7 5.5 12 5 12 10Z"/><path d="M12 10C12 10 17 10.5 17 5.5C17 5.5 12 5 12 10Z"/>`),
  receipt: () => svg(`<path d="M6 3h12v18l-2.5-1.5L13 21l-2.5-1.5L8 21l-2-1.5V3Z"/><path d="M9 8h6M9 12h6"/>`),
  users: () => svg(`<circle cx="9" cy="8" r="3"/><path d="M3 20c0-3.5 2.5-6 6-6s6 2.5 6 6"/><circle cx="17" cy="9" r="2.3"/><path d="M15.5 14c2.4.4 4.5 2.3 4.5 6"/>`),
  settings: () => svg(`<circle cx="12" cy="12" r="3"/><path d="M19 12a7 7 0 0 0-.14-1.4l1.9-1.5-2-3.3-2.2.9a7 7 0 0 0-2.4-1.4L13.8 3h-3.6l-.4 2.3a7 7 0 0 0-2.4 1.4l-2.2-.9-2 3.3 1.9 1.5a7 7 0 0 0 0 2.8l-1.9 1.5 2 3.3 2.2-.9a7 7 0 0 0 2.4 1.4l.4 2.3h3.6l.4-2.3a7 7 0 0 0 2.4-1.4l2.2.9 2-3.3-1.9-1.5c.1-.5.14-.9.14-1.4Z"/>`),
  logout: () => svg(`<path d="M9 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h3"/><path d="M15 16l4-4-4-4"/><path d="M19 12H9"/>`),
  wallet: () => svg(`<path d="M3 7a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v2H5a2 2 0 0 1-2-2Z"/><path d="M3 9h16a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9Z"/><circle cx="16" cy="14" r="1.2"/>`),
  trendUp: () => svg(`<path d="M4 16l5-5 4 4 7-8"/><path d="M14 7h6v6"/>`),
  trendDown: () => svg(`<path d="M4 8l5 5 4-4 7 8"/><path d="M14 17h6v-6"/>`),
  pencil: () => svg(`<path d="M4 20h4L19.5 8.5a2 2 0 0 0 0-2.8l-1.2-1.2a2 2 0 0 0-2.8 0L4 16v4Z"/><path d="M14 6l3 3"/>`),
  trash: () => svg(`<path d="M5 7h14"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/><path d="M7 7l1 13a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2l1-13"/>`),
  arrowUpCircle: () => svg(`<circle cx="12" cy="12" r="9"/><path d="M12 16V8M8.5 11.5 12 8l3.5 3.5"/>`),
  arrowDownCircle: () => svg(`<circle cx="12" cy="12" r="9"/><path d="M12 8v8M8.5 12.5 12 16l3.5-3.5"/>`),
  chevronRight: () => svg(`<path d="M9 5l7 7-7 7"/>`),
  target: () => svg(`<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="4"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3"/>`),
  alert: () => svg(`<path d="M12 4 3 20h18Z"/><path d="M12 10v4"/><circle cx="12" cy="17" r="0.4" fill="currentColor"/>`),
  check: () => svg(`<path d="M5 12l5 5L19 7"/>`),
  sun: () => svg(`<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.2 4.2l1.4 1.4M18.4 18.4l1.4 1.4M2 12h2M20 12h2M4.2 19.8l1.4-1.4M18.4 5.6l1.4-1.4"/>`),
  moon: () => svg(`<path d="M20 14.5A8.5 8.5 0 1 1 9.5 4a7 7 0 0 0 10.5 10.5Z"/>`),
  download: () => svg(`<path d="M12 4v11"/><path d="M7.5 11 12 15.5 16.5 11"/><path d="M5 19.5h14"/>`),
  upload: () => svg(`<path d="M12 15V4"/><path d="M7.5 8 12 3.5 16.5 8"/><path d="M5 19.5h14"/>`),
  history: () => svg(`<circle cx="12" cy="13" r="8"/><path d="M12 9v4l3 2"/><path d="M4 5v4h4"/><path d="M5.5 8.5A8 8 0 0 1 12 5"/>`),
};

function svg(innerPaths) {
  return `<svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${innerPaths}</svg>`;
}
