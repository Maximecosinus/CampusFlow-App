// notifications.js - Gestion des notifications en temps réel avec WebSocket
class NotificationManager {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.userId = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
        
        this.init();
    }

    init() {
        // Récupérer l'ID utilisateur depuis le DOM ou une variable globale
        this.userId = this.getUserId();
        if (!this.userId) {
            console.log('Utilisateur non connecté, notifications désactivées');
            return;
        }

        this.connect();
        this.setupEventListeners();
        this.loadInitialNotifications();
    }

    getUserId() {
        // Récupérer l'ID utilisateur depuis une balise meta ou un attribut data
        const userIdElement = document.querySelector('meta[name="user-id"]');
        if (userIdElement) {
            return userIdElement.getAttribute('content');
        }
        
        // Fallback: récupérer depuis un attribut data sur le body
        const body = document.body;
        return body.getAttribute('data-user-id');
    }

    connect() {
        try {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            
            // Configuration pour éviter les warnings de debug
            this.stompClient.debug = function(str) {
                // console.log('STOMP: ' + str);
            };

            this.stompClient.connect({}, (frame) => {
                console.log('Connecté aux notifications WebSocket: ' + frame);
                this.connected = true;
                this.reconnectAttempts = 0;
                
                this.subscribeToNotifications();
                this.updateConnectionStatus(true);
            }, (error) => {
                console.error('Erreur de connexion WebSocket:', error);
                this.connected = false;
                this.updateConnectionStatus(false);
                this.scheduleReconnect();
            });
        } catch (error) {
            console.error('Erreur lors de l\'initialisation de WebSocket:', error);
            this.scheduleReconnect();
        }
    }

    subscribeToNotifications() {
        if (!this.stompClient || !this.connected) return;

        // S'abonner aux notifications personnelles
        this.stompClient.subscribe('/queue/notifications/' + this.userId, (message) => {
            const notification = JSON.parse(message.body);
            this.handleNotification(notification);
        });

        // S'abonner aux notifications broadcast
        this.stompClient.subscribe('/topic/notifications', (message) => {
            const notification = JSON.parse(message.body);
            this.handleNotification(notification);
        });
    }

    handleNotification(notification) {
        console.log('Nouvelle notification reçue:', notification);
        
        // Mettre à jour le compteur de notifications
        this.updateNotificationCount();
        
        // Afficher une notification toast
        this.showToastNotification(notification);
        
        // Jouer un son de notification (optionnel)
        this.playNotificationSound();
        
        // Mettre à jour la liste des notifications dans les dropdowns
        this.addNotificationToDropdown(notification);
        this.addNotificationToDropdownAdmin(notification);
    }

    showToastNotification(notification) {
        // Créer l'élément toast
        const toast = document.createElement('div');
        toast.className = 'notification-toast';
        toast.innerHTML = `
            <div class="toast-content">
                <div class="toast-header">
                    <strong>${this.escapeHtml(notification.titre)}</strong>
                    <span class="toast-type badge bg-${this.getTypeColor(notification.type)}">${notification.type}</span>
                </div>
                <div class="toast-body">
                    ${this.escapeHtml(notification.message)}
                </div>
                <div class="toast-time">
                    ${this.formatTime(notification.dateCreation)}
                </div>
            </div>
            <button class="toast-close" onclick="this.parentElement.remove()">×</button>
        `;

        // Ajouter au conteneur des toasts
        let toastContainer = document.getElementById('toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toast-container';
            toastContainer.className = 'toast-container';
            document.body.appendChild(toastContainer);
        }

        toastContainer.appendChild(toast);

        // Animation d'apparition
        setTimeout(() => toast.classList.add('show'), 100);

        // Suppression automatique après 5 secondes
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    }

    addNotificationToDropdown(notification) {
        const notificationList = document.getElementById('notification-list');
        if (!notificationList) return;

        const notificationElement = document.createElement('div');
        notificationElement.className = 'notification-item';
        notificationElement.innerHTML = `
            <div class="notification-content">
                <div class="notification-title">${this.escapeHtml(notification.titre)}</div>
                <div class="notification-message">${this.escapeHtml(notification.message)}</div>
                <div class="notification-time">${this.formatTime(notification.dateCreation)}</div>
            </div>
            <div class="notification-actions">
                <button class="btn btn-sm btn-outline-primary" onclick="notificationManager.markAsRead('${notification.id}')">
                    Marquer comme lu
                </button>
            </div>
        `;

        // Ajouter en haut de la liste
        notificationList.insertBefore(notificationElement, notificationList.firstChild);

        // Limiter à 10 notifications dans le dropdown
        const items = notificationList.querySelectorAll('.notification-item');
        if (items.length > 10) {
            items[items.length - 1].remove();
        }
    }

    addNotificationToDropdownAdmin(notification) {
        const notificationList = document.getElementById('notification-list-admin');
        if (!notificationList) return;

        const notificationElement = document.createElement('div');
        notificationElement.className = 'notification-item';
        notificationElement.innerHTML = `
            <div class="notification-content">
                <div class="notification-title">${this.escapeHtml(notification.titre)}</div>
                <div class="notification-message">${this.escapeHtml(notification.message)}</div>
                <div class="notification-time">${this.formatTime(notification.dateCreation)}</div>
            </div>
            <div class="notification-actions">
                <button class="btn btn-sm btn-outline-primary" onclick="notificationManager.markAsRead('${notification.id}')">
                    Marquer comme lu
                </button>
            </div>
        `;

        // Ajouter en haut de la liste
        notificationList.insertBefore(notificationElement, notificationList.firstChild);

        // Limiter à 10 notifications dans le dropdown
        const items = notificationList.querySelectorAll('.notification-item');
        if (items.length > 10) {
            items[items.length - 1].remove();
        }
    }

    updateNotificationCount() {
        fetch('/api/notifications/count-non-lues')
            .then(response => response.json())
            .then(data => {
                // Mettre à jour le badge dans la navbar étudiante
                const badge = document.getElementById('notif-count');
                if (badge) {
                    badge.textContent = data.count;
                    badge.style.display = data.count > 0 ? 'inline' : 'none';
                }
                
                // Mettre à jour le badge dans la sidebar admin
                const badgeAdmin = document.getElementById('notif-count-admin');
                if (badgeAdmin) {
                    badgeAdmin.textContent = data.count;
                    badgeAdmin.style.display = data.count > 0 ? 'inline' : 'none';
                }
            })
            .catch(error => console.error('Erreur lors de la mise à jour du compteur:', error));
    }

    loadInitialNotifications() {
        // Charger les notifications récentes au chargement de la page
        fetch('/api/notifications/recentes')
            .then(response => response.json())
            .then(notifications => {
                // Mettre à jour la liste dans la navbar étudiante
                const notificationList = document.getElementById('notification-list');
                if (notificationList) {
                    notificationList.innerHTML = '';
                    notifications.forEach(notification => {
                        this.addNotificationToDropdown(notification);
                    });
                }
                
                // Mettre à jour la liste dans la sidebar admin
                const notificationListAdmin = document.getElementById('notification-list-admin');
                if (notificationListAdmin) {
                    notificationListAdmin.innerHTML = '';
                    notifications.forEach(notification => {
                        this.addNotificationToDropdownAdmin(notification);
                    });
                }
            })
            .catch(error => console.error('Erreur lors du chargement des notifications:', error));

        // Mettre à jour le compteur
        this.updateNotificationCount();
    }

    markAsRead(notificationId) {
        fetch(`/api/notifications/${notificationId}/marquer-lu`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => response.json())
        .then(data => {
            console.log('Notification marquée comme lue:', data);
            this.updateNotificationCount();
        })
        .catch(error => console.error('Erreur lors du marquage:', error));
    }

    markAllAsRead() {
        fetch('/api/notifications/marquer-tout-lu', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => response.json())
        .then(data => {
            console.log('Toutes les notifications marquées comme lues:', data);
            this.updateNotificationCount();
            
            // Mettre à jour l'interface
            const notificationList = document.getElementById('notification-list');
            if (notificationList) {
                notificationList.innerHTML = '<div class="text-center text-muted">Aucune notification non lue</div>';
            }
            
            const notificationListAdmin = document.getElementById('notification-list-admin');
            if (notificationListAdmin) {
                notificationListAdmin.innerHTML = '<div class="text-center text-muted">Aucune notification non lue</div>';
            }
        })
        .catch(error => console.error('Erreur lors du marquage:', error));
    }

    scheduleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Tentative de reconnexion ${this.reconnectAttempts}/${this.maxReconnectAttempts} dans ${this.reconnectDelay}ms`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay);
        } else {
            console.error('Nombre maximum de tentatives de reconnexion atteint');
        }
    }

    updateConnectionStatus(connected) {
        const statusIndicator = document.getElementById('connection-status');
        if (statusIndicator) {
            statusIndicator.className = connected ? 'connected' : 'disconnected';
            statusIndicator.textContent = connected ? 'Connecté' : 'Déconnecté';
        }
        
        const statusIndicatorAdmin = document.getElementById('connection-status-admin');
        if (statusIndicatorAdmin) {
            statusIndicatorAdmin.className = connected ? 'connected' : 'disconnected';
            statusIndicatorAdmin.textContent = connected ? 'Connecté' : 'Déconnecté';
        }
    }

    // Méthodes utilitaires
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    getTypeColor(type) {
        const colors = {
            'CLUB': 'primary',
            'EVENEMENT': 'success',
            'INSCRIPTION': 'info',
            'ADMIN': 'warning',
            'SYSTEME': 'secondary'
        };
        return colors[type] || 'secondary';
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // Moins d'une minute
            return 'À l\'instant';
        } else if (diff < 3600000) { // Moins d'une heure
            return `Il y a ${Math.floor(diff / 60000)} min`;
        } else if (diff < 86400000) { // Moins d'un jour
            return `Il y a ${Math.floor(diff / 3600000)} h`;
        } else {
            return date.toLocaleDateString('fr-FR');
        }
    }

    playNotificationSound() {
        // Créer et jouer un son de notification simple
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
            oscillator.frequency.setValueAtTime(600, audioContext.currentTime + 0.1);
            
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.2);
            
            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.2);
        } catch (error) {
            console.log('Impossible de jouer le son de notification:', error);
        }
    }

    setupEventListeners() {
        // Écouter les clics sur le bouton "Tout marquer comme lu"
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-action="mark-all-read"]')) {
                e.preventDefault();
                this.markAllAsRead();
            }
        });

        // Écouter les clics sur les notifications pour les marquer comme lues
        document.addEventListener('click', (e) => {
            if (e.target.closest('.notification-item')) {
                const notificationItem = e.target.closest('.notification-item');
                const notificationId = notificationItem.dataset.notificationId;
                if (notificationId) {
                    this.markAsRead(notificationId);
                }
            }
        });
    }

    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.disconnect();
            this.connected = false;
            console.log('Déconnecté des notifications WebSocket');
        }
    }
}

// Initialiser le gestionnaire de notifications quand le DOM est chargé
let notificationManager;
document.addEventListener('DOMContentLoaded', function() {
    notificationManager = new NotificationManager();
});

// Exposer globalement pour les appels depuis HTML
window.notificationManager = notificationManager;
window.markAsRead = function(notificationId) {
    if (notificationManager) {
        notificationManager.markAsRead(notificationId);
    }
};

window.markAllAsRead = function() {
    if (notificationManager) {
        notificationManager.markAllAsRead();
    }
};
