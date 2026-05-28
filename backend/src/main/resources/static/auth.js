/**
 * auth.js - Handles Login and Registration logic
 *
 * Features:
 * - Login
 * - Registration
 * - JWT token storage
 * - Redirect handling
 * - Error handling
 * - Loading buttons
 */

// ==========================================
// CONFIGURATION
// ==========================================

// Backend API Base URL
const API_BASE = '/api';


// ==========================================
// ALERT FUNCTION
// ==========================================

/**
 * Shows success/error messages
 */
function showAlert(elementId, message, isError = true) {

    const el = document.getElementById(elementId);

    if (!el) return;

    el.textContent = message;
    el.style.display = 'block';

    // Optional styling
    if (isError) {
        el.style.color = 'red';
    } else {
        el.style.color = 'green';
    }

    // Auto hide after 5 seconds
    setTimeout(() => {
        el.style.display = 'none';
    }, 5000);
}


// ==========================================
// BUTTON LOADING FUNCTION
// ==========================================

/**
 * Shows loading spinner/text on buttons
 */
function setButtonLoading(btnId, isLoading) {

    const btn = document.getElementById(btnId);

    if (!btn) return;

    const textEl = btn.querySelector('.btn-text');
    const loaderEl = btn.querySelector('.btn-loader');

    btn.disabled = isLoading;

    if (textEl) {
        textEl.style.display = isLoading ? 'none' : 'inline';
    }

    if (loaderEl) {
        loaderEl.style.display = isLoading ? 'inline' : 'none';
    }
}


// ==========================================
// ERROR PARSER
// ==========================================

/**
 * Reads backend error response
 */
async function parseErrorMessage(response) {

    try {

        const data = await response.json();

        // Validation errors
        if (data.details && typeof data.details === 'object') {
            return Object.values(data.details).join(', ');
        }

        return data.error || data.message || 'Something went wrong';

    } catch {

        return `Error ${response.status}: Server error`;

    }
}


// ==========================================
// LOGIN HANDLER
// ==========================================

const loginForm = document.getElementById('loginForm');

if (loginForm) {

    loginForm.addEventListener('submit', async (e) => {

        e.preventDefault();

        // Get form values
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        // Validation
        if (!email || !password) {

            showAlert('errorAlert', 'Please fill in all fields');

            return;
        }

        setButtonLoading('loginBtn', true);

        try {

            // API Call
            const response = await fetch(`${API_BASE}/auth/login`, {

                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify({
                    email,
                    password
                })

            });

            // SUCCESS
            if (response.ok) {

                const data = await response.json();

                // Validate token
                if (!data || !data.token) {

                    showAlert(
                        'errorAlert',
                        'Invalid login response from server'
                    );

                    return;
                }

                // Store JWT token
                localStorage.setItem('token', data.token);

                // Store user data
                localStorage.setItem('userName', data.name || '');
                localStorage.setItem('userEmail', data.email || '');
                localStorage.setItem('userId', data.userId || '');

                showAlert(
                    'successAlert',
                    'Login successful! Redirecting...',
                    false
                );

                // Redirect
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 1000);

            }

            // ERROR
            else {

                const errorMsg = await parseErrorMessage(response);

                showAlert('errorAlert', errorMsg);

            }

        }

            // NETWORK ERROR
        catch (error) {

            console.error('Login Error:', error);

            showAlert(
                'errorAlert',
                'Unable to connect to backend server'
            );

        }

        finally {

            setButtonLoading('loginBtn', false);

        }

    });

}


// ==========================================
// REGISTER HANDLER
// ==========================================

const registerForm = document.getElementById('registerForm');

if (registerForm) {

    registerForm.addEventListener('submit', async (e) => {

        e.preventDefault();

        // Get form values
        const name = document.getElementById('name').value.trim();

        const email = document.getElementById('email').value.trim();

        const password = document.getElementById('password').value;

        // Validation
        if (!name || !email || !password) {

            showAlert('errorAlert', 'Please fill all fields');

            return;
        }

        if (password.length < 6) {

            showAlert(
                'errorAlert',
                'Password must be at least 6 characters'
            );

            return;
        }

        setButtonLoading('registerBtn', true);

        try {

            // API Call
            const response = await fetch(`${API_BASE}/auth/register`, {

                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify({
                    name,
                    email,
                    password
                })

            });

            // SUCCESS
            if (response.ok) {

                const data = await response.json();

                // Validate token
                if (!data || !data.token) {

                    showAlert(
                        'errorAlert',
                        'Registration successful but token missing'
                    );

                    return;
                }

                // Store JWT token
                localStorage.setItem('token', data.token);

                // Store user data
                localStorage.setItem('userName', data.name || '');
                localStorage.setItem('userEmail', data.email || '');
                localStorage.setItem('userId', data.userId || '');

                showAlert(
                    'successAlert',
                    'Account created successfully!',
                    false
                );

                // Redirect
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 1000);

            }

            // ERROR
            else {

                const errorMsg = await parseErrorMessage(response);

                showAlert('errorAlert', errorMsg);

            }

        }

            // NETWORK ERROR
        catch (error) {

            console.error('Register Error:', error);

            showAlert(
                'errorAlert',
                'Unable to connect to backend server'
            );

        }

        finally {

            setButtonLoading('registerBtn', false);

        }

    });

}


// ==========================================
// AUTO REDIRECT IF ALREADY LOGGED IN
// ==========================================

// Get JWT token
const token = localStorage.getItem('token');

// Redirect only if already logged in
if (

    token &&

    (
        window.location.pathname.includes('login.html') ||
        window.location.pathname.includes('register.html')
    )

) {

    window.location.href = 'dashboard.html';

}