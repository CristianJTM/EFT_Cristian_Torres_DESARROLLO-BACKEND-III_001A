package com.bancoxyz.bff.web.services;

import com.bancoxyz.bff.web.clients.AccountManagementClient;
import com.bancoxyz.bff.web.dtos.management.ActualizarCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CrearCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CuentaResponse;

import feign.FeignException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountManagementResilientService {

    private final AccountManagementClient accountManagementClient;

    public AccountManagementResilientService(
            AccountManagementClient accountManagementClient) {
        this.accountManagementClient = accountManagementClient;
    }

    public CuentaResponse crear(CrearCuentaRequest request) {
        return ejecutar(() -> accountManagementClient.crear(request));
    }

    public List<CuentaResponse> listar() {
        return ejecutar(accountManagementClient::listar);
    }

    public CuentaResponse buscarPorId(Long id) {
        return ejecutar(() -> accountManagementClient.buscarPorId(id));
    }

    public CuentaResponse actualizar(
            Long id,
            ActualizarCuentaRequest request) {
        return ejecutar(
                () -> accountManagementClient.actualizar(id, request)
        );
    }

    public void eliminar(Long id) {
        ejecutar(() -> {
            accountManagementClient.eliminar(id);
            return null;
        });
    }

    private <T> T ejecutar(OperacionRemota<T> operacion) {
        try {
            return operacion.ejecutar();
        } catch (FeignException.NotFound e) {
            throw e;
        } catch (FeignException e) {
            throw new IllegalStateException(
                    "No fue posible comunicarse con Account Management Service",
                    e
            );
        }
    }

    @FunctionalInterface
    private interface OperacionRemota<T> {
        T ejecutar();
    }
}