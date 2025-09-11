package com.serviconli.patientservice.service;

import com.serviconli.patientservice.dto.*;

import java.util.List;
import java.util.Optional;

public interface PacienteService {

    List<BusquedaPacienteResponseDTO> buscarPorNombre(String nombre); //busqueda que estamos usando en el front, para buscar tanto cotizantes como pacientes
    Optional<BusquedaPacienteResponseDTO> buscarPorIdentificacion(String numeroIdentificacion);

    CotizanteResponseDTO crearCotizante(CreateCotizanteRequestDTO  requestDTO);
    BeneficiarioResponseDTO crearBeneficiario(CreateBeneficiarioRequestDTO requestDTO);

    CotizanteResponseDTO updateCotizante(String numeroIdentificacion, UpdateCotizanteRequestDTO requestDTO);
    BeneficiarioResponseDTO updateBeneficiario(String numeroIdentificacion, UpdateBeneficiarioRequestDTO requestDTO);

    void deletePaciente(String numeroIdentificacion);

}