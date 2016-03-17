import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Properties;




public class DHCP_Server extends Thread{

	private int port; 
	private int max_connections;
	private long ip_table_start;
	private long ip_table_end;
	private long lease_time;


	private HashMap<String, StoredConnection> StoredConnections; 
	private ArrayList<String[]> ClientTable;

	public DHCP_Server(int port, String config){
		this.port = port;
		config = convert_config_path(config);
		parse_config_file(config);
		System.out.println(max_connections);

		// for a clean shutdown 
		setDaemon(true);

	}



	public void run(){
		ExecutorService executor = Executors.newFixedThreadPool(this.max_connections);

		//		public static void main(String args[]) throws Exception {
		//			 -		data = sendData.getBytes();
		//			 -		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		//			 -		client_connection.receive(receivePacket);
		//			 -		InetAddress server_adress = receivePacket.getAddress();
		//			 -		int port = receivePacket.getPort();
		//			 -		String returnString = new String(receivePacket.getData());
		//			 -		System.out.println("From server: " + returnString);
		//			 -		DatagramPacket sendPacket = new DatagramPacket(data, data.length, server_adress, port);
		//			 -		client_connection.send(sendPacket);
		//			 -
		//			 -	}
		DatagramSocket clientConnection = null;

		try {
			clientConnection = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		while(true){
			byte[] receiveData = new byte[576];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientConnection.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Runnable DHCPPacketHandler = new ServerProcessPacket(this, receivePacket);
			executor.execute(DHCPPacketHandler);
		}




	}


	StoredConnection getConnection(String mac){
		return StoredConnections.get(mac); 
	}

	StoredConnection getConnection(byte[] mac){
		String key = Arrays.toString(mac);
		return StoredConnections.get(key);
	}

	void addConnection(StoredConnection connection){
		String key = Arrays.toString(connection.getMacAddress());
		StoredConnections.put(key, connection); 
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
		String start_addr = null;
		String end_addr = null;
		String lease_t = null;
		

		if (file_load_succesfull) {
			max_conn = prop.getProperty("max_connections");
			start_addr = prop.getProperty("ip_table_Start");
			end_addr = prop.getProperty("ip_table_end");
			lease_t = prop.getProperty("lease_time");
		}

		if (max_conn != null) {
			this.max_connections = Integer.parseInt(max_conn);
		}
		else {
			this.max_connections = 3;
		}
		if (start_addr != null) {
			try {
				this.ip_table_start = DHCPHelper.byte_to_long(InetAddress.getByName(start_addr).getAddress());
			} catch (UnknownHostException e) {
				byte[] temp_ip = {(byte) 192, (byte) 168, (byte) 1, (byte) 100};
				this.ip_table_start = DHCPHelper.byte_to_long(temp_ip);
			}
		}
		else {
			byte[] temp_ip = {(byte) 192, (byte) 168, (byte) 1, (byte) 100};
			this.ip_table_start = DHCPHelper.byte_to_long(temp_ip);
		}
		if (end_addr != null) {
			try {
				this.ip_table_end = DHCPHelper.byte_to_long(InetAddress.getByName(end_addr).getAddress());
			} catch (UnknownHostException e) {
				byte[] temp_ip = {(byte) 192, (byte) 168, (byte) 1, (byte) 255};
				this.ip_table_end = DHCPHelper.byte_to_long(temp_ip);
			}
		}
		else {
			byte[] temp_ip = {(byte) 192, (byte) 168, (byte) 1, (byte) 100};
			this.ip_table_end = DHCPHelper.byte_to_long(temp_ip);
		}
		if (lease_t != null) {
			this.lease_time = Integer.parseInt(lease_t);
		}
		else {
			this.lease_time = 1000;
		}
	}

	public InetAddress getAddress() throws UnknownHostException{
		return InetAddress.getLocalHost();
	}

	// print a list of the leased addresses along with their associated MAC addresses 
	public void print_leased_addresses(){
		System.out.println("list of leased addresses:");
		for (Entry<String, StoredConnection> entry : StoredConnections.entrySet()) {
			String key = entry.getKey();
			StoredConnection value = entry.getValue();
			byte[] macAddress = value.getMacAddress();
			String ipAddress = value.getDHCPReceivedAddress().getHostAddress();
			System.out.println("Mac address: " + Arrays.toString(macAddress) + " , ip address: " + ipAddress);

		}
	}
	
	public byte[] get_next_available_ip() {
		for (long ip = ip_table_start; ip < ip_table_end; ip++) {
			String temp_ip = String.valueOf(ip);
			boolean in_table = false;
			for (int i = 0; i < this.ClientTable.size(); i++) {
				if (this.ClientTable.get(i)[0] == temp_ip) {
					
				}
			}
		}
	}

}
