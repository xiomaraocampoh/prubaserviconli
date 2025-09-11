package com.serviconli.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Getter
@Setter
@Builder
public class PatientInfoDTO {
    private String tipoPaciente; // "COTIZANTE" o "BENEFICIARIO"
    private Long id;
    private String nombreCompleto;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String fechaNacimiento;
    private String fechaExpedicion;
    private String celular;
    private String correo;
    private String direccionResidencia;
    private String estado;
    private String parentesco;
    private String eps;
    private String infoAdicional;
    private CotizanteSummaryDTO cotizante;

}