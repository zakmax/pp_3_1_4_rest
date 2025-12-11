package ru.kata.spring.boot_security.demo.dao;




import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;

import java.util.Arrays;

public class UserDao {
    private Long id;
    private String firstName;
    private String lastName;
    private String password;
    private Integer age;
    private String[] roles;
    private String email;


    public UserDao() {}


    public UserDao(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.age = user.getAge();


        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Object[] objectArr = user.getRoles().stream()
                    .map(Role::getNameRole)
                    .toArray();
            this.roles = Arrays.copyOf(objectArr, objectArr.length, String[].class);
        } else {
            this.roles = new String[0];
        }
    }


    public UserDao(Long id, String firstName, String lastName, String password,
                   Integer age, String[] roles, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.age = age;
        this.roles = roles != null ? roles.clone() : new String[0];
        this.email = email;
    }


    public UserDao(String firstName, String lastName, String password,
                   Integer age, String[] roles, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.age = age;
        this.roles = roles != null ? roles.clone() : new String[0];
        this.email = email;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String[] getRoles() {
        return roles != null ? roles.clone() : new String[0];
    }

    public void setRoles(String[] roles) {
        this.roles = roles != null ? roles.clone() : new String[0];
    }

    public String getStringRole() {
        if (roles == null || roles.length == 0) {
            return "[]";
        }
        return Arrays.toString(roles);
    }


    public boolean hasRole(String roleName) {
        if (roles == null) {
            return false;
        }
        return Arrays.stream(roles)
                .anyMatch(role -> role.equalsIgnoreCase(roleName));
    }

    public boolean isAdmin() {
        return hasRole("admin");
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "UserDao{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + (password != null ? "[SET]" : "[NULL]") + '\'' +
                ", age=" + age +
                ", roles=" + Arrays.toString(roles) +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDao userDao = (UserDao) o;
        return id != null && id.equals(userDao.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

