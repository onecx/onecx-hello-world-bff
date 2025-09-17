package org.tkit.onecx.hello.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.hello.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.hello.bff.rs.mappers.HelloMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.hello.world.bff.rs.internal.HelloBffServiceApiService;
import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.CreateHelloRequestDTO;
import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.SearchHelloRequestDTO;
import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.UpdateHelloRequestDTO;
import gen.org.tkit.onecx.hello.world.client.api.HelloInternalApi;
import gen.org.tkit.onecx.hello.world.client.model.CreateHelloResponse;
import gen.org.tkit.onecx.hello.world.client.model.GetHelloByIdResponse;
import gen.org.tkit.onecx.hello.world.client.model.SearchHelloResponse;
import gen.org.tkit.onecx.hello.world.client.model.UpdateHelloResponse;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class HelloRestController implements HelloBffServiceApiService {

    @Inject
    @RestClient
    HelloInternalApi client;

    @Inject
    HelloMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createHello(CreateHelloRequestDTO createHelloRequestDTO) {
        try (Response response = client.createHello(mapper.mapCreate(createHelloRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(mapper.mapCreate(response.readEntity(CreateHelloResponse.class))).build();
        }
    }

    @Override
    public Response deleteHello(String id) {
        try (Response response = client.deleteHello(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getHelloById(String id) {
        try (Response response = client.getHelloById(id)) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(GetHelloByIdResponse.class)))
                    .build();
        }
    }

    @Override
    public Response searchHellos(SearchHelloRequestDTO searchHelloRequestDTO) {
        try (Response response = client.searchHellos(mapper.mapCriteria(searchHelloRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(mapper.mapPageResult(response.readEntity(SearchHelloResponse.class))).build();
        }
    }

    @Override
    public Response updateHello(String id, UpdateHelloRequestDTO updateHelloRequestDTO) {
        try (Response response = client.updateHello(id, mapper.mapUpdate(updateHelloRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(mapper.mapUpdate(response.readEntity(UpdateHelloResponse.class))).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
