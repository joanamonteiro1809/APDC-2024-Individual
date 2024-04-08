package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");


	public LogoutResource() {

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@CookieParam("session::apdc") Cookie cookie) {
		
		String value = cookie.getValue();
		String[] values = value.split("\\.");
		String username = values[0];
		Key userKey = userKeyFactory.newKey(username);
		Entity user = datastore.get(userKey);
		String id = UUID.randomUUID().toString();
		long currentTime = System.currentTimeMillis();
		boolean active = false;
		String fields = username + "." + id + "." + user.getString("role") + "." + currentTime + "." + 1000 * 60 * 60 * 2 + "." + active;

		String signature = SignatureUtils.calculateHMac(key, fields);
		

		value = fields + "." + signature;
		NewCookie cookie1 = new NewCookie("session::apdc", value, "/", null, "comment", 1000 * 60 * 60 * 2, false,
				true);
		return Response.ok().cookie(cookie1).build();
		
	    }
	}


