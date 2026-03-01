const API_BASE = '/api/v1';

const api = {
    async request(method, path, body = null) {
        const headers = { 'Content-Type': 'application/json' };
        const token = localStorage.getItem('token');
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const opts = { method, headers };
        if (body) {
            opts.body = JSON.stringify(body);
        }

        const res = await fetch(API_BASE + path, opts);

        if (res.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            if (!window.location.pathname.includes('login')) {
                window.location.href = '/login.html';
            }
        }

        return res;
    },

    async login(email, senha) {
        const res = await this.request('POST', '/auth/login', { email, senha });
        if (!res.ok) {
            const err = await res.json().catch(() => null);
            const msg = err?.detail || err?.title || 'Erro ao fazer login';
            throw new Error(msg);
        }
        const data = await res.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }));
        return data;
    },

    async register(nome, email, senha) {
        const res = await this.request('POST', '/auth/register', { nome, email, senha });
        if (!res.ok) {
            const err = await res.json().catch(() => null);
            const msg = err?.detail || err?.title || 'Erro ao cadastrar';
            throw new Error(msg);
        }
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login.html';
    },

    getUser() {
        const raw = localStorage.getItem('user');
        return raw ? JSON.parse(raw) : null;
    },

    isAuthenticated() {
        return !!localStorage.getItem('token');
    }
};
