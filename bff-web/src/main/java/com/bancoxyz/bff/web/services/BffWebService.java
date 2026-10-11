package com.bancoxyz.bff.web.services;

import com.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import com.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;
import com.bancoxyz.bff.web.dtos.MovimientoWebDTO;
import com.bancoxyz.bff.web.dtos.ResumenMovimientosDTO;
import com.bancoxyz.bff.web.dtos.core.CuentaCoreDTO;
import com.bancoxyz.bff.web.dtos.core.TransaccionCoreDTO;
import com.bancoxyz.bff.web.dtos.management.ActualizarCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CrearCuentaRequest;
import com.bancoxyz.bff.web.dtos.management.CuentaResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BffWebService {

    private final AccountServiceResilientService accountService;
    private final AccountManagementResilientService accountManagementService;

    public BffWebService(
            AccountServiceResilientService accountService,
            AccountManagementResilientService accountManagementService) {
        this.accountService = accountService;
        this.accountManagementService = accountManagementService;
    }

    public CuentaWebDetalleDTO obtenerDetalleCuenta(Long cuentaId) {

        CuentaCoreDTO cuenta =
                accountService.obtenerCuenta(cuentaId);

        List<TransaccionCoreDTO> transacciones =
                accountService.obtenerTransaccionesPorCuenta(cuentaId);

        ResumenMovimientosDTO resumenMovimientos =
                construirResumen(transacciones);

        resumenMovimientos.setSaldo(cuenta.getSaldo());

        CuentaWebResumenDTO resumen =
                new CuentaWebResumenDTO();

        resumen.setCuentaId(cuenta.getCuentaId());
        resumen.setResumen(resumenMovimientos);

        List<MovimientoWebDTO> movimientos =
                transacciones.stream()
                        .map(this::convertirMovimiento)
                        .toList();

        CuentaWebDetalleDTO detalle =
                new CuentaWebDetalleDTO();

        detalle.setCuentaId(cuenta.getCuentaId());
        detalle.setResumen(resumen);
        detalle.setMovimientos(movimientos);

        return detalle;
    }

    public CuentaWebResumenDTO obtenerResumenCuenta(Long cuentaId) {

        CuentaCoreDTO cuenta =
                accountService.obtenerCuenta(cuentaId);

        List<TransaccionCoreDTO> transacciones =
                accountService.obtenerTransaccionesPorCuenta(cuentaId);

        ResumenMovimientosDTO resumenMovimientos =
                construirResumen(transacciones);

        resumenMovimientos.setSaldo(cuenta.getSaldo());

        CuentaWebResumenDTO resumen =
                new CuentaWebResumenDTO();

        resumen.setCuentaId(cuenta.getCuentaId());
        resumen.setResumen(resumenMovimientos);

        return resumen;
    }

    private ResumenMovimientosDTO construirResumen(
            List<TransaccionCoreDTO> transacciones) {

        BigDecimal totalDepositos =
                transacciones.stream()
                        .filter(t ->
                                "deposito".equalsIgnoreCase(t.getTipo()))
                        .map(TransaccionCoreDTO::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRetiros =
                transacciones.stream()
                        .filter(t ->
                                !"deposito".equalsIgnoreCase(t.getTipo()))
                        .map(TransaccionCoreDTO::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        ResumenMovimientosDTO resumen =
                new ResumenMovimientosDTO();

        resumen.setTotalMovimientos(transacciones.size());
        resumen.setTotalDepositos(totalDepositos);
        resumen.setTotalRetiros(totalRetiros);

        return resumen;
    }

    private MovimientoWebDTO convertirMovimiento(
            TransaccionCoreDTO transaccion) {

        MovimientoWebDTO movimiento =
                new MovimientoWebDTO();

        movimiento.setId(transaccion.getId());
        movimiento.setFecha(transaccion.getFecha());
        movimiento.setMonto(transaccion.getMonto());
        movimiento.setTipo(transaccion.getTipo());

        return movimiento;
    }


    public CuentaResponse crearCuenta(CrearCuentaRequest request) {
        return accountManagementService.crear(request);
    }

    public List<CuentaResponse> listarCuentas() {
        return accountManagementService.listar();
    }

    public CuentaResponse buscarCuentaAdministrativa(Long id) {
        return accountManagementService.buscarPorId(id);
    }

    public CuentaResponse actualizarCuenta(
            Long id,
            ActualizarCuentaRequest request) {
        return accountManagementService.actualizar(id, request);
    }

    public void eliminarCuenta(Long id) {
        accountManagementService.eliminar(id);
    }
}