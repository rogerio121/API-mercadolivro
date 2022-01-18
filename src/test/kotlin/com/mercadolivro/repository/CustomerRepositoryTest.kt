package com.mercadolivro.repository

import com.mercadolivro.helper.buildCustomer
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerRepositoryTest {

    @Autowired
    private lateinit var customerRepositor: CustomerRepository

    @BeforeEach
    fun setUp() = customerRepositor.deleteAll()

    @Test
    fun `should return name containing`(){
        val rebeca = customerRepositor.save(buildCustomer(name = "Rebeca"))
        val renato = customerRepositor.save(buildCustomer(name = "Renato"))
        val Othavio = customerRepositor.save(buildCustomer(name = "Othavio"))

        val customers = customerRepositor.findByNameContaining("Re")

        assertEquals(listOf(rebeca, renato), customers)
    }

    @Nested
    inner class `exists by email`{
        @Test
        fun `should return true when email exists`(){
            val email = "email@teste.com"
            customerRepositor.save(buildCustomer(email = email))

            val exists = customerRepositor.existsByEmail(email)

            assertTrue(exists)
        }

        @Test
        fun `should return false when email exists`(){
            val email = "email@testefalse.com"

            val exists = customerRepositor.existsByEmail(email)

            assertFalse(exists)
        }
    }

    @Nested
    inner class `find by email`{
        @Test
        fun `should return customer when email exists`(){
            val email = "email@teste.com"
            val customer = customerRepositor.save(buildCustomer(email = email))

            val result = customerRepositor.findByEmail(email)

            assertNotNull(result)
            assertEquals(customer, result)
        }

        @Test
        fun `should return null when email not exists`(){
            val email = "email@testefalse.com"

            val result = customerRepositor.findByEmail(email)

            assertNull(result)
        }
    }
}