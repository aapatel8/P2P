package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class MultiServerThread extends Thread {
	private Socket socket = null;
	private List<Peer> peerList;
	private List<RFC> rfcList;
	
	String initHostName;
	int initPort;
//	BufferedReader in;

    public MultiServerThread(Socket socket, List<Peer> peerList, List<RFC> rfcList) {
        super("MultiServerThread");
        this.socket = socket;
        this.peerList = peerList;
        this.rfcList = rfcList;
//        this.initHostName = initHostName;
//        this.initPort = initPort;
//        this.in = in;
    }
    
    public void run() {
    	BufferedReader in = null;
    	PrintWriter out = null;
        try {
        	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	out = new PrintWriter(socket.getOutputStream(), true);
        	System.out.println("Created and running a client thread");
        	
        	String initHostName = in.readLine();
        	int initPort = Integer.parseInt(in.readLine());
        	
        	Peer newPeer = new Peer();
        	newPeer.hostname = initHostName;
        	this.initHostName = initHostName;
        	newPeer.port = initPort;
        	this.initPort = initPort;
        	peerList.add(newPeer);
        	
        	while (true) {
        		String command = in.readLine();
        		System.out.println(command);
        		if (command.equals("exit")) {
        			for (int i = 0; i < rfcList.size(); i++) {
        				RFC rfc = rfcList.get(i);
        				if (rfc.hostName.equals(this.initHostName)) {
        					rfcList.remove(rfc);
        				}
        			}
    				for (int i = 0; i < peerList.size(); i++) {
    					Peer peer = peerList.get(i);
        				if (peer.hostname.equals(this.initHostName)) {
        					peerList.remove(peer);
        				}
        			}
        			break;
        		}
        		else if (command.startsWith("ADD")) {
            		String hostLine = in.readLine();
            		String portLine = in.readLine();
            		String titleLine = in.readLine();
            		String request = command + "\n" + hostLine + "\n" + portLine + "\n" + titleLine;
        			int resp = addRFC(request, out);
        			if (resp == 0) {
        				badRequest(out);
        			}
        			else if (resp == -1) {
        				badVersion(out);
        			}
        		}
        		else if (command.startsWith("LOOKUP")) {
        			String hostLine = in.readLine();
            		String portLine = in.readLine();
            		String titleLine = in.readLine();
            		String request = command + "\n" + hostLine + "\n" + portLine + "\n" + titleLine;
        			int resp = lookUp(request, out);
        			if (resp == 0) {
        				badRequest(out);
        			}
        			else if (resp == -1) {
        				badVersion(out);
        			}
        		}
        		else if (command.startsWith("LIST ALL")) {
        			String hostLine = in.readLine();
            		String portLine = in.readLine();
            		String request = command + "\n" + hostLine + "\n" + portLine + "\n";
        			int resp = listAll(request, out);
        			if (resp == 0) {
        				badRequest(out);
        			}
        			else if (resp == -1) {
        				badVersion(out);
        			}
        		}
        	}
        	
            socket.close();
        } catch (IOException e) {
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

	private int listAll(String request, PrintWriter out) {
		String lines[] = request.split("\n");
		
		//First line
		String line1 = lines[0];
		String[] splitLine1 = line1.split(" "); //LIST ALL P2P-CI/1.0
		if (!splitLine1[2].equals("P2P-CI/1.0")) {
			return -1;
		}
		
		String header = "P2P-CI/1.0 200 OK\n";
		String response = new String();
		
		response = header + listAllResponse() + "EOF";
		
		out.println(response);
		
		return 1;
	}

	private String listAllResponse() {
		String response = new String();
		for (RFC rfc: rfcList) {
			Peer peer = findPeer(rfc.hostName);
			String tempResponse = "RFC " + rfc.num + " " + rfc.title + " " + peer.hostname + " " + peer.port + "\n";
			response += tempResponse;
		}
		return response;
	}

	private int lookUp(String request, PrintWriter out) {
		String lines[] = request.split("\n");
		
		//First line
		String line1 = lines[0];
		String[] splitLine1 = line1.split(" "); //LOOKUP RFC 123 P2P-CI/1.0
		int num;
		if (splitLine1[1].equals("RFC")) {
			try {
				num = Integer.parseInt(splitLine1[2]);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		else {
			return 0;
		}
		if (!splitLine1[3].equals("P2P-CI/1.0")) {
			return -1;
		}
		
		String header = "P2P-CI/1.0 200 OK\n";
		String response = new String();
		
		response = header + lookUpResponse(num) + "EOF";
		
		out.println(response);
		
		return 1;
	}

	private String lookUpResponse(int num) {
		String response = new String();
		for (RFC rfc: rfcList) {
			if (rfc.num == num) {
				Peer peer = findPeer(rfc.hostName);
				String tempResponse = "RFC " + rfc.num + " " + rfc.title + " " + peer.hostname + " " + peer.port + "\n";
				response += tempResponse;
			}
		}
		return response;
	}

	private Peer findPeer(String hostName) {
		for (Peer peer: peerList) {
			if (peer.hostname.equals(hostName)) {
				return peer;
			}
		}
		return null;
	}

	private int addRFC(String request, PrintWriter out) {
		String lines[] = request.split("\n");
		
		//First line
		String line1 = lines[0];
		String[] splitLine1 = line1.split(" "); //ADD RFC 123 P2P-CI/1.0
		int num;
		if (splitLine1[1].equals("RFC")) {
			try {
				num = Integer.parseInt(splitLine1[2]);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		else {
			return 0;
		}
		if (!splitLine1[3].equals("P2P-CI/1.0")) {
			return -1;
		}
		
		//Second line
		String line2 = lines[1];
		String[] splitLine2 = line2.split(": "); //
		String hostName = new String();
		if (splitLine2[0].equals("Host")) {
			hostName = splitLine2[1];
		}
		else {
			return 0;
		}
		
		//Third line
		String line3 = lines[2];
		String[] splitLine3 = line3.split(": "); //
		int port;
		if (splitLine3[0].equals("Port")) {
			port = Integer.parseInt(splitLine3[1]);
		}
		else {
			return 0;
		}
		
		//Fourth line
		String line4 = lines[3];
		String[] splitLine4 = line4.split(": "); //
		String title = new String();
		if (splitLine4[0].equals("Title")) {
			title = splitLine4[1];
		}
		else {
			return 0;
		}
		
		RFC rfc = new RFC();
		rfc.hostName = hostName;
		rfc.num = num;
		rfc.title = title;
		
		rfcList.add(rfc);
		
		String response = "P2P-CI/1.0 200 OK\nRFC " + num + " " + title + " " + hostName + " " + port + "\nEOF";
		
		out.println(response);
		
		return 1;
	}
	
	private void badRequest(PrintWriter out) {
		String response = "P2P-CI/1.0 404 Bad Request";
		out.println(response + "\nEOF");
	}
	
	private void badVersion(PrintWriter out) {
		String response = "P2P-CI/1.0 505 P2P-CI Version Not Supported";
		out.println(response + "\nEOF");
	}
}
