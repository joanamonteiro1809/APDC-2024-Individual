package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/change")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePasswordResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	public ChangePasswordResource() {

	}

	@POST
	@Path("/passWord")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(ChangePasswordData data, @CookieParam("session::apdc") Cookie cookie) {
		LOG.fine("Changing password attempt: ");
		Key userKey = userKeyFactory.newKey(data.username);
		Transaction txn = datastore.newTransaction();
		try {
			String value = cookie.getValue();
			String[] values = value.split("\\.");
			
			if (values[5].equals("false"))
				return Response.status(Status.FORBIDDEN).entity("There is no account logged in to perform this").build();
			Entity user = txn.get(userKey);
			if (user == null) {
				LOG.warning("User is not in session.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}

			String hashedOriginalPWD = (String) user.getString("password");
            if(!hashedOriginalPWD.equals(DigestUtils.sha512Hex(data.oldPwd))) {
				LOG.warning("Wrong password 1.");
				return Response.status(Status.FORBIDDEN).build();
			} else if (!data.newPwd.equals(data.confirmPwd)) { // por causa desta linha
				LOG.warning("Wrong password 2.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			} else {
				user = Entity.newBuilder(user).set("password", DigestUtils.sha512Hex(data.newPwd)).build();
				txn.update(user);
				txn.commit();
				return Response.ok(g.toJson(user)).build();
			}
		} catch (Exception e) {
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}

	}

	private boolean checkPermissions(ChangePasswordData data, Cookie cookie) {
		if (cookie == null || cookie.getValue() == null) {
			return false;
		}

		String value = cookie.getValue();
		String[] values = value.split("\\.");

		if (!values[0].equals(data.username)) {
			return false;
		}

		return true;
	}

}
