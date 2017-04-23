package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	
	private static String hostName = "localhost";
	private static int portNumber = 7734;
	
	public static void main(String args[]) throws IOException {
		Scanner sc = null;
		BufferedReader in = null;
		PrintWriter out = null;
		String response = new String();
		try (
		    Socket socket = new Socket(hostName, portNumber);
		) {
			in = new BufferedReader(
			        new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
//			System.out.println(in.readLine());
			System.out.println("Created connection with server");
			
			System.out.println("Enter hostname: ");
			sc = new Scanner(System.in);
//			String hostName = sc.nextLine();
			String hostName = "client1";
			out.println(hostName);
//			response = in.readLine();
//			System.out.println(response);
//			String portNumber = sc.nextLine();
			String portNumber = "5000";
			out.println(portNumber);
//			response = in.readLine();
//			System.out.println(response);
			
			while (true) {
				System.out.println("Enter command - ");
				String command = sc.nextLine() + "\n";
				if (command.equals("exit\n")) {
					out.println("exit");
					break;
				}
				else if (command.startsWith("ADD") || command.startsWith("LOOKUP")) {
//					String host = sc.nextLine();
					String host = "Host: client1\n";
//					String port = sc.nextLine();
					String port = "Port: 5000\n";
//					String title = sc.nextLine();
					String title = "Title: A Proferred Official ICP\n";
					String buffer = command + host + port + title;
					out.printf(buffer);
				}
				else if (command.startsWith("LIST ALL")) {
//					String host = sc.nextLine();
					String host = "Host: client1\n";
//					String port = sc.nextLine();
					String port = "Port: 5000\n";
					String buffer = command + host + port;
					out.printf(buffer);
				}
				else {
					System.out.println("Invalid command");
					continue;
				}
				String line;
				while (!(line = in.readLine()).equals("EOF")) {
					response += line + "\n";
				}
				System.out.println(response);
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

}
