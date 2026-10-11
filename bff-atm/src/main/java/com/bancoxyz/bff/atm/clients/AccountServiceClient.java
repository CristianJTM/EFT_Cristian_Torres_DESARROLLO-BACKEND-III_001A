package com.bancoxyz.bff.atm.clients;

import com.bancoxyz.bff.atm.config.FeignClientConfig;
import com.bancoxyz.bff.atm.dtos.RetiroAtmRequestDTO;
import com.bancoxyz.bff.atm.dtos.core.CuentaCoreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "account-service",
        contextId = "accountServiceClient",
        configuration = FeignClientConfig.class
)
public interface AccountServiceClient {

    @GetMapping("/api/cuentas/{cuentaId}")
    CuentaCoreDTO obtenerCuenta(
            @PathVariable("cuentaId") Long cuentaId
    );
}
