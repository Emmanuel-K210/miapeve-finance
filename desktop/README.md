# MIAPEVE Finance — Desktop (Tauri)

Coquille desktop (Rust + webview) qui lance le backend Java en sidecar et
affiche le frontend HTML/CSS/JS.

## Prérequis sur la machine de build

- Node.js 18+ et npm
- Rust (`rustup`) + toolchain stable
- Un JDK 21+ installé (pour compiler le backend et générer, via `jlink`, le
  JRE minimal embarqué dans l'installeur — voir `.github/workflows/build-windows.yml`)
- Dépendances système Tauri (Linux : `libwebkit2gtk-4.1-dev`, `libgtk-3-dev`,
  `librsvg2-dev`, `patchelf` — voir https://v2.tauri.app/start/prerequisites/)

Sur la machine de l'utilisateur final (installeur Windows produit par la CI),
**aucun Java n'est requis** : un JRE minimal est embarqué dans
`resources/jre/` et utilisé directement par le sidecar. En développement
local sans avoir généré ce JRE embarqué, le sidecar retombe sur le `java` du
PATH (il faut alors un JDK/JRE 21+ installé sur la machine de dev).

## Démarrage en développement

```bash
cd backend && mvn -q -DskipTests package && cd ..
cp backend/target/miapeve-finance-backend.jar desktop/src-tauri/resources/
# ou directement :
./desktop/scripts/prepare-backend.sh

cd desktop
npm install
npm run tauri dev
```

## Build de production

```bash
./desktop/scripts/prepare-backend.sh
cd desktop
npm run tauri build
```

Le jar backend n'est pas versionné dans git (voir `.gitignore`) : il est
régénéré à chaque build via `scripts/prepare-backend.sh`.

## Structure

- `src-tauri/` — code Rust (lance le backend Java, affiche la fenêtre)
- `src/` — frontend statique (HTML/CSS/JS), consomme l'API REST sur
  `http://127.0.0.1:8787/api`
