-- Création de la table roles
CREATE TABLE roles(
   role_id INTEGER PRIMARY KEY AUTOINCREMENT,
   libelle VARCHAR(50) NOT NULL,
   description VARCHAR(50),
   UNIQUE(libelle)
);

-- Création de la table comptes
CREATE TABLE comptes(
   compte_id INTEGER PRIMARY KEY AUTOINCREMENT,
   type_compte VARCHAR(50) NOT NULL,
   solde REAL NOT NULL
);

-- Création de la table utilisateurs
CREATE TABLE utilisateurs(
   utilisateur_id INTEGER PRIMARY KEY AUTOINCREMENT,
   nom VARCHAR(50) NOT NULL,
   prenom VARCHAR(50) NOT NULL,
   nom_utilisateur VARCHAR(50) NOT NULL,
   mot_de_passe VARCHAR(50) NOT NULL,
   role_id INTEGER NOT NULL,
   UNIQUE(nom_utilisateur),
   FOREIGN KEY(role_id) REFERENCES roles(role_id)
);

-- Création de la table programmes
CREATE TABLE programmes(
   programme_id INTEGER PRIMARY KEY AUTOINCREMENT,
   libelle VARCHAR(50) NOT NULL,
   date_programme DATE,
   heure_debut TIME NOT NULL,
   heure_fin VARCHAR(50) NOT NULL,
   utilisateur_id INTEGER NOT NULL,
   FOREIGN KEY(utilisateur_id) REFERENCES utilisateurs(utilisateur_id)
);

-- Création de la table semences
CREATE TABLE semences(
   semence_id INTEGER PRIMARY KEY AUTOINCREMENT,
   type_semence VARCHAR(50) NOT NULL,
   montant REAL NOT NULL,
   programme_id INTEGER NOT NULL,
   compte_id INTEGER NOT NULL,
   FOREIGN KEY(programme_id) REFERENCES programmes(programme_id),
   FOREIGN KEY(compte_id) REFERENCES comptes(compte_id)
);

-- Création de la table depenses
CREATE TABLE depenses(
   depense_id INTEGER PRIMARY KEY AUTOINCREMENT,
   montant REAL,
   libelle VARCHAR(50) NOT NULL,
   compte_id INTEGER NOT NULL,
   FOREIGN KEY(compte_id) REFERENCES comptes(compte_id)
);