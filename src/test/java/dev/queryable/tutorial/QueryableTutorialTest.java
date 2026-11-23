package dev.queryable.tutorial;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

@QuarkusTest
class QueryableTutorialTest {

    @BeforeEach
    void resetAudit() {
        given().delete("/test/audit").then().statusCode(204);
    }

    @Test
    void protectsGeneratedRoutesWithQuarkusConfiguration() {
        given().get("/api/v1/customers").then().statusCode(401);
        authenticated().get("/api/v1/customers").then().statusCode(200);
    }

    @Test
    void supportsQueryableFilterMatrixAndHeaders() {
        authenticated()
                .queryParam("like.name", "mario")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .header("listSize", "2");

        authenticated()
                .queryParam("obj.countries", "IT,DE")
                .queryParam("obj.active", "false")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("externalCode", hasItem("DE-003"));

        authenticated()
                .queryParam("from.createdOn", "2026-01-01")
                .queryParam("to.createdOn", "2026-12-31")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));

        authenticated()
                .queryParam("nil.notes", "1")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1));

        authenticated()
                .queryParam("notNil.notes", "1")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1));

        authenticated()
                .queryParam("obj.active", "false")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    void supportsCustomContributorOrderingAndPagination() {
        authenticated()
                .queryParam("address.city", "Milano")
                .queryParam("orderBy", "name desc")
                .queryParam("pageSize", "1")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Mario Rossi"))
                .header("pageSize", "1")
                .header("listSize", "1");
    }

    @Test
    void appliesHibernateSubqueryToResultsAndCount() {
        authenticated()
                .queryParam("obj.coordinator_id", "1")
                .queryParam("obj.coordinator_country", "IT")
                .queryParam("pageSize", "1")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].externalCode", equalTo("IT-001"))
                .header("listSize", "1");

        authenticated()
                .queryParam("obj.coordinator_id", "1")
                .queryParam("obj.coordinator_country", "FR")
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(0))
                .header("listSize", "0");

        authenticated().get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .header("listSize", "2");
    }

    @Test
    void rejectsInvalidOrIncompleteHibernateFilterParameters() {
        authenticated()
                .queryParam("obj.coordinator_id", "invalid")
                .queryParam("obj.coordinator_country", "IT")
                .get("/api/v1/customers")
                .then()
                .statusCode(400);

        authenticated()
                .queryParam("obj.coordinator_id", "1")
                .get("/api/v1/customers")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsInvalidCreateBeforePersistence() {
        authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {"externalCode":"INVALID-CREATE","country":"US","active":true}
                        """)
                .post("/api/v1/customers")
                .then()
                .statusCode(400);

        authenticated().get("/api/v1/customers/count")
                .then()
                .statusCode(200)
                .body(is("3"));
        given().get("/test/audit").then().body("$", hasSize(0));
    }

    @Test
    void rejectsInvalidUpdateBeforePersistence() {
        authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {"id":1,"name":null,"externalCode":"CHANGED","country":"US","active":true}
                        """)
                .put("/api/v1/customers/1")
                .then()
                .statusCode(400);

        authenticated().get("/api/v1/customers/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Mario Rossi"))
                .body("externalCode", equalTo("IT-001"));
        given().get("/test/audit").then().body("$", hasSize(0));
    }

    @Test
    void invokesRestLifecycleHooks() {
        String created = authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "New Customer",
                          "externalCode": " new-004 ",
                          "country": "US",
                          "createdOn": "2026-03-01",
                          "active": true,
                          "address": {"city": "Boston"}
                        }
                        """)
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .body("externalCode", equalTo("NEW-004"))
                .body("creationUser", equalTo("alice"))
                .body("creationDate", notNullValue())
                .extract()
                .path("id")
                .toString();

        given().get("/test/audit")
                .then()
                .body("$", hasItem("CREATE:" + created));

        authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": %s,
                          "name": "New Customer Updated",
                          "externalCode": "NEW-004",
                          "country": "US",
                          "createdOn": "2026-03-01",
                          "active": true,
                          "address": {"city": "Boston"}
                        }
                        """.formatted(created))
                .put("/api/v1/customers/" + created)
                .then()
                .statusCode(204);

        given().get("/test/audit")
                .then()
                .body("$", hasItem("UPDATE:" + created));

        authenticated()
                .get("/api/v1/customers/" + created)
                .then()
                .statusCode(200)
                .body("name", equalTo("New Customer Updated"))
                .body("updateUser", equalTo("alice"))
                .body("updateDate", notNullValue())
                .body("creationUser", equalTo("alice"))
                .body("creationDate", notNullValue());

        authenticated()
                .delete("/api/v1/customers/" + created)
                .then()
                .statusCode(204);

        authenticated()
                .get("/api/v1/customers/" + created)
                .then()
                .statusCode(404);

        given().get("/test/audit")
                .then()
                .body("$", hasItem("DELETE:" + created));
    }

    @Test
    void publishesGeneratedOpenApiOperations() {
        String documentText = given()
                .accept("application/json")
                .get("/q/openapi?format=JSON")
                .then()
                .statusCode(200)
                .extract()
                .asString();
        JsonPath document = JsonPath.from(documentText);
        assertThat(document.getList("paths['/api/v1/customers'].get.parameters.name"), hasItem("like.name"));
        assertThat(document.getList("paths['/api/v1/customers'].get.parameters.name"), hasItem("obj.coordinator_id"));
        assertThat(document.getList("paths['/api/v1/customers'].get.parameters.name"), hasItem("obj.coordinator_country"));
        assertThat(document.getMap("paths['/api/v1/customers'].post"), notNullValue());
        assertThat(document.getMap("paths['/api/v1/customers/{id}'].put"), notNullValue());
        assertThat(document.getMap("paths['/api/v1/customers/count'].get"), notNullValue());
    }

    @Test
    void usesOfficialRestDataCountEndpoint() {
        authenticated()
                .get("/api/v1/customers/count")
                .then()
                .statusCode(200)
                .body(is("3"));
    }

    @Test
    void supportsOriginalQueryableTutorialEntities() {
        String teamId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"secondo","tags":"typescript,react"}
                        """)
                .post("/api/teams")
                .then()
                .statusCode(201)
                .extract().path("uuid");

        given().contentType(ContentType.JSON)
                .body("""
                        {"name":"primo","tags":"java,angular"}
                        """)
                .post("/api/teams")
                .then().statusCode(201);

        given().queryParam("like.tags", "react,java")
                .get("/api/teams")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].name", equalTo("primo"))
                .body("[1].name", equalTo("secondo"));

        given().contentType(ContentType.JSON)
                .body("""
                        {
                          "name":"fiorenzo",
                          "surname":"pizza",
                          "team_uuid":"%s",
                          "active":true
                        }
                        """.formatted(teamId))
                .post("/api/developers")
                .then().statusCode(201);

        given().queryParam("like.surname", "pi")
                .queryParam("obj.team_uuid", teamId)
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("fiorenzo"));
    }

    private io.restassured.specification.RequestSpecification authenticated() {
        return given().auth().preemptive().basic("alice", "secret");
    }
}
