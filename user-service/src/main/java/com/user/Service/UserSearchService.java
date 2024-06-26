package com.user.Service;

import com.user.dto.EmployeeDto;
import com.user.exception.EmployeeNotFoundException;
import com.user.exception.InvalidInputException;
import org.springframework.data.domain.Page;

/**
 * Service interface for searching and retrieving employees.
 * Provides methods for fetching employee details by various criteria.
 * @author Emir Aktaş
 */
public interface UserSearchService {

    /**
     * Retrieves an employee by their ID.
     *
     * @param  id the ID of the employee to be retrieved
     * @return the employee data transfer object
     * @throws EmployeeNotFoundException if the employee with the given ID is not found
     */
    EmployeeDto getEmployeeById(Long id);


    /**
     * Searches for an employee by their username.
     *
     * @param  username the username of the employee to be searched
     * @return the employee data transfer object
     * @throws EmployeeNotFoundException if the employee with the given username is not found
     */
    EmployeeDto searchEmployeeByUsername(String username);


    /**
     * Retrieves a paginated list of employees with optional filtering to hide deleted employees.
     *
     * @param  pageSize    the size of the page to be returned
     * @param  pageNumber  the number of the page to be returned
     * @param  hideDeleted flag to hide deleted employees
     * @return a paginated list of employee data transfer objects
     * @throws InvalidInputException if the page size or page number is less than 1
     */
    Page<EmployeeDto> getAllEmployeesByFilterAndPagination(int pageSize,
                                                           int pageNumber,
                                                           boolean hideDeleted);
}
