# MIAPEVE Finance

Application de gestion des finances pour l'église MIAPEVE : comptes, transactions,
semences, dépenses, programmes, budgets, utilisateurs et rôles.

Refonte de l'ancienne application desktop JavaFX vers une architecture moderne :

- **`desktop/`** — coquille [Tauri](https://tauri.app) (Rust + webview) qui affiche
  le frontend et lance le backend en arrière-plan.
- **`desktop/src/`** — frontend HTML/CSS/JS statique, sans framework.
- **`backend/`** — API REST [Spring Boot](https://spring.io/projects/spring-boot)
  (Java 21), base de données SQLite.

Le backend tourne en sidecar : Tauri le lance comme un sous-processus au
démarrage de l'application et l'arrête à la fermeture. Le frontend communique
avec lui en HTTP local (`http://127.0.0.1:8787/api`).

## Fonctionnalités

- Comptes, transactions, semences, dépenses, programmes et budgets, avec
  calcul automatique des soldes
- Système de rôles et permissions entièrement modulaire : un administrateur
  peut créer n'importe quel rôle et cocher, module par module, les droits de
  consultation et de gestion — rien n'est codé en dur par rôle
- Gestion des utilisateurs
- Mode sombre (bascule manuelle, persistée)
- Impression / export des reçus de programme, relevés de compte et synthèses
  financières
- Devise : franc CFA (FCFA) uniquement

## Démarrer en développement

```bash
cd backend && mvn -q -DskipTests package && cd ..
cp backend/target/miapeve-finance-backend.jar desktop/src-tauri/resources/

cd desktop
npm install
npm run tauri dev
```

Au premier démarrage, un rôle `ADMIN` et un utilisateur `admin` / `admin123`
sont créés automatiquement — à changer depuis la page Paramètres.

Voir `backend/README.md` et `desktop/README.md` pour le détail de chaque
partie (variables d'environnement, endpoints, build de production).

## Build de l'exécutable Windows

Le workflow GitHub Actions `.github/workflows/build-windows.yml` compile le
backend, génère un JRE minimal embarqué (via `jlink`, pour que l'utilisateur
final n'ait besoin d'installer aucun Java), puis construit l'installeur Tauri
(`.exe` NSIS et `.msi`). Il se déclenche automatiquement à chaque push sur
`main` ou une branche `claude/**`, et peut aussi être lancé manuellement
depuis l'onglet **Actions** du dépôt (`workflow_dispatch`). L'installeur
produit est téléchargeable dans les **Artifacts** du run.
