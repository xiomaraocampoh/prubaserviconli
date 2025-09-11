package com.serviconli.patientservice.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TipoIdentificacion {
    CEDULA_CIUDADANIA("CÉDULA DE CIUDADANÍA"),
    TARJETA_IDENTIDAD("TARJETA DE IDENTIDAD"),
    CEDULA_EXTRANJERIA("CÉDULA DE EXTRANJERÍA"),
    REGISTRO_CIVIL("REGISTRO CIVIL"),
    PASAPORTE("PASAPORTE"),
    PEP("PEP: PERMISO ESPECIAL DE PERMANENCIA"),
    PT("PT: PERMISO POR PROTECCIÓN TEMPORAL");

    private final String descripcion;

    TipoIdentificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    @JsonValue // Esto asegura que en el JSON se envíe la descripción completa
    public String getDescripcion() {
        return descripcion;
    }
}