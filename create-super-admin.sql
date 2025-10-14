-- Script pour créer un utilisateur SUPER_ADMIN dans la base de données CampusFlow
-- À exécuter dans MySQL Workbench

USE campusflow;

-- Vérifier si un Super Admin existe déjà
SELECT COUNT(*) as 'Nombre de Super Admins' FROM utilisateurs WHERE role = 'SUPER_ADMIN';

-- Créer le Super Admin prédéfini
INSERT INTO utilisateurs (
    id, 
    nom, 
    prenom, 
    email, 
    mot_de_passe, 
    bio, 
    role
) VALUES (
    UUID(), 
    'Administrateur', 
    'Super', 
    'superadmin@campusflow.com', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- Mot de passe: superadmin123
    'Super Administrateur système de CampusFlow', 
    'SUPER_ADMIN'
);

-- Vérifier que le Super Admin a été créé
SELECT 
    id,
    prenom,
    nom,
    email,
    role,
    bio
FROM utilisateurs 
WHERE role = 'SUPER_ADMIN';

-- Afficher tous les utilisateurs pour vérification
SELECT 
    prenom,
    nom,
    email,
    role
FROM utilisateurs 
ORDER BY role, nom;
