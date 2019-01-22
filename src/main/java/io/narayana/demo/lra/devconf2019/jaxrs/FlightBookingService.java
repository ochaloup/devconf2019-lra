package io.narayana.demo.lra.devconf2019.jaxrs;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("/")
public class FlightBookingService {

    @Path("get")
	@GET
	@Produces("text/plain")
	public Response doGet() {
		return Response.ok("Hello from Thorntail!").build();
	}
}
