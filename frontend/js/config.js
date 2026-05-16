const isLocal = window.location.hostname === 'localhost'
    || window.location.hostname === '127.0.0.1';

const API_HOST = isLocal
    ? 'http://localhost:8080'
    : 'https://emulataion-bank-project-12.onrender.com';