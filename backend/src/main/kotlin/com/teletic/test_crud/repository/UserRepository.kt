package com.teletic.test_crud.repository

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
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

}