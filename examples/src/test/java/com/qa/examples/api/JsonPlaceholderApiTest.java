package com.qa.examples.api;

import com.qa.framework.api.client.ApiClient;
import com.qa.framework.api.test.BaseApiTest;
import com.qa.framework.api.validation.SchemaValidation;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

@Epic("API")
@Feature("JSONPlaceholder")
public class JsonPlaceholderApiTest extends BaseApiTest {

    @Test(groups = {"smoke", "api"})
    @Description("GET /posts/1 returns a post")
    public void getPost() {
        Response response = api.get("/posts/1");
        response.then().statusCode(200);
        Assert.assertEquals(response.jsonPath().getInt("id"), 1);
        SchemaValidation.jsonSchema(response, "schemas/post.json");
    }

    @Test(groups = {"smoke", "api"})
    @Description("GET /posts returns a collection")
    public void listPosts() {
        Response response = api.get("/posts");
        response.then().statusCode(200);
        Assert.assertTrue(response.jsonPath().getList("$").size() > 1);
    }

    @Test(groups = {"smoke", "api"})
    @Description("POST /posts creates a resource")
    public void createPost() {
        Response response = api.post("/posts", Map.of(
                "title", "hybrid-framework",
                "body", "api parallel example",
                "userId", 1));
        response.then().statusCode(201);
        Assert.assertEquals(response.jsonPath().getString("title"), "hybrid-framework");
    }

    @Test(groups = {"smoke", "api"})
    @Description("PUT /posts/1 updates a resource")
    public void updatePost() {
        Response response = api.put("/posts/1", Map.of(
                "id", 1,
                "title", "updated",
                "body", "body",
                "userId", 1));
        response.then().statusCode(200);
        Assert.assertEquals(response.jsonPath().getString("title"), "updated");
    }

    @Test(groups = {"smoke", "api"})
    @Description("PATCH /posts/1 partially updates a resource")
    public void patchPost() {
        Response response = api.patch("/posts/1", Map.of("title", "patched"));
        response.then().statusCode(200);
        Assert.assertEquals(response.jsonPath().getString("title"), "patched");
    }

    @Test(groups = {"smoke", "api"})
    @Description("DELETE /posts/1")
    public void deletePost() {
        api.delete("/posts/1").then().statusCode(200);
    }

    @Test(groups = {"api"})
    @Description("GET /users/1")
    public void getUser() {
        Response response = new ApiClient().get("/users/1");
        response.then().statusCode(200);
        Assert.assertEquals(response.jsonPath().getString("username"), "Bret");
    }

    @Test(groups = {"api"})
    @Description("GET /comments with query")
    public void commentsForPost() {
        Response response = api.get("/comments", Map.of("postId", 1));
        response.then().statusCode(200);
        Assert.assertFalse(response.jsonPath().getList("$").isEmpty());
    }
}
