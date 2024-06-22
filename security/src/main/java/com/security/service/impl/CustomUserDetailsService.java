package com.security.service.impl;

import com.security.exception.UserNotFoundException;
import com.security.model.Employee;
import com.security.model.Role;
import com.security.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Custom implementation of UserDetailsService to load user-specific data.
 * @author Emir Aktaş
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;


    /**
     * Loads the user by username.
     *
     * @param username the username identifying the user whose data is required
     * @return UserDetails the fully populated user record
     * @throws UsernameNotFoundException if the user could not be found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.trace("loadUserByUsername method begins. Username: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("loadUserByUsername: User not found with username: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);});

        Set<GrantedAuthority> grantedAuthorities = getSimpleGrantedAuthoritiesFromRoles(employee.getRoles());
        log.debug("loadUserByUsername: Granted authorities for user {}: {}", username, grantedAuthorities);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(employee.getUsername())
                .password(employee.getPassword())
                .authorities(grantedAuthorities)
                .accountExpired(false)
                .accountLocked(false)
                .disabled(false)
                .credentialsExpired(false)
                .build();

        log.info("loadUserByUsername: User loaded successfully with username: {}", username);
        log.trace("loadUserByUsername method ends. Username: {}", username);
        return userDetails;
    }


    /**
     * Retrieves the name of the user by username.
     *
     * @param username the username identifying the user whose name is required
     * @return String the name of the user, or "_" if the user is not found
     */
    public String getNameByUsername(String username){
        log.trace("getNameByUsername method begins. Username: {}", username);

        Optional<Employee> employee = employeeRepository.findByUsername(username);
        if(employee.isPresent()){
            log.trace("getNameByUsername method ends. Username: {}", username);
            return employee.get().getName();
        }
        log.warn("getNameByUsername: No name found for username: {}", username);
        log.trace("getNameByUsername method ends. Username: {}", username);
        return "_";
    }


    /**
     * Converts a set of Role objects to a set of GrantedAuthority objects.
     *
     * @param rolesSet the set of roles to be converted
     * @return Set<GrantedAuthority> the set of granted authorities derived from the roles
     */
    private Set<GrantedAuthority> getSimpleGrantedAuthoritiesFromRoles(Set<Role> rolesSet) {
        log.trace("getSimpleGrantedAuthoritiesFromRoles method begins.");

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        for(Role role: rolesSet) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        }
        log.debug("getSimpleGrantedAuthoritiesFromRoles: Granted authorities: {}", grantedAuthorities);
        log.trace("getSimpleGrantedAuthoritiesFromRoles method ends.");
        return grantedAuthorities;
    }
}
