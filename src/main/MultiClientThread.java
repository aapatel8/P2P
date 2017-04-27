package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;

public class MultiClientThread extends Thread {
	
	private int port;
	
	public MultiClientThread(String port) {
		this.port = Integer.parseInt(port);
	}
	
	public void run() {
		Socket socket = null;
		BufferedReader in = null;
    	PrintWriter out = null;
    	while (true) {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				socket = serverSocket.accept();
	        	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        	out = new PrintWriter(socket.getOutputStream(), true);
	        	
	        	String requestLine1 = in.readLine();
	        	String requestLine2 = in.readLine();
	        	String requestLine3 = in.readLine();
	        	String request = requestLine1 + "\n" + requestLine2 + "\n" + requestLine3;
	        	
	        	handlePeer(request, out, socket);
	        	
//	        	respondToPeer(115, out);
	        	
	        	String peerConfirmation = in.readLine();
//	        	System.out.println("Peer has " + peerConfirmation);
	        	socket.close();
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	        	try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	out.close();
	        }
    	}
	}

	private void handlePeer(String request, PrintWriter out, Socket socket) {
		String[] allLines = request.split("\n");
		String[] firstLine = allLines[0].split(" ");
		if (!firstLine[0].equals("GET")) {
			badRequest(out);
			return;
		}
		
		if (!firstLine[1].equals("RFC")) {
			badRequest(out);
			return;
		}
		
		int rfcNum;
		try {
			rfcNum = Integer.parseInt(firstLine[2]);
		}
		catch (NumberFormatException e) {
			badRequest(out);
			return;
		}
		
		if (!firstLine[3].equals("P2P-CI/1.0")) {
			badVersion(out);
			return;
		}
		
		String[] secondLine = allLines[1].split(": ");
		if (!secondLine[0].equals("Host")) {
			badRequest(out);
			return;
		}
		String hostName = secondLine[1];
		
		String[] thirdLine = allLines[2].split(": ");
		if (!thirdLine[0].equals("OS")) {
			badRequest(out);
			return;
		}
		String osVersion = thirdLine[1];
		
		respondToPeer(rfcNum, out);
	}
	
	private void respondToPeer(int rfcNum, PrintWriter out) {
		String filename = "rfc" + new Integer(rfcNum).toString() + ".txt";
		
		Scanner scanner = null;
		try {
			File file = new File(filename);
			String headers = getHeaders(file);
			scanner = new Scanner( new File(filename) );
			String text = scanner.useDelimiter("\\A").next() + "EOF";
			out.println(headers + text);
		} catch (FileNotFoundException e) {
			notFound(out);
			e.printStackTrace();
		} finally {
			scanner.close(); // Put this call in a finally block
		}
	}

	private String getHeaders(File file) {
		String ok = "P2P-CI/1.0 200 OK";
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
		Date date = new Date();
		String currentDate = "\nDate: " + df.format(date);
		String os = "\nOS: " + System.getProperty("os.name");
		String lastModified = new String();
		String contentLength = new String();
		String contentType = new String();
		if (file != null) {
			Date lastModifiedDate = new Date(file.lastModified());
			lastModified = "\nLast-Modified: " + df.format(lastModifiedDate);
			contentLength = "\nContent-Length: " + file.length();
			contentType = "\nContent-Type: text/text";
		}
		String headerResponse = ok + currentDate + os + lastModified + contentLength + contentType + "\n";
		return headerResponse;
	}

	private void badRequest(PrintWriter out) {
		String response = "P2P-CI/1.0 400 Bad Request";
		String headers = getHeaders(null);
		out.println(response + headers + "EOF");
	}
	
	private void badVersion(PrintWriter out) {
		String response = "P2P-CI/1.0 505 P2P-CI Version Not Supported";
		String headers = getHeaders(null);
		out.println(response + headers + "EOF");
	}
	
	private void notFound(PrintWriter out) {
		String response = "P2P-CI/1.0 404 Not Found";
		String headers = getHeaders(null);
		out.println(response + headers + "EOF");
	}

}
