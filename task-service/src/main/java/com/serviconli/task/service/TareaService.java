package com.serviconli.task.service;

import com.serviconli.task.dto.*;
import com.serviconli.task.model.*;

import java.util.List;
import java.util.Optional;

public interface TareaService {

    // --- Operaciones CRUD básicas ---
    TaskResponseDTO crearTarea(CreateTaskRequestDTO createTaskRequestDTO);
    Optional<TaskResponseDTO> obtenerTareaPorId(Long id); // Devolver Optional es más seguro
    List<TaskResponseDTO> obtenerTodasLasTareas();
    TaskResponseDTO actualizarTarea(Long id, UpdateTaskDTO updateTaskDTO);
    void eliminarTarea(Long id);

    // --- Búsquedas Específicas

    List<TaskResponseDTO> buscarPorNumeroIdentificacionPaciente(String numeroIdentificacion);


    List<TaskResponseDTO> buscarPorNombrePaciente(String nombre);

    List<TaskResponseDTO> filtrarTareasPorAtributos(
            Optional<EstadoTarea> estado,
            Optional<Prioridad> prioridad,
            Optional<TipoCita> tipoCita
    );

    List<HistorialTareaResponseDTO> obtenerHistorialPorTarea(Long tareaId);
}