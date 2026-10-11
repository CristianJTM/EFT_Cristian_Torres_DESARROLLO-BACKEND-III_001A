package com.bancoxyz.account_service.repositories;

import com.bancoxyz.account_service.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

}