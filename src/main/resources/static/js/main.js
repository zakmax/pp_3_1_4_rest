// main.js
class UserManagement {
    constructor() {
        this.baseUrl = window.location.origin;
        this.currentUser = null;
        this.init();
    }

    init() {
        this.loadCurrentUser();
        this.loadUsersTable();
        this.setupEventListeners();
    }

    // Загрузка текущего пользователя
    async loadCurrentUser() {
        try {
            const response = await fetch(`${this.baseUrl}/api/users/current`);
            if (response.ok) {
                this.currentUser = await response.json();
                this.updateUserInfo();
            }
        } catch (error) {
            console.error('Error loading current user:', error);
        }
    }

    // Обновление информации о пользователе в интерфейсе
    updateUserInfo() {
        if (this.currentUser) {
            const userInfoElements = document.querySelectorAll('.user-info');
            userInfoElements.forEach(element => {
                element.textContent = `${this.currentUser.firstName} ${this.currentUser.lastName}`;
            });

            const userEmailElements = document.querySelectorAll('.user-email');
            userEmailElements.forEach(element => {
                element.textContent = this.currentUser.email;
            });
        }
    }

    // Загрузка таблицы пользователей
    async loadUsersTable() {
        try {
            const response = await fetch(`${this.baseUrl}/api/users`);
            if (response.ok) {
                const users = await response.json();
                this.renderUsersTable(users);
            } else {
                this.showError('Ошибка загрузки пользователей');
            }
        } catch (error) {
            console.error('Error loading users:', error);
            this.showError('Ошибка загрузки пользователей');
        }
    }

    // Рендер таблицы пользователей
    renderUsersTable(users) {
        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = '';

        users.forEach(user => {
            const row = this.createUserRow(user);
            tbody.appendChild(row);
        });
    }

    // Создание строки таблицы для пользователя
    createUserRow(user) {
        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.firstName}</td>
            <td>${user.lastName}</td>
            <td>${user.email}</td>
            <td>${user.age}</td>
            <td>${this.formatRoles(user.roles)}</td>
            <td>
                <button class="btn btn-warning btn-sm edit-user" data-id="${user.id}">
                    <i class="fas fa-edit"></i> Изменить
                </button>
                <button class="btn btn-danger btn-sm delete-user" data-id="${user.id}">
                    <i class="fas fa-trash"></i> Удалить
                </button>
            </td>
        `;

        // Добавляем обработчики событий
        row.querySelector('.edit-user').addEventListener('click', () => {
            this.openEditModal(user.id);
        });

        row.querySelector('.delete-user').addEventListener('click', () => {
            this.deleteUser(user.id);
        });

        return row;
    }

    // Форматирование ролей для отображения
    formatRoles(roles) {
        if (!roles || roles.length === 0) return '-';
        return roles.map(role => this.getRoleDisplayName(role)).join(', ');
    }

    // Получение отображаемого имени роли
    getRoleDisplayName(role) {
        const roleMap = {
            'admin': 'Администратор',
            'user': 'Пользователь'
        };
        return roleMap[role] || role;
    }

    // Открытие модального окна редактирования
    async openEditModal(userId) {
        try {
            const response = await fetch(`${this.baseUrl}/api/users/${userId}`);
            if (response.ok) {
                const user = await response.json();
                this.populateEditForm(user);
                $('#editUserModal').modal('show');
            } else {
                this.showError('Ошибка загрузки данных пользователя');
            }
        } catch (error) {
            console.error('Error loading user data:', error);
            this.showError('Ошибка загрузки данных пользователя');
        }
    }

    // Заполнение формы редактирования
    populateEditForm(user) {
        document.getElementById('editUserId').value = user.id;
        document.getElementById('editFirstName').value = user.firstName;
        document.getElementById('editLastName').value = user.lastName;
        document.getElementById('editEmail').value = user.email;
        document.getElementById('editAge').value = user.age;

        // Сбрасываем выбор ролей
        const roleCheckboxes = document.querySelectorAll('#editUserForm input[name="roles"]');
        roleCheckboxes.forEach(checkbox => {
            checkbox.checked = false;
        });

        // Устанавливаем выбранные роли
        if (user.roles && user.roles.length > 0) {
            user.roles.forEach(role => {
                const checkbox = document.querySelector(`#editUserForm input[name="roles"][value="${role}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }
    }

    // Удаление пользователя
    async deleteUser(userId) {
        if (!confirm('Вы уверены, что хотите удалить этого пользователя?')) {
            return;
        }

        try {
            const response = await fetch(`${this.baseUrl}/api/users/${userId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showSuccess('Пользователь успешно удален');
                this.loadUsersTable();
            } else {
                const errorText = await response.text();
                this.showError(`Ошибка удаления: ${errorText}`);
            }
        } catch (error) {
            console.error('Error deleting user:', error);
            this.showError('Ошибка удаления пользователя');
        }
    }

    // Настройка обработчиков событий
    setupEventListeners() {
        // Обработчик формы создания пользователя
        document.getElementById('createUserForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.createUser();
        });

        // Обработчик формы редактирования пользователя
        document.getElementById('editUserForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.updateUser();
        });

        // Обработчик кнопки создания пользователя
        document.getElementById('createUserBtn').addEventListener('click', () => {
            this.openCreateModal();
        });

        // Обработчики модальных окон
        $('#createUserModal').on('hidden.bs.modal', () => {
            document.getElementById('createUserForm').reset();
        });

        $('#editUserModal').on('hidden.bs.modal', () => {
            document.getElementById('editUserForm').reset();
        });
    }

    // Открытие модального окна создания
    openCreateModal() {
        $('#createUserModal').modal('show');
    }

    // Создание пользователя
    async createUser() {
        const formData = this.getFormData('createUserForm');

        try {
            const response = await fetch(`${this.baseUrl}/api/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                this.showSuccess('Пользователь успешно создан');
                $('#createUserModal').modal('hide');
                this.loadUsersTable();
            } else {
                const errorText = await response.text();
                this.showError(`Ошибка создания: ${errorText}`);
            }
        } catch (error) {
            console.error('Error creating user:', error);
            this.showError('Ошибка создания пользователя');
        }
    }

    // Обновление пользователя
    async updateUser() {
        const userId = document.getElementById('editUserId').value;
        const formData = this.getFormData('editUserForm');
        formData.id = parseInt(userId);

        try {
            const response = await fetch(`${this.baseUrl}/api/users/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                this.showSuccess('Пользователь успешно обновлен');
                $('#editUserModal').modal('hide');
                this.loadUsersTable();
            } else {
                const errorText = await response.text();
                this.showError(`Ошибка обновления: ${errorText}`);
            }
        } catch (error) {
            console.error('Error updating user:', error);
            this.showError('Ошибка обновления пользователя');
        }
    }

    // Получение данных из формы
    getFormData(formId) {
        const form = document.getElementById(formId);
        const formData = new FormData(form);
        const data = {};

        // Базовые поля
        data.firstName = formData.get('firstName');
        data.lastName = formData.get('lastName');
        data.email = formData.get('email');
        data.age = parseInt(formData.get('age'));

        // Пароль (только если указан)
        const password = formData.get('password');
        if (password && password.trim() !== '') {
            data.password = password;
        }

        // Роли
        const roles = [];
        const roleCheckboxes = form.querySelectorAll('input[name="roles"]:checked');
        roleCheckboxes.forEach(checkbox => {
            roles.push(checkbox.value);
        });
        data.roles = roles;

        return data;
    }

    // Показать уведомление об успехе
    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    // Показать уведомление об ошибке
    showError(message) {
        this.showNotification(message, 'danger');
    }

    // Показать уведомление
    showNotification(message, type) {
        // Создаем элемент уведомления
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        // Добавляем в контейнер для уведомлений
        const container = document.getElementById('notificationContainer') || this.createNotificationContainer();
        container.appendChild(alert);

        // Автоматически скрываем через 5 секунд
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 5000);
    }

    // Создать контейнер для уведомлений
    createNotificationContainer() {
        const container = document.createElement('div');
        container.id = 'notificationContainer';
        container.className = 'position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
        return container;
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    window.userManagement = new UserManagement();
});