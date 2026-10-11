package com.bancoxyz.accountmanagement.services;

import com.bancoxyz.accountmanagement.dtos.ActualizarCuentaRequest;
import com.bancoxyz.accountmanagement.dtos.CrearCuentaRequest;
import com.bancoxyz.accountmanagement.dtos.CuentaResponse;
import com.bancoxyz.accountmanagement.exceptions.CuentaNoEncontradaException;
import com.bancoxyz.accountmanagement.kafka.CuentaProducer;
import com.bancoxyz.accountmanagement.model.Cuenta;
import com.bancoxyz.accountmanagement.repositories.CuentaRepository;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final CuentaProducer cuentaProducer;

    public CuentaService(
            CuentaRepository cuentaRepository,
            CuentaProducer cuentaProducer) {
        this.cuentaRepository = cuentaRepository;
        this.cuentaProducer = cuentaProducer;
    }

    public CuentaResponse crear(CrearCuentaRequest request) {
        validarCuenta(request.getSaldo(), request.getTipo());

        Cuenta cuenta = new Cuenta();

        // La tabla actual no se asume configurada con AUTO_INCREMENT.
        Long siguienteId = cuentaRepository.findAll()
                .stream()
                .map(Cuenta::getCuentaId)
                .filter(id -> id != null)
                .max(Comparator.naturalOrder())
                .orElse(100L) + 1L;

        cuenta.setCuentaId(siguienteId);
        cuenta.setSaldo(request.getSaldo());
        cuenta.setTipo(request.getTipo().trim());

        Cuenta guardada = cuentaRepository.save(cuenta);
        CuentaResponse response = convertirAResponse(guardada);

        cuentaProducer.publicar(
                "CuentaCreada",
                guardada.getCuentaId(),
                guardada.getSaldo(),
                guardada.getTipo()
        );

        return response;
    }

    public List<CuentaResponse> listar() {
        return cuentaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public CuentaResponse buscarPorId(Long cuentaId) {
        Cuenta cuenta = obtenerCuenta(cuentaId);
        return convertirAResponse(cuenta);
    }

    public CuentaResponse actualizar(
            Long cuentaId,
            ActualizarCuentaRequest request) {

        validarCuenta(request.getSaldo(), request.getTipo());

        Cuenta cuenta = obtenerCuenta(cuentaId);
        cuenta.setSaldo(request.getSaldo());
        cuenta.setTipo(request.getTipo().trim());

        Cuenta actualizada = cuentaRepository.save(cuenta);

        cuentaProducer.publicar(
                "CuentaActualizada",
                actualizada.getCuentaId(),
                actualizada.getSaldo(),
                actualizada.getTipo()
        );

        return convertirAResponse(actualizada);
    }

    public void eliminar(Long cuentaId) {
        Cuenta cuenta = obtenerCuenta(cuentaId);

        cuentaRepository.delete(cuenta);

        cuentaProducer.publicar(
                "CuentaEliminada",
                cuenta.getCuentaId(),
                cuenta.getSaldo(),
                cuenta.getTipo()
        );
    }

    private Cuenta obtenerCuenta(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new CuentaNoEncontradaException(
                                "No se encontró la cuenta con ID: " + cuentaId
                        )
                );
    }

    private CuentaResponse convertirAResponse(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getCuentaId(),
                cuenta.getSaldo(),
                cuenta.getTipo()
        );
    }

    private void validarCuenta(
            java.math.BigDecimal saldo,
            String tipo) {

        if (saldo == null) {
            throw new IllegalArgumentException(
                    "El saldo es obligatorio"
            );
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de cuenta es obligatorio"
            );
        }
    }
}