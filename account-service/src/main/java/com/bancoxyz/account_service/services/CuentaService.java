package com.bancoxyz.account_service.services;

import com.bancoxyz.account_service.dtos.CuentaDTO;
import com.bancoxyz.account_service.model.Cuenta;
import com.bancoxyz.account_service.exceptions.CuentaNoEncontradaException;
import com.bancoxyz.account_service.repositories.CuentaRepository;
import com.bancoxyz.account_service.repositories.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    public CuentaDTO buscarPorId(Long cuentaId) {

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new CuentaNoEncontradaException(
                                "No se encontró la cuenta con ID: " + cuentaId
                        )
                );

        return convertirADTO(cuenta);
    }

    private CuentaDTO convertirADTO(Cuenta cuenta) {

        CuentaDTO dto = new CuentaDTO();

        dto.setCuentaId(cuenta.getCuentaId());
        dto.setSaldo(cuenta.getSaldo());

        return dto;
    }
}