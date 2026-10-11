package com.bancoxyz.accountmanagement.repositories;

import com.bancoxyz.accountmanagement.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
}