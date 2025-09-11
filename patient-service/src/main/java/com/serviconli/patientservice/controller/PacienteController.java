package com.serviconli.patientservice.controller;

import com.serviconli.patientservice.dto.*;
import com.serviconli.patientservice.service.PacienteService;
import jakarta.validation.Valid; // Importante para activar las validaciones del DTO
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    // CAMBIO: El cuerpo de la petición ahora es el DTO de creación correcto.
    @PostMapping("/cotizantes")
    public ResponseEntity<CotizanteResponseDTO> crearCotizante(@Valid @RequestBody CreateCotizanteRequestDTO requestDTO) {
        CotizanteResponseDTO nuevoCotizante = pacienteService.crearCotizante(requestDTO);
        return new ResponseEntity<>(nuevoCotizante, HttpStatus.CREATED);
    }

    // CAMBIO: Igual que arriba, se usa el DTO de creación para beneficiarios.
    @PostMapping("/beneficiarios")
    public ResponseEntity<BeneficiarioResponseDTO> crearBeneficiario(@Valid @RequestBody CreateBeneficiarioRequestDTO requestDTO) {
        BeneficiarioResponseDTO nuevoBeneficiario = pacienteService.crearBeneficiario(requestDTO);
        return new ResponseEntity<>(nuevoBeneficiario, HttpStatus.CREATED);
    }

    // MEJORA: Se devuelve 200 OK con una lista vacía si no hay resultados.
    @GetMapping("/search")
    public ResponseEntity<List<BusquedaPacienteResponseDTO>> buscarPacientesPorNombre(@RequestParam(name = "nombre") String nombre) {
        List<BusquedaPacienteResponseDTO> pacientesEncontrados = pacienteService.buscarPorNombre(nombre);
        // Devolver la lista (incluso si está vacía) con un estado 200 OK es más estándar.
        return ResponseEntity.ok(pacientesEncontrados);
    }

    // CAMBIO Y MEJORA: Se usa @PathVariable y se maneja el Optional.
    @GetMapping("/{numeroIdentificacion}")
    public ResponseEntity<BusquedaPacienteResponseDTO> buscarPacientePorIdentificacion(@PathVariable String numeroIdentificacion) {
        // La capa de servicio devuelve un Optional, que manejamos aquí.
        return pacienteService.buscarPorIdentificacion(numeroIdentificacion)
                .map(paciente -> ResponseEntity.ok(paciente)) // Si el paciente existe, devuelve 200 OK con el paciente.
                .orElse(ResponseEntity.notFound().build());   // Si no existe, devuelve un 404 Not Found.
    }


    // --- ENDPOINTS PARA ACTUALIZAR (PUT) Y ELIMINAR (DELETE) ---

    @PutMapping("/cotizantes/{numeroIdentificacion}")
    public ResponseEntity<CotizanteResponseDTO> updateCotizante(
            @PathVariable String numeroIdentificacion,
            @Valid @RequestBody UpdateCotizanteRequestDTO requestDTO) {
        CotizanteResponseDTO cotizanteActualizado = pacienteService.updateCotizante(numeroIdentificacion, requestDTO);
        return ResponseEntity.ok(cotizanteActualizado);
    }

    @PutMapping("/beneficiarios/{numeroIdentificacion}")
    public ResponseEntity<BeneficiarioResponseDTO> updateBeneficiario(
            @PathVariable String numeroIdentificacion,
            @Valid @RequestBody UpdateBeneficiarioRequestDTO requestDTO) {
        BeneficiarioResponseDTO beneficiarioActualizado = pacienteService.updateBeneficiario(numeroIdentificacion, requestDTO);
        return ResponseEntity.ok(beneficiarioActualizado);
    }

    @DeleteMapping("/{numeroIdentificacion}")
    public ResponseEntity<Void> deletePaciente(@PathVariable String numeroIdentificacion) {
        pacienteService.deletePaciente(numeroIdentificacion);
        return ResponseEntity.noContent().build(); // Devuelve un estado 204 No Content
    }
}