package com.serviconli.task.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tareas")
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Vinculación con Patient-Service ---
    @NotNull(message = "El número de identificación del paciente es obligatorio")
    @Column(name = "paciente_numero_identificacion", nullable = false, length = 50)
    private String pacienteNumeroIdentificacion; // ÚNICO CAMPO para identificar al paciente

    // --- Datos de la Solicitud (Tarea) ---
    @NotNull(message = "El tipo de cita no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cita", nullable = false, length = 50)
    private TipoCita tipoCita; // Enum: ESPECIALISTA, AGENDAR_EXAMEN, etc.

    @Column(length = 120)
    private String especialidad; // Solo si tipoCita es ESPECIALISTA

    @Column(length = 80)
    private String autorizacion;

    @Column(length = 80)
    private String orden; // Campo para la orden médica

    @Column(length = 80)
    private String radicado;

    @NotNull(message = "La prioridad no puede ser nula")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    @Column(length = 1000)
    private String especificaciones; // Especificaciones de la solicitud inicial

    @Column(length = 1000)
    private String observacion; // Observaciones generales del proceso

    // --- Gestión y Estado de la Tarea ---
    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoTarea estado;

    @Column(name = "fecha_solicitud_serviconli")
    private LocalDate fechaSolicitudServiconli; // Cuando se pasa a "EN PROGRESO"

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion; // "Fecha de Solicitud Por El Afiliado"

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion; // se hace un cambio en la tarea

    // --- Datos de la Cita Asignada ---
    @Column(name = "fecha_cita")
    private LocalDate fechaCita;

    @Column(name = "hora_cita", length = 20)
    private String horaCita;

    @Column(name = "doctor", length = 150)
    private String doctor;

    @Column(name = "direccion_cita", length = 255)
    private String direccionCita; // Renombrado de "ubicacion" para mayor claridad

    @Column(name = "lugar_cita", length = 150)
    private String lugarCita; // Ej: "Consultorio 301, Torre Médica"

    @Column(name = "informacion_cita", length = 1000)
    private String informacionCita; // "información extra cita"

    @Column(name = "confirmacion_cita", length = 255)
    private String confirmacionCita;

    @Column(name = "fecha_recordatorio")
    private LocalDateTime fechaRecordatorio;

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Importante para evitar bucles infinitos al serializar a JSON
    private List<HistorialTarea> historial;


    // --- Callbacks de JPA para fechas automáticas ---
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}