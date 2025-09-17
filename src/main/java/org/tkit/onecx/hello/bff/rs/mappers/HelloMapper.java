package org.tkit.onecx.hello.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.*;
import gen.org.tkit.onecx.hello.world.client.model.*;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface HelloMapper {
    CreateHelloRequest mapCreate(CreateHelloRequestDTO createHelloRequestDTO);

    CreateHelloResponseDTO mapCreate(CreateHelloResponse createHelloResponse);

    GetHelloByIdResponseDTO map(GetHelloByIdResponse getHelloByIdResponse);

    SearchHelloRequest mapCriteria(SearchHelloRequestDTO searchHelloRequestDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    SearchHelloResponseDTO mapPageResult(SearchHelloResponse searchHelloResponse);

    UpdateHelloRequest mapUpdate(UpdateHelloRequestDTO updateHelloRequestDTO);

    UpdateHelloResponseDTO mapUpdate(UpdateHelloResponse updateHelloResponse);
}
