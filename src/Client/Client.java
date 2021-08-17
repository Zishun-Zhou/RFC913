package Client;

import java.io.*; 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; 

public class Client {
	static DataOutputStream outToServer;
	static BufferedReader inFromServer;
	static OutputStream os;
	static String receivedFileName;
	static int receivedFileSize;

	public static String currentDir() {
		return System.getProperty("user.dir");
	}

	private static String handleType(String outgoingMessage) throws IOException {
		String fileType = "";
		outToServer.writeBytes(outgoingMessage + '\n');
		outToServer.flush();
		fileType = outgoingMessage.substring(5);
		return fileType;
	}

	private static void handleSend(String outgoingMessage, Socket clientSocket, String fileType) throws IOException {
		outToServer.writeBytes(outgoingMessage + '\n');
		outToServer.flush();

		String r;
		byte[] bytes = new byte[(int)receivedFileSize]; // Declare byte array with file size
		boolean binary = false;
		clientSocket.setSoTimeout(5*1000); // Set timeout in case data doesn't come

		//DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

		try {
			for (int i = 0; i < receivedFileSize; i++) {
				bytes[i] = (byte) clientSocket.getInputStream().read();

				if ((int)bytes[i] < 0) { // File is binary if MSB is negative
					binary = true;
				}
			}

			// Stop socket timeout immediately
			clientSocket.setSoTimeout(0);

			// Check if file type is correct before storing
			boolean correctType = (!binary && fileType.equals("A")) || fileType.equals("B") || fileType.equals("C");

			// Write file if it is correct
			if (correctType) { //
				FileOutputStream createdFile = new FileOutputStream(currentDir() + "/clientFiles/" + receivedFileName);
				createdFile.write(bytes);
				createdFile.close();	  
//				r = inFromServer.readLine();
//				System.out.println(r);
			}
		}catch (SocketTimeoutException e) {
			// Stop socket timeout immediately
			clientSocket.setSoTimeout(0);
			System.out.println("Could not receive file");
		}catch(Exception e) {
			clientSocket.setSoTimeout(0);
		}
	}

	private static String handleStor(String outgoingMessage) throws IOException {
		String fileToSend; 
		fileToSend = outgoingMessage.substring(9);
		outToServer.writeBytes(outgoingMessage + '\n');
		outToServer.flush();
		return fileToSend;
	}

	private static void handleSize(String clientFiles, String fileToSend) throws IOException {
		String locationOfFile = currentDir() + clientFiles + fileToSend; 

		File file = new File(locationOfFile);		
		String r = "";
		String outgoingMessage;

		outgoingMessage = "SIZE " + Long.toString(file.length());
		outToServer.writeBytes(outgoingMessage + '\n');
		outToServer.flush();

		r = inFromServer.readLine();
		System.out.println(r);

		try {
			byte[] content = Files.readAllBytes(file.toPath());
			os.write(content);

		} catch(IOException e) {

		}	
	}

	private static void handleRetr(String outgoingMessage, String r) throws Exception {
		receivedFileName = outgoingMessage.substring(5);				
		receivedFileSize = Integer.parseInt(r);
		long totalFreeSpace =  new File("c:").getFreeSpace() ;
		if(totalFreeSpace < receivedFileSize) {
			outgoingMessage = "STOP";
			outToServer.writeBytes(outgoingMessage + '\n');
			outToServer.flush();
		}
	}
	
	private static void sendCommand(String outgoingMessage) throws IOException {
		outToServer.writeBytes(outgoingMessage + '\n');
		outToServer.flush();
	}
	
	private static String receiveMessage() throws IOException {
		String r = inFromServer.readLine();
		System.out.println(r);
		return r;
	}
	
	private static String receiveMessageNewLine() throws IOException {
		String r = inFromServer.readLine();
		System.out.println(r+"\n");
		return r;
	}




	public static void main(String argv[]) throws Exception{
		Socket clientSocket = new Socket("127.0.0.1", 6789);
		os = clientSocket.getOutputStream();


		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		outToServer = new DataOutputStream(os); 

		int receivedFileSize = 0;
		String receivedFileName = "";
		String fileType = "B";
		String outgoingMessage;
		String r;
		String fileToSend = "";
		String clientFiles = "\\clientFiles\\";

		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 

		r = receiveMessage();
		boolean sendMessage = false;

		while(true) {
			outgoingMessage = inFromUser.readLine();

			if(outgoingMessage.contains("TYPE")) {
				fileType = handleType(outgoingMessage);
			}

			else if(outgoingMessage.contains("SEND")) {
				handleSend(outgoingMessage, clientSocket, fileType);
				sendMessage = true;
			}

			else if(outgoingMessage.contains("STOR")) {
				fileToSend = handleStor(outgoingMessage);
			}

			else if(outgoingMessage.contains("SIZE")) {
				handleSize(clientFiles, fileToSend);
			}
			else {	
				sendCommand(outgoingMessage);
			}

			if(sendMessage) {
				sendMessage = false;	
				r = receiveMessage();
				while(inFromServer.ready()) {
					r = receiveMessage();
				}
			}else {
				r = receiveMessage();
				
				while(inFromServer.ready()) {
					r = receiveMessage();
				}
			}

			if(outgoingMessage.contains("RETR")) {
				handleRetr(outgoingMessage, r);
			}
		}
	}
}

