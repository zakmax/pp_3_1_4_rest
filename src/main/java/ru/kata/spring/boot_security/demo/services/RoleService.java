package ru.kata.spring.boot_security.demo.services;

import ru.kata.spring.boot_security.demo.entities.Role;

import java.util.List;

public interface RoleService {

    List<Role> allRoles();

    void addRole(Role role);

    void updateRole(Role role);

    void deleteRole(Long id);

    Role getRoleById(Long id);

    Role getRoleByName(String role);

}
