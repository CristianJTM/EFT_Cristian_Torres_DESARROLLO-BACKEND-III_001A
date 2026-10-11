
package com.bancoxyz.bff.atm.services;

import com.bancoxyz.bff.atm.clients.AccountServiceClient;
import com.bancoxyz.bff.atm.clients.PaymentServiceClient;
import com.bancoxyz.bff.atm.dtos.RetiroAtmRequestDTO;
import com.bancoxyz.bff.atm.dtos.core.CuentaCoreDTO;
import com.bancoxyz.bff.atm.exceptions.BackendCoreNoDisponibleException;
import com.bancoxyz.bff.atm.exceptions.CuentaNoEncontradaException;
import com.bancoxyz.bff.atm.exceptions.MontoInvalidoException;
import com.bancoxyz.bff.atm.exceptions.SaldoInsuficienteException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Service;

@Service
public class BackendCoreResilientService {

    private final AccountServiceClient accountServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    public BackendCoreResilientService(
            AccountServiceClient accountServiceClient,
            PaymentServiceClient paymentServiceClient) {

        this.accountServiceClient = accountServiceClient;
        this.paymentServiceClient = paymentServiceClient;
    }

    // Consultas: account-service
    @CircuitBreaker(
            name = "accountService",
            fallbackMethod = "fallbackObtenerCuenta"
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

    // Retiros: payment-service
    @CircuitBreaker(
            name = "paymentService",
            fallbackMethod = "fallbackRealizarRetiro"
    )
    @Retry(name = "paymentService")
    public CuentaCoreDTO realizarRetiro(
            Long cuentaId,
            RetiroAtmRequestDTO retiro) {

        try {
            return paymentServiceClient.realizarRetiro(
                    cuentaId,
                    retiro
            );

        } catch (FeignException.NotFound e) {

            throw new CuentaNoEncontradaException(cuentaId);

        } catch (FeignException.BadRequest e) {

            String respuesta = e.contentUTF8();

            if (respuesta.contains("\"error\":\"Monto inválido\"")) {
                throw new MontoInvalidoException(
                        "El monto del retiro debe ser mayor a cero"
                );
            }

            if (respuesta.contains("\"error\":\"Saldo insuficiente\"")) {
                throw new SaldoInsuficienteException(
                        "Saldo insuficiente para realizar el retiro"
                );
            }

            throw e;

        } catch (FeignException e) {

            throw new BackendCoreNoDisponibleException(
                    "No fue posible comunicarse con Payment Service"
            );
        }
    }

    // Fallback de Account Service
    private CuentaCoreDTO fallbackObtenerCuenta(
            Long cuentaId,
            Throwable exception) {

        if (exception instanceof CuentaNoEncontradaException ex) {
            throw ex;
        }

        throw new BackendCoreNoDisponibleException(
                "Account Service no disponible. "
                        + "No fue posible consultar la cuenta."
        );
    }

    // Fallback de Payment Service
    private CuentaCoreDTO fallbackRealizarRetiro(
            Long cuentaId,
            RetiroAtmRequestDTO retiro,
            Throwable exception) {

        if (exception instanceof CuentaNoEncontradaException ex) {
            throw ex;
        }

        if (exception instanceof MontoInvalidoException ex) {
            throw ex;
        }

        if (exception instanceof SaldoInsuficienteException ex) {
            throw ex;
        }

        throw new BackendCoreNoDisponibleException(
                "Payment Service no disponible. "
                        + "No fue posible realizar el retiro."
        );
    }
}