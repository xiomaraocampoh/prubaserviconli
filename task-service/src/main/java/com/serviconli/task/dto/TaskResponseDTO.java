package com.serviconli.task.dto;

import com.serviconli.task.model.EstadoTarea;
import com.serviconli.task.model.Prioridad;
import com.serviconli.task.model.TipoCita;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    // --- Info de la Tarea ---
    private Long id;
    private EstadoTarea estado;
    private Prioridad prioridad;
    private TipoCita tipoCita;
    private String especialidad;
    private String autorizacion;
    private String orden;
    private String radicado;
    private String especificaciones;
    private String observacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // --- Info del Paciente (obtenida de patient-service) ---
    private PatientInfoDTO paciente;

    // --- Info de la Cita (si aplica) ---
    private LocalDate fechaSolicitudServiconli;
    private LocalDate fechaCita;
    private String horaCita;
    private String doctor;
    private String direccionCita;
    private String lugarCita;
    private String informacionCita;
    private String confirmacionCita;
}