const container = document.querySelector('.container');
const registerBtn = document.querySelector('.register-btn');
const loginBtn = document.querySelector('.login-btn');

// Hàm kiểm tra lỗi trong form đăng ký
function hasErrors(form) {
    return form.querySelectorAll('.error-message').length > 0;
}

registerBtn.addEventListener('click', () => {
    const registerForm = document.querySelector('.form-box.register');
    if (!hasErrors(registerForm)) {
        container.classList.add('active');
    }
});

loginBtn.addEventListener('click', () => {
    const loginForm = document.querySelector('.form-box.login');
    if (!hasErrors(loginForm)) {
        container.classList.remove('active');
    }
});
