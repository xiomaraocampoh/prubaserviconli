package com.serviconli.patientservice.dto;


import com.serviconli.patientservice.model.enums.EstadoCliente;
import com.serviconli.patientservice.model.enums.NombreEps;
import com.serviconli.patientservice.model.enums.Parentesco;
import com.serviconli.patientservice.model.enums.TipoIdentificacion;
import lombok.Data;

@Data
public class BeneficiarioResponseDTO {

    private Long id;
    private String nombreCompleto;
    private TipoIdentificacion tipoIdentificacion;
    private String numeroIdentificacion;
    private String fechaNacimiento;
    private String fechaExpedicion;
    private String celular;
    private String correo;
    private String direccionResidencia;
    private EstadoCliente estado;
    private Parentesco parentesco;
    private NombreEps eps;
    private String infoAdicional;

    private CotizanteSummaryDTO cotizante;

}