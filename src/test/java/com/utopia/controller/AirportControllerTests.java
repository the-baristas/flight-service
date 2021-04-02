package com.utopia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.core.ApplicationContext;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.controller.AirportController;
import com.utopia.dao.AirportDao;
import com.utopia.entity.Airport;
import com.utopia.service.AirportService;

@RunWith(SpringRunner.class)
@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc
public class AirportControllerTests {

	//**
	//**
	//** Airport Controller Tests: Only test_getAllAirports_statusOkAndListLength() works!
	//** To Do: Literally figure out what is going on in this file bc I'm lost.
	//**
	
	// import mock mvc
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	AirportService airportService;
    
	// Airport Controller Tests
	@Autowired
	private AirportController controller;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	// Airport Controller Is Not Null
	@Test
	public void controllerLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

	
	@Test
	public void test_getAllAirports_statusOkAndListLength() throws Exception {
		List<Airport> airports = new ArrayList<>();
		Airport airport1 = new Airport("TA3", "Test City 3", 1);
		Airport airport2 = new Airport("TA4", "Test City 4", 1);
		airports.add(airport1);
		airports.add(airport2);
		when(airportService.getAllAirports()).thenReturn(airports);
		
		// create list of airports, pass it to thenReturn to test that getting back list of airports
		
		mockMvc.perform(get("/utopia_airlines/airport")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(2)));
		
		}
	
	@Test
	public void shouldCreateAirport() throws Exception {
		Airport mockAirport = new Airport("TA4", "Test City 4", 1);
		when(airportService.saveAirport(mockAirport)).thenReturn(mockAirport.getIataId());
	
		mockMvc.perform(post("/utopia_airlines/airport")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(mockAirport)))
				.andExpect(status().isOk());
	}
	
	@Test
	public void shouldUpdateAirport() throws Exception {
		Airport mockAirport = new Airport("TA5", "Test City 5", 1);
		airportService.saveAirport(mockAirport);
		Airport updatedAirport = new Airport("TA5", "Updated Test City", 2);
		when(airportService.updateAirport(mockAirport.getIataId(), updatedAirport)).thenReturn(updatedAirport.getIataId());
		
		mockMvc.perform(put("/utopia_airlines/airport/TA5")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(updatedAirport)))
				.andExpect(status().isOk());	
	}
	
	@Test
	public void shouldDeleteAirport() throws Exception {
		Airport mockAirport = new Airport("TA6", "Test City 6", 1);
		airportService.saveAirport(mockAirport);
		when(airportService.deleteAirport(mockAirport.getIataId())).thenReturn(mockAirport.getIataId());
		
		mockMvc.perform(delete("/utopia_airlines/airport/TA5")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(mockAirport)))
				.andExpect(status().isOk());
	}
	
	
	
	
	public static String asJsonString(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
