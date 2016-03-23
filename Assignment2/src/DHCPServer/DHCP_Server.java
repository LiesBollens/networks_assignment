 package DHCPServer;
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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import DHCPShared.DHCPHelper;

import java.util.Properties;




public class DHCP_Server extends Thread{

	private int incomming_port; 
	private int outgoing_port;
	private int max_connections;
	private long ip_table_start;
	private long ip_table_end;
	private long lease_time;


	private HashMap<String, StoredConnection> StoredConnections = new HashMap<String, StoredConnection>(); 
	// an arraylist containing the ip address, mac, lease time 
	private ArrayList<String[]> ClientTable = new ArrayList<String[]>();
	
	private ServerLeaseController timeController;

	public DHCP_Server(String config){
		// get the address of the given config path
		config = convert_config_path(config);
		// parse the config file to get the needed options 
		parse_config_file(config);
		System.out.println(max_connections);

		// for a clean shutdown 
		setDaemon(true);
		
		// get a new controller to control the lease times 
		timeController = new ServerLeaseController(this, this.StoredConnections, this.ClientTable);

	}



	public void run(){
		// create e new thread pool
		ExecutorService executor = Executors.newFixedThreadPool(this.max_connections);
		
		// start the lease controller
		timeController.start();
		
		// create a new client connection 
		DatagramSocket clientConnection = null;

		// create a new client connection on the incoming port 
		try {
			clientConnection = new DatagramSocket(getIncommingPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		while(true){
			// receive a packet 
			byte[] receiveData = new byte[576];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				// let the incoming connection receive the packet 
				clientConnection.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// create a new server proces thread to handle the received packet
			Runnable DHCPPacketHandler = new ServerProcessPacket(this, receivePacket, clientConnection);
			// execute this new server proces packet handler 
			executor.execute(DHCPPacketHandler);
		}
	}

	// get the stred connection with the given mac string as mac address
	StoredConnection getConnection(String mac){
		return StoredConnections.get(mac); 
	}

	// get the stored connection with the given mac address 
	StoredConnection getConnection(byte[] mac){
		String key = Arrays.toString(mac);
		System.out.println(StoredConnections);
		return StoredConnections.get(key);
	}

	// add a connection to the stored connections hashmap
	void addConnection(StoredConnection connection){
		String key = Arrays.toString(connection.getMacAddress());
		StoredConnections.put(key, connection); 
	}
	
	// remove a connection from both the stored connections hashmap and the client table 
	void removeConnection(String mac){
		StoredConnections.remove(mac);
		for (int i =0; i < ClientTable.size(); i++){
			if (ClientTable.get(i)[1] == mac){
				ClientTable.remove(i);
				break; 
			}
		}
	}
	
	void removeConnection(byte[] mac){
		String key = Arrays.toString(mac);
		removeConnection(key);
	}
	
	


	// get the right path from the config file 
	private String convert_config_path(String config) {
		URL config_url = getClass().getResource(config);
		if (config_url != null) {
			return config_url.getPath();
			
		}
		return config;
	}

	//  a function to parse the config file 
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
		String inc_p = null;
		String out_p = null;
		
		// get all the properties 
		if (file_load_succesfull) {
			max_conn = prop.getProperty("max_connections");
			start_addr = prop.getProperty("ip_table_start");
			end_addr = prop.getProperty("ip_table_end");
			lease_t = prop.getProperty("lease_time");
			inc_p = prop.getProperty("incomming_port");
			out_p = prop.getProperty("outgoing_port");
			
			System.out.println(start_addr);
			System.out.println(end_addr);
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
		if (inc_p != null) {
			this.incomming_port = Integer.parseInt(inc_p);
		}
		else {
			this.incomming_port = 1234;
		}
		if (out_p != null) {
			this.outgoing_port = Integer.parseInt(out_p);
		}
		else {
			this.outgoing_port = 1235;
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
	
	// a function to get the next available ip address 
	public byte[] get_next_available_ip() {
		// loop through all the available ip addresses 
		for (long ip = ip_table_start; ip < ip_table_end; ip++) {
			String temp_ip = String.valueOf(ip);
			boolean in_table = false;
			
			// check if the ip address already exists in the table, 
			// meaning that the ip address has been leased to a client 
			for (int i = 0; i < this.ClientTable.size(); i++) {
				if (this.ClientTable.get(i)[0].equals(temp_ip)) {
					in_table = true;
					break;
				}
			}
			if (in_table == false){
				System.out.println(ip);
				return DHCPHelper.long_to_byte(ip);
			}
		}
		return null;
	}
	
	// return the entry from the client table with the given ip address 
	public String[] getByIpAddress(byte[] ip){
		String ipAddress = String.valueOf(DHCPHelper.byte_to_long(ip));
		// loop through the client table 
		for (int i = 0; i < ClientTable.size(); i++){
			if ( this.ClientTable.get(i)[0].equals(ipAddress)){
				return this.ClientTable.get(i);
			}
		}
		return null; 
		
		
	}
	
	// refresh the client table 
	public void refreshClientTable(byte[] ipAddress, byte[] mac, long endTime){
		// get the antry from the client table with the given ip address 
		String[] tableEntry = getByIpAddress(ipAddress);
		
		// if there is an entry, update the end time
		if (tableEntry != null){
			tableEntry[2] = String.valueOf(endTime); 
		}
		else {
			// if there is no entry, create a new entry and add it to the table 
			String[] entry = { String.valueOf(DHCPHelper.byte_to_long(ipAddress)), Arrays.toString(mac), String.valueOf(endTime)};
			ClientTable.add(entry);
		}
	}
	
	public int getIncommingPort() {
		return incomming_port;
	}

	public int getOutgoingPort() {
		return outgoing_port;
	}


	public long getLease_time() {
		return lease_time;
	}



	public HashMap<String, StoredConnection> getStoredConnections() {
		return StoredConnections;
	}



	public ArrayList<String[]> getClientTable() {
		return ClientTable;
	}






	public void setLease_time(long lease_time) {
		this.lease_time = lease_time;
	}



	public void setStoredConnections(HashMap<String, StoredConnection> storedConnections) {
		StoredConnections = storedConnections;
	}



	public void setClientTable(ArrayList<String[]> clientTable) {
		ClientTable = clientTable;
	}

}
