# Guide de Test - Système de Notifications Temps Réel

## 🎯 Objectif
Valider que le système de notifications en temps réel fonctionne correctement dans tous les scénarios.

## 🧪 Tests à Effectuer

### 1. Test de Connexion WebSocket
- [ ] Ouvrir l'application dans un navigateur
- [ ] Vérifier que l'indicateur de connexion affiche "Connecté"
- [ ] Ouvrir la console développeur et vérifier les logs WebSocket
- [ ] Tester la reconnexion automatique en coupant/rétablissant la connexion

### 2. Test des Notifications d'Inscription
- [ ] Se connecter en tant qu'étudiant
- [ ] S'inscrire à un club
- [ ] Vérifier que le chef de club reçoit une notification
- [ ] Vérifier que les admins reçoivent une notification broadcast
- [ ] Approuver l'inscription en tant qu'admin/chef
- [ ] Vérifier que l'étudiant reçoit une notification d'approbation
- [ ] Refuser une inscription
- [ ] Vérifier que l'étudiant reçoit une notification de refus

### 3. Test des Notifications de Club
- [ ] Se connecter en tant qu'admin
- [ ] Créer un nouveau club
- [ ] Vérifier que tous les utilisateurs reçoivent une notification broadcast
- [ ] Assigner un chef de club
- [ ] Vérifier que le nouveau chef reçoit une notification
- [ ] Modifier les informations d'un club
- [ ] Vérifier que les membres reçoivent une notification

### 4. Test des Notifications d'Événement
- [ ] Créer un nouvel événement
- [ ] Vérifier que tous les utilisateurs reçoivent une notification
- [ ] Modifier un événement existant
- [ ] Vérifier que les participants reçoivent une notification
- [ ] Annuler un événement
- [ ] Vérifier que tous les utilisateurs sont notifiés

### 5. Test de l'Interface Utilisateur
- [ ] Vérifier l'icône cloche dans la navbar (étudiants)
- [ ] Vérifier l'icône cloche dans la sidebar (admins)
- [ ] Tester le dropdown des notifications
- [ ] Vérifier le compteur de notifications non lues
- [ ] Tester le bouton "Tout marquer comme lu"
- [ ] Vérifier les notifications toast
- [ ] Tester les sons de notification

### 6. Test de la Page Historique
- [ ] Accéder à la page historique des notifications
- [ ] Tester les filtres par type
- [ ] Tester les filtres par statut (lu/non lu)
- [ ] Tester les filtres par date
- [ ] Tester la recherche textuelle
- [ ] Tester la pagination
- [ ] Vérifier les statistiques

### 7. Test Multi-Utilisateurs
- [ ] Ouvrir plusieurs onglets avec différents utilisateurs
- [ ] Effectuer des actions dans un onglet
- [ ] Vérifier que les autres onglets reçoivent les notifications
- [ ] Tester avec différents rôles (étudiant, admin, chef de club)

### 8. Test de Performance
- [ ] Créer plusieurs notifications rapidement
- [ ] Vérifier que l'interface reste réactive
- [ ] Tester avec un grand nombre de notifications
- [ ] Vérifier les temps de réponse

## 🔧 Commandes de Test

### Démarrer l'application
```bash
cd d:\UniClubs
mvn spring-boot:run
```

### Vérifier les logs
```bash
# Dans la console de l'application, chercher :
# - "Connecté aux notifications WebSocket"
# - "Notification envoyée à l'utilisateur"
# - "Notification broadcast envoyée"
```

### Test de l'API REST
```bash
# Compter les notifications non lues
curl -X GET "http://localhost:8080/api/notifications/count-non-lues" \
  -H "Cookie: JSESSIONID=your-session-id"

# Récupérer l'historique
curl -X GET "http://localhost:8080/api/notifications/historique?page=0&size=10" \
  -H "Cookie: JSESSIONID=your-session-id"
```

## 🐛 Problèmes Courants et Solutions

### WebSocket ne se connecte pas
- Vérifier que le port 8080 est libre
- Vérifier les logs de l'application
- Tester avec différents navigateurs

### Notifications non reçues
- Vérifier la connexion WebSocket
- Vérifier les logs du service
- Vérifier que l'utilisateur est bien connecté

### Interface ne se met pas à jour
- Vérifier la console JavaScript
- Vérifier que les scripts sont chargés
- Vérifier les erreurs CORS

## 📊 Métriques de Succès

- [ ] Connexion WebSocket réussie dans 100% des cas
- [ ] Notifications reçues en moins de 1 seconde
- [ ] Interface utilisateur réactive
- [ ] Aucune erreur JavaScript en console
- [ ] Tous les scénarios métier fonctionnent

## 🎉 Validation Finale

Une fois tous les tests passés :
- [ ] Le système de notifications est opérationnel
- [ ] Tous les workflows métier sont couverts
- [ ] L'interface utilisateur est intuitive
- [ ] Les performances sont satisfaisantes
- [ ] Aucun bug critique n'est présent

---

**Note :** Ce guide doit être exécuté après chaque déploiement pour s'assurer que le système fonctionne correctement.
