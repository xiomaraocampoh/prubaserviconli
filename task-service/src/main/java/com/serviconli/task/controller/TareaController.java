package com.serviconli.task.controller;

import com.serviconli.task.dto.*;
import com.serviconli.task.exception.*;
import com.serviconli.task.model.*;
import com.serviconli.task.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor // Usa Lombok para inyectar las dependencias final
public class TareaController {

    private final TareaService tareaService; // Única dependencia necesaria

    // --- ENDPOINTS CRUD ---

    @PostMapping
    public ResponseEntity<TaskResponseDTO> crearTarea(@Valid @RequestBody CreateTaskRequestDTO createTaskRequestDTO) {
        TaskResponseDTO nuevaTarea = tareaService.crearTarea(createTaskRequestDTO);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> obtenerTareaPorId(@PathVariable Long id) {
        // Manejo elegante del Optional devuelto por el servicio
        return tareaService.obtenerTareaPorId(id)
                .map(ResponseEntity::ok) // Si la tarea existe, devuelve 200 OK con la tarea
                .orElse(ResponseEntity.notFound().build()); // Si no, devuelve 404 Not Found
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> actualizarTarea(@PathVariable Long id, @Valid @RequestBody UpdateTaskDTO updateTaskDTO) {
        TaskResponseDTO tareaActualizada = tareaService.actualizarTarea(id, updateTaskDTO);
        return ResponseEntity.ok(tareaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content, estándar para DELETE exitoso
    }

    // --- ENDPOINTS DE BÚSQUEDA Y CONSULTA ---

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> buscarTareas(
            // Búsqueda por datos del paciente
            @RequestParam(required = false) String numeroIdentificacion,
            @RequestParam(required = false) String nombrePaciente,
            // Filtros por atributos de la tarea
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) TipoCita tipoCita) {

        List<TaskResponseDTO> tareas;

        if (numeroIdentificacion != null && !numeroIdentificacion.isBlank()) {
            tareas = tareaService.buscarPorNumeroIdentificacionPaciente(numeroIdentificacion);
        } else if (nombrePaciente != null && !nombrePaciente.isBlank()) {
            tareas = tareaService.buscarPorNombrePaciente(nombrePaciente);
        } else if (estado != null || prioridad != null || tipoCita != null) {
            tareas = tareaService.filtrarTareasPorAtributos(
                    Optional.ofNullable(estado),
                    Optional.ofNullable(prioridad),
                    Optional.ofNullable(tipoCita)
            );
        } else {
            // Si no hay ningún parámetro, devuelve todas las tareas
            tareas = tareaService.obtenerTodasLasTareas();
        }

        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/{tareaId}/historial")
    public ResponseEntity<List<HistorialTareaResponseDTO>> obtenerHistorialPorTarea(@PathVariable Long tareaId) {
        List<HistorialTareaResponseDTO> historial = tareaService.obtenerHistorialPorTarea(tareaId);
        return ResponseEntity.ok(historial);
    }

    // --- MANEJO DE EXCEPCIONES ESPECÍFICAS DEL CONTROLADOR ---

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<String> handlePatientNotFoundException(PatientNotFoundException ex) {
        // Devuelve un 404 cuando el paciente no se encuentra en patient-service
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}