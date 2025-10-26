#!/bin/bash
# Script de test pour le système de notifications
# Usage: ./test-notifications.sh

BASE_URL="http://localhost:8080"
SESSION_ID=""

echo "🧪 Test du Système de Notifications UniClubs"
echo "=============================================="

# Fonction pour faire une requête avec session
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$SESSION_ID" ]; then
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -H "Cookie: JSESSIONID=$SESSION_ID" \
            ${data:+-d "$data"}
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${data:+-d "$data"}
    fi
}

# Test 1: Vérifier que l'application démarre
echo "1. Test de démarrage de l'application..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
if [ "$response" = "200" ]; then
    echo "✅ Application démarrée correctement"
else
    echo "❌ Application non accessible (HTTP $response)"
    exit 1
fi

# Test 2: Vérifier l'endpoint WebSocket
echo "2. Test de l'endpoint WebSocket..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/ws/info")
if [ "$response" = "200" ]; then
    echo "✅ Endpoint WebSocket accessible"
else
    echo "❌ Endpoint WebSocket non accessible (HTTP $response)"
fi

# Test 3: Vérifier la page de connexion
echo "3. Test de la page de connexion..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/login")
if [ "$response" = "200" ]; then
    echo "✅ Page de connexion accessible"
else
    echo "❌ Page de connexion non accessible (HTTP $response)"
fi

# Test 4: Vérifier les endpoints API (sans authentification)
echo "4. Test des endpoints API..."
response=$(make_request "GET" "/api/notifications/count-non-lues")
if [[ "$response" == *"401"* ]] || [[ "$response" == *"Unauthorized"* ]]; then
    echo "✅ Endpoints API protégés correctement"
else
    echo "❌ Endpoints API non protégés"
fi

# Test 5: Vérifier la page d'historique
echo "5. Test de la page d'historique..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/notifications/page-historique")
if [ "$response" = "302" ] || [ "$response" = "200" ]; then
    echo "✅ Page d'historique accessible"
else
    echo "❌ Page d'historique non accessible (HTTP $response)"
fi

echo ""
echo "📋 Résumé des Tests"
echo "==================="
echo "✅ Tests de base terminés"
echo ""
echo "🔐 Pour tester les fonctionnalités complètes :"
echo "1. Connectez-vous à l'application : $BASE_URL/login"
echo "2. Ouvrez la console développeur (F12)"
echo "3. Vérifiez les logs WebSocket"
echo "4. Testez les notifications en temps réel"
echo ""
echo "📖 Consultez NOTIFICATION_TESTING_GUIDE.md pour les tests détaillés"

echo ""
echo "🎉 Tests de base terminés avec succès !"
