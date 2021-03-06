//  Created by Yifu Zhou on 2017/4/21.
//  Copyright © 2017年 Yifu. All rights reserved.
//

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class Global {
	public static final int ID_OFFSET = 0;
	public static final int TYPE_OFFSET = 4;
	public static final int PORT_OFFSET = 5;
	public static final int TTL_OFFSET = 9;
	
	public static final int TYPE_PING = 1;
}
public class Gnutella {
	
	public static void main(String args[]) throws SocketException {
		int from = Integer.parseInt(args[0]);
		int to = Integer.parseInt(args[1]);
		Client client = new Client();
		System.out.println("Client " + from + "to Client " + to);
		client.init(from);
		client.start();
	}

}

class Util {
	private static byte int3(int x) { return (byte)(x >> 24); }
	private static byte int2(int x) { return (byte)(x >> 16); }
	private static byte int1(int x) { return (byte)(x >>  8); }
	private static byte int0(int x) { return (byte)(x >>  0); }
	public static void intToByte(byte[] to, int index, int target) {
		//to[index] = int0(target);
		to[index+1] = int1(target);
		to[index+2] = int2(target);
		to[index+3] = int3(target);
	}
	public static int byteToInt(byte[] data, int offset) {
		return ((0xff & data[offset+3]) << 24)
				| ((0xff & data[offset+2]) << 16)
				| ((0xff & data[offset+1]) << 8)
				| (0xff & data[offset]);
	}
}


class Client {
	int port;
	byte[] pingData;
	DatagramSocket servSocket;
	DatagramSocket clntSocket;
	
	//good way to revisit the object
	private static Client instance = null;
	public static Client getInstance() {
		if (instance == null) {
			instance = new Client();
		}
		return instance;
	}
	
	public void init(int port) throws SocketException {
		this.port = port;
		pingData = new byte[1024];
		pingData[Global.TYPE_OFFSET] = Global.TYPE_PING;
		Util.intToByte(pingData, Global.PORT_OFFSET, this.port);
		servSocket = new DatagramSocket(port);
		clntSocket = new DatagramSocket();
		
		
	}
	
	public void ping() throws IOException {
		//int p_port = (int)Math.round(Math.random() * 10000);
		//Util.intToByte(pingData, 0, p_port);
		
		//send pingData through UDP
		DatagramPacket packet = new DatagramPacket(pingData, pingData.length, 
				InetAddress.getByName("localhost"), port);
		clntSocket.send(packet);
	}
	
	public void start() {
		new Thread(new PingSender()).start();
		//new Thread(new Listener()).start();
	}
	
}

class PingSender extends Thread {
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				Client.getInstance().ping();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class Listener extends Thread {
	public void run() {
		byte[] receive = new byte[1024];
		while(true) {
			DatagramPacket packet = new DatagramPacket(receive, receive.length);
			try {
				Client.getInstance().servSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			new Thread(new Controller(packet.getData())).start();
		}	
	}
}

class Controller implements Runnable {
	byte[] data;
	int type;
	
	Controller(byte[] data) {
		this.data = new byte[1024];
		System.arraycopy(data, 0, this.data, 0, 1024);
		type = this.data[Global.TYPE_OFFSET];
	}
	
	public void run() {
		System.out.println(Global.TYPE_OFFSET);
	}
	
}
