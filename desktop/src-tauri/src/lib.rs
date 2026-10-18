use std::process::{Child, Command};
use std::sync::Mutex;
use std::time::Duration;

use tauri::{Manager, RunEvent};

const BACKEND_PORT: u16 = 8787;
const BACKEND_HEALTH_URL: &str = "http://127.0.0.1:8787/api/health";

/// Garde une référence au process du backend Java pour pouvoir l'arrêter
/// proprement à la fermeture de l'application.
struct BackendProcess(Mutex<Option<Child>>);

pub fn run() {
    tauri::Builder::default()
        .manage(BackendProcess(Mutex::new(None)))
        .setup(|app| {
            let handle = app.handle().clone();

            let jar_path = app
                .path()
                .resource_dir()
                .expect("impossible de résoudre le dossier des ressources")
                .join("resources")
                .join("miapeve-finance-backend.jar");

            let data_dir = app
                .path()
                .app_data_dir()
                .expect("impossible de résoudre le dossier de données de l'application");
            std::fs::create_dir_all(&data_dir).ok();
            let db_path = data_dir.join("miapeve_finance.db");

            let child = Command::new("java")
                .arg("-jar")
                .arg(&jar_path)
                .env("MIAPEVE_DB_PATH", &db_path)
                .env("MIAPEVE_PORT", BACKEND_PORT.to_string())
                .spawn()
                .expect("impossible de démarrer le backend Java (vérifiez qu'un JRE 21+ est installé)");

            if let Some(state) = handle.try_state::<BackendProcess>() {
                *state.0.lock().unwrap() = Some(child);
            }

            // Attend que le backend réponde avant d'afficher la fenêtre,
            // pour éviter un écran blanc pendant le démarrage de la JVM.
            tauri::async_runtime::spawn(async move {
                let client = reqwest::Client::new();
                for _ in 0..60 {
                    if client
                        .get(BACKEND_HEALTH_URL)
                        .timeout(Duration::from_secs(1))
                        .send()
                        .await
                        .is_ok()
                    {
                        if let Some(window) = handle.get_webview_window("main") {
                            let _ = window.show();
                        }
                        return;
                    }
                    tokio::time::sleep(Duration::from_millis(500)).await;
                }
                eprintln!("Le backend n'a pas répondu après 30s, affichage de la fenêtre malgré tout");
                if let Some(window) = handle.get_webview_window("main") {
                    let _ = window.show();
                }
            });

            Ok(())
        })
        .build(tauri::generate_context!())
        .expect("erreur lors du lancement de l'application Tauri")
        .run(|app_handle, event| {
            if let RunEvent::ExitRequested { .. } = event {
                if let Some(state) = app_handle.try_state::<BackendProcess>() {
                    if let Some(mut child) = state.0.lock().unwrap().take() {
                        let _ = child.kill();
                    }
                }
            }
        });
}
