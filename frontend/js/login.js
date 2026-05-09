document.addEventListener('DOMContentLoaded', () => {
    checkUrlForTokenOrError();

    const loginForm = document.querySelector('form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

function checkUrlForTokenOrError() {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const error = params.get('error');

    if (token) {
        console.log("OAuth2 token found, saving...");
        sessionStorage.setItem('accessToken', token);

        window.history.replaceState({}, document.title, window.location.pathname);
        window.location.href = 'dashboard.html';
        return;
    }

    if (error) {
        alert(decodeURIComponent(error));
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

async function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const btn = document.querySelector('button[type="submit"]');

    if (!email || !password) return alert("Please fill in all fields");

    const originalBtnText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in...';

    try {
        // Backend expects @RequestBody UserLogin { username, password }
        // The "username" field is actually used for email lookup on the backend
        const response = await fetch(`http://localhost:8080/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: email, password: password })
        });

        if (response.ok) {
            const data = await response.json();
            sessionStorage.setItem('accessToken', data.token);
            window.location.href = data.redirectUrl || 'dashboard.html';
        } else {
            alert("Login failed. Please check your credentials.");
        }
    } catch (error) {
        console.error('Login error:', error);
        alert("Network error. Please try again later.");
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalBtnText;
    }
}