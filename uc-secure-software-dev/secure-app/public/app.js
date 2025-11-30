const apiUrl = 'api.php';

function setMessage(text, type = '') {
  const el = document.getElementById('message');
  el.textContent = text;
  el.className = 'message ' + (type ? type : '');
}

function createCell(text) {
  const td = document.createElement('td');
  td.textContent = text; // safe: escapes automatically
  return td;
}

async function loadUsers() {
  const tbody = document.getElementById('userTableBody');
  tbody.innerHTML = '';
  const loadingRow = document.createElement('tr');
  const loadingCell = document.createElement('td');
  loadingCell.colSpan = 5;
  loadingCell.textContent = 'Loading...';
  loadingRow.appendChild(loadingCell);
  tbody.appendChild(loadingRow);

  try {
    const res = await fetch(apiUrl, { method: 'GET' });

    if (!res.ok) {
      const text = await res.text();
      tbody.innerHTML = '';
      const row = document.createElement('tr');
      const cell = document.createElement('td');
      cell.colSpan = 5;
      cell.textContent = `Server error (${res.status}): ${text}`;
      row.appendChild(cell);
      tbody.appendChild(row);
      return;
    }

    const json = await res.json();
    if (!json.success || !Array.isArray(json.data)) {
      tbody.innerHTML = '';
      const row = document.createElement('tr');
      const cell = document.createElement('td');
      cell.colSpan = 5;
      cell.textContent = 'No data';
      row.appendChild(cell);
      tbody.appendChild(row);
      return;
    }

    tbody.innerHTML = '';

    json.data.forEach(user => {
      const row = document.createElement('tr');
      row.appendChild(createCell(String(user.id)));
      row.appendChild(createCell(user.username));
      row.appendChild(createCell(user.email));
      row.appendChild(createCell(user.role));
      row.appendChild(createCell(user.created_at ?? ''));
      tbody.appendChild(row);
    });
  } catch (err) {
    tbody.innerHTML = '';
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 5;
    cell.textContent = 'Network error: ' + err.message;
    row.appendChild(cell);
    tbody.appendChild(row);
  }
}

async function createUser(e) {
  e.preventDefault();

  const usernameInput = document.getElementById('username');
  const emailInput    = document.getElementById('email');
  const roleSelect    = document.getElementById('role');

  const username = usernameInput.value.trim();
  const email    = emailInput.value.trim();
  const role     = roleSelect.value;

  // Basic client-side validation (server still validates!)
  if (username.length < 3 || username.length > 32) {
    setMessage('Username must be between 3 and 32 characters', 'error');
    return;
  }
  if (!/^[A-Za-z0-9_]+$/.test(username)) {
    setMessage('Username may only contain letters, numbers, and underscores', 'error');
    return;
  }
  if (!email || email.length > 100 || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
    setMessage('Please enter a valid email (max 100 characters)', 'error');
    return;
  }

  setMessage('Creating user...', '');

  try {
    const res = await fetch(apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, role })
    });

    const json = await res.json();

    if (!res.ok || !json.success) {
      setMessage(json.error || 'Error creating user', 'error');
      return;
    }

    setMessage(`User "${json.data.username}" created (ID ${json.data.id})`, 'success');

    // Reset form fields (safe: we're setting values, not HTML)
    usernameInput.value = '';
    emailInput.value = '';
    roleSelect.value = 'user';

    // Reload list
    loadUsers();
  } catch (err) {
    setMessage('Network error: ' + err.message, 'error');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('userForm').addEventListener('submit', createUser);
  document.getElementById('refreshBtn').addEventListener('click', loadUsers);

  loadUsers();
});
