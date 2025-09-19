package com.serviconli.task.service.impl;

import com.serviconli.task.dto.*;
import com.serviconli.task.exception.*;
import com.serviconli.task.model.*;
import com.serviconli.task.repository.*;
import com.serviconli.task.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final HistorialTareaRepository historialTareaRepository;
    private final GoogleSheetsService googleSheetsService;
    private final WebClient webClient;

    // Inyectamos el WebClient configurado
    public TaskServiceImpl(TareaRepository tareaRepository,
                           HistorialTareaRepository historialTareaRepository,
                           GoogleSheetsService googleSheetsService,
                           WebClient.Builder webClientBuilder) {
        this.tareaRepository = tareaRepository;
        this.historialTareaRepository = historialTareaRepository;
        this.googleSheetsService = googleSheetsService;
        // La URL base ya está en la configuración
        this.webClient = webClientBuilder.build();
    }

    @Override
    @Transactional
    public TaskResponseDTO crearTarea(CreateTaskRequestDTO dto) {
        // --- PASO 1: Validar que el paciente existe llamando a patient-service ---
        PatientInfoDTO patientInfo = findPatientById(dto.getPacienteNumeroIdentificacion())
                .blockOptional() // .block() convierte la llamada asíncrona en síncrona
                .orElseThrow(() -> new PatientNotFoundException("No se encontró el paciente con ID: " + dto.getPacienteNumeroIdentificacion()));

        // --- PASO 2: Crear y guardar la entidad Tarea (ahora más simple) ---
        Tarea tarea = new Tarea();
        // Asignar todos los campos del DTO a la entidad
        tarea.setPacienteNumeroIdentificacion(dto.getPacienteNumeroIdentificacion());
        tarea.setTipoCita(dto.getTipoCita());
        tarea.setPrioridad(dto.getPrioridad());
        tarea.setEstado(dto.getEstado()); // <-- Se respeta el estado del formulario
        tarea.setEspecialidad(dto.getEspecialidad());
        tarea.setAutorizacion(dto.getAutorizacion());
        tarea.setOrden(dto.getOrden());
        tarea.setRadicado(dto.getRadicado());
        tarea.setEspecificaciones(dto.getEspecificaciones());
        tarea.setObservacion(dto.getObservacion());
        tarea.setFechaSolicitudServiconli(dto.getFechaSolicitudServiconli());
        tarea.setFechaCita(dto.getFechaCita());
        tarea.setHoraCita(dto.getHoraCita()); // Se asume que el DTO usa getHora()
        tarea.setDoctor(dto.getDoctor());
        tarea.setDireccionCita(dto.getDireccionCita());
        tarea.setLugarCita(dto.getLugarCita());
        tarea.setInformacionCita(dto.getInformacionCita());
        tarea.setConfirmacionCita(dto.getConfirmacionCita());

        Tarea savedTarea = tareaRepository.save(tarea);
        registrarHistorial(savedTarea, null, savedTarea.getEstado(), "Tarea creada");


        // --- PASO 3: Enviar la información COMPLETA a Google Sheets ---
        try {
            googleSheetsService.appendRow(mapToSheetRow(savedTarea, patientInfo));
        } catch (IOException e) {
            // Es mejor usar un logger que System.err
            System.err.println("⚠ Error enviando a Google Sheets: " + e.getMessage());
        }

        // --- PASO 4: Devolver el DTO enriquecido ---
        return convertToEnrichedDto(savedTarea, patientInfo);
    }

    @Override
    public Optional<TaskResponseDTO> obtenerTareaPorId(Long id) {
        Optional<Tarea> tareaOpt = tareaRepository.findById(id);
        if (tareaOpt.isEmpty()) {
            return Optional.empty();
        }

        Tarea tarea = tareaOpt.get();
        // Enriquecemos la tarea con la información del paciente
        PatientInfoDTO patientInfo = findPatientById(tarea.getPacienteNumeroIdentificacion()).block();
        return Optional.of(convertToEnrichedDto(tarea, patientInfo));
    }

    @Override
    public List<TaskResponseDTO> obtenerTodasLasTareas() {
        List<Tarea> tareas = tareaRepository.findAll();
        // Para cada tarea, obtenemos su información de paciente y la mapeamos
        return tareas.stream()
                .map(tarea -> {
                    PatientInfoDTO patientInfo = findPatientById(tarea.getPacienteNumeroIdentificacion()).block();
                    return convertToEnrichedDto(tarea, patientInfo);
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public TaskResponseDTO actualizarTarea(Long id, UpdateTaskDTO dto) {
        // 1. Busca la tarea existente en la base de datos
        Tarea tareaExistente = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // Guarda el estado anterior para el historial
        EstadoTarea estadoAnterior = tareaExistente.getEstado();

        // 2. Mapeo MANUAL y explícito para la actualización
        // Esto asegura que todos los campos del formulario se guarden siempre
        tareaExistente.setTipoCita(dto.getTipoCita());
        tareaExistente.setPrioridad(dto.getPrioridad());
        tareaExistente.setEstado(dto.getEstado()); // Asumo que en UpdateTaskDTO se llama getEstado()
        tareaExistente.setEspecialidad(dto.getEspecialidad());
        tareaExistente.setAutorizacion(dto.getAutorizacion());
        tareaExistente.setOrden(dto.getOrden());
        tareaExistente.setRadicado(dto.getRadicado());
        tareaExistente.setEspecificaciones(dto.getEspecificaciones());
        tareaExistente.setObservacion(dto.getObservacion());
        tareaExistente.setFechaSolicitudServiconli(dto.getFechaSolicitudServiconli());
        tareaExistente.setFechaCita(dto.getFechaCita());
        tareaExistente.setHoraCita(dto.getHoraCita()); // Asumo que se llama getHoraCita()
        tareaExistente.setDoctor(dto.getDoctor());
        tareaExistente.setDireccionCita(dto.getDireccionCita());
        tareaExistente.setLugarCita(dto.getLugarCita());
        tareaExistente.setInformacionCita(dto.getInformacionCita());
        tareaExistente.setConfirmacionCita(dto.getConfirmacionCita());

        // 3. Guarda la tarea actualizada en la base de datos
        Tarea updatedTarea = tareaRepository.save(tareaExistente);
        registrarHistorial(updatedTarea, estadoAnterior, updatedTarea.getEstado(), "Tarea actualizada");

        // Volvemos a necesitar los datos del paciente para actualizar Google Sheets
        PatientInfoDTO patientInfo = findPatientById(updatedTarea.getPacienteNumeroIdentificacion()).block();
        try {
            googleSheetsService.updateRow(updatedTarea.getId().toString(), mapToSheetRow(updatedTarea, patientInfo));
        } catch (IOException e) {
            System.err.println("⚠ Error actualizando en Google Sheets: " + e.getMessage());
        }

        return convertToEnrichedDto(updatedTarea, patientInfo);
    }

    @Override
    @Transactional
    public void eliminarTarea(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar, tarea no encontrada con ID: " + id);
        }
        tareaRepository.deleteById(id);
        try {
            googleSheetsService.deleteRowById(id.toString());
        } catch (IOException e) {
            System.err.println("⚠ Error eliminando en Google Sheets: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE BÚSQUEDA REFACTORIZADOS ---

    @Override
    public List<TaskResponseDTO> buscarPorNumeroIdentificacionPaciente(String numeroIdentificacion) {
        return tareaRepository.findByPacienteNumeroIdentificacion(numeroIdentificacion).stream()
                .map(tarea -> {
                    PatientInfoDTO patientInfo = findPatientById(tarea.getPacienteNumeroIdentificacion()).block();
                    return convertToEnrichedDto(tarea, patientInfo);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> buscarPorNombrePaciente(String nombre) {
        // PASO 1: Llamar a patient-service para obtener IDs por nombre
        List<String> patientIds = findPatientIdsByName(nombre).block();
        if (patientIds == null || patientIds.isEmpty()) {
            return List.of(); // Devolver lista vacía si no hay pacientes con ese nombre
        }

        // PASO 2: Buscar en nuestro repositorio las tareas con esos IDs
        return tareaRepository.findByPacienteNumeroIdentificacionIn(patientIds).stream()
                .map(tarea -> {
                    PatientInfoDTO patientInfo = findPatientById(tarea.getPacienteNumeroIdentificacion()).block();
                    return convertToEnrichedDto(tarea, patientInfo);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> filtrarTareasPorAtributos(Optional<EstadoTarea> estado, Optional<Prioridad> prioridad, Optional<TipoCita> tipoCita) {
        // Aquí iría una lógica de consulta dinámica (ej. con Criteria API o Specifications)
        // Por simplicidad, por ahora solo devolvemos todas y filtramos en memoria (no ideal para producción)
        List<Tarea> tareas = tareaRepository.findAll(); // Reemplazar con una consulta más eficiente
        return tareas.stream()
                .filter(t -> estado.map(s -> t.getEstado() == s).orElse(true))
                .filter(t -> prioridad.map(p -> t.getPrioridad() == p).orElse(true))
                .filter(t -> tipoCita.map(tc -> t.getTipoCita() == tc).orElse(true))
                .map(tarea -> {
                    PatientInfoDTO patientInfo = findPatientById(tarea.getPacienteNumeroIdentificacion()).block();
                    return convertToEnrichedDto(tarea, patientInfo);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<HistorialTareaResponseDTO> obtenerHistorialPorTarea(Long tareaId) {
        if (!tareaRepository.existsById(tareaId)) {
            throw new ResourceNotFoundException("Tarea no encontrada con ID: " + tareaId);
        }
        return historialTareaRepository.findByTareaIdOrderByFechaCambioAsc(tareaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========================= MÉTODOS PRIVADOS DE COMUNICACIÓN Y MAPEO =========================

    private Mono<PatientInfoDTO> findPatientById(String id) {
        return webClient.get()
                .uri("/api/v1/patients/{id}", id) // Llama a GET /api/v1/patients/{id}
                .retrieve()
                .bodyToMono(PatientInfoDTO.class);
    }

    private Mono<List<String>> findPatientIdsByName(String name) {
        // Asume que patient-service tiene un endpoint para buscar por nombre
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/patients/search")
                        .queryParam("nombre", name)
                        .build())
                .retrieve()
                .bodyToFlux(PatientInfoDTO.class) // Obtiene un flujo de pacientes
                .map(PatientInfoDTO::getNumeroIdentificacion) // Extrae solo sus IDs
                .collectList(); // Los agrupa en una lista
    }

    private TaskResponseDTO convertToEnrichedDto(Tarea tarea, PatientInfoDTO patientInfo) {
        TaskResponseDTO dto = new TaskResponseDTO();
        BeanUtils.copyProperties(tarea, dto);
        dto.setPaciente(patientInfo); // ¡Aquí ocurre el enriquecimiento!
        return dto;
    }

    private HistorialTareaResponseDTO convertToDto(HistorialTarea historial) {
        HistorialTareaResponseDTO dto = new HistorialTareaResponseDTO();
        BeanUtils.copyProperties(historial, dto);
        dto.setTareaId(historial.getTarea().getId());
        return dto;
    }

    private void registrarHistorial(Tarea tarea, EstadoTarea estadoAnterior, EstadoTarea estadoNuevo, String descripcion) {
        HistorialTarea historial = new HistorialTarea();
        historial.setTarea(tarea);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setDescripcionCambio(descripcion);
        historial.setUsuarioCambio("Sistema"); // Cambiar cuando tengas seguridad
        historialTareaRepository.save(historial);
    }


    private List<Object> mapToSheetRow(Tarea tarea, PatientInfoDTO paciente) {
        DateTimeFormatter fFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter fFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        java.util.function.Function<Object, String> s = obj -> (obj == null) ? "" : obj.toString();

        // Lógica para determinar los datos del cotizante a mostrar
        String cotizanteTipoId, cotizanteNumId, cotizanteNombre;

        // Si el paciente es un beneficiario y tiene un cotizante asociado...
        if ("BENEFICIARIO".equals(paciente.getTipoPaciente()) && paciente.getCotizante() != null) {

            cotizanteTipoId = s.apply(paciente.getCotizante().getTipoIdentificacion());
            cotizanteNumId = s.apply(paciente.getCotizante().getNumeroIdentificacion());
            cotizanteNombre = s.apply(paciente.getCotizante().getNombreCompleto());
        } else {
            // ... si no (es un cotizante), usamos sus propios datos.
            cotizanteTipoId = s.apply(paciente.getTipoIdentificacion());
            cotizanteNumId = s.apply(paciente.getNumeroIdentificacion());
            cotizanteNombre = s.apply(paciente.getNombreCompleto());
        }

        return Arrays.asList(

                (tarea.getFechaCreacion() == null) ? "" : tarea.getFechaCreacion().format(fFechaHora),

                // --- Columnas del "Cotizante" (con la nueva lógica) ---
                cotizanteTipoId,      // Columna 2: Tipo ID del Cotizante
                cotizanteNumId,       // Columna 3: Número de ID cotizante
                cotizanteNombre,      // Columna 4: Nombre completo del Cotizante
                s.apply(paciente.getParentesco()), // Columna 5: Parentesco (del paciente, sea cual sea)

                // --- Columnas del "Paciente"
                s.apply(paciente.getTipoIdentificacion()),
                s.apply(paciente.getNumeroIdentificacion()),
                s.apply(paciente.getNombreCompleto()),
                s.apply(paciente.getCelular()),
                s.apply(paciente.getCorreo()),
                s.apply(paciente.getEps()),

                // --- Columnas de la Tarea/Cita (no cambian) ---
                s.apply(tarea.getTipoCita()),
                s.apply(tarea.getRadicado()),
                s.apply(tarea.getAutorizacion()),
                (tarea.getFechaSolicitudServiconli() == null) ? "" : tarea.getFechaSolicitudServiconli().format(fFecha),
                (tarea.getFechaCita() == null) ? "" : tarea.getFechaCita().format(fFecha),
                s.apply(tarea.getConfirmacionCita()),
                s.apply(tarea.getEspecificaciones()),
                (tarea.getEstado() == null) ? "" : tarea.getEstado().name(),
                s.apply(tarea.getObservacion()),
                (tarea.getPrioridad() == null) ? "" : tarea.getPrioridad().name(),
                s.apply(tarea.getId())
        );
    }

}