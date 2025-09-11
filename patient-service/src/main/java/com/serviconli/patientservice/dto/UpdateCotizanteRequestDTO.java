package com.serviconli.patientservice.dto;

import com.serviconli.patientservice.model.enums.EstadoCliente;
import com.serviconli.patientservice.model.enums.NombreEps;
import jakarta.validation.constraints.Email;
import lombok.*;

@Data
public class UpdateCotizanteRequestDTO {

    private String fechaNacimiento;
    private String celular;
    @Email
    private String correo;
    private String direccionResidencia;
    private EstadoCliente estado;
    private NombreEps eps;
    private String infoAdicional;

}
