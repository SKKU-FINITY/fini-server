package finity.fini.repository;

import finity.fini.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, String> {
    Optional<Bank> findByFinCoNo(String finCoNo);
}
