use std::fs::File;
use std::net::TcpListener;
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

const BACKEND_PORT_START: u16 = 8787;
const BACKEND_PORT_END: u16 = 8820;

fn find_available_backend_port() -> u16 {
    for port in BACKEND_PORT_START..=BACKEND_PORT_END {
        if TcpListener::bind(("127.0.0.1", port)).is_ok() {
            return port;
        }
    }
    BACKEND_PORT_START
}

/// Garde une référence au process du backend Java pour pouvoir l'arrêter
/// proprement à la fermeture de l'application.
struct BackendProcess(Mutex<Option<Child>>);

/// Les chemins résolus par Tauri sur Windows portent le préfixe `\\?\`
/// (chemin étendu). Ce préfixe est compris par les API Win32 mais pas par
/// le lanceur/zip de la JVM, qui échoue alors à lire le jar (symptôme :
/// ClassNotFoundException sur une classe pourtant présente dans l'archive).
/// On le retire avant de transmettre un chemin à Java.
fn strip_verbatim_prefix(path: &std::path::Path) -> std::path::PathBuf {
    match path.to_str() {
        Some(s) => std::path::PathBuf::from(s.strip_prefix(r"\\?\").unwrap_or(s)),
        None => path.to_path_buf(),
    }
}

/// Une restauration demandée depuis l'app (Paramètres) est écrite par le
/// backend à côté de la base active, sous `<db_path>.pending-restore`, car
/// remplacer le fichier pendant que la JVM l'a ouvert n'est pas sûr. On
/// applique l'échange ici, avant de lancer le backend, pendant qu'aucun
/// processus ne tient le fichier.
fn apply_pending_restore(db_path: &std::path::Path) {
    let mut pending = db_path.as_os_str().to_owned();
    pending.push(".pending-restore");
    let pending = std::path::PathBuf::from(pending);
    if !pending.exists() {
        return;
    }

    if db_path.exists() {
        let mut backup = db_path.as_os_str().to_owned();
        backup.push(".before-restore.bak");
        let _ = std::fs::rename(db_path, std::path::PathBuf::from(backup));
    }
    let _ = std::fs::rename(&pending, db_path);
    let _ = std::fs::remove_file(&pending);
}

pub fn run() {
    tauri::Builder::default()
        .manage(BackendProcess(Mutex::new(None)))
        .setup(|app| {
            let handle = app.handle().clone();

            let resource_dir = strip_verbatim_prefix(
                &app.path()
                    .resource_dir()
                    .expect("impossible de résoudre le dossier des ressources"),
            );
            let resources_dir = resource_dir.join("resources");
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

            let data_dir = strip_verbatim_prefix(
                &app.path()
                    .app_data_dir()
                    .expect("impossible de résoudre le dossier de données de l'application"),
            );
            std::fs::create_dir_all(&data_dir)
                .expect("impossible de créer le dossier de données de l'application");
            let db_path = data_dir.join("miapeve_finance.db");
            apply_pending_restore(&db_path);

            let backend_port = find_available_backend_port();
            let backend_health_url = format!("http://127.0.0.1:{backend_port}/api/health");

            // Toute la sortie du backend Java est capturée dans ce fichier,
            // car l'application n'a pas de console visible une fois lancée.
            let log_path = data_dir.join("backend.log");
            let stdout_log = File::create(&log_path)
                .expect("impossible de créer le fichier de log du backend");
            let stderr_log = stdout_log
                .try_clone()
                .expect("impossible de dupliquer le descripteur de log");

            let mut command = Command::new(&java_bin);
            command
                .arg("-jar")
                .arg(&jar_path)
                .env("MIAPEVE_DB_PATH", &db_path)
                .env("MIAPEVE_PORT", backend_port.to_string())
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
                        .get(&backend_health_url)
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
