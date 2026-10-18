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

- `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`
- `GET/POST/PUT/DELETE /api/comptes[/{id}]`
- `GET /api/dashboard/summary`
- `GET /api/health`

Les modules transactions, programmes, semences, dépenses et utilisateurs
restent à migrer (entités JPA déjà créées dans `entity/`, repositories dans
`repository/`) ; il manque les services/contrôleurs correspondants.
