package ru.kata.spring.boot_security.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.repository.RoleRepo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;

    public RoleServiceImpl(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }



    @Override
    public List<Role> allRoles() {
        System.out.println("=== ROLE SERVICE - GET ALL ROLES ===");
        List<Role> roles = roleRepo.findAll();
        System.out.println("Retrieved " + roles.size() + " roles from database");
        return roles;
    }

    @Override
    public void addRole(Role role) {
        roleRepo.save(role);
    }

    @Override
    public void updateRole(Role role) {
        if (getRoleById(role.getId()).getNameRole().equals(role.getNameRole()) || isRoleNameUnique(role)) {
            roleRepo.save(role);
        }
    }

    @Override
    public void deleteRole(Long id) {
        roleRepo.deleteById(id);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepo.findById(id).orElse(null);
    }




    @Override
    public Role getRoleByName(String roleName) throws IllegalStateException {
        System.out.println("=== ROLE SERVICE - GET ROLE BY NAME ===");


        String cleanedRoleName = roleName != null ? roleName.trim() : "";
        System.out.println("Searching for role: '" + cleanedRoleName + "'");
        System.out.println("Original role name: '" + roleName + "'");


        Optional<Role> role = roleRepo.findByNameRoleIgnoreCase(cleanedRoleName);

        if (role.isPresent()) {
            System.out.println("Found role: " + role.get().getNameRole());
            return role.get();
        } else {
            System.out.println("Role not found: " + cleanedRoleName);

            List<Role> allRoles = roleRepo.findAll();
            System.out.println("Available roles: " + allRoles.stream()
                    .map(Role::getNameRole)
                    .collect(Collectors.toList()));
            throw new IllegalStateException("Role not found by name: " + cleanedRoleName);
        }
    }
    private boolean isRoleNameUnique(Role role) {
        return !roleRepo.findByNameRole(role.getNameRole()).isPresent();
    }
}