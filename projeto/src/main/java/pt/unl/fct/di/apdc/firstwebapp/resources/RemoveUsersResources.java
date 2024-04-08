package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

@Path("/removeUser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveUsersResources {
	public static Map<String, UserData> users = new HashMap<String, UserData>();

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	private static final String INACTIVE = "INACTIVE";
	private static final String ACTIVE = "ACTIVE";
	private static final String SU = "SU";
	private static final String GA = "GA";
	private static final String GBO = "GBO";
	private static final String USER = "USER";

	private final Gson g = new Gson();

	public RemoveUsersResources() {

	}

	@POST
	@Path("/")
	public Response removeUser(LoginData data, @CookieParam("session::apdc") Cookie cookie) {
		if (cookie == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = userKeyFactory.newKey(data.username);
			Entity user = txn.get(userKey);
			String value = cookie.getValue();
			String[] values = value.split("\\.");
			
			if (values[5].equals("false"))
				return Response.status(Status.FORBIDDEN).entity("There is no account logged in to perform this").build();

			if (user != null) {
				String curRole = user.getString("role");
				String username = user.getString("username");

				if (checkPermissions(cookie, curRole, username)) {
					txn.delete(userKey);
					txn.commit();
					return Response.ok("User '" + data.username + "' removed successfully.").build();
				} else {
					return Response.status(Status.FORBIDDEN).entity("You don't have permissions to remove users.")
							.build();
				}
			} else {
				return Response.status(Status.NOT_FOUND).entity("User not found.").build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	public static boolean checkPermissions(Cookie cookie, String role, String username) {
		if (cookie == null || cookie.getValue() == null) {
			return false;
		}

		String value = cookie.getValue();
		String[] values = value.split("\\.");

		String signatureNew = SignatureUtils.calculateHMac(key,
				values[0] + "." + values[1] + "." + values[2] + "." + values[3] + "." + values[4] + "." +  values[5]);
		System.out.println(values[0]);
		String signatureOld = values[6];

		if (!signatureNew.equals(signatureOld) || values[2].equals(GBO) || (values[2].equals(GA) && role.equals(GA))
				|| (values[2].equals(GBO) && role.equals(GBO))
				|| (!values[0].equals(username) && values[2].equals(USER))) {
			return false;
		}

		int neededRole = convertRole(role); // input
		int userInSessionRole = convertRole(values[2]); // oq ele já tem

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
			result = 3;
			break;
		case GA:
			result = 2;
			break;
		case GBO:
			result = 0;
			break;
		case USER:
			result = 1;
			break;
		default:
			result = -1;
			break;
		}
		return result;
	}

}
