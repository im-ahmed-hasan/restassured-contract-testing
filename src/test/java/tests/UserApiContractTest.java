package tests;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import config.AuthTokenProvider;

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
     * - Positive and negative test scenarios
     * - Console logs for test traceability
     * - Bearer token fetched from a separate AuthTokenProvider class
     */

    static Faker faker = new Faker();
    private static String BEARER_TOKEN;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://gorest.co.in/public/v2";
        System.out.println("Base URI set to: " + RestAssured.baseURI);

        // Fetch the token from the AuthTokenProvider
        BEARER_TOKEN = AuthTokenProvider.getToken();
        System.out.println("API Token fetched from AuthTokenProvider.");
    }

    @Test
    @DisplayName("Create User and Validate Contract via JSON Schema")
    public void testCreateUserWithSchemaValidation() {
        // Generate dynamic user data
        String gender = faker.options().option("male", "female");
        String status = faker.options().option("active", "inactive");

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", faker.name().fullName());
        payload.put("email", faker.internet().emailAddress());
        payload.put("gender", gender);
        payload.put("status", status);

        System.out.println("Sending POST request to create user with payload:");
        System.out.println(payload);

        // Send POST request, validate and extract ID
        int userId = RestAssured
                .given()
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/users")
                .then()
                .log().ifValidationFails()
                .statusCode(201) // Expecting 201 Created status code
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
                .body("id", is(notNullValue()))
                .body("name", equalTo(payload.get("name")))
                .body("email", equalTo(payload.get("email")))
                .body("gender", equalTo(gender))
                .body("status", equalTo(status))
                .extract()
                .path("id");

        System.out.println("User created and validated successfully with ID: " + userId);
    }

    @Test
    @DisplayName("Fail to Create User with Invalid Email")
    public void testCreateUserWithInvalidEmail() {
        // Generate payload with invalid data
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", faker.name().fullName());
        payload.put("email", "this-is-not-a-valid-email"); // Invalid data
        payload.put("gender", "female");
        payload.put("status", "active");

        System.out.println("Sending request with invalid email to test failure case...");

        // Send request and validate the expected error response
        RestAssured
                .given()
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/users")
                .then()
                .log().ifValidationFails()
                .statusCode(422) // Expecting a validation error status code
                .body("[0].field", equalTo("email"))
                .body("[0].message", containsString("is invalid"));

        System.out.println("Request correctly failed as expected.");
    }

    @Test
    @DisplayName("Fail to Create User with Missing Name")
    public void testCreateUserWithMissingName() {
        // Generate payload with a missing required field
        Map<String, Object> payload = new HashMap<>();
        // Name field is intentionally omitted
        payload.put("email", faker.internet().emailAddress());
        payload.put("gender", "male");
        payload.put("status", "active");

        System.out.println("Sending request with missing name field to test failure case...");

        // Send request and validate the expected error response
        RestAssured
                .given()
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/users")
                .then()
                .log().ifValidationFails()
                .statusCode(422)
                .body("[0].field", equalTo("name"))
                .body("[0].message", equalTo("can't be blank"));

        System.out.println("Request correctly failed as expected.");
    }
}