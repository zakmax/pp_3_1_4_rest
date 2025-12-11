package ru.kata.spring.boot_security.demo.restControllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDao>> getAllUsers() {
        try {
            List<UserDao> users = userService.getAllUsersAsDao();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDao> getUserById(@PathVariable Long id) {
        try {
            UserDao user = userService.getUserDaoById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<UserDao> getCurrentUser() {
        try {
            UserDao currentUser = userService.getCurrentUserAsDao();
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDao> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            UserDao userDao = new UserDao(user);
            return ResponseEntity.ok(userDao);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDao userDao) {
        try {
            boolean created = userService.addUser(userDao);
            if (created) {
                return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDao userDao) {
        try {
            userDao.setId(id);
            boolean updated = userService.updateUser(userDao);
            if (updated) {
                return ResponseEntity.ok("User updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists or user not found");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }



    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            System.out.println("=== PATCH USER ===");
            System.out.println("User ID: " + id);
            System.out.println("Updates: " + updates);

            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                System.out.println("User not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }


            UserDao existingUserDao = new UserDao(existingUser);


            if (updates.containsKey("firstName")) {
                existingUserDao.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                existingUserDao.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                existingUserDao.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("age")) {
                existingUserDao.setAge((Integer) updates.get("age"));
            }
            if (updates.containsKey("roles")) {

                Object rolesObj = updates.get("roles");
                if (rolesObj instanceof List) {
                    List<String> rolesList = (List<String>) rolesObj;
                    String[] rolesArray = rolesList.toArray(new String[0]);
                    existingUserDao.setRoles(rolesArray);
                }
            }
            if (updates.containsKey("password")) {
                String password = (String) updates.get("password");
                if (password != null && !password.trim().isEmpty()) {
                    existingUserDao.setPassword(password);
                }
            }

            existingUserDao.setId(id);
            System.out.println("Updated UserDao: " + existingUserDao);

            boolean updated = userService.updateUser(existingUserDao);

            if (updated) {
                System.out.println("User updated successfully via PATCH");
                return ResponseEntity.ok("User updated successfully");
            } else {
                System.out.println("Failed to update user via PATCH");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists or update failed");
            }
        } catch (Exception e) {
            System.out.println("EXCEPTION in partialUpdateUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user: " + e.getMessage());
        }
    }
}