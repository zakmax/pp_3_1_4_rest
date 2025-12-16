
// main.js
class UserManagement {
    constructor() {
        this.baseUrl = window.location.origin;
        this.currentUser = null;
        this.csrfToken = null;
        this.init();
    }

    init() {
        this.loadCsrfToken();
        this.loadCurrentUser();
        this.loadUsersTable();
        this.setupEventListeners();
        this.setupLogoutButton();
    }

    // Загрузка CSRF токена
    loadCsrfToken() {
        // Ищем CSRF токен в разных местах
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfInput = document.querySelector('input[name="_csrf"]');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

        if (csrfMeta) {
            this.csrfToken = csrfMeta.content;
            console.log('CSRF token loaded from meta tag');
        } else if (csrfInput) {
            this.csrfToken = csrfInput.value;
            console.log('CSRF token loaded from input');
        }

        if (this.csrfToken) {
            console.log('CSRF token:', this.csrfToken.substring(0, 10) + '...');
        } else {
            console.warn('CSRF token not found');
        }
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
        if (!tbody) return;

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
                method: 'DELETE',
                headers: this.getHeaders()
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
        const createForm = document.getElementById('createUserForm');
        if (createForm) {
            createForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.createUser();
            });
        }

        // Обработчик формы редактирования пользователя
        const editForm = document.getElementById('editUserForm');
        if (editForm) {
            editForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.updateUser();
            });
        }

        // Обработчик кнопки создания пользователя
        const createBtn = document.getElementById('createUserBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => {
                this.openCreateModal();
            });
        }

        // Обработчики модальных окон
        $('#createUserModal').on('hidden.bs.modal', () => {
            const form = document.getElementById('createUserForm');
            if (form) form.reset();
        });

        $('#editUserModal').on('hidden.bs.modal', () => {
            const form = document.getElementById('editUserForm');
            if (form) form.reset();
        });
    }

    // Настройка кнопки logout
    setupLogoutButton() {
        const logoutForms = document.querySelectorAll('form[action*="/logout"]');

        logoutForms.forEach(form => {
            // Убираем стандартное поведение формы
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.performLogout(form);
            });
        });
    }

    // Выполнение logout через Fetch API
    async performLogout(formElement) {
        try {
            // Собираем данные формы
            const formData = new FormData(formElement);

            // Добавляем CSRF токен если есть
            if (this.csrfToken) {
                formData.append('_csrf', this.csrfToken);
            }

            // Отправляем запрос
            const response = await fetch('/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(formData).toString()
            });

            if (response.ok || response.redirected) {
                // Редирект на страницу логина
                window.location.href = '/login?logout';
            } else {
                this.showError('Ошибка при выходе из системы');
            }
        } catch (error) {
            console.error('Logout error:', error);
            this.showError('Ошибка при выходе из системы');
        }
    }

    // Получение заголовков для запросов
    getHeaders() {
        const headers = {
            'Content-Type': 'application/json',
        };

        // Добавляем CSRF токен если есть
        if (this.csrfToken) {
            headers['X-CSRF-TOKEN'] = this.csrfToken;
        }

        return headers;
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
                headers: this.getHeaders(),
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
                headers: this.getHeaders(),
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
        if (!form) return {};

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
    console.log('DOM loaded, initializing UserManagement...');
    window.userManagement = new UserManagement();

    // Если мы на странице admin, загружаем таблицу
    if (window.location.pathname.includes('/admin')) {
        console.log('Admin page detected, loading users table...');
        window.userManagement.loadUsersTable();
    }
});