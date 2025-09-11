// ==================== CONFIG ====================
const TASK_API_URL = "http://localhost:8080/api/v1/tareas";
const PATIENT_API_URL = "http://localhost:8080/api/v1/patients";

let tasks = [];
let editingTaskId = null;

const STATE_PROGRESSION = {
    "PENDIENTE": "EN_PROGRESO",
    "EN_PROGRESO": "CITA_CONFIRMADA",
    "CITA_CONFIRMADA": "ENVIADA",
    "ENVIADA": "COMPLETADA"
};

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

// ==================== INIT ====================
document.addEventListener("DOMContentLoaded", function () {
    checkAuth();
    populateEpsSelects();
    bindSearchControls();
    loadTasks();
});

// ==================== AUTH ====================
function checkAuth() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "index.html";
    }
}

function getAuthHeaders() {
    return {
        "Authorization": `Bearer ${localStorage.getItem("token")}`,
        "Content-Type": "application/json"
    };
}

// ==================== EPS ====================
function populateEpsSelects() {
    const epsSelect = document.getElementById("eps");
    if (!epsSelect) return;
    epsSelect.innerHTML = '<option value="">Seleccione EPS</option>';
    EPS_LIST.forEach(eps => {
        const option = document.createElement("option");
        option.value = eps;
        option.textContent = eps.replace(/_/g, " ");
        epsSelect.appendChild(option);
    });
}

// ==================== TASKS ====================
async function loadTasks() {
    try {
        const response = await fetch(TASK_API_URL, { headers: getAuthHeaders() });
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) logout();
            throw new Error("Error al cargar las tareas");
        }
        tasks = await response.json();
        populateEspecialidadFilter();
        renderTasks();
    } catch (error) {
        showNotification("Error al cargar tareas. Inicie sesión de nuevo.", "error");
        console.error("Error:", error);
    }
}

function renderTasks() {
    const states = ["PENDIENTE", "EN_PROGRESO", "CITA_CONFIRMADA", "ENVIADA", "COMPLETADA"];
    states.forEach(state => {
        const column = document.getElementById(`column-${state}`);
        const count = document.getElementById(`count-${state}`);
        const stateTasks = tasks.filter(task => task.estado== state);

        count.textContent = stateTasks.length;
        column.innerHTML = stateTasks.length > 0 ? stateTasks.map(createTaskCard).join("") : '<div class="empty-state">No hay tareas</div>';
    });
}

function createTaskCard(task) {
    const { paciente, estado, prioridad, especialidad, doctor, fechaCita, horaCita, direccionCita, lugarCita, id } = task;
    const nextState = STATE_PROGRESSION[estado];

    return `
        <div class="task-card">
            <div class="task-header">
                <span class="task-state state-${estado}">${estado.replace("_", " ")}</span>
                <span class="task-priority priority-${prioridad}">${prioridad}</span>
            </div>
            <div class="task-patient">${paciente?.nombreCompleto || "Paciente desconocido"}</div>
            <div class="task-details">
                <div class="task-detail">🏥 ${especialidad || "Cita General"} - ${doctor || "Por asignar"}</div>
                <div class="task-detail">📅 ${fechaCita || "Fecha por confirmar"} - ${horaCita || ""}</div>
                <div class="task-detail">📍 ${direccionCita || "Lugar por definir"} - ${lugarCita || ""}</div>
                <div class="task-detail">📞 ${paciente?.celular || "No registrado"}</div>
                <div class="task-detail">🏥 ${paciente?.eps || "Sin EPS"}</div>
            </div>
            <div class="task-actions">
                <button class="task-action" onclick="viewObservations(${id})">👁️ Observaciones</button>
                ${nextState ? `<button class="task-action advance" onclick="advanceTask(${id})">➡️ ${nextState.replace("_", " ")}</button>` : ""}
                <button class="task-action edit" onclick="editTask(${id})">✏️ Editar</button>
                <button class="task-action delete" onclick="deleteTask(${id})">🗑️ Eliminar</button>
                <button class="task-action whatsapp" onclick="sendWhatsApp(${id})">📲 WhatsApp</button>
            </div>
        </div>
    `;
}

function viewObservations(taskId) {
    const task = tasks.find(t => t.id === taskId);
    if (!task) return;
    const content = document.getElementById("observacionContent");
    content.innerHTML = `
        <p><strong>Autorización:</strong> ${task.autorizacion || "N/A"}</p>
        <p><strong>Orden:</strong> ${task.orden || "N/A"}</p>
        <p><strong>Radicado:</strong> ${task.radicado || "N/A"}</p>
        <p><strong>Especificaciones:</strong> ${task.especificaciones || "Ninguna"}</p>
        <p><strong>Información Cita:</strong> ${task.informacionCita || "Ninguna"}</p>
        <p><strong>Observación General:</strong> ${task.observacion || "Ninguna"}</p>
    `;
    document.getElementById("observacionModal").classList.add("active");
}

function closeObservacionModal() {
    document.getElementById("observacionModal").classList.remove("active");
}

async function advanceTask(taskId) {
    const task = tasks.find(t => t.id === taskId);
    if (task && STATE_PROGRESSION[task.estado]) {
        await updateTaskState(taskId, STATE_PROGRESSION[task.estado]);
    }
}

async function updateTaskState(taskId, newState) {
    try {
        const task = tasks.find(t => t.id === taskId);
        if (!task) throw new Error("Tarea no encontrada en memoria");

        const payload = {
            pacienteNumeroIdentificacion: task.paciente?.numeroIdentificacion,
            tipoCita: task.tipoCita,
            prioridad: task.prioridad,
            estado: newState,
            especialidad: task.especialidad,
            autorizacion: task.autorizacion,
            orden: task.orden,
            radicado: task.radicado,
            especificaciones: task.especificaciones,
            observacion: task.observacion,
            fechaSolicitudServiconli: task.fechaSolicitudServiconli,
            fechaCita: task.fechaCita,
            horaCita: task.horaCita, // ✅ corregido
            doctor: task.doctor,
            direccionCita: task.direccionCita,
            lugarCita: task.lugarCita,
            informacionCita: task.informacionCita,
            confirmacionCita: task.confirmacionCita,
            eps: task.paciente?.eps
        };

        const response = await fetch(`${TASK_API_URL}/${taskId}`, {
            method: "PUT",
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error("Error al actualizar la tarea");
        showNotification(`Tarea actualizada a ${newState.replace("_", " ")}`, "success");
        loadTasks();
    } catch (error) {
        showNotification("Error al actualizar la tarea", "error");
        console.error("Error:", error);
    }
}

function enableManualPatientEntry() {
    // --- Desbloquear todos los campos ---
    document.getElementById("tipoIdentificacionPaciente").disabled = false;
    document.getElementById("numeroIdPaciente").readOnly = false;
    document.getElementById("nombrePaciente").readOnly = false;
    document.getElementById("celularPaciente").readOnly = false;
    document.getElementById("correoPaciente").readOnly = false;
    document.getElementById("direccionPaciente").readOnly = false;
    document.getElementById("eps").disabled = false;
    const parentescoField = document.getElementById("parentesco");
    if (parentescoField) {
        parentescoField.disabled = false;
        parentescoField.value = "";
    }

    // --- Limpiar los valores previos ---
    document.getElementById("tipoIdentificacionPaciente").value = "";
    document.getElementById("numeroIdPaciente").value = "";
    document.getElementById("nombrePaciente").value = "";
    document.getElementById("celularPaciente").value = "";
    document.getElementById("correoPaciente").value = "";
    document.getElementById("direccionPaciente").value = "";
    document.getElementById("eps").value = "";

    // --- Rellenar nuevamente el listado de EPS ---
    populateEpsSelects();
}



// ==================== CRUD: EDIT / DELETE / SAVE ====================
function editTask(taskId) {
    const task = tasks.find(t => t.id === taskId);
    if (!task) {
        showNotification("No se encontró la tarea seleccionada", "error");
        return;
    }
    editingTaskId = task.id;
    openModal(true);

    // Paciente
    document.getElementById("numeroIdPaciente").value = task.paciente?.numeroIdentificacion || "";
    document.getElementById("nombrePaciente").value = task.paciente?.nombreCompleto || "";
    document.getElementById("tipoIdentificacionPaciente").value = task.paciente?.tipoIdentificacion || "";
    document.getElementById("celularPaciente").value = task.paciente?.celular || "";
    document.getElementById("correoPaciente").value = task.paciente?.correo || "";
    document.getElementById("direccionPaciente").value = task.paciente?.direccionResidencia || "";
    populateEpsSelects();
    if (task.paciente?.eps) document.getElementById("eps").value = task.paciente.eps;

    // Cotizante si aplica
    const cotSection = document.getElementById("cotizanteSection");
    if (task.paciente?.tipoPaciente === "BENEFICIARIO" && task.paciente.cotizante) {
        cotSection.style.display = "block";
        document.getElementById("cotizanteNombre").value = task.paciente.cotizante.nombreCompleto || "";
        document.getElementById("parentesco").value = task.paciente.parentesco || "";
        document.getElementById("cotizanteTipoId").value = task.paciente.cotizante.tipoIdentificacion || "";
        document.getElementById("cotizanteNumeroId").value = task.paciente.cotizante.numeroIdentificacion || "";
    } else cotSection.style.display = "none";

    // Datos de cita
    document.getElementById("tipoCita").value = task.tipoCita || "";
    document.getElementById("especialidad").value = task.especialidad || "";
    document.getElementById("autorizacion").value = task.autorizacion || "";
    document.getElementById("orden").value = task.orden || "";
    document.getElementById("radicado").value = task.radicado || "";
    document.getElementById("especificaciones").value = task.especificaciones || "";
    document.getElementById("observacion").value = task.observacion || "";
    document.getElementById("fechaCita").value = task.fechaCita || "";
    document.getElementById("horaCita").value = task.horaCita || "";
    document.getElementById("doctor").value = task.doctor || "";
    document.getElementById("direccionCita").value = task.direccionCita || "";
    document.getElementById("lugarCita").value = task.lugarCita || "";
    document.getElementById("informacionCita").value = task.informacionCita || "";
    document.getElementById("confirmacionCita").value = task.confirmacionCita || "";
    document.getElementById("estado").value = task.estado || "PENDIENTE";
    document.getElementById("prioridad").value = task.prioridad || "MEDIA";
    document.getElementById("fechaSolicitudServiconli").value = task.fechaSolicitudServiconli || "";
}

async function deleteTask(taskId) {
    if (!confirm("¿Está seguro de que desea eliminar esta tarea?")) return;
    try {
        const res = await fetch(`${TASK_API_URL}/${taskId}`, { method: "DELETE", headers: getAuthHeaders() });
        if (!res.ok) throw new Error("Error al eliminar la tarea");
        showNotification("Tarea eliminada", "success");
        loadTasks();
    } catch (err) {
        console.error(err);
        showNotification("Error al eliminar la tarea", "error");
    }
}

async function saveTask(event) {
    if (event) event.preventDefault();
    const isEditing = !!editingTaskId;

    const saveBtn = document.getElementById("saveTaskBtn");
    if (saveBtn) saveBtn.disabled = true;

    try {
        let numeroIdentificacion = (document.getElementById("numeroIdPaciente")?.value || "").trim();
        const nombrePaciente = (document.getElementById("nombrePaciente")?.value || "").trim();
        if (!numeroIdentificacion) {
            showNotification("El número de identificación del paciente es obligatorio.", "error");
            return;
        }

        const pacientesEncontrados = await buscarPacienteAndShowResults(numeroIdentificacion);
        const pacienteExistente = (Array.isArray(pacientesEncontrados) && pacientesEncontrados.length) ? pacientesEncontrados[0] : null;

        const patientPayload = {
            nombreCompleto: nombrePaciente || null,
            tipoIdentificacion: document.getElementById("tipoIdentificacionPaciente")?.value || null,
            numeroIdentificacion,
            celular: (document.getElementById("celularPaciente")?.value || "").trim() || null,
            correo: (document.getElementById("correoPaciente")?.value || "").trim() || null,
            direccionResidencia: (document.getElementById("direccionPaciente")?.value || "").trim() || null,
            eps: document.getElementById("eps")?.value || null,
            tipoPaciente: tipoPacienteSeleccionado || "COTIZANTE"
        };

        if (patientPayload.tipoPaciente === "BENEFICIARIO") {
            const cotizanteNumero = (document.getElementById("cotizanteNumeroId")?.value || "").trim();
            const parentesco = document.getElementById("parentesco")?.value || null;

            patientPayload.parentesco = parentesco;
            patientPayload.cotizanteNumeroIdentificacion = cotizanteNumero;

            console.log("Beneficiario payload:", patientPayload)
        }


        // --- Crear o actualizar paciente según corresponda ---
        if (!pacienteExistente) {
            if (!patientPayload.nombreCompleto || !patientPayload.numeroIdentificacion) {
                showNotification("Debe llenar al menos el nombre y el número de identificación del paciente.", "error");
                return;
            }

            // <-- CAMBIO 1: Construir la URL de CREACIÓN correctamente
            const endpoint = patientPayload.tipoPaciente === 'BENEFICIARIO' ? '/beneficiarios' : '/cotizantes';
            const createPatientUrl = PATIENT_API_URL + endpoint;

            const resCreate = await fetch(createPatientUrl, { // <-- Usar la URL corregida
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(patientPayload)
            });

            if (!resCreate.ok) {
                const err = await resCreate.json().catch(() => ({}));
                throw new Error(err.message || "Error creando el paciente");
            }

            const created = await resCreate.json().catch(() => null);
            if (created && created.numeroIdentificacion) numeroIdentificacion = created.numeroIdentificacion;

            showNotification("Paciente creado correctamente", "success");

        } else {
            let changed = false;
            const keysToCompare = ["nombreCompleto", "celular", "correo", "direccionResidencia", "eps"];
            for (const k of keysToCompare) {
                if ((patientPayload[k] || "") !== (pacienteExistente[k] || "")) {
                    changed = true;
                    break;
                }
            }
            if (!changed && patientPayload.tipoPaciente === "BENEFICIARIO" && patientPayload.cotizante) {
                const existingCot = pacienteExistente.cotizante || {};
                const cot = patientPayload.cotizante;
                if ((cot.nombreCompleto || "") !== (existingCot.nombreCompleto || "") ||
                    (cot.numeroIdentificacion || "") !== (existingCot.numeroIdentificacion || "")) {
                    changed = true;
                }
            }

            if (changed) {
                // <-- CAMBIO 2: Construir la URL de ACTUALIZACIÓN correctamente
                const patientTypePath = (pacienteExistente.tipoPaciente === 'BENEFICIARIO' ? '/beneficiarios' : '/cotizantes');
                const identifier = pacienteExistente.id || numeroIdentificacion;
                const updateUrl = `${PATIENT_API_URL}${patientTypePath}/${identifier}`;

                const resUpdate = await fetch(updateUrl, { // <-- Usar la URL corregida
                    method: "PUT",
                    headers: getAuthHeaders(),
                    body: JSON.stringify(patientPayload)
                });

                if (!resUpdate.ok) {
                    const err = await resUpdate.json().catch(() => ({}));
                    throw new Error(err.message || "Error actualizando el paciente");
                }
                showNotification("Datos del paciente actualizados", "success");
            }
        }

        // --- Preparar payload de la tarea ---
        const url = isEditing ? `${TASK_API_URL}/${editingTaskId}` : TASK_API_URL;
        const method = isEditing ? "PUT" : "POST";

        const taskData = {
            pacienteNumeroIdentificacion: numeroIdentificacion,
            tipoCita: document.getElementById("tipoCita")?.value || null,
            prioridad: document.getElementById("prioridad")?.value || "MEDIA",
            estado: document.getElementById("estado")?.value || "PENDIENTE",
            especialidad: (document.getElementById("especialidad")?.value || "").trim() || null,
            autorizacion: (document.getElementById("autorizacion")?.value || "").trim() || null,
            orden: (document.getElementById("orden")?.value || "").trim() || null,
            radicado: (document.getElementById("radicado")?.value || "").trim() || null,
            especificaciones: (document.getElementById("especificaciones")?.value || "").trim() || null,
            observacion: (document.getElementById("observacion")?.value || "").trim() || null,
            fechaSolicitudServiconli: document.getElementById("fechaSolicitudServiconli")?.value || null,
            fechaCita: document.getElementById("fechaCita")?.value || null,
            horaCita: document.getElementById("horaCita")?.value || null,
            doctor: (document.getElementById("doctor")?.value || "").trim() || null,
            direccionCita: (document.getElementById("direccionCita")?.value || "").trim() || null,
            lugarCita: (document.getElementById("lugarCita")?.value || "").trim() || null,
            informacionCita: (document.getElementById("informacionCita")?.value || "").trim() || null,
            confirmacionCita: (document.getElementById("confirmacionCita")?.value || "").trim() || null,
            eps: document.getElementById("eps")?.value || null
        };

        const cotizanteNumero = (document.getElementById("cotizanteNumeroId")?.value || "").trim();
        if (cotizanteNumero) taskData.cotizanteNumeroIdentificacion = cotizanteNumero;

        // --- Crear / Actualizar tarea ---
        const resTask = await fetch(url, {
            method,
            headers: getAuthHeaders(),
            body: JSON.stringify(taskData)
        });

        if (!resTask.ok) {
            const err = await resTask.json().catch(() => ({}));
            throw new Error(err.message || "Error al guardar la tarea");
        }

        showNotification(isEditing ? "Tarea actualizada correctamente" : "Tarea creada correctamente", "success");
        closeModal();
        loadTasks();

    } catch (err) {
        console.error("Error al guardar tarea:", err);
        showNotification(err.message || "Error al guardar la tarea", "error");
    } finally {
        if (saveBtn) saveBtn.disabled = false;
    }
}



// ==================== PACIENTES ====================
function fillPatientData(paciente) {
    document.getElementById("numeroIdPaciente").value = paciente.numeroIdentificacion || "";
    document.getElementById("nombrePaciente").value = paciente.nombreCompleto || "";
    document.getElementById("tipoIdentificacionPaciente").value = paciente.tipoIdentificacion || "";
    document.getElementById("celularPaciente").value = paciente.celular || "";
    document.getElementById("correoPaciente").value = paciente.correo || "";
    document.getElementById("direccionPaciente").value = paciente.direccionResidencia || "";
    populateEpsSelects();
    if (paciente.eps) {
        const epsSelect = document.getElementById("eps");
        epsSelect.value = paciente.eps;
        epsSelect.disabled = true;
    }
    const cotizanteSection = document.getElementById("cotizanteSection");
    if (paciente.tipoPaciente === "BENEFICIARIO" && paciente.cotizante) {
        cotizanteSection.style.display = "block";
        document.getElementById("cotizanteNombre").value = paciente.cotizante.nombreCompleto || "";
        document.getElementById("cotizanteTipoId").value = paciente.cotizante.tipoIdentificacion || "";
        document.getElementById("cotizanteNumeroId").value = paciente.cotizante.numeroIdentificacion || "";
    } else cotizanteSection.style.display = "none";

    // 🔹 NUEVO: si ya existe beneficiario, rellenar parentesco
    if (paciente.parentesco) {
        const parentescoSelect = document.getElementById("parentesco");
        if (parentescoSelect) {
            parentescoSelect.value = paciente.parentesco;
            parentescoSelect.disabled = true; // lo bloqueamos para evitar edición innecesaria
        }
    } else {
    cotizanteSection.style.display = "none";
}

togglePatientFields(false);



}


async function buscarPacienteAndShowResults(query) {
    try {
        const token = localStorage.getItem("token");
        const url = /^\d+$/.test(query)
            ? `${PATIENT_API_URL}/${query}`
            : `${PATIENT_API_URL}/search?nombre=${encodeURIComponent(query)}`;

        const response = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
        if (response.status === 404) return [];
        const data = await response.json();
        return Array.isArray(data) ? data : (data ? [data] : []);
    } catch (err) {
        console.error("Error en búsqueda:", err);
        return [];
    }
}

function bindSearchControls() {
    const inputBusqueda = document.getElementById("busquedaPaciente");
    const btnBuscar = document.getElementById("btnBuscarPaciente");
    const resultadosDiv = document.getElementById("resultadosBusqueda");
    if (!btnBuscar || !inputBusqueda || !resultadosDiv) return;

    btnBuscar.addEventListener("click", async () => {
        const query = inputBusqueda.value.trim();
        if (!query) return showNotification("Ingrese un nombre o identificación", "error");
        resultadosDiv.innerHTML = '<div class="autocomplete-item">Buscando...</div>';
        const pacientes = await buscarPacienteAndShowResults(query);
        resultadosDiv.innerHTML = "";
        if (pacientes.length === 0) {
            resultadosDiv.innerHTML = '<div class="autocomplete-item">No se encontraron pacientes</div>';
            enableManualPatientEntry();
            return;
        }
        pacientes.forEach(p => {
            const item = document.createElement("div");
            item.className = "autocomplete-item";
            item.textContent = `${p.nombreCompleto} (${(p.tipoPaciente || "PACIENTE").toLowerCase()}) - ${p.numeroIdentificacion}`;
            item.onclick = () => {
                fillPatientData(p);
                inputBusqueda.value = `${p.nombreCompleto} - ${p.numeroIdentificacion}`;
                resultadosDiv.innerHTML = "";
            };
            resultadosDiv.appendChild(item);
        });
    });
    document.addEventListener("click", (ev) => {
        if (!resultadosDiv.contains(ev.target) && ev.target !== inputBusqueda && ev.target !== btnBuscar) {
            resultadosDiv.innerHTML = "";
        }
    });
}

let tipoPacienteSeleccionado = null;

function selectTipoPaciente(tipo) {
    tipoPacienteSeleccionado = tipo;
    document.getElementById("tipoPacienteSelector").style.display = "none";
    document.getElementById("taskForm").style.display = "block";
    // 🔹 Si es beneficiario, mostrar de una vez la sección del cotizante
    const cotizanteSection = document.getElementById("cotizanteSection");
    if (tipo === "BENEFICIARIO" && cotizanteSection) {
        cotizanteSection.style.display = "block";
    } else if (cotizanteSection) {
        cotizanteSection.style.display = "none";
    }
}


// ==================== MODAL ====================
function openModal(isEditing = false) {
    if (!isEditing) {
        const form = document.getElementById("taskForm");
        if (form) form.reset();

        // Reiniciar selección de tipo de paciente
        tipoPacienteSeleccionado = null;
        document.getElementById("tipoPacienteSelector").style.display = "block";
        document.getElementById("taskForm").style.display = "none";
        togglePatientFields(true);
    }

    document.getElementById("modalTitle").textContent = isEditing ? "Editar Tarea" : "Nueva Tarea";
    document.getElementById("taskModal").classList.add("active");
}


function closeModal() {
    document.getElementById("taskModal").classList.remove("active");
    editingTaskId = null;
}

// ==================== OTHER ====================
function logout() {
    localStorage.removeItem("token");
    window.location.href = "index.html";
}

function showNotification(message, type = "success") {
    const notification = document.getElementById("notification");
    if (!notification) return console.log(type.toUpperCase(), message);
    notification.textContent = message;
    notification.className = `notification ${type} show`;
    setTimeout(() => notification.classList.remove("show"), 3500);
}

// ==================== WhatsApp ====================
function sendWhatsApp(taskId) {
    const task = tasks.find(t => t.id === taskId);
    if (!task) return showNotification("No se encontró la tarea", "error");
    const paciente = task.paciente || {};
    const nombre = paciente.nombreCompleto || "[Nombre]";
    const tipo = task.tipoCita || "[Tipo de cita]";
    const fecha = task.fechaCita || "[Fecha]";
    const horaCita = task.horaCita || "[Hora]";
    const doctor = task.doctor || "[Doctor]";
    const lugar = task.lugarCita || task.direccionCita || "[Lugar]";
    const especificaciones = task.especificaciones ? ` (${task.especificaciones})` : "";
    const diaTexto = fecha !== "[Fecha]" ? new Date(fecha).toLocaleDateString("es-CO", { weekday: "long" }) : "[Día]";
    const message = `La cita de ${nombre} para ${tipo} fue asignada para el día ${diaTexto} ${fecha} a las ${horaCita} con el DR.${doctor}. En ${lugar}, recuerde llegar 30 minutos antes con el documento de identidad original, cuota moderadora y tapabocas.${especificaciones}`;
    const telefono = (paciente.celular || "").replace(/\D/g, "");
    if (!telefono) return showNotification("No se encontró número de celular del paciente", "error");
    window.open(`https://wa.me/57${telefono}?text=${encodeURIComponent(message)}`, "_blank");
    showNotification("Abriendo WhatsApp...");
}


// ==================== EXPORT REPORT ====================
function exportReport() {
    if (!tasks || tasks.length === 0) {
        showNotification("No hay tareas para exportar", "error");
        return;
    }

    // Prepara los datos en formato plano para Excel
    const data = tasks.map(task => ({
        "ID": task.id,
        "Estado": task.estado,
        "Prioridad": task.prioridad,
        "Tipo de Cita": task.tipoCita,
        "Especialidad": task.especialidad || "",
        "Autorización": task.autorizacion || "",
        "Radicado": task.radicado || "",
        "Paciente": task.paciente?.nombreCompleto || "",
        "Identificación": task.paciente ? `${task.paciente.tipoIdentificacion} ${task.paciente.numeroIdentificacion}` : "",
        "Celular": task.paciente?.celular || "",
        "EPS": task.paciente?.eps || "",
        "Cotizante": task.paciente?.tipoPaciente === "BENEFICIARIO" && task.paciente.cotizante
            ? `${task.paciente.cotizante.nombreCompleto} (${task.paciente.cotizante.numeroIdentificacion})`
            : "",
        "Fecha Creación": task.fechaCreacion ? new Date(task.fechaCreacion).toLocaleString() : "",
        "Fecha Cita": task.fechaCita || "",
        "Hora Cita": task.horaCita || "",
        "Doctor": task.doctor || "",
        "Dirección Cita": task.direccionCita || "",
        "Lugar Cita": task.lugarCita || "",
        "Observación": task.observacion || "",
        "Especificaciones": task.especificaciones || ""
    }));

    // Genera el libro y la hoja
    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Tareas");

    // Descarga el archivo
    const fileName = `Reporte_Tareas_${new Date().toISOString().split("T")[0]}.xlsx`;
    XLSX.writeFile(wb, fileName);
    showNotification("Reporte generado y descargado", "success");
}

// funcion habilitar desahabilitar campos
function togglePatientFields(enabled) {
    const fields = [
        "numeroIdPaciente", "nombrePaciente", "celularPaciente",
        "correoPaciente", "direccionPaciente"
    ];
    const selects = ["tipoIdentificacionPaciente", "eps"];

    fields.forEach(id => {
        document.getElementById(id).readOnly = !enabled;
    });

    selects.forEach(id => {
        document.getElementById(id).disabled = !enabled;
    });
}

// ==================== FILTERS ====================
function applyFilters() {
    const estadoFiltro = document.getElementById("filterEstado")?.value || "";
    const especialidadFiltro = (document.getElementById("filterEspecialidad")?.value || "").toLowerCase();
    const prioridadFiltro = document.getElementById("filterPrioridad")?.value || "";
    const search = (document.getElementById("searchClients")?.value || "").toLowerCase();

    const filteredTasks = tasks.filter(task => {
        const matchEstado = !estadoFiltro || task.estado === estadoFiltro;
        const matchEspecialidad = !especialidadFiltro || (task.especialidad || "").toLowerCase().includes(especialidadFiltro);
        const matchPrioridad = !prioridadFiltro || task.prioridad === prioridadFiltro;

        // 🔎 Buscar por nombre o identificación
        const paciente = task.paciente || {};
        const matchSearch =
            !search ||
            (paciente.nombreCompleto && paciente.nombreCompleto.toLowerCase().includes(search)) ||
            (paciente.numeroIdentificacion && paciente.numeroIdentificacion.includes(search));

        return matchEstado && matchEspecialidad && matchPrioridad && matchSearch;
    });

    renderFilteredTasks(filteredTasks);
}

function populateEspecialidadFilter() {
    const select = document.getElementById("filterEspecialidad");
    if (!select) return;

    // Limpiar y poner la opción por defecto
    select.innerHTML = '<option value="">Todas las especialidades</option>';

    // Extraer especialidades únicas
    const especialidades = [...new Set(tasks.map(t => t.especialidad).filter(e => e))];
    especialidades.forEach(e => {
        const option = document.createElement("option");
        option.value = e;
        option.textContent = e;
        select.appendChild(option);
    });
}


function renderFilteredTasks(filteredTasks) {
    const states = ["PENDIENTE", "EN_PROGRESO", "CITA_CONFIRMADA", "ENVIADA", "COMPLETADA"];
    states.forEach(state => {
        const column = document.getElementById(`column-${state}`);
        const count = document.getElementById(`count-${state}`);
        const stateTasks = filteredTasks.filter(task => task.estado === state);

        count.textContent = stateTasks.length;
        column.innerHTML = stateTasks.length > 0 ? stateTasks.map(createTaskCard).join("") : '<div class="empty-state">No hay tareas</div>';
    });
}


// ==================== RECORDATORIOS INTERNOS ====================

function updateReminderBanner() {
    if (!tasks || tasks.length === 0) {
        document.getElementById("reminderBanner").style.display = "none";
        return;
    }

    const pendientes = tasks.filter(t => t.estado === "PENDIENTE").length;
    const enProgreso = tasks.filter(t => t.estado === "EN_PROGRESO").length;

    const banner = document.getElementById("reminderBanner");
    if (pendientes > 0 || enProgreso > 0) {
        banner.style.display = "block";
        banner.textContent = `📌 Tienes ${pendientes} tareas pendientes y ${enProgreso} en progreso`;

        // 🔊 reproducir sonido cada vez que se muestre el recordatorio
        const audio = document.getElementById("reminderSound");
        if (audio) {
            audio.currentTime = 0;
            audio.play().catch(err => console.log("No se pudo reproducir el sonido:", err));
        }

        // aplicar animación visual
        banner.classList.add("sound-alert");
        setTimeout(() => banner.classList.remove("sound-alert"), 2000);

    } else {
        banner.style.display = "none";
    }
}

// Llamar cada vez que se carguen las tareas
async function loadTasks() {
    try {
        const response = await fetch(TASK_API_URL, { headers: getAuthHeaders() });
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) logout();
            throw new Error("Error al cargar las tareas");
        }
        tasks = await response.json();
        renderTasks();
        updateReminderBanner();
    } catch (error) {
        showNotification("Error al cargar tareas. Inicie sesión de nuevo.", "error");
        console.error("Error:", error);
    }
}


setInterval(updateReminderBanner, 7200000);
