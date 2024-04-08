package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
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

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.*;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.gson.Gson;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.appengine.repackaged.com.google.protobuf.StringValue;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Entity.Builder;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

@Path("/changeRole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeUserRoleResource {

	public static Map<String, UserData> users = new HashMap<String, UserData>();

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	private static final String SU = "SU";
	private static final String GA = "GA";
	private static final String GBO = "GBO";
	private static final String USER = "USER";
	private final Gson g = new Gson();

	public ChangeUserRoleResource() {

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeUserRole(ChangeRoleData data, @CookieParam("session::apdc") Cookie cookie) {
		if (cookie == null || cookie.getValue() == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access ").build();
		}

		LOG.fine("Attempt to change user role: " + data.username);
		Transaction txn = datastore.newTransaction();

		try {
			Key userKey1 = userKeyFactory.newKey(data.username);
			Entity user = txn.get(userKey1);
			
			String value = cookie.getValue();
			String[] values = value.split("\\.");
			
			if (values[5].equals("false"))
				return Response.status(Status.FORBIDDEN).entity("There is no account logged in to perform this").build();

			if (user != null) {
				String curRole = user.getString("role"); // USER
				if (checkPermissions(cookie, data.newRole) && checkPermissions(cookie, curRole)) {
					Entity.Builder userB = Entity.newBuilder(user);
					userB.set("role", data.newRole);
					Entity userUpdate = userB.build();
					txn.update(userUpdate);
					txn.commit();
					return Response.ok("Role changed successfully.").build();

				} else {
					txn.rollback();
					return Response.status(Status.FORBIDDEN).entity("You don't have permissions to change roles")
							.build();
				}
			} else {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("User not found").build();
			}

		} catch (Exception e) {
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("error!").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}

	}

	public static boolean checkPermissions(Cookie cookie, String role) { // cookie -- pessoa que está a mexer a conta e
																			// o role a conta de quem está a ter a conta
																			// mexida
		if (cookie == null || cookie.getValue() == null) {
			return false;
		}

		String value = cookie.getValue();
		String[] values = value.split("\\.");

		String signatureNew = SignatureUtils.calculateHMac(key,
				values[0] + "." + values[1] + "." + values[2] + "." + values[3] + "." + values[4] + "." + values[5]);
		String signatureOld = values[6];

		if (!signatureNew.equals(signatureOld) || values[2].equals(USER) || values[2].equals(GBO)) {
			return false;
		}

		int neededRole = convertRole(role);
		int userInSessionRole = convertRole(values[2]);

		if (userInSessionRole < neededRole) {
			return false;
		}

		if (System.currentTimeMillis() > (Long.valueOf(values[3]) + Long.valueOf(values[4]) * 1000)) {

			return false;
		}

		return true;
	}

	private static int convertRole(String role) {
		int result = 0;

		switch (role) {
		case SU:
			result = 2;
			break;
		case GA:
			result = 1;
			break;
		case GBO:
			result = 0;
			break;
		case USER:
			result = 0;
			break;
		default:
			result = -1;
			break;
		}
		return result;
	}

}
