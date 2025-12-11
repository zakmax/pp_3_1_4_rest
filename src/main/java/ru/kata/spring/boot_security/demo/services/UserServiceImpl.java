package ru.kata.spring.boot_security.demo.services;

import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.repository.UserRepo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepo userRepo, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> allUsers() {
        return userRepo.findAll();
    }

    @Override
    public List<UserDao> getAllUsersAsDao() {
        return userRepo.findAll().stream()
                .map(UserDao::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addUser(UserDao userDao) {
        System.out.println("=== USER SERVICE - ADD USER ===");
        System.out.println("Email: " + userDao.getEmail());

        validateUserData(userDao);

        if (!isEmailUnique(userDao)) {
            System.out.println("Email already exists: " + userDao.getEmail());
            return false;
        }
        System.out.println("Email is unique");

        if (userDao.getPassword() == null || userDao.getPassword().trim().isEmpty()) {
            System.out.println("Password is empty!");
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            User user = createUserFromForm(userDao);
            userRepo.save(user);
            System.out.println("User saved successfully with ID: " + user.getId());
            return true;

        } catch (Exception e) {
            System.out.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateUser(UserDao userDao) {
        System.out.println("=== USER SERVICE - UPDATE USER ===");
        System.out.println("Updating user with ID: " + userDao.getId());

        validateUserData(userDao);

        try {
            User existingUser = getUserById(userDao.getId());
            if (existingUser == null) {
                System.out.println("User not found with ID: " + userDao.getId());
                return false;
            }

            if (!isEmailUniqueForUser(userDao.getId(), userDao.getEmail())) {
                System.out.println("Email already exists: " + userDao.getEmail());
                return false;
            }

            User updatedUser = updateUserFromForm(userDao, existingUser);
            userRepo.save(updatedUser);
            System.out.println("User updated successfully");
            return true;

        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deleteUser(Long id) {
        System.out.println("Deleting user with ID: " + id);
        userRepo.deleteById(id);
    }

    @Override
    public User getUserById(Long id) {
        System.out.println("Getting user by ID: " + id);
        return userRepo.findById(id).orElse(null);
    }

    @Override
    public UserDao getUserDaoById(Long id) {
        User user = getUserById(id);
        return user != null ? new UserDao(user) : null;
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserByEmail(String email) throws IllegalStateException {
        System.out.println("Getting user by email: " + email);
        return userRepo.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found by email"));
    }

    @Override
    public UserDao getCurrentUserAsDao() {
        User currentUser = getCurrentUser();
        return new UserDao(currentUser);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Loading user by username (email): " + email);
        User user = getUserByEmail(email);
        Hibernate.initialize(user.getRoles());
        return user;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return getUserByEmail(authentication.getName());
    }

    @Override
    public boolean isEmailUniqueForUser(Long userId, String email) {
        User existingUser = userRepo.findByEmail(email).orElse(null);
        return existingUser == null || existingUser.getId().equals(userId);
    }

    @Override
    public void validateUserData(UserDao userDao) {
        if (userDao.getFirstName() == null || userDao.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (userDao.getLastName() == null || userDao.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (userDao.getEmail() == null || userDao.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (userDao.getAge() == null || userDao.getAge() <= 0) {
            throw new IllegalArgumentException("Age must be positive");
        }
    }

    @Override
    public boolean isEmailUnique(UserDao userDao) {
        return !userRepo.findByEmail(userDao.getEmail()).isPresent();
    }

    private User createUserFromForm(UserDao userDao) {
        User user = new User();
        user.setFirstName(userDao.getFirstName());
        user.setLastName(userDao.getLastName());
        user.setAge(userDao.getAge());
        user.setEmail(userDao.getEmail());

        if (userDao.getPassword() != null && !userDao.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDao.getPassword()));
        } else {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        setRoles(user, userDao);
        return user;
    }

    private User updateUserFromForm(UserDao userDao, User existingUser) {
        System.out.println("Updating user from form data");

        existingUser.setFirstName(userDao.getFirstName());
        existingUser.setLastName(userDao.getLastName());
        existingUser.setAge(userDao.getAge());
        existingUser.setEmail(userDao.getEmail());

        setRoles(existingUser, userDao);

        if (userDao.getPassword() != null && !userDao.getPassword().trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(userDao.getPassword());
            existingUser.setPassword(encodedPassword);
            System.out.println("Password updated");
        } else {
            System.out.println("Password not changed");
        }

        return existingUser;
    }

    private void setRoles(User user, UserDao userDao) {
        System.out.println("Setting roles for user");

        if (userDao.getRoles() != null && userDao.getRoles().length > 0) {
            Set<Role> userRoles = Arrays.stream(userDao.getRoles())
                    .map(roleName -> {
                        try {
                            return roleService.getRoleByName(roleName);
                        } catch (Exception e) {
                            System.out.println("Role not found: " + roleName);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(userRoles);
            System.out.println("Roles set: " + userRoles);
        } else {
            try {
                Role userRole = roleService.getRoleByName("user");
                user.setRoles(Set.of(userRole));
                System.out.println("Default role set: user");
            } catch (Exception e) {
                System.out.println("Default role 'user' not found!");
            }
        }
    }
}