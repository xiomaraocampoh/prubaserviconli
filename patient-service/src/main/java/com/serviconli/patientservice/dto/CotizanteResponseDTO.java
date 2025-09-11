package com.serviconli.patientservice.dto;

import com.serviconli.patientservice.model.enums.EstadoCliente;
import com.serviconli.patientservice.model.enums.NombreEps;
import com.serviconli.patientservice.model.enums.Parentesco;
import com.serviconli.patientservice.model.enums.TipoIdentificacion;
import lombok.Data;

import java.util.List;

@Data
public class CotizanteResponseDTO {
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

    // Incluimos la lista de beneficiarios para una respuesta completa
    private List<BeneficiarioResponseDTO> beneficiarios;
}
