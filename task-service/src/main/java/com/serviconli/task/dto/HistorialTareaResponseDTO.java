package com.serviconli.task.dto;

import com.serviconli.task.model.EstadoTarea;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialTareaResponseDTO {
    private Long id;
    private Long tareaId; // ID de la tarea a la que pertenece este historial
    private EstadoTarea estadoAnterior;
    private EstadoTarea estadoNuevo;
    private LocalDateTime fechaCambio;
    private String usuarioCambio;
    private String descripcionCambio;
}