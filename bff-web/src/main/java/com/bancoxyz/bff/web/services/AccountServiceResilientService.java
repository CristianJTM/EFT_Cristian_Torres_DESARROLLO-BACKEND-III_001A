package com.bancoxyz.bff.web.services;

import com.bancoxyz.bff.web.clients.AccountServiceClient;
import com.bancoxyz.bff.web.dtos.core.CuentaCoreDTO;
import com.bancoxyz.bff.web.dtos.core.TransaccionCoreDTO;
import com.bancoxyz.bff.web.exceptions.BackendCoreNoDisponibleException;
import com.bancoxyz.bff.web.exceptions.CuentaNoEncontradaException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceResilientService {

    private final AccountServiceClient accountServiceClient;

    public AccountServiceResilientService(
            AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @CircuitBreaker(
            name = "accountService",
            fallbackMethod = "fallbackCuenta"
    )
    @Retry(name = "accountService")
    public CuentaCoreDTO obtenerCuenta(Long cuentaId) {
        try {
            return accountServiceClient.obtenerCuenta(cuentaId);
        } catch (FeignException.NotFound e) {
            throw new CuentaNoEncontradaException(cuentaId);
        } catch (FeignException e) {
            throw new BackendCoreNoDisponibleException(
                    "No fue posible comunicarse con Account Service"
            );
        }
    }

    @CircuitBreaker(
            name = "accountService",
            fallbackMethod = "fallbackTransacciones"
    )
    @Retry(name = "accountService")
    public List<TransaccionCoreDTO> obtenerTransaccionesPorCuenta(
            Long cuentaId) {
        try {
            return accountServiceClient
                    .obtenerTransaccionesPorCuenta(cuentaId);
        } catch (FeignException.NotFound e) {
            throw new CuentaNoEncontradaException(cuentaId);
        } catch (FeignException e) {
            throw new BackendCoreNoDisponibleException(
                    "No fue posible obtener las transacciones desde Account Service"
            );
        }
    }

    private CuentaCoreDTO fallbackCuenta(
            Long cuentaId,
            Throwable exception) {
        throw new BackendCoreNoDisponibleException(
                "Account Service no disponible. No fue posible obtener la cuenta."
        );
    }

    private List<TransaccionCoreDTO> fallbackTransacciones(
            Long cuentaId,
            Throwable exception) {
        throw new BackendCoreNoDisponibleException(
                "Account Service no disponible. No fue posible obtener las transacciones."
        );
    }
}