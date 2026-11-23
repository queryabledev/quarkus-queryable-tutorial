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
        given().get("/api/developers").then().statusCode(401);
        authenticated().get("/api/developers").then().statusCode(200);
    }

    @Test
    void supportsQueryableFilterMatrixAndHeaders() {
        authenticated()
                .queryParam("like.name", "mario")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].surname", equalTo("rossi"))
                .header("listSize", "1");

        authenticated()
                .queryParam("from.hiredOn", "2026-01-01")
                .queryParam("to.hiredOn", "2026-12-31")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));

        // legacy Queryable semantics: nil.<field> filters IS NOT NULL
        authenticated()
                .queryParam("nil.notes", "1")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("mario"));

        // and notNil.<field> filters IS NULL
        authenticated()
                .queryParam("notNil.notes", "1")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("fiorenzo"));

        authenticated()
                .queryParam("obj.active", "false")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("luigi"));

        authenticated()
                .queryParam("obj.team_uuid", "team-2")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("mario"));
    }

    @Test
    void supportsCustomContributorOrderingAndPagination() {
        authenticated()
                .queryParam("search", "ross")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("mario"));

        authenticated()
                .queryParam("search", "i")
                .queryParam("orderBy", "name desc")
                .queryParam("pageSize", "1")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("mario"))
                .header("pageSize", "1")
                .header("listSize", "2");
    }

    @Test
    void filtersDevelopersByProjectWithHibernateFilter() {
        authenticated()
                .queryParam("obj.project_uuid", "proj-1")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].surname", equalTo("pizza"))
                .body("[1].surname", equalTo("rossi"))
                .header("listSize", "2");

        // dev-3 is assigned to proj-2 but logically deleted
        authenticated()
                .queryParam("obj.project_uuid", "proj-2")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(0))
                .header("listSize", "0");

        authenticated()
                .queryParam("obj.project_uuid", "proj-2")
                .queryParam("obj.active", "false")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("luigi"));

        authenticated()
                .queryParam("obj.project_uuid", "unknown-project")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void filtersDevelopersByTeam() {
        String alpha = createTeam("alpha", "calcio");
        String beta = createTeam("beta", "basket");

        String d1 = createDeveloper("anna", "bianchi", alpha);
        String d2 = createDeveloper("bruno", "neri", alpha);
        String d3 = createDeveloper("carla", "gialli", alpha);
        String d4 = createDeveloper("dario", "viola", beta);
        String d5 = createDeveloper("elena", "azzurri", beta);

        // only the players of team alpha, ordered by surname
        authenticated()
                .queryParam("obj.team_uuid", alpha)
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("[0].surname", equalTo("bianchi"))
                .body("[1].surname", equalTo("gialli"))
                .body("[2].surname", equalTo("neri"))
                .header("listSize", "3");

        // only the players of team beta
        authenticated()
                .queryParam("obj.team_uuid", beta)
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].surname", equalTo("azzurri"))
                .body("[1].surname", equalTo("viola"))
                .header("listSize", "2");

        for (String uuid : new String[] { d1, d2, d3, d4, d5 }) {
            authenticated().delete("/api/developers/" + uuid).then().statusCode(204);
        }
        for (String uuid : new String[] { alpha, beta }) {
            given().delete("/api/teams/" + uuid).then().statusCode(204);
        }
    }

    @Test
    void rejectsInvalidCreateBeforePersistence() {
        authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"nosurname","team_uuid":"team-1","active":true}
                        """)
                .post("/api/developers")
                .then()
                .statusCode(400);

        authenticated().get("/api/developers/count")
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
                        {"uuid":"dev-1","name":null,"surname":"pizza","team_uuid":"team-1","active":true}
                        """)
                .put("/api/developers/dev-1")
                .then()
                .statusCode(400);

        authenticated().get("/api/developers/dev-1")
                .then()
                .statusCode(200)
                .body("name", equalTo("fiorenzo"))
                .body("surname", equalTo("pizza"));
        given().get("/test/audit").then().body("$", hasSize(0));
    }

    @Test
    void invokesRestLifecycleHooks() {
        String created = authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "nuovo",
                          "surname": "sviluppatore",
                          "team_uuid": "team-1",
                          "hiredOn": "2026-03-01",
                          "active": true
                        }
                        """)
                .post("/api/developers")
                .then()
                .statusCode(201)
                .body("creationUser", equalTo("alice"))
                .body("creationDate", notNullValue())
                .extract()
                .path("uuid");

        given().get("/test/audit")
                .then()
                .body("$", hasItem("CREATE:" + created));

        authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "uuid": "%s",
                          "name": "nuovo",
                          "surname": "aggiornato",
                          "team_uuid": "team-1",
                          "hiredOn": "2026-03-01",
                          "active": true
                        }
                        """.formatted(created))
                .put("/api/developers/" + created)
                .then()
                .statusCode(204);

        given().get("/test/audit")
                .then()
                .body("$", hasItem("UPDATE:" + created));

        authenticated()
                .get("/api/developers/" + created)
                .then()
                .statusCode(200)
                .body("surname", equalTo("aggiornato"))
                .body("updateUser", equalTo("alice"))
                .body("updateDate", notNullValue())
                .body("creationUser", equalTo("alice"))
                .body("creationDate", notNullValue());

        authenticated()
                .delete("/api/developers/" + created)
                .then()
                .statusCode(204);

        authenticated()
                .get("/api/developers/" + created)
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
        assertThat(document.getList("paths['/api/developers'].get.parameters.name"), hasItem("like.surname"));
        assertThat(document.getList("paths['/api/developers'].get.parameters.name"), hasItem("obj.project_uuid"));
        assertThat(document.getMap("paths['/api/developers'].post"), notNullValue());
        assertThat(document.getMap("paths['/api/developers/{id}'].put"), notNullValue());
        assertThat(document.getMap("paths['/api/developers/count'].get"), notNullValue());
    }

    @Test
    void usesOfficialRestDataCountEndpoint() {
        authenticated()
                .get("/api/developers/count")
                .then()
                .statusCode(200)
                .body(is("3"));
    }

    @Test
    void supportsOriginalQueryableTutorialEntities() {
        given().queryParam("like.tags", "react,java")
                .get("/api/teams")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].name", equalTo("primo"))
                .body("[1].name", equalTo("secondo"));

        authenticated()
                .queryParam("like.surname", "pi")
                .queryParam("obj.team_uuid", "team-1")
                .get("/api/developers")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("fiorenzo"));
    }

    private String createTeam(String name, String tags) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"%s","tags":"%s"}
                        """.formatted(name, tags))
                .post("/api/teams")
                .then()
                .statusCode(201)
                .extract().path("uuid");
    }

    private String createDeveloper(String name, String surname, String teamUuid) {
        return authenticated()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"%s","surname":"%s","team_uuid":"%s","active":true}
                        """.formatted(name, surname, teamUuid))
                .post("/api/developers")
                .then()
                .statusCode(201)
                .extract().path("uuid");
    }

    private io.restassured.specification.RequestSpecification authenticated() {
        return given().auth().preemptive().basic("alice", "secret");
    }
}
