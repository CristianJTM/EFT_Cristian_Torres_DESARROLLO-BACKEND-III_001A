package com.bancoxyz.bff.web.clients;

import com.bancoxyz.bff.web.config.FeignClientConfig;
import com.bancoxyz.bff.web.dtos.management.ActualizarCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CrearCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CuentaResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "account-management-service",
        contextId = "webAccountManagementClient",
        configuration = FeignClientConfig.class
)
public interface AccountManagementClient {

    @PostMapping("/api/cuentas")
    CuentaResponse crear(
            @RequestBody CrearCuentaRequest request
    );

    @GetMapping("/api/cuentas")
    List<CuentaResponse> listar();

    @GetMapping("/api/cuentas/{id}")
    CuentaResponse buscarPorId(
            @PathVariable("id") Long id
    );

    @PutMapping("/api/cuentas/{id}")
    CuentaResponse actualizar(
            @PathVariable("id") Long id,
            @RequestBody ActualizarCuentaRequest request
    );

    @DeleteMapping("/api/cuentas/{id}")
    void eliminar(
            @PathVariable("id") Long id
    );
}