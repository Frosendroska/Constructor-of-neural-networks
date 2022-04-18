package org.hse.nnbuilder.services

import kotlinx.coroutines.test.runBlockingTest
import org.aspectj.lang.annotation.Before
import org.hse.nnbuilder.exception.UserNotFoundException
import org.hse.nnbuilder.user.UserRepository
import org.hse.nnbuilder.user.UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.assertj.core.api.Assertions.assertThat



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class AuthServiceTest {

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var userService: UserService

    @Test
    fun resisterTest() = runBlockingTest {
        val request = Auth.RegisterRequest.newBuilder()
            .setName("Ivan")
            .setEmail("ivan@mail.ru")
            .setPassword("password")
            .build()

        authService.register(request)
        try {
            userService.findByEmail("ivan@mail.ru")
        } catch (e: UserNotFoundException) {
            fail(e.message)
        }
        //delete this user
    }

}