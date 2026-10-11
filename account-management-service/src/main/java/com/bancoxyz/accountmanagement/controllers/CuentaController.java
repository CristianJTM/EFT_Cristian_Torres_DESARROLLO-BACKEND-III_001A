package com.bancoxyz.accountmanagement.controllers;

import com.bancoxyz.accountmanagement.dtos.ActualizarCuentaRequest;
import com.bancoxyz.accountmanagement.dtos.CrearCuentaRequest;
import com.bancoxyz.accountmanagement.dtos.CuentaResponse;
import com.bancoxyz.accountmanagement.services.CuentaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(
            @RequestBody CrearCuentaRequest request) {

        CuentaResponse cuenta = cuentaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(cuenta);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar() {
        return ResponseEntity.ok(cuentaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(cuentaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarCuentaRequest request) {

        return ResponseEntity.ok(cuentaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}