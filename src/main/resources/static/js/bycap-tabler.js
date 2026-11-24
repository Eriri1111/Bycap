const API = '/api';

const REGISTERED_CREDENTIALS = {
  username: 'AlfonsoAraujo15',
  password: 'Araujo15'
};

let cachedTransactions = [];

const fmtCop = (cents) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })
    .format((cents || 0) / 100);

function appendAuditLog(message) {
  const list = document.getElementById('auditLogList');
  if (!list) return;
  const item = document.createElement('li');
  item.className = 'list-group-item small';
  item.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
  list.prepend(item);
}

// ==================== LOGIN ====================
const loginScreen = document.getElementById('loginScreen');
const appShell = document.getElementById('appShell');

function openDashboard(username) {
  sessionStorage.setItem('bycap_auth_user', username);
  document.getElementById('activeUserLabel').textContent = username;
  loginScreen.classList.add('d-none');
  appShell.classList.remove('d-none');
  appendAuditLog(`User "${username}" signed in.`);
  loadAllData();
}

const savedUser = sessionStorage.getItem('bycap_auth_user');
if (savedUser === REGISTERED_CREDENTIALS.username) {
  openDashboard(savedUser);
}

document.getElementById('loginForm').addEventListener('submit', (e) => {
  e.preventDefault();
  const user = document.getElementById('loginUser').value.trim();
  const pass = document.getElementById('loginPass').value.trim();
  const errBox = document.getElementById('loginError');

  if (user === REGISTERED_CREDENTIALS.username && pass === REGISTERED_CREDENTIALS.password) {
    errBox.classList.add('d-none');
    openDashboard(user);
  } else {
    errBox.classList.remove('d-none');
  }
});

document.getElementById('btnLogout').addEventListener('click', () => {
  sessionStorage.removeItem('bycap_auth_user');
  appShell.classList.add('d-none');
  loginScreen.classList.remove('d-none');
  document.getElementById('loginUser').value = '';
  document.getElementById('loginPass').value = '';
});

// ==================== COLLAPSIBLE SIDEBAR & NAVIGATION ====================
function toggleSidebar() {
  document.body.classList.toggle('sidebar-collapsed');
  const collapsed = document.body.classList.contains('sidebar-collapsed');
  const icon = document.getElementById('floatingTabIcon');
  icon.className = collapsed
    ? 'ti ti-layout-sidebar-left-expand fs-2'
    : 'ti ti-layout-sidebar-left-collapse fs-2';
}

document.getElementById('topSidebarToggle').addEventListener('click', toggleSidebar);
document.getElementById('floatingSidebarTab').addEventListener('click', toggleSidebar);

document.querySelectorAll('#sidebarNavList .nav-link').forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    document.querySelectorAll('#sidebarNavList .nav-link').forEach(l => l.classList.remove('active'));
    link.classList.add('active');

    const viewId = link.getAttribute('data-view');
    document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
    document.getElementById(viewId).classList.add('active');
    document.getElementById('currentViewTitle').textContent = link.textContent.trim();
  });
});

// ==================== DATA LOADING ====================
async function loadAllData() {
  try {
    const [health, metrics, txs, emps] = await Promise.all([
      fetch(`${API}/health`).then(r => r.json()),
      fetch(`${API}/dashboard/metrics`).then(r => r.json()),
      fetch(`${API}/transactions`).then(r => r.json()),
      fetch(`${API}/employees`).then(r => r.json())
    ]);

    cachedTransactions = txs;

    document.getElementById('dbStatusBadge').textContent = `● ${health.databaseEngine} Connected`;
    document.getElementById('kpiVolume').textContent = fmtCop(metrics.totalApprovedAmountCents);
    document.getElementById('kpiTotalTx').textContent = metrics.totalTransactionsCount;
    document.getElementById('kpiApprovalRate').textContent = `${metrics.approvalRate}%`;
    document.getElementById('kpiTeam').textContent = emps.length;

    const byMethod = metrics.volumeByMethodCents || {};
    const totalApproved = metrics.totalApprovedAmountCents || 1;
    document.getElementById('channelBreakdownBox').innerHTML = Object.entries(byMethod).map(([method, cents]) => {
      const pct = Math.round((cents / totalApproved) * 100);
      return `
        <div class="mb-3">
          <div class="d-flex justify-content-between mb-1">
            <strong>${method}</strong>
            <span>${fmtCop(cents)} (${pct}%)</span>
          </div>
          <div class="progress progress-sm">
            <div class="progress-bar bg-primary" style="width: ${pct}%"></div>
          </div>
        </div>
      `;
    }).join('');

    renderTransactionsTable();
    renderEmployeesTable(emps);
    appendAuditLog(`Synced ${txs.length} transactions and ${emps.length} staff records.`);
  } catch (err) {
    console.error('Failed to load dashboard data:', err);
  }
}

function renderTransactionsTable() {
  const q = document.getElementById('ledgerSearch').value.toLowerCase();
  const statusFilter = document.getElementById('ledgerStatusFilter').value;

  const filtered = cachedTransactions.filter(tx => {
    const matchesQ = (tx.reference || '').toLowerCase().includes(q) ||
                     (tx.customerName || '').toLowerCase().includes(q) ||
                     (tx.customerEmail || '').toLowerCase().includes(q);
    const matchesStatus = statusFilter === 'ALL' || tx.status === statusFilter;
    return matchesQ && matchesStatus;
  });

  document.getElementById('tablerTxBody').innerHTML = filtered.map(tx => `
    <tr>
      <td><code>${tx.reference}</code></td>
      <td>
        <div>${tx.customerName || 'Customer'}</div>
        <div class="text-secondary small">${tx.customerEmail || ''}</div>
      </td>
      <td><span class="badge bg-azure-lt">${tx.paymentMethod}</span></td>
      <td><strong>${fmtCop(tx.amountInCents)}</strong></td>
      <td><span class="badge ${tx.status === 'APPROVED' ? 'bg-green-lt' : 'bg-red-lt'}">${tx.status}</span></td>
      <td class="text-secondary small"><code>${tx.gatewayTransactionId || '-'}</code></td>
    </tr>
  `).join('');
}

document.getElementById('ledgerSearch').addEventListener('input', renderTransactionsTable);
document.getElementById('ledgerStatusFilter').addEventListener('change', renderTransactionsTable);

// ==================== PAYMENTS ====================
async function submitPayment({ customerName, customerEmail, amountCop, paymentMethodCode, token }) {
  const res = await fetch(`${API}/transactions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      customerName,
      customerEmail,
      amountInCents: amountCop * 100,
      paymentMethodCode,
      token
    })
  });
  const data = await res.json();
  await loadAllData();
  appendAuditLog(`Transaction ${data.reference || ''} (${paymentMethodCode}) -> ${data.status}`);
  return { ok: res.ok, data };
}

document.getElementById('tablerPayForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const result = await submitPayment({
    customerName: document.getElementById('payName').value,
    customerEmail: document.getElementById('payEmail').value,
    amountCop: Number(document.getElementById('payAmount').value),
    paymentMethodCode: document.getElementById('payMethod').value,
    token: document.getElementById('payToken').value
  });

  const box = document.getElementById('terminalReceiptBox');
  if (result.ok) {
    box.innerHTML = `
      <div class="alert ${result.data.status === 'APPROVED' ? 'alert-success' : 'alert-danger'} mb-0">
        <strong>Status: ${result.data.status}</strong><br>
        Reference: <code>${result.data.reference}</code><br>
        Amount: ${fmtCop(result.data.amountInCents)}<br>
        Message: ${result.data.gatewayMessage}
      </div>`;
  } else {
    box.innerHTML = `<div class="alert alert-danger mb-0">${result.data.message || 'Payment failed'}</div>`;
  }
});

document.querySelectorAll('.quick-nfc-btn').forEach(btn => {
  btn.addEventListener('click', async () => {
    const amountCop = Number(btn.getAttribute('data-amount'));
    const result = await submitPayment({
      customerName: 'NFC Tap Customer',
      customerEmail: 'nfc@bycap.io',
      amountCop,
      paymentMethodCode: 'NFC',
      token: 'tok_nfc_tap'
    });
    const alertDiv = document.getElementById('quickNfcAlert');
    if (result.ok) {
      alertDiv.innerHTML = `<div class="alert alert-success small mb-0">Approved NFC: <code>${result.data.reference}</code> (${fmtCop(result.data.amountInCents)})</div>`;
    }
  });
});

// ==================== TEAM & WEBHOOK ====================
function renderEmployeesTable(emps) {
  document.getElementById('teamTableBody').innerHTML = emps.map(emp => `
    <tr>
      <td>${emp.fullName}</td>
      <td class="text-secondary">${emp.email}</td>
      <td><span class="badge bg-purple-lt">${emp.role}</span></td>
      <td><span class="badge bg-green-lt">${emp.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
    </tr>
  `).join('');
}

document.getElementById('employeeForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const res = await fetch(`${API}/employees`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fullName: document.getElementById('empName').value,
      email: document.getElementById('empEmail').value,
      role: document.getElementById('empRole').value
    })
  });
  if (res.ok) {
    document.getElementById('empName').value = '';
    document.getElementById('empEmail').value = '';
    await loadAllData();
    appendAuditLog('New merchant employee saved.');
  }
});

document.getElementById('btnSendWebhook').addEventListener('click', async () => {
  const reference = document.getElementById('whReference').value.trim();
  const status = document.getElementById('whStatus').value;
  const amountInCents = Number(document.getElementById('whAmountCents').value);
  const timestamp = Math.floor(Date.now() / 1000);
  const wompiTransactionId = `wompi_evt_${Date.now()}`;
  const raw = `${wompiTransactionId}${status}${amountInCents}COP${timestamp}prod_events_bycap_secret_2026`;

  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(raw));
  const signature = Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('');

  const res = await fetch(`${API}/transactions/webhook/wompi`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      reference,
      wompiTransactionId,
      status,
      amountInCents,
      currency: 'COP',
      timestamp,
      signature
    })
  });
  const data = await res.json();
  document.getElementById('webhookOutput').textContent = JSON.stringify(data, null, 2);
  await loadAllData();
  appendAuditLog(`Dispatched webhook for ${reference || 'N/A'}.`);
});
document.getElementById('btnSyncAll').addEventListener('click', loadAllData);