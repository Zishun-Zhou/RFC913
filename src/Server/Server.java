package Server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class Server {

	static DataOutputStream  outToClient;
	static Account account = new Account();
	static boolean loggedIn = false;


	public static String currentDir() {
		return System.getProperty("user.directory");
	}

	public static void clientPrinter(String message) throws IOException {
		try {
			outToClient.writeBytes(message+"\n");
			outToClient.flush();
		}catch (Exception e) {
			System.out.println("Error");
		}
	}

	private static void handleUser(String fullCommand) throws IOException {
		String user = fullCommand.substring(5) ;
		loggedIn = false;
		if(account.validUser(user)) {
			if(account.isLoggedIn(user)) {
				loggedIn = true;
				clientPrinter("!"+ user + " logged in");
			}
			else {
				clientPrinter("+User id valid, send account and password");
			}
		}
		else {
			clientPrinter("-Invalid user id, check and try another one");
		}
	}

	private static void handleAcct(String fullCommand) throws IOException {
		String accountName = fullCommand.substring(4);
		if(account.validAccount(accountName)) {
			clientPrinter("+Valid account, send password");
		}
		else {
			clientPrinter("Invalid account, check and try another one");
		}	
	}

	private static void handlePass(String fullCommand) throws IOException {
		String password = fullCommand.substring(4);
		if(account.alreadyInAccount() && account.validPassword(password)){
			loggedIn = true;
			clientPrinter("! Logged in");
		}
		else if(!account.alreadyInAccount() && account.validPassword(password)) {
			clientPrinter("Password is valid but the account is not specified\"");
		}
		else {
			clientPrinter("-Incorrect password, check and try again");
		}		
	}

	private static String handleType(String fullCommand) throws IOException {
		if(loggedIn) {
			String typeFromUser = fullCommand.substring(5);

			if(typeFromUser.equals("A")) {
				clientPrinter("+Using Ascii mode");
				return "A";
			}
			else if(typeFromUser.equals("C")) {
				clientPrinter("+Using Continuous mode");
				return "C";
			}
			else if(typeFromUser.equals("B")) {
				clientPrinter("+Using Binary mode");
				return "B";
			}
			else {
				clientPrinter("-Type Invalid");
				return "B";
			}
		}
		else {
			clientPrinter("Please log in");
			return "B";
		}
	}

	private static void handleList(String fullCommand, UserFiles myFiles, String currentDirectory) throws IOException {
		if(loggedIn) {
			String directory = currentDirectory + "\\" + fullCommand.substring(6).trim();
			String format = fullCommand.substring(5,6);
			if(format.contentEquals("F")) {
				String MessageToPrint = myFiles.listAllFiles(directory,"F");
				clientPrinter(MessageToPrint);
			}
			else if(format.contentEquals("V")) {
				String MessageToPrint = myFiles.listAllFiles(directory,"V");
				clientPrinter(MessageToPrint);
			}
		}
		else {
			clientPrinter("Please log in");
		}
	}

	private static String handleCDIR(String fullCommand, Account account, String currentDirectory) throws IOException {
		String newDir = fullCommand.substring(5);
		String checkNewDir = Paths.get(currentDirectory, newDir).toString();
		Path path = Paths.get(checkNewDir);
		if(loggedIn) {
			if(newDir.equals("..")) {
				currentDirectory =  new File(System.getProperty("user.directory")).getParentFile().toString();
				clientPrinter("!Working directory updated to "+currentDirectory);
			}
			else if(newDir.equals("/")) {
				currentDirectory = "C:\\";
				clientPrinter("!Working directory updated to "+path);
			}
			else {
				if(Files.exists(path)) {
					currentDirectory = checkNewDir;
					clientPrinter("!Working directory updated to "+path);
				}
				else {
					clientPrinter("-Can't connect to directory: directory doesn't exist");
				}
			}
		}
		else {
			if(Files.exists(path)) {
				clientPrinter("+directory ok, send account/password");
			}
			else {
				clientPrinter("-Can't connect to directory because: directory doesn't exist");
			}
		}
		return currentDirectory;

	}

	private static void handleKill(String fullCommand, Account account, String currentDirectory) throws IOException {
		if(loggedIn) {
			String FilesDelete = "";
			try {
				FilesDelete = fullCommand.substring(5);
			} catch (Exception e) {
				clientPrinter("-Not deleted because file doesn't exist"); 
			}
			String fileLocation = Paths.get(currentDirectory, FilesDelete).toString();
			File file = new File(fileLocation); 
			if(file.delete()) { 
				clientPrinter("+" + FilesDelete + " deleted"); 
			} 
			else { 
				clientPrinter("-Failed: file doesn't exist"); 
			}
		}
		else {
			clientPrinter("-Please send PASS and ACCT to use CDIR");
		}
	}

	private static String handleName(String fullCommand, Account account, String currentDirectory) throws IOException {
		String fileToRename = "";
		if(loggedIn) {
			String tempFileToRename = "";
			try {
				tempFileToRename = fullCommand.substring(5);

				String fileLocation = Paths.get(currentDirectory + "\\" + tempFileToRename).toString();
				Path path = Paths.get(fileLocation);
				//File file = new File(fileLocation); 
				if(Files.exists(path)) {
					fileToRename = tempFileToRename;
					clientPrinter("+File exists");
				}
				else {
					clientPrinter("-Can't find "+ fileToRename +"\n NAME command is aborted, don't send TOBE.");
				}
			}catch (Exception e) {
				clientPrinter("-Can't find "+ fileToRename +"\n NAME command is aborted, don't send TOBE.");
			}
		}
		else {
			clientPrinter("-You must send ACCT and PASS to use CDIR");
		}
		return fileToRename;
	}

	private static void HandleTOBE(String fullCommand, String currentDirectory, String fileToRename) throws IOException {
		if(loggedIn) {
			try {
				String newFileName = fullCommand.substring(5);
				if(fileToRename.equals("")) {
					clientPrinter("-Failed: filename was not specified or was invalid");
				}
				else {
					String fileLocation = Paths.get(currentDirectory, fileToRename).toString();
					String newName = Paths.get(currentDirectory, newFileName).toString();
					File file = new File(fileLocation);
					File fileRenameTo = new File(newName);
					file.renameTo(fileRenameTo);
					newFileName = "";
				}
			} catch(Exception e) {
				clientPrinter("-Failed: filename was not specified or was invalid");
			}
		}
	}

	private static String handleRETR(String fullCommand, String currentDirectory, String fileToSendLocation) throws IOException {
		try {
			String fileName = fullCommand.substring(5);
			String fileLocation = Paths.get(currentDirectory, fileName).toString();
			File file = new File(fileLocation);
			Path path = Paths.get(fileLocation);
			if(Files.exists(path)) {
				fileToSendLocation = fileLocation;
				long fileSize = file.length() ;
				clientPrinter(String.valueOf(fileSize));
			}
			else {
				clientPrinter("-File doesn't exist");
			}
			return fileToSendLocation;
		} catch(Exception e) {
			clientPrinter("-File doesn't exist");
		}
		return null;
	}

	private static String handleSend(OutputStream os, String fileToSendLocation) throws IOException {
		File fileToSend = new File(fileToSendLocation);		
		String message = "";
		try {
			byte[] content = Files.readAllBytes(fileToSend.toPath());
			os.write(content);
			os.flush();
			message = "File Saved on Client's side";
			
		} catch(IOException e) {
			clientPrinter("File Could not be saved");
		}	
		return message;
	}

	private static void handleSize(String fullCommand, Socket connectionSocket, int storeType, String serverFiles, String fileNameToStore) throws IOException {
		String sizeOfFileString = fullCommand.substring(5);
		long sizeOfFile = Integer.parseInt(sizeOfFileString);
		long totalFreeSpace =  new File("c:").getFreeSpace() ;

		if(totalFreeSpace < sizeOfFile) {
			clientPrinter("-Failed: No enough room");
		}
		else {
			clientPrinter("+waiting for file");
			byte[] receivedFile = new byte[(int) sizeOfFile];
			for (int i=0; i<sizeOfFile; i++) {
				receivedFile[i] = (byte) connectionSocket.getInputStream().read();
			}
			try {
				if ((storeType == 1) || (storeType == 3)) {
					FileOutputStream stream = new FileOutputStream(currentDir() + serverFiles + fileNameToStore);
					stream.write(receivedFile);
					stream.close();
				} else if (storeType == 2) {
					fileNameToStore = "new-" + fileNameToStore;
					FileOutputStream stream = new FileOutputStream(currentDir() + serverFiles + fileNameToStore);
					stream.write(receivedFile);
					stream.close();
				} else {
					FileOutputStream stream = new FileOutputStream(currentDir() + serverFiles + fileNameToStore, true);
					stream.write(receivedFile);
					stream.close();
				}
				clientPrinter("+Saved " + fileNameToStore);
			} catch (Exception e) {
				storeType = 0;
				clientPrinter("-Couldn't save");
			}
		}
	}
	private static int handleSTOR(String fullCommand, String serverFiles) throws IOException {
		String param = fullCommand.substring(5,8);
		String filename = fullCommand.substring(9);
		String locationOfFile = currentDir() + serverFiles + filename; 
		int storeType = 0;

		File file = new File(locationOfFile);

		if(param.equals("NEW")) {
			if(file.exists()) {
				storeType = 2;
				clientPrinter("+File exists, new generation of file is created");
			}
			else {
				storeType = 0 ;
				clientPrinter("+File does not exist, create new file");
			}
		}
		else if(param.contentEquals("OLD")) {
			if(file.exists()) {
				storeType = 3;
				clientPrinter("+write over old file");
			}
			else {
				storeType = 1;
				clientPrinter("+create new file");
			}
		}
		else if(param.contentEquals("APP")) {
			if(file.exists()) {
				storeType = 0;
				clientPrinter("+append to file");
			}
			else {
				storeType = 1;
				clientPrinter("+create file");
			}
		}
		return storeType;
	}

	// STILL HAVE TO DO STORAGE CHECKING
	public static void main(String argv[]) throws Exception{
		try {
			ServerSocket welcomeSocket = new ServerSocket(6789);

			UserFiles myFiles = new UserFiles();
			String serverFiles = "\\files\\";
			String currentDirectory = currentDir();
			String fileToRename = "";
			String fileToSendLocation = "";
			String fullcommand = "";
			String command = "";
			String fileNameToStore = "";
			String type = "B";	// default type in binary
			int storeType = 0;


			boolean running = true;

			while(true) {
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("Connected");

				OutputStream os = connectionSocket.getOutputStream();
				BufferedReader inFromClient = 
						new BufferedReader(new
								InputStreamReader(connectionSocket.getInputStream())); 
				outToClient = 
						new DataOutputStream(os); 

				clientPrinter("+ zzho711 Server SFTP Service");

				while(running) {

					fullcommand = inFromClient.readLine();
					try {
						command = fullcommand.substring(0,4);
					}catch (IndexOutOfBoundsException e) {
						command = "";
					}
					try {
						switch (command) {
						case "USER":
							handleUser(fullcommand);
							break;

						case "ACCT":
							handleAcct(fullcommand);
							break;

						case "PASS":
							handlePass(fullcommand);
							break;

						case "TYPE":
							type = handleType(fullcommand);
							break;

						case "LIST":
							handleList(fullcommand, myFiles, currentDirectory);
							break;

						case "CDIR":
							currentDirectory = handleCDIR(fullcommand, account, currentDirectory);
							break;

						case "KILL":
							handleKill(fullcommand, account, currentDirectory);
							break;

						case "NAME":
							fileToRename = handleName(fullcommand, account, currentDirectory);
							break;

						case "TOBE":
							HandleTOBE(fullcommand, currentDirectory, fileToRename);
							break;

						case "DONE":
							clientPrinter("+Connection Closed");
							running = false;
							break;

						case "RETR":
							fileToSendLocation = handleRETR(fullcommand, currentDirectory, fileToSendLocation);
							break;

						case "SEND":
							String response;
							response = handleSend(os, fileToSendLocation);
							clientPrinter(response);
							break;

						case "STOP":
							clientPrinter("+ok, RETR aborted");
							break;

						case "STOR":
							fileNameToStore = fullcommand.substring(9);
							storeType = handleSTOR(fullcommand, serverFiles);
							break;

						case "SIZE":
							handleSize(fullcommand, connectionSocket, storeType, serverFiles, fileNameToStore); 
							break;

						default:
							clientPrinter("-Unknown Command");
							break;
						}
					}
					catch (Exception e) {
						clientPrinter("-Unknown Command");	
					}
				}
				connectionSocket.close();
			}
		}
		catch(Exception ioException) {
			clientPrinter("server ERROR");
		}
	}

}

