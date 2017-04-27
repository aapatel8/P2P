package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {

	private static String serverHostName = "localhost";
	private static int serverPortNumber = 7734;
	private static String myHostName;
	private static int myPortNumber;
	private static List<RFCClientInfo> rfcClientInfo = new ArrayList<RFCClientInfo>();

	public static void main(String args[]) throws IOException {
		Scanner sc = null;
		BufferedReader in = null;
		PrintWriter out = null;
		String response = new String();
		try (Socket socket = new Socket(serverHostName, serverPortNumber);) {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			// System.out.println(in.readLine());
			System.out.println("Created connection with server");

			System.out.println("Enter hostname: ");
			sc = new Scanner(System.in);
			 String hostName = sc.nextLine();
//			String hostName = "localhost";
			myHostName = hostName;
			out.println(hostName);
			// response = in.readLine();
			// System.out.println(response);
			System.out.println("Enter port number: ");
			 String portNumber = sc.nextLine();
//			String portNumber = "5000";
			myPortNumber = Integer.parseInt(portNumber);
			out.println(portNumber);
			new MultiClientThread(portNumber).start();
			// response = in.readLine();
			// System.out.println(response);

			while (true) {
				System.out.println("Enter command - ");
				String command = sc.nextLine() + "\n";
				if (command.equals("exit\n")) {
					out.println("exit");
					break;
				} else if (command.startsWith("ADD")) {
					// String host = sc.nextLine();
					String host = "Host: " + myHostName + "\n";
					// String port = sc.nextLine();
					String port = "Port: " + myPortNumber + "\n";
					System.out.println("Enter title - ");
					String title = "Title: " + sc.nextLine() + "\n";
//					String title = "Title: A Proferred Official ICP\n";
					String buffer = command + host + port + title;
					out.printf(buffer);
				} else if (command.startsWith("LOOKUP")) {
					// String host = sc.nextLine();
					String host = "Host: " + myHostName + "\n";
					// String port = sc.nextLine();
					String port = "Port: " + myPortNumber + "\n";
//					String title = "Title: A Proferred Official ICP\n";
					String buffer = command + host + port;
					out.printf(buffer);
				} else if (command.startsWith("LIST ALL")) {
					// String host = sc.nextLine();
					String host = "Host: " + myHostName + "\n";
					// String port = sc.nextLine();
					String port = "Port: " + myPortNumber + "\n";
					String buffer = command + host + port;
					out.printf(buffer);
				} else if (command.startsWith("GET")) {
					System.out.println("Enter host - ");
					String host = sc.nextLine();
					getRFCFileFromPeer(command, host);
					continue;
				} else {
					System.out.println("Invalid command");
					continue;
				}
				String line;
				while (!(line = in.readLine()).equals("EOF")) {
					response += line + "\n";
				}
				System.out.println(response);
				if (command.startsWith("LOOKUP") || command.startsWith("LIST ALL")) {
					addRFCHost(response);
				}
				response = new String();
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			sc.close();
			in.close();
			out.close();
		}
	}

	private static void getRFCFileFromPeer(String command, String host) throws IOException {
//		String host = "localhost";
		String os = System.getProperty("os.name");;
		String[] splitCommand = command.split(" ");
		int portNum = 0;
		Socket socket = null;
		for (RFCClientInfo clientInfo : rfcClientInfo) {
			if (clientInfo.rfcNum == Integer.parseInt(splitCommand[2]) && clientInfo.hostName.equals(host)) {
				portNum = clientInfo.portNum;
				try {
					socket = new Socket(host, portNum);
					String request = command + "Host: " + myHostName + "\nOS: " + os;
					getRFCFileFromPeerHelper(socket, request);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					socket.close();
				}
			}
		}
		return;
	}

	private static void getRFCFileFromPeerHelper(Socket socket, String request) throws IOException {
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(request);
			String responseHeaders = in.readLine() + "\n" + in.readLine() + "\n" + in.readLine() + "\n" + in.readLine()
					+ "\n" + in.readLine() + "\n" + in.readLine() + "\n";
			System.out.println(responseHeaders);
			String responseFile = new String();
			String line;
			while (!(line = in.readLine()).equals("EOF")) {
				responseFile += line + "\n";
			}
			System.out.println("( " + responseFile + ")\n");
			File file = new File("rfc" + 115 + ".txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(responseFile);
			fileWriter.flush();
			fileWriter.close();

			out.println("received file");
		} catch (IOException e) {
			out.println("received error when retreiving/downloading file");
			e.printStackTrace();
		} finally {
			in.close();
			out.close();
		}
	}

	private static void addRFCHost(String response) {
		String splitResponse[] = response.split("\n");
		for (int i = 1; i < splitResponse.length; i++) {
			String line = splitResponse[i];
			String tokens[] = line.split(" ");
			int rfcNum = Integer.parseInt(tokens[1]);
			String hostName = tokens[tokens.length - 2];
			int portNum = Integer.parseInt(tokens[tokens.length - 1]);
			RFCClientInfo tempClientInfo = new RFCClientInfo();
			tempClientInfo.rfcNum = rfcNum;
			tempClientInfo.hostName = hostName;
			tempClientInfo.portNum = portNum;
			rfcClientInfo.add(tempClientInfo);
		}
	}
}

class RFCClientInfo {
	int rfcNum;
	String hostName;
	int portNum;
}