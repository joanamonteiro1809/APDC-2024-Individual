package pt.unl.fct.di.apdc.firstwebapp.util;

public class LoginData {

	public String username;
	public String password;
	public String email;
	public String name;
	public String passwordConf;
	public String role;
	public String state;
	public String phone;


	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public LoginData() {
		
	}

	public boolean validLogin() { // ver isto
		// TODO Auto-generated method stub
		return (!username.isEmpty() && !email.isEmpty() && !name.isEmpty() && !phone.isEmpty() && !password.isEmpty());
	}

}
