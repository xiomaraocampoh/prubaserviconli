document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    initializeApp();
    addEventListeners();
    loadUserData();
    updateStatistics();
});

/**
 * Verifica autenticación
 */
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
    }
}

/**
 * Inicializar aplicación
 */
function initializeApp() {
    console.log('ServiSoft System Loading...');

    // Animación de entrada para cards
    const cards = document.querySelectorAll('.menu-card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${0.2 * (index + 1)}s`;
    });

    // Mostrar saludo
    showWelcomeMessage();
}

/**
 * Listeners de eventos
 */
function addEventListeners() {
    const menuCards = document.querySelectorAll('.menu-card');

    menuCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });

        card.addEventListener('mousedown', function() {
            this.style.transform = 'translateY(-8px) scale(0.98)';
        });

        card.addEventListener('mouseup', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === '1' || e.key.toLowerCase() === 'c') {
            navigateTo('clients.html');
        } else if (e.key === '2' || e.key.toLowerCase() === 't') {
            navigateTo('task.html');
        }
    });

    const userAvatar = document.querySelector('.user-avatar');
    if (userAvatar) {
        userAvatar.addEventListener('click', function() {
            showUserMenu();
        });
    }
}

/**
 * Navegación
 */
function navigateTo(page) {
    showLoadingState();

    const cards = document.querySelectorAll('.menu-card');
    cards.forEach(card => {
        card.classList.add('loading');
    });

    setTimeout(() => {
        if (page === 'clients.html' || page === 'task.html') {
            console.log(`Navegando a: ${page}`);
            document.body.style.opacity = '0';
            document.body.style.transform = 'scale(0.95)';

            setTimeout(() => {
                window.location.href = page;
            }, 300);
        } else {
            console.error('Página no encontrada:', page);
            showNotification('Página no encontrada', 'error');
            cards.forEach(card => card.classList.remove('loading'));
        }
    }, 500);
}

/**
 * Loading overlay
 */
function showLoadingState() {
    const loadingOverlay = document.createElement('div');
    loadingOverlay.className = 'loading-overlay';
    loadingOverlay.innerHTML = `
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>Cargando...</p>
        </div>
    `;

    const style = document.createElement('style');
    style.textContent = `
        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(245, 247, 250, 0.9);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            backdrop-filter: blur(5px);
        }
        
        .loading-spinner {
            text-align: center;
            color: #00b894;
        }
        
        .spinner {
            width: 40px;
            height: 40px;
            border: 4px solid rgba(0, 184, 148, 0.3);
            border-top: 4px solid #00b894;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 1rem;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;

    document.head.appendChild(style);
    document.body.appendChild(loadingOverlay);
}

/**
 * Cargar datos de usuario
 */
function loadUserData() {
    const userData = JSON.parse(localStorage.getItem('servisoft_user')) || {
        name: 'Administrador',
        avatar: 'A',
        welcomeMessage: 'Bienvenido al Sistema'
    };

    const userNameElement = document.querySelector('.user-name');
    const userAvatarElement = document.querySelector('.user-avatar span');
    const welcomeTextElement = document.querySelector('.welcome-text');

    if (userNameElement) userNameElement.textContent = userData.name;
    if (userAvatarElement) userAvatarElement.textContent = userData.avatar;
    if (welcomeTextElement) welcomeTextElement.textContent = userData.welcomeMessage;
}

/**
 * Cargar estadísticas desde el backend
 */
async function updateStatistics() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        const [clientsRes, tasksRes] = await Promise.all([
            fetch('http://localhost:8080/api/v1/pacientes', {
                headers: { Authorization: `Bearer ${token}` }
            }),
            fetch('http://localhost:8080/api/v1/tareas', {
                headers: { Authorization: `Bearer ${token}` }
            })
        ]);

        const clients = await clientsRes.json();
        const tasks = await tasksRes.json();

        const stats = {
            activeClients: clients.length,
            pendingTasks: tasks.filter(t => t.estado === 'PENDIENTE').length
        };

        animateNumber('.stat-card:nth-child(1) .stat-number', stats.activeClients);
        animateNumber('.stat-card:nth-child(2) .stat-number', stats.pendingTasks);

        localStorage.setItem('servisoft_stats', JSON.stringify(stats));
    } catch (err) {
        console.error('Error cargando estadísticas', err);
        showNotification('No se pudieron cargar las estadísticas', 'error');
    }
}

/**
 * Animar números
 */
function animateNumber(selector, finalValue) {
    const element = document.querySelector(selector);
    if (!element || isNaN(finalValue)) return;

    let currentValue = 0;
    const increment = finalValue / 50;
    const duration = 1000;
    const stepTime = duration / 50;

    const timer = setInterval(() => {
        currentValue += increment;
        if (currentValue >= finalValue) {
            element.textContent = finalValue;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(currentValue);
        }
    }, stepTime);
}

/**
 * Saludo dinámico
 */
function showWelcomeMessage() {
    const currentHour = new Date().getHours();
    let greeting;

    if (currentHour < 12) {
        greeting = 'Buenos días';
    } else if (currentHour < 18) {
        greeting = 'Buenas tardes';
    } else {
        greeting = 'Buenas noches';
    }

    setTimeout(() => {
        const welcomeText = document.querySelector('.welcome-text');
        if (welcomeText) {
            welcomeText.textContent = greeting;
        }
    }, 1000);
}

/**
 * Menú de usuario
 */
function showUserMenu() {
    const menu = document.createElement('div');
    menu.className = 'user-menu';
    menu.innerHTML = `
        <div class="menu-item" onclick="showProfile()">
            <span>👤 Perfil</span>
        </div>
        <div class="menu-item" onclick="showSettings()">
            <span>⚙️ Configuración</span>
        </div>
        <div class="menu-item" onclick="logout()">
            <span>🚪 Cerrar Sesión</span>
        </div>
    `;

    const style = document.createElement('style');
    style.textContent = `
        .user-menu {
            position: absolute;
            top: 100%;
            right: 0;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            min-width: 180px;
            z-index: 1000;
            overflow: hidden;
            margin-top: 0.5rem;
        }
        
        .menu-item {
            padding: 0.75rem 1rem;
            cursor: pointer;
            transition: all 0.2s;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .menu-item:hover {
            background: #f8f9fa;
            color: #00b894;
        }
        
        .menu-item:last-child {
            border-bottom: none;
        }
    `;

    document.head.appendChild(style);

    const userSection = document.querySelector('.user-section');
    userSection.style.position = 'relative';
    userSection.appendChild(menu);

    setTimeout(() => {
        document.addEventListener('click', function closeMenu(e) {
            if (!userSection.contains(e.target)) {
                menu.remove();
                document.removeEventListener('click', closeMenu);
            }
        });
    }, 100);
}

/**
 * Notificaciones
 */
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    if (!document.querySelector('style[data-notification]')) {
        const style = document.createElement('style');
        style.setAttribute('data-notification', 'true');
        style.textContent = `
            .notification {
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 1rem 1.5rem;
                border-radius: 8px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.1);
                z-index: 9999;
                font-weight: 500;
                animation: slideInRight 0.3s ease-out;
            }
            
            .notification.success {
                background: #10b981;
                color: white;
            }
            
            .notification.error {
                background: #ef4444;
                color: white;
            }
            
            .notification.info {
                background: #3b82f6;
                color: white;
            }
            
            @keyframes slideInRight {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideInRight 0.3s ease-out reverse';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

/**
 * Funciones de menú
 */
function showProfile() {
    showNotification('Función de perfil en desarrollo', 'info');
}

function showSettings() {
    showNotification('Función de configuración en desarrollo', 'info');
}

function logout() {
    if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
        localStorage.removeItem('servisoft_user');
        localStorage.removeItem('token');
        showNotification('Cerrando sesión...', 'success');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 1000);
    }
}

/**
 * Refrescar stats al volver a la pestaña
 */
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        updateStatistics();
    }
});
