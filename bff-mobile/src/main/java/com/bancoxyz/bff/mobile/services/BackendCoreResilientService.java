package com.bancoxyz.bff.mobile.services;

import com.bancoxyz.bff.mobile.clients.AccountServiceClient;
import com.bancoxyz.bff.mobile.dtos.core.CuentaCoreDTO;
import com.bancoxyz.bff.mobile.dtos.core.TransaccionCoreDTO;
import com.bancoxyz.bff.mobile.exceptions.BackendCoreNoDisponibleException;
import com.bancoxyz.bff.mobile.exceptions.CuentaNoEncontradaException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackendCoreResilientService {

    private final AccountServiceClient accountServiceClient;

    public BackendCoreResilientService(
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
            fallbackMethod = "fallbackTransaccion"
    )
    @Retry(name = "accountService")
    public TransaccionCoreDTO obtenerTransaccion(Long id) {
        try {
            return accountServiceClient.obtenerTransaccion(id);

        } catch (FeignException.NotFound e) {
            throw e;

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
            return accountServiceClient.obtenerTransaccionesPorCuenta(cuentaId);

        } catch (FeignException.NotFound e) {
            throw new CuentaNoEncontradaException(cuentaId);

        } catch (FeignException e) {
            throw new BackendCoreNoDisponibleException(
                    "No fue posible comunicarse con Account Service"
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

    private TransaccionCoreDTO fallbackTransaccion(
            Long id,
            Throwable exception) {
        throw new BackendCoreNoDisponibleException(
                "Account Service no disponible. No fue posible obtener la transacción."
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