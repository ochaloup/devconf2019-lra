package io.narayana.demo.lra.devconf2019.jaxrs;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.narayana.demo.lra.devconf2019.BookingManager;
import io.narayana.demo.lra.devconf2019.FlightManager;
import io.narayana.demo.lra.devconf2019.jpa.Booking;
import io.narayana.demo.lra.devconf2019.jpa.BookingStatus;
import io.narayana.demo.lra.devconf2019.jpa.Flight;


@Path("/book")
public class FlightBookingService {
    private static final Logger log = Logger.getLogger(FlightBookingService.class);
    private static volatile int counter = 1;

    @Inject
    private BookingManager bookingManager;

    @Inject
    private FlightManager flightManager;

    @Inject
    private LRAClient lraClient;

    @Inject @ConfigProperty(name = "target.call", defaultValue = "")
    private String targetCallConfig;


    @LRA(cancelOn = {Status.EXPECTATION_FAILED, Status.NOT_FOUND})
    @POST
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response book(String jsonData) {
        log.infof("Making booking with LRA id '%s' and calling '%s'",
                lraClient.getCurrent().toExternalForm(), targetCallConfig);

        String dateToFind = "2019-01-27";
        Flight matchingFlight = flightManager
                .getByDate(FlightManagementService.parseDate(dateToFind)).get(0);

        return processBooking(matchingFlight,
                "The great guy " + (counter++), Optional.ofNullable(targetCallConfig));
    }

    @LRA(cancelOn = Status.NOT_FOUND)
    @POST
    @Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response bookWithData(String jsonData) {
        log.infof("Booking with data '%s' as part of LRA id '%s'",
                jsonData, lraClient.getCurrent().toExternalForm());

        Map<String,String> jsonMap = parseJson(jsonData);
        Flight matchingFlight = findMatchingFlightForBooking(jsonMap);

        String targetCallFromJson = jsonMap.get("target.call");

        return processBooking(matchingFlight,
                jsonMap.get("name"), Optional.ofNullable(targetCallFromJson));
	}

    public Response processBooking(Flight matchingFlight, String name, Optional<String> targetCall) {
        // save booking
        Booking booking = new Booking()
                .setFlight(matchingFlight)
                .setName(name)
                .setStatus(BookingStatus.IN_PROGRESS)
                .setLraId(lraClient.getCurrent().toExternalForm());
        bookingManager.save(booking);
        log.infof("Booking '%s' was created", booking);

        // calling next service
        if(targetCall.isPresent()) {
            Response response = ClientBuilder.newClient().target(targetCall.get())
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.text("book the hotel for: " + booking.getName()));

            if(response.getStatus() != Status.OK.getStatusCode()) {
                throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity(String.format("Call to %s failed", targetCall))
                        .type("text/plain").build());
            }
        }

        return Response.ok(booking.getId()).build();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        log.info("Completing...");
        boolean wasBooked = confirmBooking(lraId);
        log.infof("LRA ID '%s' was completed", lraId);
        CompensatorStatus completeStatus = wasBooked ? CompensatorStatus.Completed : CompensatorStatus.FailedToComplete;
        return Response.ok(completeStatus.name()).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        log.info("Compensating...");
        undoBooking(lraId);
        log.warnf("LRA ID '%s' was compensated", lraId);
        return Response.ok().build();
    }

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        List<Booking> allBookings = bookingManager.getAllBookings();
        if(log.isDebugEnabled()) log.debugf("All flights: %s", allBookings);
        return Response.ok().entity(allBookings).build();
    }

    @SuppressWarnings("unchecked")
    private Map<String,String> parseJson(String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String,String> jsonMap = objectMapper.readValue(jsonData, HashMap.class);
            if(log.isDebugEnabled())
                log.debugf("The incoming body '%s' was parsed for JSON format '%s'", jsonData, jsonMap);
            return jsonMap;
        } catch (IOException ioe) {
            throw new WebApplicationException(ioe, Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(String.format("Cannot parse the provided body '%s' to JSON format", jsonData))
                    .type("text/plain").build());
        }
    }

    private Flight findMatchingFlightForBooking(Map<String,String> jsonMap) {
        if(jsonMap.get("date") == null || jsonMap.get("name") == null) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(String.format("Invalid format of json data '%s' as does not contain fields 'date' and/or 'name'", jsonMap))
                    .type("text/plain").build());
        }

        Date parsedDate = FlightManagementService.parseDate(jsonMap.get("date"));
        List<Flight> foundFlights = flightManager.getByDate(parsedDate);
        if(foundFlights == null || foundFlights.isEmpty()) {
            log.errorf("No flight at date '%s' is available", parsedDate);
            throw new NotFoundException(String.format("No flight at date '%s' is available", parsedDate));
        }

        Optional<Flight> matchingFlight = foundFlights.stream()
                .filter(f -> f.getNumberOfSeats() > f.getBookedSeats()).findFirst();
        if(!matchingFlight.isPresent()) {
            log.errorf("There is no flight which would not be already occupied at the date '%s'", parsedDate);
            throw new NotFoundException("There is no flight which would not be already occupied at the date " + parsedDate);
        }

        return matchingFlight.get();
    }

    private boolean confirmBooking(String lraId) {
        boolean wasSuccesful = true;
        List<Booking> byLraBookings = bookingManager.getByLraId(lraId);
        for(Booking booking: byLraBookings) {
            booking.setStatus(BookingStatus.BOOKED);
            int bookedSeats = booking.getFlight().getBookedSeats();
            int availableSeats = booking.getFlight().getNumberOfSeats();

            if(bookedSeats + 1 > availableSeats) {
                log.errorf("Cannot finish booking '%s' for LRA '%s'. The flight '%s' is already full.",
                        booking, lraId, booking.getFlight());
                wasSuccesful = false;
            }

            flightManager.update(booking.getFlight().setBookedSeats(bookedSeats + 1));
            bookingManager.update(booking);
            log.infof("Confirmed booking: '%s'", booking);
        }
        return wasSuccesful;
    }

    private void undoBooking(String lraId) {
        List<Booking> byLraBookings = bookingManager.getByLraId(lraId);
        for(Booking booking: byLraBookings) {
            booking.setStatus(BookingStatus.CANCELED);
            bookingManager.update(booking);
            log.infof("Undone booking: '%s'", booking);
        }
    }
}
