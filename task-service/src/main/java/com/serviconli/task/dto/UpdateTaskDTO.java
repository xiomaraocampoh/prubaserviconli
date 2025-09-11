package com.serviconli.task.dto;

import com.serviconli.task.model.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Data
public class UpdateTaskDTO {
    @NotNull(message = "El estado no puede ser nulo.")
    private EstadoTarea estado;

    private TipoCita tipoCita;


    private Prioridad prioridad;

    // --- Campos que se actualizan cuando la tarea progresa ---
    private LocalDate fechaSolicitudServiconli;
    private LocalDate fechaCita;

    @Size(max = 20)
    private String horaCita;

    @Size(max = 150)
    private String doctor;

    @Size(max = 150)
    private String direccionCita;

    @Size(max = 150)
    private String lugarCita;

    @Size(max = 1000)
    private String informacionCita;

    private String especialidad;

    private String autorizacion;

    private String orden;

    private String radicado;

    private String especificaciones;

    @Size(max = 1000)
    private String observacion; // Observaciones del progreso

    @Size(max = 255)
    private String confirmacionCita;
}