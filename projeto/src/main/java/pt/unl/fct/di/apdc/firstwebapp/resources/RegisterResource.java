package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/do")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private int num_account = 0;

	public RegisterResource() { // criar classe change role

	}

	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistrationV2(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Missing or wrong parameter." + "Password must start with a capital letter").build();
		} else if(num_account == 4) {
			return Response.status(Status.BAD_REQUEST).entity("There are already 4 accounts registered.").build();
		} 

		Transaction txn = datastore.newTransaction();
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

		try {
			Entity user = txn.get(userKey);
			if (user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				num_account++;
				// while (!data.state.equals("INATIVO")) {
				user = Entity.newBuilder(userKey).set("username", data.username).set("email", data.email)
						.set("name", data.name).set("phone", data.phone)
						.set("password", DigestUtils.sha512Hex(data.password)).set("state", "INACTIVE")
						.set("viewState", data.viewState).set("role", "USER").set("ocupacao", data.ocupacao)
						.set("localTrabalho", data.localTrabalho).set("morada", data.morada).set("cp", data.cp)
						.set("nif", data.nif).set("user_creation_time", Timestamp.now()).build();
				txn.add(user);
				txn.commit();
				LOG.info("User registered" + data.username);
				// }

				return Response.ok("{}").build();
			}

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}

		}

	}
	
	@POST
	@Path("/logout")
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
		num_account--;
		return Response.ok().cookie(cookie1).build();
		
	    }

}
