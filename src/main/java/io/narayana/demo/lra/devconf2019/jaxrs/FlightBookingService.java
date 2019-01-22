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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.narayana.demo.lra.devconf2019.BookingManager;
import io.narayana.demo.lra.devconf2019.FlightManager;
import io.narayana.demo.lra.devconf2019.jpa.Booking;
import io.narayana.demo.lra.devconf2019.jpa.Flight;


@Path("/book")
public class FlightBookingService {
    private static final Logger log = Logger.getLogger(FlightBookingService.class);

    @Inject
    private BookingManager bookingManager;

    @Inject
    private FlightManager flightManager;

    @POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response book(String jsonData) {
	    Map<String,String> jsonMap = parseJson(jsonData);

	    if(jsonMap.get("date") == null || jsonMap.get("name") == null) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(String.format("Invalid format of json data '%s' as does not contain fields 'date' and/or 'name'", jsonData))
                    .type("text/plain").build());
	    }

        Date parsedDate = FlightManagementService.parseDate(jsonMap.get("date"));
        List<Flight> foundFlights = flightManager.findByDate(parsedDate);
        if(foundFlights == null || foundFlights.isEmpty()) {
            throw new NotFoundException(String.format("No flight at date '%s' is available", parsedDate));
        }

        Optional<Flight> matchingFlight = foundFlights.stream()
                .filter(f -> f.getNumberOfSeats() > f.getBookedSeats()).findFirst();
        if(!matchingFlight.isPresent()) {
            throw new NotFoundException("There is no flight which would not be already occupied at the date " + parsedDate);
        }

        Booking booking = new Booking()
                .setFlight(matchingFlight.get())
                .setName(jsonMap.get("name"));
        bookingManager.save(booking);
		return Response.ok().entity(booking.getId()).build();
	}

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        List<Booking> allBookings = bookingManager.getAllBookings();
        if(log.isDebugEnabled()) log.debugf("All flights: %s", allBookings);
        return Response.ok().entity(allBookings).build();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        // service.get(lraId).setStatus(Booking.BookingStatus.CONFIRMED);
        // return Response.ok(service.get(lraId).toJson()).build();
        return Response.ok().build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        // service.get(lraId).setStatus(Booking.BookingStatus.CANCELLED);
        // return Response.ok(service.get(lraId).toJson()).build();
        return Response.ok().build();
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
}
