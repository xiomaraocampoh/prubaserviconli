package com.serviconli.patientservice.model;

import com.serviconli.patientservice.model.enums.EstadoCliente;
import com.serviconli.patientservice.model.enums.NombreEps;
import com.serviconli.patientservice.model.enums.Parentesco;
import com.serviconli.patientservice.model.enums.TipoIdentificacion;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "cotizantes")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cotizante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_identificacion", nullable = false)
    private TipoIdentificacion tipoIdentificacion;

    @Column(name = "numero_identificacion", unique = true)
    private String numeroIdentificacion;

    @Column(name = "fecha_nacimiento")
    private String fechaNacimiento;

    @Column(name = "fecha_expedicion")
    private String fechaExpedicion;

    @Column(name = "celular")
    private String celular;

    @Column(name = "correo")
    private String correo;

    @Column(name = "direccion_residencia")
    private String direccionResidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cotizante")
    private EstadoCliente estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "eps", nullable = false)
    private NombreEps eps;

    @Enumerated(EnumType.STRING)
    @Column(name = "parentesco", nullable = false)
    private Parentesco parentesco = Parentesco.COTIZANTE;

    @Column(name = "info_adicional")
    private String infoAdicional;

    @OneToMany(mappedBy = "cotizante", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore // Evita que al serializar un Cotizante se traiga a todos los beneficiarios en un bucle infinito.
    private List<Beneficiario> beneficiarios;

}