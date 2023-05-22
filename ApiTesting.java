package com.ApiTesting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.ApiTesting.dtos.User;
import com.ApiTesting.dtos.UserResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
public class ApiTesting {

    private String baseUrl = "https://reqres.in/api";
    private String usersEndpoint = baseUrl + "/users";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = baseUrl;
    }

    @Test
    public void testGetUsers() {
        int expectedStatusCode = 200;

        // GET users
        UserResponse response = given()
                .queryParam("page", 1)
                .when()
                .get(usersEndpoint)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .as(UserResponse.class);

        // Filter out first name and email of all users
        for (User user : response.getData()) {
            System.out.println("First Name: " + user.getFirstName());
            System.out.println("Email: " + user.getEmail());
        }

        // Choose an ID for the next steps
        long userId = response.getData().get(0).getId();

        // Reusable method to get user details by ID
        User userById = getUserById(userId);
        Assert.assertNotNull(userById, "User details by ID not found");

        // Validate response code and user details
        Assert.assertEquals(userById.getId(), userId, "User ID mismatch");
        Assert.assertEquals(userById.getFirstName(), response.getData().get(0).getFirstName(), "First name mismatch");
        Assert.assertEquals(userById.getEmail(), response.getData().get(0).getEmail(), "Email mismatch");
    }

    @Test(dependsOnMethods = "testGetUsers")
    public void testCreateUser() {
        int expectedStatusCode = 201;

        // Create a new user
        User newUser = new User("John1", "john@example.com");

        // POST user
        User createdUser = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post(usersEndpoint)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .as(User.class);

        // Validate response code and user details
        Assert.assertNotNull(createdUser.getId(), "Newly created user ID not found");

        // PUT user
         expectedStatusCode = 200;

        User updatedUser  = given()
                .contentType(ContentType.JSON)
                .body(new User("Updated John2", "updatedjohn2@example.com"))
                .when()
                .put(usersEndpoint + "/" + createdUser.getId())
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .as(User.class);


        // Validate response code and updated user details
        Assert.assertEquals(updatedUser.getFirstName(), "Updated John2", "Updated first name mismatch");
        Assert.assertEquals(updatedUser.getEmail(), "updatedjohn2@example.com", "Updated email mismatch");
    }


    private User getUserById(long id) {
        UserResponse userResponse = given()
                .pathParam("id", id)
                .when()
                .get(usersEndpoint + "/{id}")
                .then()
                .statusCode(200)
                .extract()
                .as(UserResponse.class);
        return userResponse.getData().get(0);
    }
    @Test
    public void testFilterById() {
        String baseUrl = "https://reqres.in"; // Base URL of the API
       int idToFilter =3;
              given()
                .baseUri(baseUrl)
                .when()
                .get("/api/users") // Replace with the correct endpoint to retrieve the JSON data
                .then()
                .statusCode(200) // Specify the expected status code
                .extract().response().jsonPath()
                .getList("data.findAll { it.id == " + idToFilter + " }")
                .forEach(System.out::println);
    }
}
