package ru.kata.spring.boot_security.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.entities.Role;

import java.util.List;
import java.util.Optional;


@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
    List<Role> findAll();
    Optional<Role> findByNameRole(String nameRole);
    Optional<Role> findById(Long id);

    @Query("SELECT r FROM Role r WHERE LOWER(r.nameRole) = LOWER(:nameRole)")
    Optional<Role> findByNameRoleIgnoreCase(@Param("nameRole") String nameRole);
}