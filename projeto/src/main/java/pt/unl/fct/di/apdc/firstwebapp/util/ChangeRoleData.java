package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeRoleData {
	public String username;
	public String password;
	public String role;
	public String newRole;

	public ChangeRoleData(String username, String password, String role, String newRole) {
		this.username = username;
		this.password = password;
		this.role = role;
		this.newRole = newRole;

	}

	public ChangeRoleData() {

	}

}
