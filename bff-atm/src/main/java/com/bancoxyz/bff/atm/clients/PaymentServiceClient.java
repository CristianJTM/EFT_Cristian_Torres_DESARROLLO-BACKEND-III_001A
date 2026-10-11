package com.bancoxyz.bff.atm.clients;

import com.bancoxyz.bff.atm.config.FeignClientConfig;
import com.bancoxyz.bff.atm.dtos.RetiroAtmRequestDTO;
import com.bancoxyz.bff.atm.dtos.core.CuentaCoreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "payment-service",
        contextId = "paymentServiceClient",
        configuration = FeignClientConfig.class
)
public interface PaymentServiceClient {

    @PostMapping("/api/cuentas/{cuentaId}/retiros")
    CuentaCoreDTO realizarRetiro(
            @PathVariable("cuentaId") Long cuentaId,
            @RequestBody RetiroAtmRequestDTO retiro
    );
}