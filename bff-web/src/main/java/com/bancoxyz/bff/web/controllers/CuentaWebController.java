package com.bancoxyz.bff.web.controllers;

import com.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import com.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;
import com.bancoxyz.bff.web.dtos.management.ActualizarCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CrearCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CuentaResponse;
import com.bancoxyz.bff.web.services.BffWebService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/web/cuentas")
public class CuentaWebController {

    private final BffWebService bffWebService;

    public CuentaWebController(BffWebService bffWebService) {
        this.bffWebService = bffWebService;
    }

    // Consultas existentes

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaWebDetalleDTO> obtenerDetalleCuenta(
            @PathVariable Long cuentaId) {
        return ResponseEntity.ok(
                bffWebService.obtenerDetalleCuenta(cuentaId)
        );
    }

    @GetMapping("/{cuentaId}/resumen")
    public ResponseEntity<CuentaWebResumenDTO> obtenerResumenCuenta(
            @PathVariable Long cuentaId) {
        return ResponseEntity.ok(
                bffWebService.obtenerResumenCuenta(cuentaId)
        );
    }

    // Administración de cuentas

    @PostMapping
    public ResponseEntity<CuentaResponse> crearCuenta(
            @RequestBody CrearCuentaRequest request) {
        CuentaResponse cuenta = bffWebService.crearCuenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cuenta);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listarCuentas() {
        return ResponseEntity.ok(bffWebService.listarCuentas());
    }

    @GetMapping("/administracion/{id}")
    public ResponseEntity<CuentaResponse> buscarCuentaAdministrativa(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                bffWebService.buscarCuentaAdministrativa(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizarCuenta(
            @PathVariable Long id,
            @RequestBody ActualizarCuentaRequest request) {
        return ResponseEntity.ok(
                bffWebService.actualizarCuenta(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(
            @PathVariable Long id) {
        bffWebService.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }
}