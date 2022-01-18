package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
@WithMockUser
internal class CustomerControllerTest{

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should return all customers`(){
        val customer1 = customerRepository.save(buildCustomer())
        val customer2 = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(customer1.id))
            .andExpect(jsonPath("$[0].name").value(customer1.name))
            .andExpect(jsonPath("$[0].email").value(customer1.email))
            .andExpect(jsonPath("$[0].status").value(customer1.status.name))
            .andExpect(jsonPath("$[1].id").value(customer2.id))
            .andExpect(jsonPath("$[1].name").value(customer2.name))
            .andExpect(jsonPath("$[1].email").value(customer2.email))
            .andExpect(jsonPath("$[1].status").value(customer2.status.name))
    }

    @Test
    fun `should filter all customers by name when get all`(){
        val customer2 = customerRepository.save(buildCustomer(name = "José"))

        mockMvc.perform(get("/customers?name=jo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(customer2.id))
            .andExpect(jsonPath("$[0].name").value(customer2.name))
            .andExpect(jsonPath("$[0].email").value(customer2.email))
            .andExpect(jsonPath("$[0].status").value(customer2.status.name))
    }

    @Test
    fun `should create customer`(){
        val request = PostCustomerRequest("fake name", "${UUID.randomUUID().toString()}@fakeemail.com", "123123")
        mockMvc.perform(post("/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .contentType(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)

        val customer = customerRepository.findAll().toList()
        assertEquals(1, customer.size)
        assertEquals(request.name, customer[0].name)
        assertEquals(request.email, customer[0].email)

    }

    @Test
    fun `should get user by id when user has the same id`(){
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers/${customer.id}").with(user(UserCustomDetails(customer))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(customer.id))
            .andExpect(jsonPath("$.name").value(customer.name))
            .andExpect(jsonPath("$.email").value(customer.email))

    }

    @Test
    fun `should returne forbidden when user has diffent id`(){
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers/0").with(user(UserCustomDetails(customer))))
            .andExpect(status().isForbidden)

    }

    @Test
    fun `should returne error when update customer has invalid information`(){
        val request = PutCustomerRequest("", "emailupdate@fakeemail.com")
        mockMvc.perform(put("/customers/1")
            .contentType(MediaType.APPLICATION_JSON)
            .contentType(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.message").value("Invalid Request"))
            .andExpect(jsonPath("$.internalCode").value("ML-001"))

    }

    @Test
    fun `should delete customer`(){
        val customer = customerRepository.save(buildCustomer())
        mockMvc.perform(delete("/customers/${customer.id}"))
            .andExpect(status().isNoContent)

        val custumerDeleted = customerRepository.findById(customer.id!!)

        assertEquals(CustomerStatus.INATIVO, custumerDeleted.get().status)

    }

    @Test
    fun `should return error when delete customer`(){
        val customer = customerRepository.save(buildCustomer())
        mockMvc.perform(delete("/customers/2"))
            .andExpect(status().isNotFound)

    }
}