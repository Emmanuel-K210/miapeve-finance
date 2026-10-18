#!/usr/bin/env bash
# Compile le backend Spring Boot et copie le jar dans src-tauri/resources
# pour qu'il soit embarqué par Tauri comme sidecar.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "==> Build du backend (Maven)"
(cd "$ROOT_DIR/backend" && mvn -q -DskipTests package)

mkdir -p "$SCRIPT_DIR/../src-tauri/resources"
cp "$ROOT_DIR/backend/target/miapeve-finance-backend.jar" "$SCRIPT_DIR/../src-tauri/resources/"

echo "==> Jar backend copié dans desktop/src-tauri/resources/"
