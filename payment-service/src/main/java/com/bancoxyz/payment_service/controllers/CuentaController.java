package com.bancoxyz.payment_service.controllers;

import com.bancoxyz.payment_service.dtos.CuentaDTO;
import com.bancoxyz.payment_service.dtos.RetiroDTO;
import com.bancoxyz.payment_service.services.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping("/{cuentaId}/retiros")
    public CuentaDTO realizarRetiro(
            @PathVariable Long cuentaId,
            @RequestBody RetiroDTO retiro) {

        return cuentaService.realizarRetiro(
                cuentaId,
                retiro.getMonto()
        );
    }
}