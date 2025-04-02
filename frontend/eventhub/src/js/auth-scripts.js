document.addEventListener('DOMContentLoaded', function() {
  // Переключение между вкладками
  const tabs = document.querySelectorAll('.auth-tab');
  tabs.forEach(tab => {
    tab.addEventListener('click', function() {
      const tabName = this.getAttribute('data-tab');
      switchTab(tabName);
    });
  });

  // Обработчики форм
  document.getElementById('login-form').addEventListener('submit', function(e) {
    e.preventDefault();
    handleLogin();
  });

  document.getElementById('register-form').addEventListener('submit', function(e) {
    e.preventDefault();
    handleRegister();
  });

  const toggleOptions = document.querySelectorAll('.toggle-option');
  const roleToggle = document.querySelector('.role-toggle');
  const roleInput = document.getElementById('user-role');

  toggleOptions.forEach(option => {
    option.addEventListener('click', function() {
      const role = this.getAttribute('data-role');

      // Обновляем активное состояние
      toggleOptions.forEach(opt => opt.classList.remove('active'));
      this.classList.add('active');

      // Обновляем значение в скрытом поле
      roleInput.value = role;

      // Добавляем атрибут для анимации
      roleToggle.setAttribute('data-active-role', role);
    });
  });
});

function switchTab(tabName) {
  // Переключение активных вкладок
  document.querySelectorAll('.auth-tab').forEach(tab => {
    tab.classList.remove('active');
  });

  document.querySelector(`.auth-tab[data-tab="${tabName}"]`).classList.add('active');

  // Переключение активных форм
  document.querySelectorAll('.auth-form').forEach(form => {
    form.classList.remove('active');
  });

  document.getElementById(`${tabName}-form`).classList.add('active');
}

function handleLogin() {
  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;

  console.log('Login attempt with:', { email, password });
  alert('Форма входа отправлена!');
  // Здесь можно добавить AJAX-запрос для авторизации
}

function handleRegister() {
  const name = document.getElementById('register-name').value;
  const email = document.getElementById('register-email').value;
  const password = document.getElementById('register-password').value;
  const confirm = document.getElementById('register-confirm').value;
  const role = document.getElementById('user-role').value;

  if (password !== confirm) {
    alert('Пароли не совпадают!');
    return;
  }

  console.log('Registration attempt with:', {
    name,
    email,
    password,
    role: role === 'participant' ? 'Участник' : 'Организатор'
  });

  alert(`Регистрация успешна! Роль: ${role === 'participant' ? 'Участник' : 'Организатор'}`);
  // Здесь можно добавить AJAX-запрос для регистрации
}
