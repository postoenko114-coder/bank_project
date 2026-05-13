document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const error = params.get('error');

    if (token) {
        sessionStorage.setItem('accessToken', token);

        setTimeout(() => {
            window.location.href = '/dashboard.html';
        }, 500);
    } else {
        const errorMsg = error || 'Authentication failed';
        window.location.href = '/login.html?error=' + encodeURIComponent(errorMsg);
    }
});