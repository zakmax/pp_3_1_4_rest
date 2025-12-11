package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.net.Authenticator;
import java.util.Arrays;



@Controller
@RequestMapping("/admin")
public class AdminController {

    private UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getAllUsers(Model model) {
        try {
            System.out.println("=== ADMIN PANEL ===");

            UserDao currentUser = userService.getCurrentUserAsDao();
            model.addAttribute("currentUser", currentUser);

            model.addAttribute("userList", userService.getAllUsersAsDao());

            System.out.println("Users count: " + userService.getAllUsersAsDao().size());
            return "table";
        } catch (Exception e) {
            System.out.println("Error in admin panel: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error";
        }
    }

    @GetMapping("/newUser")
    public String showNewUserForm(Model model,
                                  @RequestParam(value = "error", required = false) String error) {
        try {
            System.out.println("=== NEW USER FORM ===");

            UserDao currentUser = userService.getCurrentUserAsDao();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userDao", new UserDao());

            if ("email_exists".equals(error)) {
                model.addAttribute("errorMessage", "User with this email already exists!");
            }

            System.out.println("Showing new-user form");
            return "new-user";
        } catch (Exception e) {
            System.out.println("Error showing new user form: " + e.getMessage());
            return "redirect:/admin?error";
        }
    }

    @PostMapping("/userAdd")
    public String addUser(@ModelAttribute UserDao userDao,
                          @RequestParam(value = "roles", required = false) String[] roles) {

        System.out.println("=== ADD USER PROCESSING ===");
        System.out.println("Roles param: " + (roles != null ? Arrays.toString(roles) : "null"));

        if (roles != null) {
            userDao.setRoles(roles);
            System.out.println("Roles set to UserDao: " + Arrays.toString(userDao.getRoles()));
        }

        try {
            boolean result = userService.addUser(userDao);
            System.out.println("Add user result: " + result);

            if (result) {
                System.out.println("User added successfully, redirecting to /admin");
                return "redirect:/admin";
            } else {
                System.out.println("Failed to add user (email exists), redirecting with error");
                return "redirect:/admin/newUser?error=email_exists";
            }
        } catch (Exception e) {
            System.out.println("EXCEPTION in addUser: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/newUser?error=system_error";
        }
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") long id) {
        System.out.println("Deleting user with id: " + id);
        try {
            userService.deleteUser(id);
            System.out.println("User deleted successfully");
            return "redirect:/admin";
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin?error=delete_failed";
        }
    }

    @GetMapping("/updateUserForm")
    public String showUpdateUserForm(@RequestParam("id") long id, Model model) {
        try {
            System.out.println("=== UPDATE USER FORM ===");
            System.out.println("User ID to update: " + id);

            UserDao currentUser = userService.getCurrentUserAsDao();
            model.addAttribute("currentUser", currentUser);

            UserDao userDao = userService.getUserDaoById(id);
            if (userDao == null) {
                System.out.println("User not found with ID: " + id);
                return "redirect:/admin?error=user_not_found";
            }

            model.addAttribute("userDao", userDao);

            System.out.println("User to edit: " + userDao.getFirstName() + " " + userDao.getLastName());
            System.out.println("User roles: " + Arrays.toString(userDao.getRoles()));

            return "updateUserForm";
        } catch (Exception e) {
            System.out.println("Error showing update form: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin?error";
        }
    }

    @PostMapping("/editUser")
    public String editUser(@ModelAttribute UserDao userDao,
                           @RequestParam(value = "roles", required = false) String[] roles) {

        System.out.println("=== EDIT USER PROCESSING ===");
        System.out.println("UserDao ID: " + userDao.getId());
        System.out.println("First Name: " + userDao.getFirstName());
        System.out.println("Last Name: " + userDao.getLastName());
        System.out.println("Email: " + userDao.getEmail());
        System.out.println("Age: " + userDao.getAge());
        System.out.println("Password: " + (userDao.getPassword() != null ? "[SET]" : "[NULL]"));
        System.out.println("Roles param: " + (roles != null ? Arrays.toString(roles) : "null"));

        if (roles != null) {
            userDao.setRoles(roles);
            System.out.println("Roles set to UserDao: " + Arrays.toString(userDao.getRoles()));
        } else {
            System.out.println("No roles selected, setting empty roles array");
            userDao.setRoles(new String[0]);
        }

        try {
            boolean result = userService.updateUser(userDao);
            System.out.println("Update user result: " + result);

            if (result) {
                System.out.println("User updated successfully, redirecting to /admin");
                return "redirect:/admin";
            } else {
                System.out.println("Failed to update user, redirecting with error");
                return "redirect:/admin/updateUserForm?id=" + userDao.getId() + "&error=update_failed";
            }
        } catch (Exception e) {
            System.out.println("EXCEPTION in editUser: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/updateUserForm?id=" + userDao.getId() + "&error=system_error";
        }
    }

    @GetMapping("/getUserData")
    @ResponseBody
    public UserDao getUserData(@RequestParam("id") long id) {
        try {
            System.out.println("=== GET USER DATA FOR MODAL ===");
            System.out.println("User ID: " + id);

            UserDao userDao = userService.getUserDaoById(id);
            if (userDao == null) {
                System.out.println("User not found with ID: " + id);
                throw new RuntimeException("User not found");
            }

            System.out.println("Returning user data: " + userDao.getFirstName() + " " + userDao.getLastName());

            return userDao;
        } catch (Exception e) {
            System.out.println("Error getting user data: " + e.getMessage());
            throw new RuntimeException("Error loading user data");
        }
    }
}


