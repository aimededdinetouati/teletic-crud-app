package com.teletic.test_crud.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener::class)
class Role (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    var users: MutableSet<User> = mutableSetOf(),

    @CreatedDate
    val createdDate: Instant? = null,

    @LastModifiedDate
    var lastModifiedDate: Instant? = null
)