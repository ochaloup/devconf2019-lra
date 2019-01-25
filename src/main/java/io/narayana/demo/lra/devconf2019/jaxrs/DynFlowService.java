package io.narayana.demo.lra.devconf2019.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.narayana.demo.lra.devconf2019.BookingManager;
import io.narayana.demo.lra.devconf2019.FlightManager;
import io.narayana.demo.lra.devconf2019.jpa.Booking;
import io.narayana.demo.lra.devconf2019.jpa.BookingStatus;
import io.narayana.demo.lra.devconf2019.jpa.Flight;

@Path("/dynflow")
public class DynFlowService {
    private static final Logger log = Logger.getLogger(DynFlowService.class);
    private static volatile int nameCounter = 1;

    @Inject
    private BookingManager bookingManager;

    @Inject
    private FlightManager flightManager;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response book() {
        Flight flight = flightManager.getByDate(FlightManagementService.parseDate("2019-01-27")).get(0);
        Booking booking = new Booking()
                .setFlight(flight)
                .setName("The great guy " + (nameCounter++));
        bookingManager.save(booking);
        log.infof("Created booking: '%s'", booking);

        return Response.ok(booking).build();
    }

    @POST
    @Path("/{id}/compensate")
    @Produces(MediaType.TEXT_PLAIN)
    public Response compensate(@PathParam("id") String bookingId) {
        int id = Integer.parseInt(bookingId);
        Booking booking = bookingManager.get(id);
        log.infof("Compensating booking with id '%s' of id '%d'", booking, id);
        booking.setStatus(BookingStatus.CANCELED);
        bookingManager.update(booking);
        return Response.ok("compensated").build();
    }
}
