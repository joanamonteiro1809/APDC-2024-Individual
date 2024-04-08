package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeUserAttributesData {
	public String username, email,  name,  phone,  password,  role,  state ;
	
	public ChangeUserAttributesData() {
		
	}
	
	public ChangeUserAttributesData(String username, String email, String name, String phone, String password, String role, String state ) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.phone = phone;
		this.password = password;
		this.role = role;
		this.state = state;
	}



}
