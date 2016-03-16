import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;


public class DHCP_Server extends Thread{

	private int port; 
	private int max_connections;
	
	
	private HashMap<String, StoredConnection> StoredConnections; 

	public DHCP_Server(int port, String config){
		this.port = port;
		config = convert_config_path(config);
		parse_config_file(config);
		System.out.println(max_connections);
		
		// for a clean shutdown 
		setDaemon(true);

	}
	
	
	
	public void run(){
//		public static void main(String args[]) throws Exception {
//			 -		DatagramSocket client_connection = new DatagramSocket(12345);
//			 -		byte[] data = new byte[1024];
//			 -		byte[] receiveData = new byte[1024];
//			 -
//			 -		String sendData = "Wij zijn mooi.";
//			 -		data = sendData.getBytes();
//			 -		System.out.println("Voor");
//			 -		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			 -		client_connection.receive(receivePacket);
//			 -		System.out.println("Na");
//			 -		InetAddress server_adress = receivePacket.getAddress();
//			 -		int port = receivePacket.getPort();
//			 -		String returnString = new String(receivePacket.getData());
//			 -		System.out.println("From server: " + returnString);
//			 -		DatagramPacket sendPacket = new DatagramPacket(data, data.length, server_adress, port);
//			 -		client_connection.send(sendPacket);
//			 -
//			 -	}
		try {
			DatagramSocket clientConnection = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] receiveData = new byte[576];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		
	}
	
	StoredConnection getByMacaddress(String mac){
		return StoredConnections.get(mac); 
	}
	
	void addConnection(StoredConnection connection){
		
	}

	private String convert_config_path(String config) {
		URL config_url = getClass().getResource(config);
		if (config_url != null) {
			return config_url.getPath();
		}
		return config;
	}

	private void parse_config_file(String config_path) {
		boolean file_load_succesfull = true;
		
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream(config_path);
			prop.load(input);

		} catch( Exception e ){
			System.out.println(e);
			file_load_succesfull = false; 
		}
		
		String max_conn = null;
		
		if (file_load_succesfull) {
			max_conn = prop.getProperty("max_connections");
		}
		
		if (max_conn != null) {
			this.max_connections = Integer.parseInt(max_conn);
		}
		else {
			this.max_connections = 3;
		}
	}
	
	public InetAddress getAddress() throws UnknownHostException{
		return InetAddress.getLocalHost();
	}
	
	
}
