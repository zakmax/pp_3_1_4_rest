package ru.kata.spring.boot_security.demo.restControllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.services.RoleService;

import java.util.List;


@RestController
@RequestMapping("/api/roles")
public class RoleRestController {

    private final RoleService roleService;

    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        try {
            System.out.println("=== GET ALL ROLES ===");
            List<Role> roles = roleService.allRoles();
            System.out.println("Found " + roles.size() + " roles");
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            System.out.println("Error getting roles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        try {
            System.out.println("=== GET ROLE BY ID ===");
            System.out.println("Role ID: " + id);
            Role role = roleService.getRoleById(id);
            if (role != null) {
                System.out.println("Found role: " + role.getNameRole());
                return ResponseEntity.ok(role);
            } else {
                System.out.println("Role not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error getting role by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        try {
            System.out.println("=== GET ROLE BY NAME ===");
            System.out.println("Original role name: '" + name + "'");


            String cleanedName = name != null ? name.trim() : "";
            System.out.println("Cleaned role name: '" + cleanedName + "'");

            Role role = roleService.getRoleByName(cleanedName);
            System.out.println("Found role: " + role.getNameRole());
            return ResponseEntity.ok(role);
        } catch (IllegalStateException e) {
            System.out.println("Role not found with name: '" + name + "'");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Error getting role by name: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try {
            System.out.println("=== CREATE ROLE ===");
            System.out.println("Role name: " + role.getNameRole());
            roleService.addRole(role);
            return ResponseEntity.status(HttpStatus.CREATED).body("Role created successfully");
        } catch (Exception e) {
            System.out.println("Error creating role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating role");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            System.out.println("=== UPDATE ROLE ===");
            System.out.println("Role ID: " + id);
            System.out.println("New role name: " + role.getNameRole());
            role.setId(id);
            roleService.updateRole(role);
            return ResponseEntity.ok("Role updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating role");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            System.out.println("=== DELETE ROLE ===");
            System.out.println("Role ID to delete: " + id);
            roleService.deleteRole(id);
            return ResponseEntity.ok("Role deleted successfully");
        } catch (Exception e) {
            System.out.println("Error deleting role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting role");
        }
    }
}