package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeUserAttributesData;

@Path("/listUsers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class listUsersResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	private static final String SU = "SU";
	private static final String GA = "GA";
	private static final String GBO = "GBO";
	private static final String USER = "USER";
	private final Gson g = new Gson();

	public listUsersResource() {

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listUsers(@CookieParam("session::apdc") Cookie cookie) {
		if (cookie == null || cookie.getValue() == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access ").build();
		}
		Transaction txn = datastore.newTransaction();
		try {

			String value = cookie.getValue();
			String[] values = value.split("\\.");

			String username = values[0];

			String role = values[2];
			if (values[5].equals("false"))
				return Response.status(Status.FORBIDDEN).entity("There is no account logged in to perform this")
						.build();

			Query<Entity> query;
			if (role.equals(USER)) {
				query = Query.newEntityQueryBuilder().setKind("User")
						.setFilter(CompositeFilter.and(PropertyFilter.eq("state", "ACTIVE"),
								PropertyFilter.eq("role", "USER"), PropertyFilter.eq("viewState", "PUBLIC")))
						.build();
			} else if (role.equals(GBO)) {
				query = Query.newEntityQueryBuilder().setKind("User").setFilter(PropertyFilter.eq("role", "USER"))
						.build();
			} else if (role.equals(GA)) {
				query = Query.newEntityQueryBuilder().setKind("User").setFilter(PropertyFilter.neq("role", "SU"))
						.build();
			} else {
				query = Query.newEntityQueryBuilder().setKind("User").build();
			}

			QueryResults<Entity> users = datastore.run(query);
			List<Entity> usersList = new ArrayList();

			if (role.equals(USER)) {
				List<String> paramList = new ArrayList<String>();
				users.forEachRemaining(entity -> {
					paramList.add(entity.getString("username"));
					paramList.add(entity.getString("email"));
					paramList.add(entity.getString("name"));
				});
				txn.commit();
				return Response.ok(g.toJson(paramList)).build();
			}

			users.forEachRemaining(Entity -> {
				usersList.add(Entity);
			});
			txn.commit();
			return Response.ok(g.toJson(usersList)).build();

		} catch (Exception e) {
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("error!").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
