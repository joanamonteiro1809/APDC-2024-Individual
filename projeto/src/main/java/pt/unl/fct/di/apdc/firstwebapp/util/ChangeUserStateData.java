package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeUserStateData {

	public String username;
	public String password;
	public String newstate;

	public ChangeUserStateData(String username, String password, String newstate) {
		this.username = username;
		this.password = password;
		this.newstate = newstate;

	}

	public ChangeUserStateData() {

	}
}
