package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	public String username;

	public String email;

	public String name;

	public String phone;

	public String password;
	public String passwordConf;
	public String role;
	public String state;
	public String viewState;
	public String ocupacao;
	public String localTrabalho;
	public String morada;
	public String cp;
	public String nif;

	public RegisterData(String username, String email, String name, String phone, String password, String passwordConf,
			String viewState, String ocupacao, String localTrabalho, String morada, String cp, String nif) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.phone = phone;
		this.password = password;
		this.passwordConf = passwordConf;
		role = "USER";
		state = "INACTIVE";
		this.viewState = viewState;
		this.ocupacao = ocupacao;
		this.localTrabalho = localTrabalho;
		this.morada = morada;
		this.cp = cp;
		this.nif = nif;

	}

	public RegisterData() {

	}

	public boolean validRegistration() {
		return (validPassword() && !username.isEmpty() && !email.isEmpty() && !name.isEmpty() && !phone.isEmpty()
				&& !password.isEmpty());
	}

	public boolean validPassword() {
		return (password.equals(passwordConf) && Character.isUpperCase(password.charAt(0)));
	}

	public String getState() {
		return this.state;
	}

}
