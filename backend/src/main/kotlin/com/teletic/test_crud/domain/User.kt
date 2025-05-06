package com.teletic.test_crud.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal
import java.time.Instant

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(columnDefinition = "VARCHAR(100)")
    var fullName: String,

    @Column(unique = true, columnDefinition = "VARCHAR(50)")
    var email: String,

    @Column(columnDefinition = "VARCHAR(100)")
    private var password: String,

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    var locked: Boolean = false,

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    var enabled: Boolean = true,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "users_id")],
        inverseJoinColumns = [JoinColumn(name = "roles_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDate: Instant? = null,

    @LastModifiedDate
    @Column(insertable = false)
    var lastModifiedDate: Instant? = null
) : UserDetails, Principal {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles
            .map { r -> SimpleGrantedAuthority(r.name) }
            .toMutableList()
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return email
    }

    override fun getName(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return !locked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return enabled
    }
}