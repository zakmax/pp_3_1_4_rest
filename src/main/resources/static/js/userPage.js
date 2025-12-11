// userPage.js
class UserPage {
    constructor() {
        this.baseUrl = window.location.origin;
        this.init();
    }

    async init() {
        await this.loadUserInfo();
    }

    async loadUserInfo() {
        try {
            const response = await fetch(`${this.baseUrl}/api/users/current`);
            if (response.ok) {
                const user = await response.json();
                this.renderUserInfo(user);
            }
        } catch (error) {
            console.error('Error loading user info:', error);
        }
    }

    renderUserInfo(user) {
        document.getElementById('userFirstName').textContent = user.firstName;
        document.getElementById('userLastName').textContent = user.lastName;
        document.getElementById('userEmail').textContent = user.email;
        document.getElementById('userAge').textContent = user.age;

        const rolesElement = document.getElementById('userRoles');
        rolesElement.innerHTML = user.roles.map(role =>
            `<span class="badge bg-primary">${this.getRoleDisplayName(role)}</span>`
        ).join(' ');
    }

    getRoleDisplayName(role) {
        const roleMap = {
            'admin': 'Администратор',
            'user': 'Пользователь'
        };
        return roleMap[role] || role;
    }
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    new UserPage();
});