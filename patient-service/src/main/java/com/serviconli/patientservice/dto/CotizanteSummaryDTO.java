package com.serviconli.patientservice.dto;

import com.serviconli.patientservice.model.enums.TipoIdentificacion;
import lombok.*;

//DTO auxiliar para no anidar el CotizanteResponseDTO completo y evitar bucles.
@Data
public class CotizanteSummaryDTO {

    private Long id;
    private String nombreCompleto;
    private TipoIdentificacion tipoIdentificacion;
    private String numeroIdentificacion;

}
