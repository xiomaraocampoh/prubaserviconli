package com.serviconli.patientservice.dto;

import com.serviconli.patientservice.model.enums.EstadoCliente;
import com.serviconli.patientservice.model.enums.NombreEps;
import com.serviconli.patientservice.model.enums.Parentesco;
import com.serviconli.patientservice.model.enums.TipoIdentificacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBeneficiarioRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio.")
    private String nombreCompleto;

    @NotNull(message = "El tipo de identificación es obligatorio.")
    private TipoIdentificacion tipoIdentificacion;

    @NotBlank(message = "El número de identificación es obligatorio.")
    private String numeroIdentificacion;

    private String fechaNacimiento;

    private String fechaExpedicion;

    private String celular;

    @Email(message = "El formato del correo electrónico no es válido.")
    private String correo;

    private String direccionResidencia;

    private EstadoCliente estado;

    @NotNull(message = "El parentesco es obligatorio.")
    private Parentesco parentesco;

    @NotNull(message = "La EPS es obligatoria.")
    private NombreEps eps;

    @NotBlank(message = "El número de identificación del cotizante es obligatorio.")
    private String cotizanteNumeroIdentificacion;
}