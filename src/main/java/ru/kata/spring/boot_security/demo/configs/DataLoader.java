package ru.kata.spring.boot_security.demo.configs;

import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import javax.annotation.PostConstruct;

@Component
public class DataLoader {

    private final RoleService roleService;
    private final UserService userService;

    public DataLoader(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @PostConstruct
    public void loadData() {
        System.out.println("=== DataLoader: Initializing application data ===");


        try {
            roleService.getRoleByName("user");
            System.out.println("Role 'user' already exists");
        } catch (Exception e) {
            roleService.addRole(new Role(null, "user"));
            System.out.println("Created role 'user'");
        }

        try {
            roleService.getRoleByName("admin");
            System.out.println("Role 'admin' already exists");
        } catch (Exception e) {
            roleService.addRole(new Role(null, "admin"));
            System.out.println("Created role 'admin'");
        }


        try {
            userService.getUserByEmail("admin@admin.com");
            System.out.println("Admin user already exists");
        } catch (Exception e) {

            UserDao adminUser = new UserDao();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Administrator");
            adminUser.setEmail("admin@admin.com");
            adminUser.setPassword("admin");
            adminUser.setAge(30);
            adminUser.setRoles(new String[]{"admin", "user"});

            boolean created = userService.addUser(adminUser);
            if (created) {
                System.out.println("Created admin user: admin@admin.com / admin");
            } else {
                System.out.println("Failed to create admin user");
            }
        }

        System.out.println("=== DataLoader: Initialization complete ===");
    }
}
