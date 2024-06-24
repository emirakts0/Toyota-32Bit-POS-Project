package com.user.Service;

import com.user.dto.RegisterRequestDto;
import com.user.dto.UpdateRequestDto;
import com.user.exception.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/**
 * Service interface for managing users.
 * Provides methods for registering, updating, deleting, and reactivating employees.
 * @author Emir Aktaş
 */
public interface UserManagementService {

    /**
     * Registers a set of employees.
     *
     * @param registerRequestDtoSet the set of employee registration request DTOs
     * @return the set of registered employee DTOs
     * @throws TakenUsernameException if a username is already taken
     * @throws TakenEmailException if an email is already taken
     */
    Set<RegisterRequestDto> registerEmployee(Set<RegisterRequestDto> registerRequestDtoSet);


    /**
     * Updates an existing employee by their ID.
     *
     * @param  id the ID of the employee to be updated
     * @param  updateRequestDto the employee update request DTO
     * @return the updated employee DTO
     * @throws EmployeeNotFoundException if the employee with the given ID is not found
     * @throws TakenUsernameException if the new username is already taken
     * @throws TakenEmailException if the new email is already taken
     */
    @Transactional
    UpdateRequestDto updateEmployeeById(Long id, UpdateRequestDto updateRequestDto);


    /**
     * Deletes an employee by their ID.
     *
     * @param  id the ID of the employee to be deleted
     * @return the ID of the deleted employee
     * @throws EmployeeNotFoundException if the employee with the given ID is not found
     * @throws EmployeeAlreadyDeletedException if the employee with the given ID is already deleted
     */
    @Transactional
    Long deleteEmployeeById(Long id);


    /**
     * Reactivates a previously deleted employee by their ID.
     *
     * @param id the ID of the employee to be reactivated
     * @return the ID of the reactivated employee
     * @throws EmployeeNotFoundException if the employee with the given ID is not found
     * @throws EmployeeAlreadyExistsException if the employee with the given ID is not marked as deleted
     */
    @Transactional
    Long reactivateEmployeeById(Long id);
}
