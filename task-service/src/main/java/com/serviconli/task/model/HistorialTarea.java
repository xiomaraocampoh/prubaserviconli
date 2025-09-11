package com.serviconli.task.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_tareas")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @Column(nullable = true, length = 30)
    @Enumerated(EnumType.STRING)
    private EstadoTarea estadoAnterior;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EstadoTarea estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    @Column(length = 255)
    private String usuarioCambio;

    @Column(length = 500)
    private String descripcionCambio;

    @PrePersist
    protected void onCreate() {
        fechaCambio = LocalDateTime.now();
        if (this.usuarioCambio == null || this.usuarioCambio.isEmpty()) {
            this.usuarioCambio = "Sistema"; // Valor por defecto si no se especifica
        }
    }
}