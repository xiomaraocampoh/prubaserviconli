const API_PATIENTS = 'http://localhost:8080/api/v1/patients';
let clients = [];
let filteredClients = [];
let editingClientNumeroId = null;
let editingClientType = null;

// Lista de EPS
const EPS_LIST = [
    "EPS_SURA", "COOSALUD_EPS_S", "NUEVA_EPS", "MUTUAL_SER", "ALIANSALUD_EPS",
    "SALUD_TOTAL_EPS_SA", "EPS_SANITAS", "FAMISANAR", "SERVICIO_OCCIDENTAL_DE_SALUD_EPS_SOS",
    "SALUD_MIA", "COMFENALCO", "COMPENSAR_EPS", "EPM_EMPRESAS_PUBLICAS_DE_MEDELLIN",
    "FONDO_DE_PASIVO_SOCIAL_DE_FERROCARRILES_NACIONALES_DE_COLOMBIA", "CAJACOPI_ATLANTICO",
    "CAPRESOCA", "COMFACHOCO", "COMFAMILIAR_DE_LA_GUAJIRA", "COMFAORIENTE",
    "EPS_FAMILIAR_DE_COLOMBIA", "ASMET_SALUD", "CAPITAL_SALUD_EPS_S", "CONVIDA",
    "SAVIA_SALUD_EPS", "DUSAKAWI_EPSI", "ASOCIACION_INDIGENA_DEL_CAUCA_EPSI",
    "ANAS_WAYUU_EPSI", "MALLAMAS_EPSI", "PIJAOS_SALUD_EPS"
];

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    loadClients();
    populateEpsSelects();
    setupEventListeners();
});

// Verificar autenticación
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.nombre) {
        document.getElementById('userInfo').textContent = userInfo.nombre;
    }
}

// Headers con token
function getAuthHeaders() {
    return {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
    };
}

// Event Listeners
function setupEventListeners() {
    document.addEventListener('click', function(event) {
        const modals = [
            document.getElementById('cotizanteModal'),
            document.getElementById('beneficiarioModal'),
            document.getElementById('clientDetailModal')
        ];
        modals.forEach(modal => {
            if (event.target === modal) modal.classList.remove('active');
        });
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeCotizanteModal();
            closeBeneficiarioModal();
            closeClientDetailModal();
        }
    });
}

// EPS selects
function populateEpsSelects() {
    const selects = [
        document.getElementById('cotizanteEps'),
        document.getElementById('beneficiarioEps'),
        document.getElementById('filterEps')
    ];

    selects.forEach(select => {
        if (!select) return;
        while (select.children.length > 1) {
            select.removeChild(select.lastChild);
        }
        EPS_LIST.forEach(eps => {
            const option = document.createElement('option');
            option.value = eps;
            option.textContent = formatEpsName(eps);
            select.appendChild(option);
        });
    });
}

// Formato EPS
function formatEpsName(eps) {
    return eps.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
}

// Cargar clientes con campos adicionales y clasificación de tipoPaciente
async function loadClients() {
    try {
        showLoadingState();

        const response = await fetch(`${API_PATIENTS}/search?nombre=`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error("Error al obtener clientes");

        const data = await response.json();

        clients = data.map(c => {
            // Inferencia del tipo de paciente
            const inferred =
                (c.parentesco && c.parentesco !== 'COTIZANTE') ? 'BENEFICIARIO' : 'COTIZANTE';

            return {
                ...c,
                tipoPaciente: (c.tipoPaciente && c.tipoPaciente.toUpperCase()) || inferred,
                // Campos adicionales de cotizantes
                estadoCliente: c.estadoCliente || 'ACTIVO',
                fechaExpedicion: c.fechaExpedicion || null,
                infoAdicional: c.infoAdicional || '',
                // Campos adicionales de beneficiarios
                celular: c.celular || null,
                correo: c.correo || null,
                direccionResidencia: c.direccionResidencia || null,
                fechaNacimiento: c.fechaNacimiento || null,
                parentesco: c.parentesco || null,
                cotizanteNumeroIdentificacion: c.cotizanteNumeroIdentificacion || null
            };
        });

        filteredClients = [...clients];
        renderClients();
        updateFilters();

        // Autocompletado para cotizantes en formulario de beneficiario
        updateCotizantesDatalist(clients);
    } catch (error) {
        console.error('Error al cargar clientes:', error);
        showNotification('Error al cargar los clientes', 'error');
        showEmptyState('Error al cargar clientes');
    }
}

// Generar datalist de cotizantes para autocompletar beneficiarios
function updateCotizantesDatalist(allClients) {
    const datalist = document.getElementById('cotizantesList');
    if (!datalist) return;

    datalist.innerHTML = '';
    allClients
        .filter(c => c.tipoPaciente === 'COTIZANTE')
        .forEach(c => {
            const option = document.createElement('option');
            option.value = c.numeroIdentificacion;
            option.label = c.nombreCompleto;
            datalist.appendChild(option);
        });
}



// Renderizado
function showLoadingState() {
    document.getElementById('clientsGrid').innerHTML = `<div class="empty-state">⏳ Cargando clientes...</div>`;
}
function showEmptyState(message = 'No hay clientes registrados') {
    document.getElementById('clientsGrid').innerHTML = `
        <div class="empty-state">👥 ${message}</div>
    `;
}
function renderClients() {
    const grid = document.getElementById('clientsGrid');
    if (filteredClients.length === 0) return showEmptyState('No se encontraron clientes');

    grid.innerHTML = filteredClients.map(client => `
        <div class="client-card ${client.tipoPaciente.toLowerCase()}" data-id="${client.numeroIdentificacion}">
            <div class="client-header">
                <div class="client-type ${client.tipoPaciente.toLowerCase()}">
                    ${client.tipoPaciente === 'COTIZANTE' ? '👤 Cotizante' : '👤  Beneficiario'}
                </div>
            </div>
            <div class="client-name">${client.nombreCompleto}</div>
            <div class="client-id">${client.tipoIdentificacion} ${client.numeroIdentificacion}</div>
            <div class="client-details">
                <div class="client-detail">🏥 ${formatEpsName(client.eps)}</div>
                ${client.celular ? `<div class="client-detail">📱 ${client.celular}</div>` : ''}
                ${client.correo ? `<div class="client-detail">✉️ ${client.correo}</div>` : ''}
                ${client.tipoPaciente === 'BENEFICIARIO' && client.parentesco ?
        `<div class="client-parentesco">${formatParentesco(client.parentesco)}</div>` : ''}
            </div>
            ${client.tipoPaciente === 'BENEFICIARIO' && client.cotizanteNumeroIdentificacion ? `
                <div class="client-cotizante-info">
                    <div class="label">Cotizante asociado:</div>
                    <div class="value">${client.cotizanteNumeroIdentificacion}</div>
                </div>` : ''}
            <div class="client-actions">
                <button class="client-action view" onclick="viewClient('${client.numeroIdentificacion}')">👁️ Ver</button>
                <button class="client-action edit" onclick="editClient('${client.numeroIdentificacion}')">✏️ Editar</button>
                <button class="client-action delete" onclick="deleteClient('${client.numeroIdentificacion}')">🗑️ Eliminar</button>
            </div>
        </div>
    `).join('');
}

// Parentesco
function formatParentesco(p) {
    const map = { 'ESPOSO_A':'Esposo(a)', 'HIJO_A':'Hijo(a)', 'HERMANO_A':'Hermano(a)', 'MAMA':'Mamá', 'PAPA':'Papá' };
    return map[p] || p;
}

// Filtros
function updateFilters() {
    const filterEps = document.getElementById('filterEps');
    const usedEps = [...new Set(clients.map(c => c.eps))];
    while (filterEps.children.length > 1) filterEps.removeChild(filterEps.lastChild);
    usedEps.forEach(eps => {
        const option = document.createElement('option');
        option.value = eps;
        option.textContent = formatEpsName(eps);
        filterEps.appendChild(option);
    });
}
function applyFilters() {
    const tipo = document.getElementById('filterTipo').value;
    const eps = document.getElementById('filterEps').value;
    const search = document.getElementById('searchClients').value.toLowerCase();
    filteredClients = clients.filter(c =>
        (!tipo || c.tipoPaciente === tipo) &&
        (!eps || c.eps === eps) &&
        (!search || c.nombreCompleto.toLowerCase().includes(search) || c.numeroIdentificacion.includes(search))
    );
    renderClients();
}

// --------------------- CRUD ---------------------

// Cotizante
function openCotizanteModal() {
    editingClientNumeroId = null;
    editingClientType = 'COTIZANTE';
    document.getElementById('formCotizante').reset();
    document.getElementById('cotizanteModal').classList.add('active');
}
function closeCotizanteModal() { document.getElementById('cotizanteModal').classList.remove('active'); }
async function saveCotizante() {
    const data = {
        nombreCompleto: document.getElementById('cotizanteNombre').value,
        tipoIdentificacion: document.getElementById('cotizanteTipoId').value,
        numeroIdentificacion: document.getElementById('cotizanteNumeroId').value,
        eps: document.getElementById('cotizanteEps').value,
        celular: document.getElementById('cotizanteCelular').value || null,
        correo: document.getElementById('cotizanteCorreo').value || null,
        direccionResidencia: document.getElementById('cotizanteDireccion').value || null,
        fechaNacimiento: document.getElementById('cotizanteFechaNacimiento').value || null,
        fechaExpedicion: document.getElementById('cotizanteFechaExpedicion').value || null,
        estadoCliente: document.getElementById('cotizanteEstadoCliente').value,
        infoAdicional: document.getElementById('cotizanteInfoAdicional').value || null

    };
    const method = editingClientNumeroId ? 'PUT' : 'POST';
    const url = editingClientNumeroId ? `${API_PATIENTS}/cotizantes/${editingClientNumeroId}` : `${API_PATIENTS}/cotizantes`;
    const res = await fetch(url, { method, headers: getAuthHeaders(), body: JSON.stringify(data) });
    if (res.ok) { closeCotizanteModal(); loadClients(); showNotification('Cotizante guardado','success'); }
    else showNotification('Error al guardar cotizante','error');
}

// Beneficiario
function openBeneficiarioModal() {
    editingClientNumeroId = null;
    editingClientType = 'BENEFICIARIO';
    document.getElementById('formBeneficiario').reset();
    document.getElementById('beneficiarioModal').classList.add('active');
}
function closeBeneficiarioModal() { document.getElementById('beneficiarioModal').classList.remove('active'); }
async function saveBeneficiario() {
    const data = {
        nombreCompleto: document.getElementById('beneficiarioNombre').value,
        tipoIdentificacion: document.getElementById('beneficiarioTipoId').value,
        numeroIdentificacion: document.getElementById('beneficiarioNumeroId').value,
        eps: document.getElementById('beneficiarioEps').value,
        parentesco: document.getElementById('beneficiarioParentesco').value,
        cotizanteNumeroIdentificacion: document.getElementById('beneficiarioCotizanteNumeroId').value,
        celular: document.getElementById('beneficiarioCelular').value || null,
        correo: document.getElementById('beneficiarioCorreo').value || null,
        direccionResidencia: document.getElementById('beneficiarioDireccion').value || null,
        fechaExpedicion: document.getElementById('beneficiarioFechaExpedicion').value || null,
        fechaNacimiento: document.getElementById('beneficiarioFechaNacimiento').value || null,
        infoAdicional: document.getElementById('beneficiarioInfoAdicional').value || null

    };
    const method = editingClientNumeroId ? 'PUT' : 'POST';
    const url = editingClientNumeroId ? `${API_PATIENTS}/beneficiarios/${editingClientNumeroId}` : `${API_PATIENTS}/beneficiarios`;
    const res = await fetch(url, { method, headers: getAuthHeaders(), body: JSON.stringify(data) });
    if (res.ok) { closeBeneficiarioModal(); loadClients(); showNotification('Beneficiario guardado','success'); }
    else showNotification('Error al guardar beneficiario','error');
}

// Ver / Editar / Eliminar
function viewClient(numeroId) {
    const client = clients.find(c => c.numeroIdentificacion === numeroId);
    if (!client) return;
    document.getElementById('clientDetailContent').innerHTML = `
        <h4>Información</h4>
        <p><b>Nombre:</b> ${client.nombreCompleto}</p>
        <p><b>Identificación:</b> ${client.tipoIdentificacion} ${client.numeroIdentificacion}</p>
        <p><b>EPS:</b> ${formatEpsName(client.eps)}</p>
        <p><b>Celular:</b> ${client.celular || 'No registrado'}</p>
        <p><b>Correo:</b> ${client.correo || 'No registrado'}</p>
        <p><b>Dirección:</b> ${client.direccionResidencia || 'No registrada'}</p>
        ${client.parentesco ? `<p><b>Parentesco:</b> ${formatParentesco(client.parentesco)}</p>` : ''}
    `;
    document.getElementById('clientDetailModal').classList.add('active');
}
function closeClientDetailModal() { document.getElementById('clientDetailModal').classList.remove('active'); }

function editClient(numeroId) {
    const client = clients.find(c => c.numeroIdentificacion === numeroId);
    if (!client) return;
    if (client.tipoPaciente === 'COTIZANTE') {
        editingClientNumeroId = client.numeroIdentificacion;
        document.getElementById('cotizanteNombre').value = client.nombreCompleto;
        document.getElementById('cotizanteTipoId').value = client.tipoIdentificacion;
        document.getElementById('cotizanteNumeroId').value = client.numeroIdentificacion;
        document.getElementById('cotizanteEps').value = client.eps;
        document.getElementById('cotizanteCelular').value = client.celular || '';
        document.getElementById('cotizanteCorreo').value = client.correo || '';
        document.getElementById('cotizanteDireccion').value = client.direccionResidencia || '';
        document.getElementById('cotizanteFechaNacimiento').value = client.fechaNacimiento || '';
        document.getElementById('cotizanteFechaExpedicion').value = client.fechaExpedicion || '';
        document.getElementById('cotizanteEstadoCliente').value = client.estadoCliente || 'ACTIVO';
        document.getElementById('cotizanteInfoAdicional').value = client.infoAdicional || '';

        document.getElementById('cotizanteModal').classList.add('active');
    } else {
        editingClientNumeroId = client.numeroIdentificacion;
        document.getElementById('beneficiarioNombre').value = client.nombreCompleto;
        document.getElementById('beneficiarioTipoId').value = client.tipoIdentificacion;
        document.getElementById('beneficiarioNumeroId').value = client.numeroIdentificacion;
        document.getElementById('beneficiarioEps').value = client.eps;
        document.getElementById('beneficiarioParentesco').value = client.parentesco || '';
        document.getElementById('beneficiarioCotizanteNumeroId').value = client.cotizanteNumeroIdentificacion || '';
        document.getElementById('beneficiarioCelular').value = client.celular || '';
        document.getElementById('beneficiarioCorreo').value = client.correo || '';
        document.getElementById('beneficiarioDireccion').value = client.direccionResidencia || '';
        document.getElementById('beneficiarioFechaExpedicion').value = client.fechaExpedicion || '';
        document.getElementById('beneficiarioFechaNacimiento').value = client.fechaNacimiento || '';
        document.getElementById('beneficiarioInfoAdicional').value = client.infoAdicional || '';

        document.getElementById('beneficiarioModal').classList.add('active');
    }
}
async function deleteClient(numeroId) {
    if (!confirm('¿Eliminar cliente?')) return;
    const res = await fetch(`${API_PATIENTS}/${numeroId}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (res.ok) { showNotification('Cliente eliminado','success'); loadClients(); }
    else showNotification('Error al eliminar','error');
}

// Reporte CSV
async function exportClientReport() {
    const headers = ['Tipo','Nombre','Tipo ID','Número ID','EPS','Celular','Correo','Dirección','Fecha Nacimiento','Parentesco','Cotizante'];
    const rows = filteredClients.map(c => [
        c.tipoPaciente, c.nombreCompleto, c.tipoIdentificacion, c.numeroIdentificacion,
        formatEpsName(c.eps), c.celular||'', c.correo||'', c.direccionResidencia||'',
        c.fechaNacimiento||'', c.parentesco?formatParentesco(c.parentesco):'', c.cotizanteNumeroIdentificacion||''
    ]);
    const csv = [headers, ...rows].map(r => r.map(f => `"${f}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'reporte_clientes.csv';
    link.click();
}

// Notificación
function showNotification(message, type='info') {
    const n = document.getElementById('notification');
    n.textContent = message;
    n.className = `notification ${type}`;
    n.style.display = 'block';
    setTimeout(() => n.style.display = 'none', 4000);
}

// Navegación
function goToHome() { window.location.href = 'main.html'; }
function goToTasks() { window.location.href = 'task.html'; }
function logout() { localStorage.clear(); window.location.href = 'index.html'; }
