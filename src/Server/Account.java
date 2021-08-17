package Server;

import java.io.File;
import java.net.URL;
import java.util.Scanner;

public class Account {
	static String user = "";
	static String account = "";
	static String password = "";
	String superUsername = "super";
	int userFoundInLine;
	static String filepath = System.getProperty("user.dir") + "\\src\\server\\data.txt";
	boolean loggedIn = false;
	boolean correctPassword = false;

	private static Scanner x;

	public boolean isLoggedIn(String user) {
		if(this.user.equals(user)) {
			return loggedIn;
		}
		else {
			return false;
		}
	}
	
	public boolean passwordEntered() {
		if(correctPassword) {
			return true;
		}
		return false;
	}

	public boolean alreadyInAccount() {
		if(account == ""){
			return false;
		}
		return true;
	}

	public boolean validPassword(String password) {
		boolean found = false;
		try {
			String tempPassword = "";
			x = new Scanner(new File(filepath));
			x.useDelimiter("[,\n]");

			int count = 0;
			while(count<userFoundInLine) {
				x.nextLine();
				count++;
			}
			x.next();
			x.next();
			tempPassword = x.next();

			if(tempPassword.trim().equals(password.trim())) {
				correctPassword = true;
				found = true; 
			}

			x.close();			
		}catch(Exception e) {
			System.out.println("Error");
		}
		return found;
	}

	public boolean validAccount(String account) {
		boolean found = false;
		try {
			String tempAccount = "";
			x = new Scanner(new File(filepath));
			x.useDelimiter("[,\n]");
			int count = 0;
			while(count<userFoundInLine) {
				x.nextLine();
				count++;
			}
			x.next();
			tempAccount = x.next();
			x.next();

			if(tempAccount.trim().equals(account.trim())) {
				this.account = account;
				found = true; 
			}
			x.close();

		}catch(Exception e) {
			System.out.println("Error");
		}
		return found;
	}

	public boolean validUser(String user) {
		boolean found = false;
		boolean superUser = false;
		userFoundInLine = 0;
		this.account = "";
		this.user = "";
		this.password = "";
		
		try {
			String tempUser = "";
			x = new Scanner(new File(filepath));
			x.useDelimiter("[,\n]");

			while(x.hasNext() && !found) {
				tempUser = x.next();
				x.next();
				x.next();
				if(tempUser.trim().equals(user.trim())) {
					found = true; 
					if(tempUser.trim().equals(superUsername)) {
						superUser = true;
					}
				}
				else {
					userFoundInLine = userFoundInLine + 1 ;
				}
			}

			if(superUser == true) {
				this.user = superUsername;
				loggedIn = true;
			}
			x.close();

		}catch(Exception e) {
			System.out.println("Error");
		}
		return found;
	}
}
