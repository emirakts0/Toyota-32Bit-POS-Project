package com.userservice.repository;

import com.userservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    //Optional<Set<Role>> findByRoleNameIn(Set<String> roleNames);
    Optional<Role> findByRoleName(String roleName);

}



