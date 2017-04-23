package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	
	private static int port = 7734;
	private static List<Peer> peerList = new ArrayList<Peer>();
	private static List<RFC> rfcList = new ArrayList<RFC>();
	
	public static void main(String args[]) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
            	Socket socket = serverSocket.accept();
//            	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            	String initHostName = in.readLine();
//            	int initPort = Integer.parseInt(in.readLine());
//            	
//            	Peer peer = new Peer();
//            	peer.hostname = initHostName;
//            	peer.port = initPort;
//            	peerList.add(peer);
            	
	            new MultiServerThread(socket, peerList, rfcList).start();
	        }
	    } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
	}
}