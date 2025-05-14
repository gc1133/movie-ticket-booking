package com.example.userservice.service;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;
import com.example.userservice.event.UserCreatedEvent;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class UserServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("userdb")
            .withUsername("admin")
            .withPassword("password");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Rahul");
        userDTO.setEmail("rahul@example.com");
        userDTO.setPhoneNumber("1234567890");

        when(kafkaTemplate.executeInTransaction(any())).thenReturn(true);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result.getId());
        assertEquals("Rahul", result.getName());
        verify(kafkaTemplate, times(1)).executeInTransaction(any());
    }

    @Test
    void testGetUser() {
        User user = new User();
        user.setName("Rahul");
        user.setEmail("rahul@example.com");
        user.setPhoneNumber("1234567890");
        user = userRepository.save(user);

        UserDTO result = userService.getUser(user.getId());

        assertEquals(user.getId(), result.getId());
        assertEquals("Rahul", result.getName());
    }
}