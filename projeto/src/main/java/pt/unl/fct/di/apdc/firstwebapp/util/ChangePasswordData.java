package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
	public String username, oldPwd, newPwd, confirmPwd;
	
	public ChangePasswordData() {
		// TODO Auto-generated constructor stub
	}
	
	public ChangePasswordData(String username, String oldPwd , String confirmPwd, String newPwd) {
		// TODO Auto-generated constructor stub
		this.username = username;
		this.oldPwd = oldPwd;
		this.confirmPwd = confirmPwd;
		this.newPwd = newPwd;
	}
}