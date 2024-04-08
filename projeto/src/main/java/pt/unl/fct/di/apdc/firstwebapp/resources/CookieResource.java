package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

@Path("/getCookie")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class CookieResource {
	private final Gson g = new Gson();

	public CookieResource() {

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCookie(@CookieParam("session::apdc") Cookie cookie) {

		if (cookie == null || cookie.getValue() == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access ").build();
		}

		return Response.ok(g.toJson(cookie)).build();

	}

}
