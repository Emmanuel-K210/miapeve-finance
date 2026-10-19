use std::fs::File;
use std::io::Write;
use std::process::{Child, Command, Stdio};
use std::sync::Mutex;
use std::time::Duration;

#[cfg(windows)]
use std::os::windows::process::CommandExt;

// Empêche Windows d'ouvrir une fenêtre de console noire pour le process
// Java, qui n'a pas de GUI et dont la sortie est déjà redirigée vers
// backend.log.
#[cfg(windows)]
const CREATE_NO_WINDOW: u32 = 0x08000000;

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

            let raw_resource_dir = app
                .path()
                .resource_dir()
                .expect("impossible de résoudre le dossier des ressources");
            let resources_dir = raw_resource_dir.join("resources");
            let jar_path = resources_dir.join("miapeve-finance-backend.jar");

            // Utilise le JRE embarqué (généré par jlink au moment du build)
            // s'il est présent, pour ne pas dépendre d'une installation Java
            // sur le poste de l'utilisateur. En développement local, où le
            // JRE n'a pas été embarqué, on retombe sur le `java` du PATH.
            let bundled_java = resources_dir.join("jre").join("bin").join(if cfg!(windows) {
                "java.exe"
            } else {
                "java"
            });
            let java_bin = if bundled_java.exists() {
                bundled_java
            } else {
                std::path::PathBuf::from("java")
            };

            let data_dir = app
                .path()
                .app_data_dir()
                .expect("impossible de résoudre le dossier de données de l'application");
            std::fs::create_dir_all(&data_dir)
                .expect("impossible de créer le dossier de données de l'application");
            let db_path = data_dir.join("miapeve_finance.db");

            // Toute la sortie du backend Java est capturée dans ce fichier,
            // car l'application n'a pas de console visible une fois lancée.
            let log_path = data_dir.join("backend.log");
            let mut stdout_log = File::create(&log_path)
                .expect("impossible de créer le fichier de log du backend");

            // Diagnostic : les chemins résolus, pour comparer avec un lancement manuel.
            let _ = writeln!(
                stdout_log,
                "--- Diagnostic de démarrage ---\n\
                 resource_dir (brut) = {raw_resource_dir:?}\n\
                 resources_dir       = {resources_dir:?}\n\
                 jar_path            = {jar_path:?} (existe: {})\n\
                 java_bin            = {java_bin:?} (existe: {})\n\
                 current_dir         = {:?}\n\
                 --------------------------------",
                jar_path.exists(),
                java_bin.exists(),
                std::env::current_dir(),
            );

            let stderr_log = stdout_log
                .try_clone()
                .expect("impossible de dupliquer le descripteur de log");

            let mut command = Command::new(&java_bin);
            command
                .arg("-jar")
                .arg(&jar_path)
                .env("MIAPEVE_DB_PATH", &db_path)
                .env("MIAPEVE_PORT", BACKEND_PORT.to_string())
                .stdout(Stdio::from(stdout_log))
                .stderr(Stdio::from(stderr_log));
            #[cfg(windows)]
            command.creation_flags(CREATE_NO_WINDOW);

            let child = command
                .spawn()
                .unwrap_or_else(|e| {
                    panic!(
                        "impossible de démarrer le backend Java (vérifiez qu'un JRE 21+ est installé) : {e}"
                    )
                });

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
