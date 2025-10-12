-- Script pour créer des événements de test
-- Ces événements seront créés pour tester la fonctionnalité admin

-- Événement 1: Conférence sur l'IA
INSERT INTO evenements (id, titre, description, lieu, date_heure_debut, capacite_max, statut, club_id, created_at, updated_at)
VALUES (
    UUID(),
    'Conférence sur l\'Intelligence Artificielle',
    'Une conférence passionnante sur les dernières avancées en intelligence artificielle et leur impact sur notre société.',
    'Amphithéâtre A - Campus Principal',
    '2024-12-15 14:00:00',
    150,
    'PUBLIE',
    NULL, -- Événement universitaire
    NOW(),
    NOW()
);

-- Événement 2: Tournoi de Football
INSERT INTO evenements (id, titre, description, lieu, date_heure_debut, capacite_max, statut, club_id, created_at, updated_at)
VALUES (
    UUID(),
    'Tournoi de Football Inter-Clubs',
    'Tournoi de football entre les différents clubs de l\'université. Inscriptions ouvertes à tous les étudiants.',
    'Stade Universitaire',
    '2024-12-20 10:00:00',
    200,
    'PUBLIE',
    NULL, -- Événement universitaire
    NOW(),
    NOW()
);

-- Événement 3: Atelier de Programmation
INSERT INTO evenements (id, titre, description, lieu, date_heure_debut, capacite_max, statut, club_id, created_at, updated_at)
VALUES (
    UUID(),
    'Atelier de Programmation Web',
    'Apprenez les bases du développement web avec HTML, CSS et JavaScript. Matériel fourni.',
    'Salle Informatique B-205',
    '2024-12-18 16:00:00',
    30,
    'BROUILLON',
    NULL, -- Événement universitaire
    NOW(),
    NOW()
);

-- Événement 4: Soirée Culturelle
INSERT INTO evenements (id, titre, description, lieu, date_heure_debut, capacite_max, statut, club_id, created_at, updated_at)
VALUES (
    UUID(),
    'Soirée Culturelle Internationale',
    'Découvrez les cultures du monde à travers la musique, la danse et la gastronomie.',
    'Centre Culturel',
    '2024-12-22 19:00:00',
    100,
    'PUBLIE',
    NULL, -- Événement universitaire
    NOW(),
    NOW()
);

-- Événement 5: Séminaire de Recherche
INSERT INTO evenements (id, titre, description, lieu, date_heure_debut, capacite_max, statut, club_id, created_at, updated_at)
VALUES (
    UUID(),
    'Séminaire de Recherche en Biologie',
    'Présentation des dernières recherches en biologie moléculaire par nos chercheurs.',
    'Laboratoire de Biologie',
    '2024-12-25 09:00:00',
    50,
    'ANNULE',
    NULL, -- Événement universitaire
    NOW(),
    NOW()
);
