package com.bancoxyz.payment_service.repositories;

import com.bancoxyz.payment_service.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

}