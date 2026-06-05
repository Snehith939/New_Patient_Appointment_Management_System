const BASE_URL = 'http://localhost:8081/api';

// ═══════════════════════════════════════
//  AUTH UTILITIES
// ═══════════════════════════════════════

function getLoggedInUser() {
  return JSON.parse(localStorage.getItem('user'));
}

function requireLogin() {
  if (!getLoggedInUser()) {
    window.location.href = 'login.html';
  }
}

function logout() {
  fetch(`${BASE_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include'
  }).finally(() => {
    localStorage.removeItem('user');
    window.location.href = 'login.html';
  });
}

// ✅ Simple authenticated fetch — just sends session cookie
async function authFetch(url, options = {}) {
  if (!getLoggedInUser()) {
    window.location.href = 'login.html';
    return;
  }

  options.credentials = 'include';
  options.headers = {
    ...options.headers,
    'Content-Type': 'application/json'
  };

  const res = await fetch(url, options);

  if (res.status === 401) {
    alert('Session expired. Please login again.');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
    return;
  }

  return res;
}


// ═══════════════════════════════════════
//  REGISTER — Dynamic Fields
// ═══════════════════════════════════════

const roleSelect = document.getElementById('role');
const extraFields = document.getElementById('extraFields');

if (roleSelect && extraFields) {
  roleSelect.addEventListener('change', () => {
    const role = roleSelect.value;

    if (role === 'PATIENT') {
      extraFields.innerHTML = `
        <input type="text" class="form-control mb-2" id="phone"
               placeholder="Phone" required>
        <input type="text" class="form-control mb-2" id="address"
               placeholder="Address" required>
        <label class="form-label">Date of Birth</label>
        <input type="date" class="form-control mb-2" id="dob" required>
      `;
    }
    else if (role === 'DOCTOR') {
      extraFields.innerHTML = `
        <input type="text" class="form-control mb-2" id="phone"
               placeholder="Phone" required>
        <input type="text" class="form-control mb-2" id="specialization"
               placeholder="Specialization" required>
        <label class="form-label">Start Time</label>
        <input type="time" class="form-control mb-2" id="start" required>
        <label class="form-label">End Time</label>
        <input type="time" class="form-control mb-2" id="end" required>
      `;
    }
    else if (role === 'ADMIN') {
      extraFields.innerHTML = `
        <input type="text" class="form-control mb-2" id="adminRole"
               placeholder="Admin Role" required>
      `;
    }
    else {
      extraFields.innerHTML = '';
    }
  });
}


// ═══════════════════════════════════════
//  REGISTER SUBMIT
// ═══════════════════════════════════════

const registerForm = document.getElementById('registerForm');

if (registerForm) {
  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const role = document.getElementById('role').value;
    const msg  = document.getElementById('message');

    if (!role || role === 'ROLE') {
      msg.innerText = '⚠️ Select a role';
      msg.className = 'message error';
      return;
    }

    let data = {
      name:     document.getElementById('name').value,
      email:    document.getElementById('email').value,
      password: document.getElementById('password').value,
      role:     role
    };

    if (role === 'PATIENT') {
      data.phone   = document.getElementById('phone').value;
      data.address = document.getElementById('address').value;
      data.dob     = document.getElementById('dob').value;
    }
    else if (role === 'DOCTOR') {
      data.phone          = document.getElementById('phone').value;
      data.specialization = document.getElementById('specialization').value;
      data.startTime      = document.getElementById('start').value;
      data.endTime        = document.getElementById('end').value;
    }
    else if (role === 'ADMIN') {
      data.adminRole = document.getElementById('adminRole').value;
    }

    try {
      const res = await fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (res.ok) {
        msg.innerText = '✅ Registered! Redirecting...';
        msg.className = 'message success';
        setTimeout(() => window.location.href = 'login.html', 1500);
      } else {
        const err = await res.json();
        msg.innerText = err.message || '❌ Failed!';
        msg.className = 'message error';
      }
    } catch (e) {
      msg.innerText = '❌ Server error!';
      msg.className = 'message error';
    }
  });
}


// ═══════════════════════════════════════
//  LOGIN SUBMIT
// ═══════════════════════════════════════

const loginForm = document.getElementById('loginForm');

if (loginForm) {
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const msg = document.getElementById('loginMessage');

    const data = {
      email:    document.getElementById('loginEmail').value,
      password: document.getElementById('loginPassword').value,
      role:     document.getElementById('loginRole').value
    };

    if (!data.role) {
      msg.innerText = '⚠️ Select a role';
      msg.className = 'message error';
      return;
    }

    try {
      const res = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data)
      });

      const result = await res.json();

      if (res.ok) {
        msg.innerText = '✅ Login successful!';
        msg.className = 'message success';

        localStorage.setItem('user', JSON.stringify(result));

        setTimeout(() => {
          if (result.role === 'ADMIN')
            window.location.href = 'admin_appointments.html';
          else if (result.role === 'DOCTOR')
            window.location.href = 'doctor_dashboard.html';
          else
            window.location.href = 'patient_dashboard.html';
        }, 1000);

      } else {
        msg.innerText = result.message || '❌ Login failed!';
        msg.className = 'message error';
      }
    } catch (e) {
      msg.innerText = '❌ Server error!';
      msg.className = 'message error';
    }
  });
}