package com.serviconli.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CotizanteSummaryDTO {
    private Long id;
    private String nombreCompleto;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
}