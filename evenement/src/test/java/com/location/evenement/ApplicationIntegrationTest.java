package com.location.evenement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("emailtest")
@SpringBootTest(
        properties = {
                "JWT_SECRET=test-secret-for-it",
                "app.jwt.secret=test-secret-for-it"
        }
)
class ApplicationIntegrationTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}