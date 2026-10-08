import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiskResourcesTest {

    private static String token;

    @BeforeAll
    public static void beforeAll() {
        token = System.getenv("DISK_TOKEN");
        assertNotNull(token, "There is no variable DISK_TOKEN");
    }

    @Test
    void getResources() {

        given()
                .baseUri("https://cloud-api.yandex.net")
                .header("Authorization", "OAuth " + token)
                .queryParam("path", "/")
                .when()
                .get("/v1/disk/resources")
                .then()
                .statusCode(200)
                .body("type", equalTo("dir"))
                .body("path", equalTo("disk:/"));
    }

    @Test
    void getResources_notFound() {

        given()
                .baseUri("https://cloud-api.yandex.net")
                .header("Authorization", "OAuth " + token)
                .queryParam("path", "/non-existent-resource")
                .when()
                .get("/v1/disk/resources")
                .then()
                .statusCode(404);
    }

    @Test
    void folderLifecycle() {

        String folder = "/folder-" + UUID.randomUUID();
        String folderCopy = folder + "-copy";

        try {
            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folder)
                    .when()
                    .put("/v1/disk/resources")
                    .then()
                    .statusCode(201);

            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("from", folder)
                    .queryParam("path", folderCopy)
                    .when()
                    .post("/v1/disk/resources/copy")
                    .then()
                    .statusCode(201);

            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folder)
                    .when()
                    .get("/v1/disk/resources")
                    .then()
                    .statusCode(200);

            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folderCopy)
                    .when()
                    .get("/v1/disk/resources")
                    .then()
                    .statusCode(200);

            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folder)
                    .when()
                    .delete("/v1/disk/resources")
                    .then()
                    .statusCode(204);

            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folder)
                    .when()
                    .get("/v1/disk/resources")
                    .then()
                    .statusCode(404);

        } finally {
            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folder)
                    .when()
                    .delete("/v1/disk/resources");

            given()
                    .baseUri("https://cloud-api.yandex.net")
                    .header("Authorization", "OAuth " + token)
                    .queryParam("path", folderCopy)
                    .when()
                    .delete("/v1/disk/resources");
        }
    }
}