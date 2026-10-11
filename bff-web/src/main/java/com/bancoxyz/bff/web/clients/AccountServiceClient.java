package com.bancoxyz.bff.web.clients;

import com.bancoxyz.bff.web.config.FeignClientConfig;
import com.bancoxyz.bff.web.dtos.core.CuentaCoreDTO;
import com.bancoxyz.bff.web.dtos.core.TransaccionCoreDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "account-service",
        contextId = "webAccountServiceClient",
        configuration = FeignClientConfig.class
)
public interface AccountServiceClient {

    @GetMapping("/api/cuentas/{cuentaId}")
    CuentaCoreDTO obtenerCuenta(
            @PathVariable("cuentaId") Long cuentaId
    );

    @GetMapping("/api/transacciones/cuenta/{cuentaId}")
    List<TransaccionCoreDTO> obtenerTransaccionesPorCuenta(
            @PathVariable("cuentaId") Long cuentaId
    );
}