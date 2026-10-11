package com.bancoxyz.account_service.controllers;

import com.bancoxyz.account_service.dtos.CuentaDTO;
import com.bancoxyz.account_service.services.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping("/{cuentaId}")
    public CuentaDTO buscarPorId(@PathVariable Long cuentaId) {

        return cuentaService.buscarPorId(cuentaId);
    }
}