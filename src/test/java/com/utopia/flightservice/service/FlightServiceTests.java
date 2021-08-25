package com.utopia.flightservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.utopia.flightservice.email.EmailSender;
import com.utopia.flightservice.entity.Airplane;
import com.utopia.flightservice.entity.Airport;
import com.utopia.flightservice.entity.Flight;
import com.utopia.flightservice.entity.FlightQuery;
import com.utopia.flightservice.entity.Route;
import com.utopia.flightservice.entity.User;
import com.utopia.flightservice.exception.FlightNotSavedException;
import com.utopia.flightservice.repository.FlightDao;
import com.utopia.flightservice.repository.RouteDao;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest
class FlightServiceTests {

    @Autowired
    private FlightService flightService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private AirplaneService airplaneService;

    @MockBean
    private FlightDao flightDao;

    @MockBean
    private EmailSender emailSender;

    @MockBean
    private GraphService graphService;

    @MockBean
    private RouteDao routeDao;

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void findAllFlights_FindsFlights() {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);
        List<Flight> allFlights = Arrays.asList(flight);
        when(flightDao.findAll()).thenReturn(allFlights);

        List<Flight> foundFlights = flightService.getAllFlights();
        assertEquals(allFlights, foundFlights);
    }

    @Test
    void findFlightById_FindsFlight() {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Optional<Flight> flight = Optional.ofNullable(new Flight());
        flight.get().setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.get().setRoute(route);
        flight.get().setAirplane(airplane);
        flight.get().setDepartureTime(departureTime);
        flight.get().setArrivalTime(arrivalTime);
        flight.get().setFirstReserved(0);
        flight.get().setFirstPrice(350.00f);
        flight.get().setBusinessReserved(0);
        flight.get().setBusinessPrice(300.00f);
        flight.get().setEconomyReserved(0);
        flight.get().setEconomyPrice(200.00f);
        flight.get().setIsActive(true);

        when(flightDao.findById(101)).thenReturn(flight);

        Optional<Flight> foundFlight = flightService.getFlightById(101);
        assertThat(flight.get().getId(), is(foundFlight.get().getId()));
        assertThat(flight.get().getRoute(), is(foundFlight.get().getRoute()));
        assertThat(flight.get().getAirplane(),
                is(foundFlight.get().getAirplane()));
        assertThat(flight.get().getDepartureTime(),
                is(foundFlight.get().getDepartureTime()));
        assertThat(flight.get().getArrivalTime(),
                is(foundFlight.get().getArrivalTime()));
        assertThat(flight.get().getFirstReserved(),
                is(foundFlight.get().getFirstReserved()));
        assertThat(flight.get().getFirstPrice(),
                is(foundFlight.get().getFirstPrice()));
        assertThat(flight.get().getBusinessReserved(),
                is(foundFlight.get().getBusinessReserved()));
        assertThat(flight.get().getBusinessPrice(),
                is(foundFlight.get().getBusinessPrice()));
        assertThat(flight.get().getEconomyReserved(),
                is(foundFlight.get().getEconomyReserved()));
        assertThat(flight.get().getEconomyPrice(),
                is(foundFlight.get().getEconomyPrice()));
        assertThat(flight.get().getIsActive(),
                is(foundFlight.get().getIsActive()));
    }

    @Test
    void addFlight_AndSaveIt() throws FlightNotSavedException {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);
        when(flightDao.save(flight)).thenReturn(flight);

        Integer savedAirportID = flightService.saveFlight(flight);
        assertThat(flight.getId(), is(savedAirportID));
    }

    @Test
    void findAllFlightPages() {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);
        List<Flight> allFlights = Arrays.asList(flight);
        Pageable paging = PageRequest.of(0, 10, Sort.by("id"));
        Page<Flight> flightPage = new PageImpl<Flight>(allFlights);
        when(flightDao.findAll(paging)).thenReturn(flightPage);

        Page<Flight> foundFlights = flightService.getPagedFlights(0, 10, "id");
        assertEquals(flightPage, foundFlights);
    }

    @Test
    void shouldGetFlight_ByRouteId() {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);

        List<Route> routes = new ArrayList<Route>();
        Route route1 = new Route(1, originAirport, destinationAirport, true);
        Route route2 = new Route(2, originAirport, destinationAirport, true);
        Route route3 = new Route(3, originAirport, destinationAirport, true);
        routes.add(route1);
        routes.add(route2);
        routes.add(route3);

        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route1);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);
        List<Flight> allFlights = Arrays.asList(flight);
        Pageable paging = PageRequest.of(0, 10, Sort.by("id"));
        Page<Flight> flightPage = new PageImpl<Flight>(allFlights);

        when(flightDao.findAllByRouteIn(routes, paging)).thenReturn(flightPage);

        Page<Flight> foundFlights = flightService.getFlightsByRoute(0, 10, "id",
                routes);
        assertEquals(flightPage, foundFlights);
    }

    @Test
    public void shouldGetFlightsByRouteAndDate_FilterAll() {

        // params
        Integer pageNo = 0;
        Integer pageSize = 10;
        String sortBy = "id";

        Airport airport1 = new Airport("LAX", "Test City 1", true);
        Airport airport2 = new Airport("JFK", "Test City 2", true);

        Route route1 = new Route(1, airport1, airport2, true);
        List<Route> routes = new ArrayList<Route>();
        routes.add(route1);

        FlightQuery flightQuery = new FlightQuery(3, 16, 2021, "all");

        // mocks

        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        Flight flight = new Flight();
        flight.setId(101);
        flight.setRoute(route1);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);

        List<Flight> flights = new ArrayList<Flight>();
        flights.add(flight);
        Pageable paging = PageRequest.of(0, 10, Sort.by(sortBy));
        Page<Flight> flightPage = new PageImpl<Flight>(flights);

        Integer month = Integer.valueOf(flightQuery.getMonth());
        Integer date = Integer.valueOf(flightQuery.getDate());
        Integer year = Integer.valueOf(flightQuery.getYear());
        Integer hour = 00;
        Integer min = 00;

        LocalDateTime departure = LocalDateTime.of(year, month, date, hour,
                min);
        LocalDateTime departureHelper = LocalDateTime.of(year, month, date + 1,
                hour, min);

        when(flightDao
                .findByRouteInAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        routes, departure, departureHelper, paging))
                                .thenReturn(flightPage);

        Page<Flight> foundFlights = flightService.getFlightsByRouteAndDate(
                pageNo, pageSize, sortBy, routes, flightQuery);
        assertEquals(flightPage, foundFlights);

    }

    @Test
    public void shouldGetFlightsByRouteAndDate_FilterMorning() {

        // params
        Integer pageNo = 0;
        Integer pageSize = 10;
        String sortBy = "id";

        Airport airport1 = new Airport("LAX", "Test City 1", true);
        Airport airport2 = new Airport("JFK", "Test City 2", true);

        Route route1 = new Route(1, airport1, airport2, true);
        List<Route> routes = new ArrayList<Route>();
        routes.add(route1);

        FlightQuery flightQuery = new FlightQuery(3, 16, 2021, "morning");

        // mocks

        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        Flight flight = new Flight();
        flight.setId(101);
        flight.setRoute(route1);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);

        List<Flight> flights = new ArrayList<Flight>();
        flights.add(flight);
        Pageable paging = PageRequest.of(0, 10, Sort.by(sortBy));
        Page<Flight> flightPage = new PageImpl<Flight>(flights);

        Integer month = Integer.valueOf(flightQuery.getMonth());
        Integer date = Integer.valueOf(flightQuery.getDate());
        Integer year = Integer.valueOf(flightQuery.getYear());
        Integer hour = 00;
        Integer min = 00;

        LocalDateTime departure = LocalDateTime.of(year, month, date, 04, min);
        LocalDateTime departureHelper = LocalDateTime.of(year, month, date, 12,
                min);

        when(flightDao
                .findByRouteInAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        routes, departure, departureHelper, paging))
                                .thenReturn(flightPage);

        Page<Flight> foundFlights = flightService.getFlightsByRouteAndDate(
                pageNo, pageSize, sortBy, routes, flightQuery);
        assertEquals(flightPage, foundFlights);

    }

    @Test
    public void shouldGetFlightsByRouteAndDate_FilterAfternoon() {

        // params
        Integer pageNo = 0;
        Integer pageSize = 10;
        String sortBy = "id";

        Airport airport1 = new Airport("LAX", "Test City 1", true);
        Airport airport2 = new Airport("JFK", "Test City 2", true);

        Route route1 = new Route(1, airport1, airport2, true);
        List<Route> routes = new ArrayList<Route>();
        routes.add(route1);

        FlightQuery flightQuery = new FlightQuery(3, 16, 2021, "afternoon");

        // mocks

        String str1 = "2020-09-01 12:01:15";
        String str2 = "2020-09-01 17:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        Flight flight = new Flight();
        flight.setId(101);
        flight.setRoute(route1);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);

        List<Flight> flights = new ArrayList<Flight>();
        flights.add(flight);
        Pageable paging = PageRequest.of(0, 10, Sort.by(sortBy));
        Page<Flight> flightPage = new PageImpl<Flight>(flights);

        Integer month = Integer.valueOf(flightQuery.getMonth());
        Integer date = Integer.valueOf(flightQuery.getDate());
        Integer year = Integer.valueOf(flightQuery.getYear());
        Integer hour = 00;
        Integer min = 00;

        LocalDateTime departure = LocalDateTime.of(year, month, date, 12, min);
        LocalDateTime departureHelper = LocalDateTime.of(year, month, date, 18,
                min);

        when(flightDao
                .findByRouteInAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        routes, departure, departureHelper, paging))
                                .thenReturn(flightPage);

        Page<Flight> foundFlights = flightService.getFlightsByRouteAndDate(
                pageNo, pageSize, sortBy, routes, flightQuery);
        assertEquals(flightPage, foundFlights);

    }

    @Test
    public void shouldGetFlightsByRouteAndDate_FilterEvening() {

        // params
        Integer pageNo = 0;
        Integer pageSize = 10;
        String sortBy = "id";

        Airport airport1 = new Airport("LAX", "Test City 1", true);
        Airport airport2 = new Airport("JFK", "Test City 2", true);

        Route route1 = new Route(1, airport1, airport2, true);
        List<Route> routes = new ArrayList<Route>();
        routes.add(route1);

        FlightQuery flightQuery = new FlightQuery(3, 16, 2021, "evening");

        // mocks

        String str1 = "2020-09-01 20:01:15";
        String str2 = "2020-09-01 21:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        Flight flight = new Flight();
        flight.setId(101);
        flight.setRoute(route1);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);

        List<Flight> flights = new ArrayList<Flight>();
        flights.add(flight);
        Pageable paging = PageRequest.of(0, 10, Sort.by(sortBy));
        Page<Flight> flightPage = new PageImpl<Flight>(flights);

        Integer month = Integer.valueOf(flightQuery.getMonth());
        Integer date = Integer.valueOf(flightQuery.getDate());
        Integer year = Integer.valueOf(flightQuery.getYear());
        Integer hour = 00;
        Integer min = 00;

        LocalDateTime departure = LocalDateTime.of(year, month, date, 18, min);
        LocalDateTime departureHelper = LocalDateTime.of(year, month, date + 1,
                04, min);

        when(flightDao
                .findByRouteInAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        routes, departure, departureHelper, paging))
                                .thenReturn(flightPage);

        Page<Flight> foundFlights = flightService.getFlightsByRouteAndDate(
                pageNo, pageSize, sortBy, routes, flightQuery);
        assertEquals(flightPage, foundFlights);

    }

    @Test
    public void testUpdateflight() throws FlightNotSavedException {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);

        Flight flight2 = new Flight();
        flight2.setId(101);
        flight2.setRoute(route);
        flight2.setAirplane(airplane);
        flight2.setDepartureTime(departureTime);
        flight2.setArrivalTime(arrivalTime);
        flight2.setFirstReserved(0);
        flight2.setFirstPrice(350.00f);
        flight2.setBusinessReserved(0);
        flight2.setBusinessPrice(300.00f);
        flight2.setEconomyReserved(0);
        flight2.setEconomyPrice(200.00f);
        flight2.setIsActive(false);

        when(flightDao.save(flight)).thenReturn(flight);
        doNothing().when(flightDao).updateFlight(101, flight.getRoute(),
                flight.getAirplane(), flight.getDepartureTime(),
                flight.getArrivalTime(), flight.getFirstReserved(),
                flight.getFirstPrice(), flight.getBusinessReserved(),
                flight.getBusinessPrice(), flight.getEconomyReserved(),
                flight.getEconomyPrice(), flight.getIsActive());

        Integer savedFlightId = flightService.saveFlight(flight);
        Integer updatedFlightId = flightService.updateFlight(101, flight2);

        assertThat(flight.getId(), is(updatedFlightId));
    }

    @Test
    public void testDeleteFlight_NotFound() throws FlightNotSavedException {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);
        String flightMsg = flightService.deleteFlight(101);
        Optional<Flight> flightOpt = Optional.of(flight);

        when(flightService.getFlightById(101)).thenReturn(flightOpt);
        when(flightDao.findById(101)).thenReturn(flightOpt);
        doNothing().when(flightDao).delete(flight);

        assertThat(flightMsg, is("Flight not found!"));
    }

    @Test
    public void testDeleteFlight_FlightFound() throws FlightNotSavedException {
        String str1 = "2020-09-01 09:01:15";
        String str2 = "2020-09-01 11:01:15";
        LocalDateTime departureTime = LocalDateTime.parse(str1, formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(str2, formatter);

        Flight flight = new Flight();
        flight.setId(101);

        Airport originAirport = new Airport("TC1", "Test City 1", true);
        Airport destinationAirport = new Airport("TC2", "Test City 2", true);
        Route route = new Route(1, originAirport, destinationAirport, true);
        Airplane airplane = new Airplane(1l, 100l, 100l, 100l, "Model 1");

        flight.setRoute(route);
        flight.setAirplane(airplane);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setFirstReserved(0);
        flight.setFirstPrice(350.00f);
        flight.setBusinessReserved(0);
        flight.setBusinessPrice(300.00f);
        flight.setEconomyReserved(0);
        flight.setEconomyPrice(200.00f);
        flight.setIsActive(true);

        Optional<Flight> flightOpt = Optional.of(flight);

        when(flightDao.findById(101)).thenReturn(flightOpt);
        doNothing().when(flightDao).delete(flight);

        String deleteMsg = flightService.deleteFlight(101);

        assertThat(deleteMsg, is("Flight Deleted!"));
    }

    void testEmailFlightDetailsToAllBookedUsers() {
        Flight flight = new Flight();
        HashSet<User> users = new HashSet<>();
        users.add(new User(1l, "name", "email", "11111111111"));
        users.add(new User(2l, "name2", "email2", "22222222222"));
        flight.setBookedUsers(users);

        assertDoesNotThrow(() -> {
            flightService.emailFlightDetailsToAllBookedUsers(flight);
        });
    }

    @Test
    public void testSearchFlights_WithGraph() {
        Airport startAirport = new Airport("LAX", "Los Angeles", true);
        Airport middleAirport = new Airport("DFW", "Dallas", true);
        Airport endAirport = new Airport("JFK", "New York", true);

        Route startToMiddleRoute = new Route(1, startAirport, middleAirport,
                true);
        Route middleToEndRoute = new Route(2, middleAirport, endAirport, true);
        Route startToEndRoute = new Route(3, startAirport, endAirport, true);
        Airplane airplane = new Airplane(1L, 100L, 100L, 100L, "Model 1");

        LocalDateTime startToMiddleStartTime = LocalDateTime.of(5, 5, 5, 1, 0);
        LocalDateTime startToMiddleEndTime = LocalDateTime.of(5, 5, 5, 2, 0);

        LocalDateTime middleToEndStartTime = LocalDateTime.of(5, 5, 5, 2, 0);
        LocalDateTime middleToEndEndTime = LocalDateTime.of(5, 5, 5, 3, 0);

        LocalDateTime startToEndStartTime = LocalDateTime.of(5, 5, 5, 3, 0);
        LocalDateTime startToEndEndTime = LocalDateTime.of(5, 5, 5, 4, 0);

        Flight startToMiddleFlight = new Flight();
        startToMiddleFlight.setId(1);
        startToMiddleFlight.setRoute(startToMiddleRoute);
        startToMiddleFlight.setAirplane(airplane);
        startToMiddleFlight.setDepartureTime(startToMiddleStartTime);
        startToMiddleFlight.setArrivalTime(startToMiddleEndTime);
        startToMiddleFlight.setFirstReserved(0);
        startToMiddleFlight.setFirstPrice(350.00f);
        startToMiddleFlight.setBusinessReserved(0);
        startToMiddleFlight.setBusinessPrice(300.00f);
        startToMiddleFlight.setEconomyReserved(0);
        startToMiddleFlight.setEconomyPrice(200.00f);
        startToMiddleFlight.setIsActive(true);

        Flight middleToEndFlight = new Flight();
        middleToEndFlight.setId(2);
        middleToEndFlight.setRoute(middleToEndRoute);
        middleToEndFlight.setAirplane(airplane);
        middleToEndFlight.setDepartureTime(middleToEndStartTime);
        middleToEndFlight.setArrivalTime(middleToEndEndTime);
        middleToEndFlight.setFirstReserved(0);
        middleToEndFlight.setFirstPrice(350.00f);
        middleToEndFlight.setBusinessReserved(0);
        middleToEndFlight.setBusinessPrice(300.00f);
        middleToEndFlight.setEconomyReserved(0);
        middleToEndFlight.setEconomyPrice(200.00f);
        middleToEndFlight.setIsActive(true);

        Flight startToEndFlight = new Flight();
        startToEndFlight.setId(3);
        startToEndFlight.setRoute(startToEndRoute);
        startToEndFlight.setAirplane(airplane);
        startToEndFlight.setDepartureTime(startToEndStartTime);
        startToEndFlight.setArrivalTime(startToEndEndTime);
        startToEndFlight.setFirstReserved(0);
        startToEndFlight.setFirstPrice(350.00f);
        startToEndFlight.setBusinessReserved(0);
        startToEndFlight.setBusinessPrice(300.00f);
        startToEndFlight.setEconomyReserved(0);
        startToEndFlight.setEconomyPrice(200.00f);
        startToEndFlight.setIsActive(true);

        LocalDateTime searchStartTime = LocalDateTime.of(5, 5, 5, 0, 0);
        LocalDateTime searchEndTime = LocalDateTime.of(5, 5, 6, 0, 0);

        Graph<Airport, DefaultEdge> graph = new SimpleDirectedGraph<>(
                DefaultEdge.class);
        GraphWalk<Airport, DefaultEdge> nonStopPath = new GraphWalk<Airport, DefaultEdge>(
                graph, Arrays.asList(startAirport, middleAirport, endAirport),
                0);
        GraphWalk<Airport, DefaultEdge> oneStopPath = new GraphWalk<Airport, DefaultEdge>(
                graph, Arrays.asList(startAirport, endAirport), 0);
        List<GraphPath<Airport, DefaultEdge>> paths = Arrays.asList(nonStopPath,
                oneStopPath);

        when(graphService.getPaths(startAirport, endAirport)).thenReturn(paths);
        when(routeDao.findByOriginAirportAndDestinationAirport(startAirport,
                endAirport)).thenReturn(Optional.of(startToEndRoute));
        when(routeDao.findByOriginAirportAndDestinationAirport(startAirport,
                middleAirport)).thenReturn(Optional.of(startToMiddleRoute));
        when(routeDao.findByOriginAirportAndDestinationAirport(middleAirport,
                endAirport)).thenReturn(Optional.of(middleToEndRoute));
        when(flightDao
                .findByRouteAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        startToMiddleRoute, searchStartTime, searchEndTime))
                                .thenReturn(Arrays.asList(startToMiddleFlight));
        when(flightDao
                .findByRouteAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        middleToEndRoute, startToMiddleEndTime, searchEndTime))
                                .thenReturn(Arrays.asList(middleToEndFlight));
        when(flightDao
                .findByRouteAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        startToEndRoute, searchStartTime, searchEndTime))
                                .thenReturn(Arrays.asList(startToEndFlight));

        List<LinkedList<Flight>> allTrips = flightService
                .searchFlights(startAirport, endAirport, searchStartTime);
        LinkedList<Flight> oneStopTrip = new LinkedList<Flight>();
        oneStopTrip.add(startToMiddleFlight);
        oneStopTrip.add(middleToEndFlight);

        LinkedList<Flight> nonStopTrip = new LinkedList<Flight>();
        nonStopTrip.add(startToEndFlight);

        List<LinkedList<Flight>> mockTrips = Arrays.asList(oneStopTrip,
                nonStopTrip);

        assertThat(allTrips.size(), is(2));
        assertThat(allTrips, is(mockTrips));
    }

    @Test
    public void searchFlights_TwoFlightsWithSameRoute_TwoLinkedLists() {
        Airport startAirport = new Airport("LAX", "Los Angeles", true);
        Airport middleAirport = new Airport("DFW", "Dallas", true);
        Airport endAirport = new Airport("JFK", "New York", true);

        Route startToMiddleRoute = new Route(1, startAirport, middleAirport,
                true);
        Route middleToEndRoute = new Route(2, middleAirport, endAirport, true);
        Route startToEndRoute = new Route(3, startAirport, endAirport, true);
        Airplane airplane = new Airplane(1L, 100L, 100L, 100L, "Model 1");

        LocalDateTime startToMiddleStartTime = LocalDateTime.of(5, 5, 5, 1, 0);
        LocalDateTime startToMiddleEndTime = LocalDateTime.of(5, 5, 5, 2, 0);

        LocalDateTime middleToEndStartTime = LocalDateTime.of(5, 5, 5, 2, 0);
        LocalDateTime middleToEndEndTime = LocalDateTime.of(5, 5, 5, 3, 0);

        Flight startToMiddleFlight = new Flight();
        startToMiddleFlight.setId(1);
        startToMiddleFlight.setRoute(startToMiddleRoute);
        startToMiddleFlight.setAirplane(airplane);
        startToMiddleFlight.setDepartureTime(startToMiddleStartTime);
        startToMiddleFlight.setArrivalTime(startToMiddleEndTime);
        startToMiddleFlight.setFirstReserved(0);
        startToMiddleFlight.setFirstPrice(350.00f);
        startToMiddleFlight.setBusinessReserved(0);
        startToMiddleFlight.setBusinessPrice(300.00f);
        startToMiddleFlight.setEconomyReserved(0);
        startToMiddleFlight.setEconomyPrice(200.00f);
        startToMiddleFlight.setIsActive(true);

        Flight startToMiddleFlight2 = new Flight();
        startToMiddleFlight2.setId(4);
        startToMiddleFlight2.setRoute(startToMiddleRoute);
        startToMiddleFlight2.setAirplane(airplane);
        startToMiddleFlight2.setDepartureTime(startToMiddleStartTime);
        startToMiddleFlight2.setArrivalTime(startToMiddleEndTime);
        startToMiddleFlight2.setFirstReserved(0);
        startToMiddleFlight2.setFirstPrice(350.00f);
        startToMiddleFlight2.setBusinessReserved(0);
        startToMiddleFlight2.setBusinessPrice(300.00f);
        startToMiddleFlight2.setEconomyReserved(0);
        startToMiddleFlight2.setEconomyPrice(200.00f);
        startToMiddleFlight2.setIsActive(true);

        Flight middleToEndFlight = new Flight();
        middleToEndFlight.setId(2);
        middleToEndFlight.setRoute(middleToEndRoute);
        middleToEndFlight.setAirplane(airplane);
        middleToEndFlight.setDepartureTime(middleToEndStartTime);
        middleToEndFlight.setArrivalTime(middleToEndEndTime);
        middleToEndFlight.setFirstReserved(0);
        middleToEndFlight.setFirstPrice(350.00f);
        middleToEndFlight.setBusinessReserved(0);
        middleToEndFlight.setBusinessPrice(300.00f);
        middleToEndFlight.setEconomyReserved(0);
        middleToEndFlight.setEconomyPrice(200.00f);
        middleToEndFlight.setIsActive(true);

        LocalDateTime searchStartTime = LocalDateTime.of(5, 5, 5, 0, 0);
        LocalDateTime searchEndTime = LocalDateTime.of(5, 5, 6, 0, 0);

        Graph<Airport, DefaultEdge> graph = new SimpleDirectedGraph<>(
                DefaultEdge.class);
        GraphWalk<Airport, DefaultEdge> nonStopPath = new GraphWalk<Airport, DefaultEdge>(
                graph, Arrays.asList(startAirport, middleAirport, endAirport),
                0);
        GraphWalk<Airport, DefaultEdge> oneStopPath = new GraphWalk<Airport, DefaultEdge>(
                graph, Arrays.asList(startAirport, endAirport), 0);
        List<GraphPath<Airport, DefaultEdge>> paths = Arrays.asList(nonStopPath,
                oneStopPath);

        when(graphService.getPaths(startAirport, endAirport)).thenReturn(paths);
        when(routeDao.findByOriginAirportAndDestinationAirport(startAirport,
                endAirport)).thenReturn(Optional.of(startToEndRoute));
        when(routeDao.findByOriginAirportAndDestinationAirport(startAirport,
                middleAirport)).thenReturn(Optional.of(startToMiddleRoute));
        when(routeDao.findByOriginAirportAndDestinationAirport(middleAirport,
                endAirport)).thenReturn(Optional.of(middleToEndRoute));
        when(flightDao
                .findByRouteAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        startToMiddleRoute, searchStartTime, searchEndTime))
                                .thenReturn(Arrays.asList(startToMiddleFlight,
                                        startToMiddleFlight2));
        when(flightDao
                .findByRouteAndDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                        middleToEndRoute, startToMiddleEndTime, searchEndTime))
                                .thenReturn(Arrays.asList(middleToEndFlight));

        List<LinkedList<Flight>> allTrips = flightService
                .searchFlights(startAirport, endAirport, searchStartTime);
        LinkedList<Flight> oneStopTrip = new LinkedList<Flight>();
        oneStopTrip.add(startToMiddleFlight);
        oneStopTrip.add(middleToEndFlight);

        LinkedList<Flight> oneStopTrip2 = new LinkedList<Flight>();
        oneStopTrip2.add(startToMiddleFlight2);
        oneStopTrip2.add(middleToEndFlight);

        List<LinkedList<Flight>> mockTrips = Arrays.asList(oneStopTrip,
                oneStopTrip2);

        assertThat(allTrips.size(), is(2));
        assertThat(allTrips, is(mockTrips));
    }
}
