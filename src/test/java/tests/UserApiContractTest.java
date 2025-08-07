package tests;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class UserApiContractTest {

    /*
     * Author: Ahmed Hasan
     * © Ahmed Hasan 2025. All rights reserved.
     *
     * This test uses REST Assured 5.4.0 to perform API contract testing.
     * Features:
     * - JSON Schema validation
     * - Dynamic payload using Java Faker
     * - Console logs for test traceability
     */

    static Faker faker = new Faker();

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://gorest.co.in/public/v2";
        System.out.println("🔧 Base URI set to: " + RestAssured.baseURI);
    }

    @Test
    @DisplayName("Create User and Validate Contract via JSON Schema")
    public void testCreateUserWithSchemaValidation() {
        // Step 1: Generate dynamic user data
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", faker.name().fullName());
        payload.put("email", faker.internet().emailAddress());
        payload.put("gender", "male");
        payload.put("status", "active");

        System.out.println("📤 Sending POST request to create user with payload:");
        System.out.println(payload);

        // Step 2: Send POST request and validate response
        RestAssured
                .given()
                .header("Authorization", "Bearer YOUR_API_TOKEN")  // Replace with real token
                .contentType(ContentType.JSON)
                .body(payload)
                //.log().all()  Log request details
                .when()
                .post("/users")
                .then()
                // .log().all()
                .log().ifValidationFails()  // Log response details if validation fails
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
                .body("email", equalTo(payload.get("email")))
                .body("status", equalTo("active"));

        System.out.println("User created and response validated against JSON Schema.");
    }
}