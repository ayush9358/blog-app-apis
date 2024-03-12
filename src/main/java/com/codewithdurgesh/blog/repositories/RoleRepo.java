package com.codewithdurgesh.blog.repositories;

import com.codewithdurgesh.blog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepo extends JpaRepository<Role, Integer> {
}
