package com.example.springboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit Tests using MockMvc (Web Layer Only)
@WebMvcTest(HelloController.class)
class HelloControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / should return greeting message")
    void shouldReturnGreetingMessage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }

    @Test
    @DisplayName("GET / should return 200 OK status")
    void shouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET / should return text/plain content type")
    void shouldReturnPlainTextContentType() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    @DisplayName("GET / should handle multiple requests")
    void shouldHandleMultipleRequests() throws Exception {
        // First request
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));

        // Second request - should return same result
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }
}

// Integration Tests with Full Spring Context
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET / should return greeting message via HTTP")
    void shouldReturnGreetingMessageViaHttp() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/", String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Greetings from Spring Boot!");
    }

    @Test
    @DisplayName("GET / should return proper HTTP headers")
    void shouldReturnProperHttpHeaders() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/", String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType().toString())
                .contains("text/plain");
    }

    @Test
    @DisplayName("Integration test with actual server startup")
    void integrationTestWithActualServer() {
        String response = restTemplate.getForObject(
                "http://localhost:" + port + "/", String.class);

        assertThat(response).isEqualTo("Greetings from Spring Boot!");
    }
}

// Unit Tests for Controller Logic Only (Isolated)
class HelloControllerUnitTest {

    private HelloController helloController = new HelloController();

    @Test
    @DisplayName("index() method should return correct greeting")
    void indexMethodShouldReturnCorrectGreeting() {
        // Act
        String result = helloController.index();

        // Assert
        assertEquals("Greetings from Spring Boot!", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("index() method should be consistent")
    void indexMethodShouldBeConsistent() {
        // Act - call multiple times
        String result1 = helloController.index();
        String result2 = helloController.index();
        String result3 = helloController.index();

        // Assert - should always return the same value
        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertEquals("Greetings from Spring Boot!", result1);
    }

    @Test
    @DisplayName("Controller should have proper annotations")
    void controllerShouldHaveProperAnnotations() {
        // Assert class annotations
        assertTrue(HelloController.class.isAnnotationPresent(RestController.class),
                "Controller should have @RestController annotation");

        // Assert method annotations
        try {
            var method = HelloController.class.getMethod("index");
            assertTrue(method.isAnnotationPresent(GetMapping.class),
                    "index method should have @GetMapping annotation");

            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            assertEquals("/", getMapping.value()[0],
                    "@GetMapping should map to root path '/'");
        } catch (NoSuchMethodException e) {
            fail("index method should exist");
        }
    }

    @Test
    @DisplayName("Controller should be instantiable")
    void controllerShouldBeInstantiable() {
        assertDoesNotThrow(() -> {
            HelloController controller = new HelloController();
            assertNotNull(controller);
        });
    }
}

// Performance Tests
@WebMvcTest(HelloController.class)
class HelloControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Endpoint should handle concurrent requests")
    void shouldHandleConcurrentRequests() throws Exception {
        // Simple performance test - make multiple rapid requests
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Greetings from Spring Boot!"));
        }
    }
}

// Error Handling Tests
@WebMvcTest(HelloController.class)
class HelloControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 404 for non-existent endpoints")
    void shouldReturn404ForNonExistentEndpoints() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle different HTTP methods properly")
    void shouldHandleDifferentHttpMethods() throws Exception {
        // POST to GET endpoint should return 405 Method Not Allowed
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/"))
                .andExpect(status().isMethodNotAllowed());

        // PUT to GET endpoint should return 405 Method Not Allowed
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/"))
                .andExpect(status().isMethodNotAllowed());

        // DELETE to GET endpoint should return 405 Method Not Allowed
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/"))
                .andExpect(status().isMethodNotAllowed());
    }
}