/**
 * dashboard.js - Main dashboard logic
 *
 * Handles:
 * - Auth guard (redirect to login if not authenticated)
 * - Loading and displaying expenses
 * - Adding new expenses
 * - Deleting expenses
 * - Filtering and searching expenses
 * - Getting AI budget suggestions
 * - Stats calculation
 */

// ==========================================
// CONFIGURATION & STATE
// ==========================================

const API_BASE = 'http://localhost:8080/api';

// In-memory store for all fetched expenses (for filtering without re-fetching)
let allExpenses = [];

// ==========================================
// AUTH GUARD - Protect Dashboard
// ==========================================

/**
 * Check if user is logged in. If not, redirect to login page.
 * This runs immediately when the page loads.
 */
const token = localStorage.getItem('token');
if (!token) {
    window.location.href = 'login.html';
}

/**
 * Get the stored JWT token.
 * Every API call to a secured endpoint needs this in the Authorization header.
 */
function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}` // Format: "Bearer eyJhbGci..."
    };
}

// ==========================================
// INITIALIZATION
// ==========================================

/**
 * Initialize the dashboard when the page loads.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Display user's name from localStorage
    const userName = localStorage.getItem('userName') || 'User';
    document.getElementById('userName').textContent = userName;

    // Set default date in form to today
    document.getElementById('expDate').value = new Date().toISOString().split('T')[0];

    // Load expenses from backend
    loadExpenses();
});

// ==========================================
// LOGOUT
// ==========================================

/**
 * Logout: clear localStorage (removes JWT), redirect to login.
 * Since JWT is stateless, there's nothing to do on the server side.
 * The token will expire on its own after 24 hours.
 */
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userId');
    window.location.href = 'login.html';
}

// ==========================================
// LOAD EXPENSES
// ==========================================

/**
 * Fetch all expenses for the logged-in user from the backend.
 */
async function loadExpenses() {
    showLoadingState();

    try {
        const response = await fetch(`${API_BASE}/expenses`, {
            method: 'GET',
            headers: getAuthHeaders() // JWT token required!
        });

        // If 401 Unauthorized, token is invalid/expired → redirect to login
        if (response.status === 401) {
            logout();
            return;
        }

        if (response.ok) {
            const expenses = await response.json();
            allExpenses = expenses; // Cache for filtering
            renderExpenses(expenses);
            updateStats(expenses);
        } else {
            showErrorInList('Failed to load expenses. Please refresh.');
        }

    } catch (error) {
        console.error('Load expenses error:', error);
        showErrorInList('Cannot connect to server. Make sure the backend is running on port 8080.');
    }
}

// ==========================================
// ADD EXPENSE
// ==========================================

/**
 * Handle expense form submission.
 */
document.getElementById('expenseForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const title = document.getElementById('expTitle').value.trim();
    const amount = parseFloat(document.getElementById('expAmount').value);
    const category = document.getElementById('expCategory').value;
    const date = document.getElementById('expDate').value;

    // Client-side validation
    if (!title || !amount || !category || !date) {
        showFormAlert('Please fill in all fields', true);
        return;
    }

    if (amount <= 0) {
        showFormAlert('Amount must be greater than 0', true);
        return;
    }

    setAddButtonLoading(true);

    try {
        const response = await fetch(`${API_BASE}/expenses`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                title,
                amount,
                category,
                date // Format: "2024-01-15" (ISO date)
            })
        });

        if (response.status === 401) { logout(); return; }

        if (response.ok) {
            const newExpense = await response.json();

            // Add new expense to the front of the array (newest first)
            allExpenses.unshift(newExpense);

            // Re-render and update stats
            renderExpenses(allExpenses);
            updateStats(allExpenses);

            // Clear form
            document.getElementById('expenseForm').reset();
            document.getElementById('expDate').value = new Date().toISOString().split('T')[0];

            showFormAlert('Expense added successfully! 🎉', false);

        } else {
            const data = await response.json();
            const errorMsg = data.details
                ? Object.values(data.details).join(', ')
                : (data.error || 'Failed to add expense');
            showFormAlert(errorMsg, true);
        }

    } catch (error) {
        console.error('Add expense error:', error);
        showFormAlert('Cannot connect to server.', true);
    } finally {
        setAddButtonLoading(false);
    }
});

// ==========================================
// DELETE EXPENSE
// ==========================================

/**
 * Delete an expense by ID.
 * @param {number} expenseId - The ID of the expense to delete
 */
async function deleteExpense(expenseId) {
    if (!confirm('Delete this expense?')) return;

    try {
        const response = await fetch(`${API_BASE}/expenses/${expenseId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.status === 401) { logout(); return; }

        if (response.ok) {
            // Remove from local array (no need to re-fetch from server)
            allExpenses = allExpenses.filter(exp => exp.id !== expenseId);
            renderExpenses(allExpenses);
            updateStats(allExpenses);
        } else {
            alert('Failed to delete expense. Please try again.');
        }

    } catch (error) {
        console.error('Delete error:', error);
        alert('Cannot connect to server.');
    }
}

// ==========================================
// RENDER EXPENSES
// ==========================================

/**
 * Render the list of expenses to the DOM.
 * @param {Array} expenses - Array of expense objects
 */
function renderExpenses(expenses) {
    const listEl = document.getElementById('expenseList');
    const loadingEl = document.getElementById('expenseListLoading');
    const emptyEl = document.getElementById('emptyState');

    loadingEl.style.display = 'none';

    if (expenses.length === 0) {
        listEl.style.display = 'none';
        emptyEl.style.display = 'flex';
        return;
    }

    emptyEl.style.display = 'none';
    listEl.style.display = 'flex';

    // Build HTML for all expense items
    listEl.innerHTML = expenses.map(expense => `
        <div class="expense-item" data-id="${expense.id}">
            <div class="expense-item-left">
                <div class="expense-emoji">${getCategoryEmoji(expense.category)}</div>
                <div class="expense-info">
                    <div class="expense-title">${escapeHtml(expense.title)}</div>
                    <div class="expense-meta">
                        <span class="category-badge badge-${expense.category.toLowerCase()}">${expense.category}</span>
                        &nbsp; ${formatDate(expense.date)}
                    </div>
                </div>
            </div>
            <div class="expense-item-right">
                <span class="expense-amount">₹${formatAmount(expense.amount)}</span>
                <button class="btn-delete" onclick="deleteExpense(${expense.id})" title="Delete">✕</button>
            </div>
        </div>
    `).join('');
}

// ==========================================
// STATS CALCULATION
// ==========================================

/**
 * Calculate and display summary statistics.
 */
function updateStats(expenses) {
    if (expenses.length === 0) {
        document.getElementById('totalSpent').textContent = '₹0.00';
        document.getElementById('totalCount').textContent = '0';
        document.getElementById('topCategory').textContent = '–';
        document.getElementById('latestExpense').textContent = '–';
        return;
    }

    // Total amount
    const total = expenses.reduce((sum, exp) => sum + parseFloat(exp.amount), 0);
    document.getElementById('totalSpent').textContent = `₹${formatAmount(total)}`;

    // Total count
    document.getElementById('totalCount').textContent = expenses.length;

    // Top spending category
    const categoryTotals = {};
    expenses.forEach(exp => {
        categoryTotals[exp.category] = (categoryTotals[exp.category] || 0) + parseFloat(exp.amount);
    });
    const topCategory = Object.keys(categoryTotals).reduce((a, b) =>
        categoryTotals[a] > categoryTotals[b] ? a : b
    );
    document.getElementById('topCategory').textContent = `${getCategoryEmoji(topCategory)} ${topCategory}`;

    // Latest expense (first in array since sorted by date desc)
    document.getElementById('latestExpense').textContent = expenses[0].title.substring(0, 15);
}

// ==========================================
// FILTER & SEARCH
// ==========================================

/**
 * Filter expenses by search text and/or category.
 * Works on the cached allExpenses array — no API call needed.
 */
function filterExpenses() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const categoryFilter = document.getElementById('categoryFilter').value;

    const filtered = allExpenses.filter(expense => {
        const matchesSearch = !searchText ||
            expense.title.toLowerCase().includes(searchText) ||
            expense.category.toLowerCase().includes(searchText);

        const matchesCategory = !categoryFilter || expense.category === categoryFilter;

        return matchesSearch && matchesCategory;
    });

    renderExpenses(filtered);
}

// ==========================================
// AI SUGGESTION
// ==========================================

/**
 * Get AI budget suggestions from the backend.
 * Backend calls OpenAI and returns personalized advice.
 */
async function getAiSuggestion() {
    const aiBtn = document.getElementById('aiBtn');
    const aiLoading = document.getElementById('aiLoading');
    const aiContainer = document.getElementById('aiContainer');
    const aiContent = document.getElementById('aiContent');

    // Show loading state
    aiBtn.disabled = true;
    aiBtn.textContent = '⏳ Analyzing...';
    aiLoading.style.display = 'flex';
    aiContainer.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE}/expenses/ai-suggestion`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.status === 401) { logout(); return; }

        if (response.ok) {
            const data = await response.json();

            // Display the AI suggestion
            aiContent.textContent = data.suggestion;
            aiContainer.style.display = 'block';

        } else {
            aiContent.textContent = '⚠️ Could not generate AI suggestion. Please check your OpenAI API key in application.properties.';
            aiContainer.style.display = 'block';
        }

    } catch (error) {
        console.error('AI suggestion error:', error);
        aiContent.textContent = '⚠️ Cannot connect to server. Make sure the backend is running.';
        aiContainer.style.display = 'block';
    } finally {
        aiLoading.style.display = 'none';
        aiBtn.disabled = false;
        aiBtn.textContent = '✨ Get AI Suggestion';
    }
}

// ==========================================
// UI HELPERS
// ==========================================

function showLoadingState() {
    document.getElementById('expenseListLoading').style.display = 'flex';
    document.getElementById('expenseList').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
}

function showErrorInList(message) {
    document.getElementById('expenseListLoading').style.display = 'none';
    document.getElementById('emptyState').style.display = 'flex';
    document.getElementById('emptyState').innerHTML = `
        <div class="empty-icon">⚠️</div>
        <p>${message}</p>
    `;
}

function showFormAlert(message, isError) {
    const errorEl = document.getElementById('formError');
    const successEl = document.getElementById('formSuccess');

    if (isError) {
        errorEl.textContent = message;
        errorEl.style.display = 'block';
        successEl.style.display = 'none';
        setTimeout(() => { errorEl.style.display = 'none'; }, 4000);
    } else {
        successEl.textContent = message;
        successEl.style.display = 'block';
        errorEl.style.display = 'none';
        setTimeout(() => { successEl.style.display = 'none'; }, 3000);
    }
}

function setAddButtonLoading(isLoading) {
    const btn = document.getElementById('addExpBtn');
    const textEl = btn.querySelector('.btn-text');
    const loaderEl = btn.querySelector('.btn-loader');
    btn.disabled = isLoading;
    textEl.style.display = isLoading ? 'none' : 'inline';
    loaderEl.style.display = isLoading ? 'inline' : 'none';
}

// ==========================================
// FORMATTING HELPERS
// ==========================================

/**
 * Format a number as currency (e.g., 1234.5 → "1,234.50")
 */
function formatAmount(amount) {
    return parseFloat(amount).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

/**
 * Format a date string (e.g., "2024-01-15" → "Jan 15, 2024")
 */
function formatDate(dateStr) {
    const date = new Date(dateStr + 'T00:00:00'); // Force local timezone
    return date.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

/**
 * Get emoji for a category.
 */
function getCategoryEmoji(category) {
    const emojis = {
        'Food':          '🍔',
        'Travel':        '✈️',
        'Shopping':      '🛍️',
        'Bills':         '⚡',
        'Healthcare':    '🏥',
        'Entertainment': '🎬',
        'Education':     '📚',
        'Groceries':     '🛒',
        'Rent':          '🏠',
        'Other':         '📦'
    };
    return emojis[category] || '💳';
}

/**
 * Prevent XSS by escaping HTML characters in user-provided text.
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}
