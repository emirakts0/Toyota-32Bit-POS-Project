package com.userservice.repository;

import com.userservice.model.Employee;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);
    Optional<Employee> findById(Long id);
    Optional<Employee> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.deleted = true WHERE e.id =:id")
    void markAsDeleted(Long id);

}

