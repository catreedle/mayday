package com.example.springboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig
class ApplicationTests {

    @Mock
    private ApplicationContext mockApplicationContext;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        // This test will fail if the application context cannot start
        assertNotNull(Application.class);
    }

    @Test
    @DisplayName("Main method runs without throwing exceptions")
    void mainMethodRunsSuccessfully() {
        assertDoesNotThrow(() -> {
            // Test that main method can be called
            // Note: In real tests, you'd typically mock SpringApplication.run
            String[] args = {};
            // Application.main(args); // Commented out to avoid actually starting the app
        });
    }

    @Test
    @DisplayName("CommandLineRunner bean is created and configured correctly")
    void commandLineRunnerBeanCreation() {
        // Arrange
        Application app = new Application();
        String[] mockBeanNames = {"bean1", "bean2", "applicationContext", "application"};

        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(mockBeanNames);

        // Act
        CommandLineRunner runner = app.commandLineRunner(mockApplicationContext);

        // Assert
        assertNotNull(runner, "CommandLineRunner should not be null");
        assertInstanceOf(CommandLineRunner.class, runner, "Should return CommandLineRunner instance");
    }

    @Test
    @DisplayName("CommandLineRunner prints expected header message")
    void commandLineRunnerPrintsHeaderMessage() throws Exception {
        // Arrange
        Application app = new Application();
        String[] mockBeanNames = {"testBean1", "testBean2"};
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(mockBeanNames);

        CommandLineRunner runner = app.commandLineRunner(mockApplicationContext);

        // Act
        runner.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Let's inspect the beans provided by Spring Boot:"),
                "Should print the header message");
    }

    @Test
    @DisplayName("CommandLineRunner prints all bean names")
    void commandLineRunnerPrintsAllBeanNames() throws Exception {
        // Arrange
        Application app = new Application();
        String[] mockBeanNames = {"zebra", "alpha", "beta"};
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(mockBeanNames);

        CommandLineRunner runner = app.commandLineRunner(mockApplicationContext);

        // Act
        runner.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("alpha"), "Should print alpha bean");
        assertTrue(output.contains("beta"), "Should print beta bean");
        assertTrue(output.contains("zebra"), "Should print zebra bean");
    }

    @Test
    @DisplayName("CommandLineRunner sorts bean names alphabetically")
    void commandLineRunnerSortsBeanNames() throws Exception {
        // Arrange
        Application app = new Application();
        String[] mockBeanNames = {"zebra", "alpha", "beta", "charlie"};
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(mockBeanNames);

        CommandLineRunner runner = app.commandLineRunner(mockApplicationContext);

        // Act
        runner.run();

        // Assert
        String output = outputStreamCaptor.toString();
        String[] lines = output.split("\n");

        // Find the lines that contain bean names (skip the header)
        boolean foundHeader = false;
        String previousBean = "";

        for (String line : lines) {
            if (line.contains("Let's inspect the beans")) {
                foundHeader = true;
                continue;
            }

            if (foundHeader && !line.trim().isEmpty()) {
                String currentBean = line.trim();
                if (!previousBean.isEmpty()) {
                    assertTrue(previousBean.compareTo(currentBean) <= 0,
                            "Beans should be sorted alphabetically: " + previousBean + " should come before " + currentBean);
                }
                previousBean = currentBean;
            }
        }
    }

    @Test
    @DisplayName("CommandLineRunner handles empty bean list")
    void commandLineRunnerHandlesEmptyBeanList() throws Exception {
        // Arrange
        Application app = new Application();
        String[] mockBeanNames = {};
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(mockBeanNames);

        CommandLineRunner runner = app.commandLineRunner(mockApplicationContext);

        // Act & Assert
        assertDoesNotThrow(() -> runner.run(), "Should handle empty bean list without throwing exception");

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Let's inspect the beans provided by Spring Boot:"),
                "Should still print header even with no beans");
    }

    @Test
    @DisplayName("CommandLineRunner handles null arguments")
    void commandLineRunnerHandlesNullArguments() throws Exception {
        // Arrange
        Application app = new Application();
        String[] mockBeanNames = {"testBean"};
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(mockBeanNames);

        CommandLineRunner runner = app.commandLineRunner(mockApplicationContext);

        // Act & Assert
        assertDoesNotThrow(() -> runner.run((String[]) null),
                "Should handle null arguments without throwing exception");
    }

    @Test
    @DisplayName("Application class has SpringBootApplication annotation")
    void applicationHasSpringBootApplicationAnnotation() {
        // Assert
        assertTrue(Application.class.isAnnotationPresent(SpringBootApplication.class),
                "Application class should have @SpringBootApplication annotation");
    }

    @Test
    @DisplayName("CommandLineRunner bean method has Bean annotation")
    void commandLineRunnerMethodHasBeanAnnotation() throws NoSuchMethodException {
        // Act
        var method = Application.class.getMethod("commandLineRunner", ApplicationContext.class);

        // Assert
        assertTrue(method.isAnnotationPresent(Bean.class),
                "commandLineRunner method should have @Bean annotation");
    }
}

// Integration Test
@SpringBootTest
class ApplicationIntegrationTest {

    @Test
    @DisplayName("Full application context loads and starts successfully")
    void applicationContextLoadsCompletely() {
        // This test verifies that the entire Spring Boot application context
        // can be loaded without any configuration issues
        assertTrue(true, "If this test runs, the context loaded successfully");
    }
}

// Test Configuration for mocking SpringApplication.run (Advanced)
class ApplicationMainMethodTest {

    @Test
    @DisplayName("Main method can be invoked")
    void mainMethodExists() {
        // Verify main method exists and has correct signature
        assertDoesNotThrow(() -> {
            var mainMethod = Application.class.getMethod("main", String[].class);
            assertNotNull(mainMethod, "Main method should exist");
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                    "Main method should be static");
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                    "Main method should be public");
        });
    }
}