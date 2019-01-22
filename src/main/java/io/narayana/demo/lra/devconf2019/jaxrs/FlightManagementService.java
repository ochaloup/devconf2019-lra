package io.narayana.demo.lra.devconf2019.jaxrs;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.narayana.demo.lra.devconf2019.FlightManagement;
import io.narayana.demo.lra.devconf2019.jpa.Flight;

@Path("/flights")
public class FlightManagementService {
    private static final Logger log = Logger.getLogger(FlightManagement.class);

    @Inject
    private FlightManagement management;

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        List<Flight> flights = management.getAllFlights();
        if(log.isDebugEnabled()) log.debugf("All flights: %s", flights);
        return Response.ok().entity(flights).build();
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFlight(@PathParam("id") String flightId) throws NotFoundException {
        int id;
        try {
            id = Integer.parseInt(flightId);
        } catch (NumberFormatException nfe) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
            .entity(String.format("The path parameter '%s' of flight id can't be converted to number",
                    flightId)).build();
        }
        Flight flight = management.find(id);
        if(flight == null) {
            throw new NotFoundException(String.format("Cannot find a flight with id '%d'", id));
        }
        log.debugf("On requested id '%s' found a flight '%s'", id, flight);
        return Response.ok().entity(flight).build();
    }

}
