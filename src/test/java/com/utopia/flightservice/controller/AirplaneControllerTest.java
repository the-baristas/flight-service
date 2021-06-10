package com.utopia.flightservice.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.flightservice.dto.AirplaneDto;
import com.utopia.flightservice.entity.Airplane;
import com.utopia.flightservice.service.AirplaneService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@WebMvcTest(AirplaneController.class)
public class AirplaneControllerTest {
    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private AirplaneService airplaneService;

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Disabled
    @Test
    public void findAllAirplanes_JsonArray() throws Exception {
        Airplane airplane = new Airplane();
        airplane.setId(1L);
        airplane.setFirstClassSeatsMax(1L);
        airplane.setBusinessClassSeatsMax(1L);
        airplane.setEconomyClassSeatsMax(1L);
        List<Airplane> foundAirplanes = Arrays.asList(airplane);
        when(airplaneService.findAllAirplanes()).thenReturn(foundAirplanes);
        List<AirplaneDto> foundAirplaneDtos = foundAirplanes.stream()
                .map((Airplane a) -> modelMapper.map(a, AirplaneDto.class))
                .collect(Collectors.toList());

        webTestClient.get().uri("/airplanes").accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(new ObjectMapper()
                        .writeValueAsString(foundAirplaneDtos));
    }

    @Test
    public void findAirplaneById_AirplaneFound() throws Exception {
        Airplane foundAirplane = new Airplane();
        foundAirplane.setId(1L);
        foundAirplane.setFirstClassSeatsMax(0L);
        foundAirplane.setBusinessClassSeatsMax(0L);
        foundAirplane.setEconomyClassSeatsMax(0L);
        when(airplaneService.findAirplaneById(foundAirplane.getId()))
                .thenReturn(foundAirplane);
        AirplaneDto foundAirplaneDto = modelMapper.map(foundAirplane,
                AirplaneDto.class);

        webTestClient.get().uri("/airplanes/{id}", foundAirplane.getId())
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AirplaneDto.class).isEqualTo(foundAirplaneDto);
    }

    @Test
    public void createAirplane_Airplane_AirplaneFound() throws Exception {
        Airplane airplane = new Airplane();
        airplane.setId(1L);
        airplane.setFirstClassSeatsMax(1L);
        airplane.setBusinessClassSeatsMax(1L);
        airplane.setEconomyClassSeatsMax(1L);
        when(airplaneService.createAirplane(airplane)).thenReturn(airplane);
        AirplaneDto airplaneDto = modelMapper.map(airplane, AirplaneDto.class);

        webTestClient.post().uri("/airplanes")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(airplaneDto)
                .exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(AirplaneDto.class).isEqualTo(airplaneDto);
    }

    @Disabled
    @Test
    public void updateAirplane_ValidAirplaneId_AirplaneUpdated() {
        Airplane airplane = new Airplane();
        Long id = 1L;
        airplane.setId(id);
        airplane.setFirstClassSeatsMax(0L);
        airplane.setBusinessClassSeatsMax(0L);
        airplane.setEconomyClassSeatsMax(0L);
        when(airplaneService.updateAirplane(airplane)).thenReturn(airplane);
        AirplaneDto airplaneDto = modelMapper.map(airplane, AirplaneDto.class);

        webTestClient.put().uri("/airplanes")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(airplane)
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(AirplaneDto.class).isEqualTo(airplaneDto);
    }

    @Test
    public void deleteAirplane_ValidAirplaneId_AirplaneDeleted() {
        Airplane airplane = new Airplane();
        airplane.setId(1L);
        airplane.setFirstClassSeatsMax(0L);
        airplane.setBusinessClassSeatsMax(0L);
        airplane.setEconomyClassSeatsMax(0L);

        airplaneService.deleteAirplaneById(airplane.getId());
        verify(airplaneService, times(1)).deleteAirplaneById(airplane.getId());
    }
}
