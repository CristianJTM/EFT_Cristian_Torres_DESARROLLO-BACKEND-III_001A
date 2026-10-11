package com.bancoxyz.account_service.services;

import com.bancoxyz.account_service.dtos.TransaccionDTO;
import com.bancoxyz.account_service.model.Transaccion;
import com.bancoxyz.account_service.exceptions.TransaccionNoEncontradaException;
import com.bancoxyz.account_service.repositories.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public TransaccionDTO buscarPorId(Long id) {

        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() ->
                        new TransaccionNoEncontradaException(
                                "No se encontró la transacción con ID: " + id
                        )
                );

        return convertirADTO(transaccion);
    }

    public List<TransaccionDTO> buscarPorCuenta(Long cuentaId) {

        return transaccionRepository.findByCuentaId(cuentaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private TransaccionDTO convertirADTO(Transaccion transaccion) {

        TransaccionDTO dto = new TransaccionDTO();

        dto.setId(transaccion.getId());
        dto.setCuentaId(transaccion.getCuentaId());
        dto.setFecha(transaccion.getFecha());
        dto.setMonto(transaccion.getMonto());
        dto.setTipo(transaccion.getTipo());

        return dto;
    }
}