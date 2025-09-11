package com.serviconli.task.dto;

import com.serviconli.task.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@Data
public class CreateTaskRequestDTO {

        @NotBlank(message = "El número de identificación del paciente no puede estar vacío.")
        private String pacienteNumeroIdentificacion;

        @NotNull(message = "El tipo de cita es obligatorio.")
        private TipoCita tipoCita;

        @NotNull(message = "La prioridad es obligatoria.")
        private Prioridad prioridad;

        // --- Campos Opcionales en la Creación ---
        private String especialidad;

        private String autorizacion;

        private String orden;

        private String radicado;

        private String observacion;

        private String especificaciones;

        private EstadoTarea estado;

        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        private LocalDate fechaSolicitudServiconli;
        private LocalDate fechaCita;
        private String horaCita;
        private String doctor;
        private String direccionCita;
        private String lugarCita;
        private String informacionCita;
        private String confirmacionCita;

    }
