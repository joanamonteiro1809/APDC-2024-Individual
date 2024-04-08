package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.*;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	// Settings that must be in the database
	public static final String ADMIN = "Admin";
	public static final String BACKOFFICE = "Backoffice";
	public static final String REGULAR = "Regular";
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	public static Map<String, UserData> users = new HashMap<String, UserData>();
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	public LoginResource() {

	}

	@SuppressWarnings("unused")
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		Key userKey = userKeyFactory.newKey(data.username);
		Entity user = datastore.get(userKey);
		LOG.fine("Login attempt by user: " + data.username);

		if (user != null) {
			String hashedPWD = (String) user.getString("password");
			if (!hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
			} else if (user.getString("viewState").equals("PRIVATE")) {
				return Response.status(Status.FORBIDDEN)
						.entity("Your view state is private. You must have a public view state to login").build();
			}
			String id = UUID.randomUUID().toString();
			long currentTime = System.currentTimeMillis();
			boolean active = true;
			String fields = data.username + "." + id + "." + user.getString("role") + "." + currentTime + "."
					+ 1000 * 60 * 60 * 2 + "." + active;

			String signature = SignatureUtils.calculateHMac(key, fields);
			if (signature == null) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while signing token. See logs.")
						.build();
			}

			String value = fields + "." + signature;
			NewCookie cookie = new NewCookie("session::apdc", value, "/", null, "comment", 1000 * 60 * 60 * 2, false,
					true);
			return Response.ok(g.toJson(cookie)).cookie(cookie).build();
		} else {
			LOG.warning("Wrong password for: " + data.username);
			return Response.status(Status.FORBIDDEN).entity("ERRO").build();
		}

	}

	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		UserData user = users.get(username);

		if (user != null) {
			return Response.ok().entity(g.toJson(false)).build();
		}

		return Response.ok().entity(g.toJson(true)).build();
	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(UserData data) {
		LOG.fine("Attempting to create user with username: " + data.username);

		UserData user = users.get(data.username);

		if (user != null) {
			return Response.status(Status.FORBIDDEN).entity("User with username " + data.username + " already exists.")
					.build();
		}

		users.put(data.username, data);

		return Response.ok().build();
	}

}
