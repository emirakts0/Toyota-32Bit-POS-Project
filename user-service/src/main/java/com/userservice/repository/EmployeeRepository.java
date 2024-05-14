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
    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByIdAndDeletedFalse(Long id);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.deleted = true WHERE e.id =:id")
    void markAsDeleted(Long id);

    //@Transactional
    @Modifying
    @Transactional
    @Query("""
              UPDATE Employee e SET
              e.username = COALESCE(:username, e.username),
              e.email = COALESCE(:email, e.email),
              e.password = COALESCE(:password, e.password),
              e.lastUpdateDate = COALESCE(:lastUpdateDate, e.lastUpdateDate)
              WHERE e.id =:id""")
    void updateEmployeeById(Long id,
                            String username,
                            String email,
                            String password,
                            LocalDateTime lastUpdateDate);
}

