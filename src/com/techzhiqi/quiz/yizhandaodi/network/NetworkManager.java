package com.techzhiqi.quiz.yizhandaodi.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import protocol.ClientSignal;
import protocol.ServerSignal;
import android.util.Log;

public class NetworkManager {

	private static NetworkManager manager = new NetworkManager();
	private Socket sock;
	// private String serverName = "86.50.138.119";
	// private String serverName = "192.168.13.47";
	private String serverName = "chinesemonster.serveblog.net";
	private int portNum = 81;
	private ObjectInputStream is;
	private ObjectOutputStream os;
	private volatile boolean isConnected = false;

	private NetworkManager() {

	}

	public static NetworkManager getInstance() {
		return manager;
	}

	public void setServerName(String server) {
		this.serverName = server;
	}

	public void setPortNum(int num) {
		this.portNum = num;
	}

	public void connect2Server() throws UnknownHostException, IOException {
		sock = new Socket();
		sock.connect(new InetSocketAddress(serverName, portNum), 5000);
		Log.i("yzdd", "sock connected");
		is = new ObjectInputStream(sock.getInputStream());
		Log.i("yzdd", "is got");
		os = new ObjectOutputStream(sock.getOutputStream());
		Log.i("yzdd", "os got");
		isConnected = true;
	}

	public void connect2ServerWithTimeout() throws IOException {
		// Log.i("yzdd", "serverName="+serverName);
		sock = new Socket();
		sock.connect(new InetSocketAddress(serverName, portNum), 5000);
		sock.setSoTimeout(10000);
		Log.i("yzdd", "sock connected");

		is = new ObjectInputStream(sock.getInputStream());
		Log.i("yzdd", "is got");
		os = new ObjectOutputStream(sock.getOutputStream());
		Log.i("yzdd", "os got");
		isConnected = true;
	}

	public void send2Server(ClientSignal sig) throws IOException {
		Log.i("yzdd", "send2Server called");
		os.writeObject(sig);
		Log.i("yzdd", "send2Server succeeded");
	}

	/*
	 * it's client class's responsibility to handle abnornal Object
	 */
	public ServerSignal readFromServer() throws ClassNotFoundException,
			IOException {
		ServerSignal sig = null;
		try {
			sig = (ServerSignal) is.readObject();
		} catch (ClassCastException e) {
			Log.e("yzdd-readFromServer",
					"ClassCastException, connection broken");
		}
		return sig;
	}

	public void closeConnection() throws IOException {
		if (sock != null) {
			sock.close();
			isConnected = false;
		}

	}

	public boolean isConnected() {
		return isConnected;
	}
}
