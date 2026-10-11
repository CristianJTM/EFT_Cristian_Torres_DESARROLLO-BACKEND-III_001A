package com.bancoxyz.account_service.repositories;

import com.bancoxyz.account_service.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByCuentaId(Long cuentaId);

    @Query("SELECT MAX(t.id) FROM Transaccion t")
    Long obtenerUltimoId();
}