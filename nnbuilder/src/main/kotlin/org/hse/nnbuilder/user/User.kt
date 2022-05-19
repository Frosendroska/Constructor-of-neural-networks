package org.hse.nnbuilder.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
class User() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: Long = 0

    @Column
    private var name = ""

    @Column(unique = true)
    private var email = ""

    @Column
    @JsonIgnore
    private var password = ""
        set(value) {
            field = BCryptPasswordEncoder().encode(value)
        }

    constructor(name: String, email: String, password: String) : this() {
        this.name = name
        this.email = email
        this.password = password
    }

    fun getId(): Long {
        return id
    }

    fun getEmail(): String {
        return email
    }

    fun getPassword(): String {
        return password
    }
}
