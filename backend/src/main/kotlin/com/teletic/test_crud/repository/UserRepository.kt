package com.teletic.test_crud.repository

import com.teletic.test_crud.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Query(
        """
    SELECT CASE 
        WHEN COUNT(u) = 1 AND MAX(u.id) = :userId 
        THEN true 
        ELSE false 
    END 
    FROM User u JOIN u.roles r 
    WHERE r.name = :roleName
    """
    )
    fun isLastUserWithRole(@Param("userId") userId: Long, @Param("roleName") roleName: String): Boolean

    // Search users by name and/or email with role filtering capability
    @Query(
        """
    SELECT DISTINCT u FROM User u 
    JOIN u.roles r 
    WHERE (:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
    AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
    AND (:role IS NULL OR r.name = :role)
    """
    )
    fun search(
        @Param("fullName") fullName: String?,
        @Param("email") email: String?,
        @Param("role") role: String?,
        pageable: Pageable
    ): Page<User>
}