package com.userservice.Service;

import com.userservice.dto.EmployeeDto;
import org.springframework.data.domain.Page;

public interface UserSearchService {

    EmployeeDto getEmployeeById(Long id);

    EmployeeDto searchEmployeeByUsername(String username);

    Page<EmployeeDto> getAllEmployeesByFilterAndPagination(int pageSize,
                                                           int pageNumber,
                                                           boolean hideDeleted);
}
