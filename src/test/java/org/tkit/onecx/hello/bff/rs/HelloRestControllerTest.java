package org.tkit.onecx.hello.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.hello.bff.rs.controllers.HelloRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.*;
import gen.org.tkit.onecx.hello.world.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(HelloRestController.class)
class HelloRestControllerTest extends AbstractTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();
    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String MOCK_ID = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void getHelloByIdTest() {
        Hello data = new Hello();
        data.setId("test-id-1");
        data.setName("test-name");

        GetHelloByIdResponse response = new GetHelloByIdResponse();
        response.setResource(data);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/" + data.getId()).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", data.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetHelloByIdResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getId(), output.getResource().getId());
        Assertions.assertEquals(data.getName(), output.getResource().getName());
    }

    @Test
    void deleteHelloTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/" + id).withMethod(HttpMethod.DELETE))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void createHelloTest() {
        CreateHelloRequest request = new CreateHelloRequest();
        Hello data = new Hello();

        data.setName("value1");
        request.setResource(data);
        CreateHelloResponse response = new CreateHelloResponse();
        response.setResource(data);
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateHelloRequestDTO input = new CreateHelloRequestDTO();
        HelloDTO updateCreateDTO = new HelloDTO();
        updateCreateDTO.setName("value1");
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateHelloResponseDTO.class);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getName(), output.getResource().getName());
    }

    @Test
    void createHelloFailTest() {
        CreateHelloRequest data = new CreateHelloRequest();
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(data)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateHelloRequestDTO input = new CreateHelloRequestDTO();
        HelloDTO updateCreateDTO = new HelloDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void searchHelloByCriteriaTest() {
        SearchHelloRequest criteria = new SearchHelloRequest();
        criteria.setPageNumber(1);
        criteria.setName("test");
        criteria.setPageSize(1);

        Hello t1 = new Hello();
        t1.setId("1");
        t1.setName("test");

        SearchHelloResponse data = new SearchHelloResponse();
        data.setNumber(1);
        data.setSize(1);
        data.setTotalElements(1L);
        data.setTotalPages(1L);
        data.setStream(List.of(t1));

        SearchHelloRequestDTO searchHelloRequestDTO = new SearchHelloRequestDTO();
        searchHelloRequestDTO.setPageNumber(1);
        searchHelloRequestDTO.setPageSize(1);
        searchHelloRequestDTO.setName("test");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(searchHelloRequestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SearchHelloResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getSize(), output.getSize());
        Assertions.assertEquals(data.getStream().size(), output.getStream().size());
        Assertions.assertEquals(data.getStream().get(0).getName(), output.getStream().get(0).getName());
    }

    @Test
    void searchHelloByEmptyCriteriaTest() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/search").withMethod(HttpMethod.POST))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void updateHelloTest() {
        String testId = "testId";
        UpdateHelloRequest updateHello = new UpdateHelloRequest();
        Hello hello = new Hello();
        hello.setName("test-name");
        updateHello.setResource(hello);

        UpdateHelloResponse response = new UpdateHelloResponse();
        Hello data = new Hello();
        data.setName("test-name");
        response.setResource(data);
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateHello)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(response))
                        .withContentType(MediaType.APPLICATION_JSON));

        HelloDTO updateCreateDTO = new HelloDTO();
        updateCreateDTO.setName("test-name");
        UpdateHelloRequestDTO input = new UpdateHelloRequestDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UpdateHelloResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(hello.getName(), output.getResource().getName());
    }

    @Test
    void updateHelloFailTest() {
        String testId = "testId";
        UpdateHelloRequest updateHello = new UpdateHelloRequest();
        Hello data = new Hello();
        data.setName("test");
        updateHello.setResource(data);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(Response.Status.BAD_REQUEST.toString());
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateHello)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse))
                        .withContentType(MediaType.APPLICATION_JSON));

        HelloDTO updateCreateDTO = new HelloDTO();
        updateCreateDTO.setName("test");
        UpdateHelloRequestDTO input = new UpdateHelloRequestDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void getHelloByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/hello/" + notFoundId).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", notFoundId)
                .get("/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }
    //
    //    @Test
    //    void serverConstraintTest() {
    //        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
    //        problemDetailResponse.setErrorCode("400");
    //        CreateHello createHello = new CreateHello();
    //        // create mock rest endpoint
    //        mockServerClient.when(request().withPath("/internal/hellos").withMethod(HttpMethod.POST)
    //                .withBody(JsonBody.json(createHello)))
    //                .withId(MOCK_ID)
    //                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
    //                        .withContentType(MediaType.APPLICATION_JSON)
    //                        .withBody(JsonBody.json(problemDetailResponse)));
    //
    //        CreateHelloRequestDTO createHelloRequestDTO = new CreateHelloRequestDTO();
    //        HelloUpdateCreateDTO helloUpdateCreateDTO = new HelloUpdateCreateDTO();
    //        createHelloRequestDTO.setResource(helloUpdateCreateDTO);
    //
    //        var output = given()
    //                .when()
    //                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
    //                .header(APM_HEADER_PARAM, ADMIN)
    //                .contentType(APPLICATION_JSON)
    //                .body(createHelloRequestDTO)
    //                .post()
    //                .then()
    //                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
    //                .contentType(APPLICATION_JSON)
    //                .extract().as(ProblemDetailResponseDTO.class);
    //
    //        Assertions.assertNotNull(output);
    //    }
}
