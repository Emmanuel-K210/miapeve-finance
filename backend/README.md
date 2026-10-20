# MIAPEVE Finance — Backend (Spring Boot)

API REST qui remplace la logique métier de l'ancienne application JavaFX.
Conçue pour être lancée en sidecar par l'application Tauri (`desktop/`), mais
fonctionne aussi comme un service autonome en local.

## Lancer en local

```bash
mvn -q -DskipTests package
java -jar target/miapeve-finance-backend.jar
```

Variables d'environnement utiles :

- `MIAPEVE_DB_PATH` — chemin du fichier SQLite (défaut `./data/miapeve_finance.db`)
- `MIAPEVE_PORT` — port d'écoute (défaut `8787`)
- `MIAPEVE_FRONTEND_ORIGIN` — origines autorisées en CORS, séparées par des virgules

Au premier démarrage, les rôles par défaut, le compte `admin` / `admin123`
et une caisse principale sont créés automatiquement (`DataSeeder`).

## Endpoints disponibles

- `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`,
  `PUT /api/auth/me/password`
- `GET/POST/PUT/DELETE /api/comptes[/{id}]`
- `GET/POST/PUT/DELETE /api/transactions[/{id}]`
- `GET/POST/DELETE /api/semences[/{id}]`
- `GET/POST/DELETE /api/depenses[/{id}]`
- `GET/POST/PUT/DELETE /api/programmes[/{id}]`
- `GET/POST/DELETE /api/budgets[/{id}]`
- `GET/POST/PUT/DELETE /api/utilisateurs[/{id}]`
- `GET/POST/PUT/DELETE /api/roles[/{id}]`
- `GET /api/dashboard/summary`
- `GET /api/health`

Toutes les routes sous `/api/**` (sauf `/api/auth/login` et `/api/health`)
exigent un en-tête `Authorization: Bearer <token>` obtenu via le login, et
sont filtrées par les permissions du rôle de l'utilisateur
(`permission/PermissionCatalog.java` + `security/AuthInterceptor.java`) :
chaque module expose une permission `_VIEW` (routes `GET`) et `_MANAGE`
(routes de mutation).
