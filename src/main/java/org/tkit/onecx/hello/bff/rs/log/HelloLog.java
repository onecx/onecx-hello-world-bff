package org.tkit.onecx.hello.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.CreateHelloRequestDTO;
import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.SearchHelloRequestDTO;
import gen.org.tkit.onecx.hello.world.bff.rs.internal.model.UpdateHelloRequestDTO;

@ApplicationScoped
public class HelloLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateHelloRequestDTO.class,
                        x -> "CreateHelloRequestDTO[ name: " +
                                ((CreateHelloRequestDTO) x).getResource().getName()
                                + " ]"),
                this.item(10, UpdateHelloRequestDTO.class,
                        x -> "UpdateHelloRequestDTO[ name: " + ((UpdateHelloRequestDTO) x).getResource().getName()
                                + " ]"),
                this.item(10, SearchHelloRequestDTO.class,
                        x -> "SearchHelloRequestDTO[ name: " + ((SearchHelloRequestDTO) x).getName()
                                + " ]"));
    }
}
