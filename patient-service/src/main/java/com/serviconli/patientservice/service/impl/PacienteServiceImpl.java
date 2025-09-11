package com.serviconli.patientservice.service.impl;

import com.serviconli.patientservice.dto.*;
import com.serviconli.patientservice.model.*;
import com.serviconli.patientservice.model.enums.*;
import com.serviconli.patientservice.repository.*;
import com.serviconli.patientservice.service.PacienteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final CotizanteRepository cotizanteRepo;
    private final BeneficiarioRepository beneficiarioRepo;

    @Override
    @Transactional(readOnly = true)
    public List<BusquedaPacienteResponseDTO> buscarPorNombre(String nombre) {
        Stream<BusquedaPacienteResponseDTO> cotizantesStream = cotizanteRepo.findByNombreCompletoContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToBusquedaDTO);

        Stream<BusquedaPacienteResponseDTO> beneficiariosStream = beneficiarioRepo.findByNombreCompletoContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToBusquedaDTO);

        return Stream.concat(cotizantesStream, beneficiariosStream).collect(Collectors.toList());
    }

    // Este método es requerido por la interfaz PacienteService
    @Override
    @Transactional(readOnly = true)
    public Optional<BusquedaPacienteResponseDTO> buscarPorIdentificacion(String numeroIdentificacion) {
        // Buscar primero en cotizantes
        Optional<Cotizante> cotizanteOpt = cotizanteRepo.findByNumeroIdentificacion(numeroIdentificacion);
        if (cotizanteOpt.isPresent()) {
            return Optional.of(mapToBusquedaDTO(cotizanteOpt.get()));
        }

        // Si no, buscar en beneficiarios
        Optional<Beneficiario> beneficiarioOpt = beneficiarioRepo.findByNumeroIdentificacion(numeroIdentificacion);
        if (beneficiarioOpt.isPresent()) {
            return Optional.of(mapToBusquedaDTO(beneficiarioOpt.get()));
        }

        // Si no se encuentra en ninguna tabla, devolvemos un Optional vacío.
        return Optional.empty();
    }


    @Override
    @Transactional
    public CotizanteResponseDTO crearCotizante(CreateCotizanteRequestDTO requestDTO) {
        if (cotizanteRepo.findByNumeroIdentificacion(requestDTO.getNumeroIdentificacion()).isPresent() ||
                beneficiarioRepo.findByNumeroIdentificacion(requestDTO.getNumeroIdentificacion()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un paciente con el número de identificación: " + requestDTO.getNumeroIdentificacion());
        }

        Cotizante cotizante = new Cotizante();
        cotizante.setNombreCompleto(requestDTO.getNombreCompleto());
        cotizante.setTipoIdentificacion(requestDTO.getTipoIdentificacion());
        cotizante.setNumeroIdentificacion(requestDTO.getNumeroIdentificacion());
        cotizante.setFechaNacimiento(requestDTO.getFechaNacimiento());
        cotizante.setFechaExpedicion(requestDTO.getFechaExpedicion());
        cotizante.setCelular(requestDTO.getCelular());
        cotizante.setCorreo(requestDTO.getCorreo());
        cotizante.setDireccionResidencia(requestDTO.getDireccionResidencia());
        cotizante.setEps(requestDTO.getEps());
        cotizante.setInfoAdicional(requestDTO.getInfoAdicional());
        cotizante.setEstado(requestDTO.getEstado() != null ? requestDTO.getEstado() : EstadoCliente.ACTIVO);
        cotizante.setParentesco(Parentesco.COTIZANTE);

        Cotizante guardado = cotizanteRepo.save(cotizante);
        return mapToCotizanteResponseDTO(guardado);
    }

    @Override
    @Transactional
    public BeneficiarioResponseDTO crearBeneficiario(CreateBeneficiarioRequestDTO requestDTO) {
        Cotizante cotizante = cotizanteRepo.findByNumeroIdentificacion(requestDTO.getCotizanteNumeroIdentificacion())
                .orElseThrow(() -> new EntityNotFoundException("Cotizante con número de identificación " + requestDTO.getCotizanteNumeroIdentificacion() + " no encontrado."));

        if (cotizanteRepo.findByNumeroIdentificacion(requestDTO.getNumeroIdentificacion()).isPresent() ||
                beneficiarioRepo.findByNumeroIdentificacion(requestDTO.getNumeroIdentificacion()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un paciente con el número de identificación: " + requestDTO.getNumeroIdentificacion());
        }

        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setNombreCompleto(requestDTO.getNombreCompleto());
        beneficiario.setTipoIdentificacion(requestDTO.getTipoIdentificacion());
        beneficiario.setNumeroIdentificacion(requestDTO.getNumeroIdentificacion());
        beneficiario.setFechaNacimiento(requestDTO.getFechaNacimiento());
        beneficiario.setFechaExpedicion(requestDTO.getFechaExpedicion());
        beneficiario.setCelular(requestDTO.getCelular());
        beneficiario.setCorreo(requestDTO.getCorreo());
        beneficiario.setDireccionResidencia(requestDTO.getDireccionResidencia());
        beneficiario.setEps(requestDTO.getEps());
        beneficiario.setParentesco(requestDTO.getParentesco());
        beneficiario.setEstado(requestDTO.getEstado() != null ? requestDTO.getEstado() : EstadoCliente.ACTIVO);
        beneficiario.setCotizante(cotizante);

        Beneficiario guardado = beneficiarioRepo.save(beneficiario);
        return mapToBeneficiarioResponseDTO(guardado);
    }

    // Estos son los métodos  para la búsqueda
    private BusquedaPacienteResponseDTO mapToBusquedaDTO(Cotizante cotizante) {
        BusquedaPacienteResponseDTO dto = new BusquedaPacienteResponseDTO();
        dto.setTipoPaciente("COTIZANTE");
        dto.setId(cotizante.getId());
        dto.setNombreCompleto(cotizante.getNombreCompleto());
        // ... (resto de campos)
        dto.setTipoIdentificacion(cotizante.getTipoIdentificacion());
        dto.setNumeroIdentificacion(cotizante.getNumeroIdentificacion());
        dto.setFechaNacimiento(cotizante.getFechaNacimiento());
        dto.setFechaExpedicion(cotizante.getFechaExpedicion());
        dto.setCelular(cotizante.getCelular());
        dto.setCorreo(cotizante.getCorreo());
        dto.setDireccionResidencia(cotizante.getDireccionResidencia());
        dto.setEstado(cotizante.getEstado());
        dto.setParentesco(cotizante.getParentesco());
        dto.setEps(cotizante.getEps());
        dto.setInfoAdicional(cotizante.getInfoAdicional());
        return dto;
    }

    private BusquedaPacienteResponseDTO mapToBusquedaDTO(Beneficiario beneficiario) {
        BusquedaPacienteResponseDTO dto = new BusquedaPacienteResponseDTO();
        dto.setTipoPaciente("BENEFICIARIO");
        dto.setId(beneficiario.getId());
        dto.setNombreCompleto(beneficiario.getNombreCompleto());
        // ... (resto de campos)
        dto.setTipoIdentificacion(beneficiario.getTipoIdentificacion());
        dto.setNumeroIdentificacion(beneficiario.getNumeroIdentificacion());
        dto.setFechaNacimiento(beneficiario.getFechaNacimiento());
        dto.setFechaExpedicion(beneficiario.getFechaExpedicion());
        dto.setCelular(beneficiario.getCelular());
        dto.setCorreo(beneficiario.getCorreo());
        dto.setDireccionResidencia(beneficiario.getDireccionResidencia());
        dto.setEstado(beneficiario.getEstado());
        dto.setParentesco(beneficiario.getParentesco());
        dto.setEps(beneficiario.getEps());
        if (beneficiario.getCotizante() != null) {
            CotizanteSummaryDTO summaryDTO = new CotizanteSummaryDTO();
            summaryDTO.setId(beneficiario.getCotizante().getId());
            summaryDTO.setNombreCompleto(beneficiario.getCotizante().getNombreCompleto());
            summaryDTO.setTipoIdentificacion(beneficiario.getCotizante().getTipoIdentificacion());
            summaryDTO.setNumeroIdentificacion(beneficiario.getCotizante().getNumeroIdentificacion());
            dto.setCotizante(summaryDTO);
        }
        return dto;
    }





    // --- NUEVOS MÉTODOS PARA ACTUALIZAR Y ELIMINAR ---

    @Override
    @Transactional
    public CotizanteResponseDTO updateCotizante(String numeroIdentificacion, UpdateCotizanteRequestDTO requestDTO) {
        // 1. Buscar el cotizante existente o lanzar un error si no se encuentra.
        Cotizante cotizante = cotizanteRepo.findByNumeroIdentificacion(numeroIdentificacion)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un cotizante con el número de identificación: " + numeroIdentificacion));

        // 2. Actualizar solo los campos que vienen en el DTO (permite actualizaciones parciales).
        if (requestDTO.getCelular() != null) {
            cotizante.setCelular(requestDTO.getCelular());
        }
        if (requestDTO.getCorreo() != null) {
            cotizante.setCorreo(requestDTO.getCorreo());
        }
        if (requestDTO.getDireccionResidencia() != null) {
            cotizante.setDireccionResidencia(requestDTO.getDireccionResidencia());
        }
        if (requestDTO.getEstado() != null) {
            cotizante.setEstado(requestDTO.getEstado());
        }
        if (requestDTO.getEps() != null) {
            cotizante.setEps(requestDTO.getEps());
        }
        if (requestDTO.getInfoAdicional() != null) {
            cotizante.setInfoAdicional(requestDTO.getInfoAdicional());
        }

        // 3. Guardar los cambios en la base de datos.
        Cotizante cotizanteActualizado = cotizanteRepo.save(cotizante);

        // 4. Devolver la versión actualizada.
        return mapToCotizanteResponseDTO(cotizanteActualizado);
    }

    @Override
    @Transactional
    public BeneficiarioResponseDTO updateBeneficiario(String numeroIdentificacion, UpdateBeneficiarioRequestDTO requestDTO) {
        // 1. Buscar el beneficiario existente o lanzar un error.
        Beneficiario beneficiario = beneficiarioRepo.findByNumeroIdentificacion(numeroIdentificacion)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un beneficiario con el número de identificación: " + numeroIdentificacion));

        // 2. Actualizar los campos del DTO.
        if (requestDTO.getCelular() != null) {
            beneficiario.setCelular(requestDTO.getCelular());
        }
        if (requestDTO.getCorreo() != null) {
            beneficiario.setCorreo(requestDTO.getCorreo());
        }
        if (requestDTO.getDireccionResidencia() != null) {
            beneficiario.setDireccionResidencia(requestDTO.getDireccionResidencia());
        }
        if (requestDTO.getEstado() != null) {
            beneficiario.setEstado(requestDTO.getEstado());
        }
        if (requestDTO.getEps() != null) {
            beneficiario.setEps(requestDTO.getEps());
        }
        if (requestDTO.getParentesco() != null) {
            beneficiario.setParentesco(requestDTO.getParentesco());
        }
        if (requestDTO.getInfoAdicional() != null) {
            beneficiario.setInfoAdicional(requestDTO.getInfoAdicional());
        }

        // 3. Guardar los cambios.
        Beneficiario beneficiarioActualizado = beneficiarioRepo.save(beneficiario);

        // 4. Devolver la versión actualizada.
        return mapToBeneficiarioResponseDTO(beneficiarioActualizado);
    }

    @Override
    @Transactional
    public void deletePaciente(String numeroIdentificacion) {
        // 1. Intentar buscar y eliminar como si fuera un cotizante.
        Optional<Cotizante> cotizanteOpt = cotizanteRepo.findByNumeroIdentificacion(numeroIdentificacion);
        if (cotizanteOpt.isPresent()) {
            cotizanteRepo.delete(cotizanteOpt.get());
            // Gracias a la configuración de cascada, al eliminar un cotizante,
            // se eliminarán automáticamente todos sus beneficiarios asociados.
            return; // Termina la ejecución si se encontró y eliminó.
        }

        // 2. Si no se encontró como cotizante, intentar buscar y eliminar como beneficiario.
        Optional<Beneficiario> beneficiarioOpt = beneficiarioRepo.findByNumeroIdentificacion(numeroIdentificacion);
        if (beneficiarioOpt.isPresent()) {
            beneficiarioRepo.delete(beneficiarioOpt.get());
            return; // Termina la ejecución.
        }

        // 3. Si no se encontró en ninguna de las dos tablas, lanzar un error.
        throw new EntityNotFoundException("No se encontró ningún paciente (cotizante o beneficiario) con el número de identificación: " + numeroIdentificacion);
    }

    // Estos son los métodos que usan crearCotizante y crearBeneficiario para devolver la respuesta.

    private CotizanteResponseDTO mapToCotizanteResponseDTO(Cotizante cotizante) {
        CotizanteResponseDTO dto = new CotizanteResponseDTO();
        dto.setId(cotizante.getId());
        dto.setNombreCompleto(cotizante.getNombreCompleto());
        dto.setTipoIdentificacion(cotizante.getTipoIdentificacion());
        dto.setNumeroIdentificacion(cotizante.getNumeroIdentificacion());
        dto.setFechaNacimiento(cotizante.getFechaNacimiento());
        dto.setFechaExpedicion(cotizante.getFechaExpedicion());
        dto.setCelular(cotizante.getCelular());
        dto.setCorreo(cotizante.getCorreo());
        dto.setDireccionResidencia(cotizante.getDireccionResidencia());
        dto.setEstado(cotizante.getEstado());
        dto.setParentesco(cotizante.getParentesco());
        dto.setEps(cotizante.getEps());
        dto.setInfoAdicional(cotizante.getInfoAdicional());

        // Mapea la lista de beneficiarios si existen
        if (cotizante.getBeneficiarios() != null) {
            dto.setBeneficiarios(
                    cotizante.getBeneficiarios().stream()
                            .map(this::mapToBeneficiarioResponseDTO)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    private BeneficiarioResponseDTO mapToBeneficiarioResponseDTO(Beneficiario beneficiario) {
        BeneficiarioResponseDTO dto = new BeneficiarioResponseDTO();
        dto.setId(beneficiario.getId());
        dto.setNombreCompleto(beneficiario.getNombreCompleto());
        dto.setTipoIdentificacion(beneficiario.getTipoIdentificacion());
        dto.setNumeroIdentificacion(beneficiario.getNumeroIdentificacion());
        dto.setFechaNacimiento(beneficiario.getFechaNacimiento());
        dto.setFechaExpedicion(beneficiario.getFechaExpedicion());
        dto.setCelular(beneficiario.getCelular());
        dto.setCorreo(beneficiario.getCorreo());
        dto.setDireccionResidencia(beneficiario.getDireccionResidencia());
        dto.setEstado(beneficiario.getEstado());
        dto.setParentesco(beneficiario.getParentesco());
        dto.setEps(beneficiario.getEps());
        dto.setInfoAdicional(beneficiario.getInfoAdicional());

        if (beneficiario.getCotizante() != null) {
            CotizanteSummaryDTO summaryDTO = new CotizanteSummaryDTO();
            summaryDTO.setId(beneficiario.getCotizante().getId());
            summaryDTO.setNombreCompleto(beneficiario.getCotizante().getNombreCompleto());
            summaryDTO.setTipoIdentificacion(beneficiario.getCotizante().getTipoIdentificacion());
            summaryDTO.setNumeroIdentificacion(beneficiario.getCotizante().getNumeroIdentificacion());
            dto.setCotizante(summaryDTO);
        }
        return dto;
    }
}