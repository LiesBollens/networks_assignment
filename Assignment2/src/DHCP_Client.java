import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import sun.nio.cs.ext.MacArabic;

import com.sun.org.apache.xpath.internal.axes.SelfIteratorNoPredicate;




public class DHCP_Client {
	
	private InetAddress ipAddress; 
	private int port; 
	private DatagramPacket DHCP_discover_packet;
	private InetAddress DHCPReceivedAddress;
	private InetAddress DHCPServerAddress;
	private byte[] receivedIpAddress;
	private byte[] serverIpAdress;
	private byte[] MAC;
	private DatagramSocket client_connection;
	
	public DHCP_Client(String ipAddress, int port) throws Exception {
		this.ipAddress = InetAddress.getByName(ipAddress);
		this.port = port; 
		byte[] chaddr_1 = {(byte) 0xac, (byte) 0xbc, (byte) 0x32, (byte) 0xc4, (byte) 0xcd, (byte) 0x27};
		byte[] chaddr_2 = new byte[10];
		Arrays.fill( chaddr_2, (byte) 0 );
		this.MAC = array_concatenate(chaddr_1, chaddr_2);
	
	}
	
	void send_packet(byte[] packet) throws IOException {
		if (this.client_connection == null){
			return ;
		}
		DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, this.ipAddress, this.port);
		client_connection.send(sendPacket);
	}
	
	DatagramPacket receive_packet() throws IOException{
		if (this.client_connection == null){
			return null;
		}
		byte[] receiveData = new byte[576];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		this.client_connection.receive(receivePacket);
		String returnString = Arrays.toString(receivePacket.getData());
		System.out.println("From server: " + returnString);
		return receivePacket;
	}
	
//	public String send_DHCP_discover() throws Exception{
//		DHCP_discover_packet = send_packet(DHCP_discover());
//		byte[] DHCPData = DHCP_discover_packet.getData();
//		receivedIpAddress = Arrays.copyOfRange(DHCPData, 16, 20);
//		serverIpAdress = Arrays.copyOfRange(DHCPData, 20, 24);
//		DHCPReceivedAddress = InetAddress.getByAddress(receivedIpAddress);
//		DHCPServerAddress = InetAddress.getByAddress(serverIpAdress);
//		System.out.println("Server Address: " + DHCPServerAddress.getHostAddress());
//		return DHCPReceivedAddress.getHostAddress();  
//		
//	}
	
	public void execute_DHCP() throws Exception {
		this.client_connection = new DatagramSocket();
		send_packet(DHCP_discover());
		while (! is_valid_mac(DHCP_discover_packet)){
			this.DHCP_discover_packet = receive_packet();
		}
		parse_DHCP_offer(this.DHCP_discover_packet);
		client_connection.close();
	}
	
	private void parse_DHCP_offer(DatagramPacket offer) throws UnknownHostException{
		if (offer == null){
			return ;
		}
		byte[] DHCPData = offer.getData();
		receivedIpAddress = Arrays.copyOfRange(DHCPData, 16, 20);
		serverIpAdress = Arrays.copyOfRange(DHCPData, 20, 24);
		DHCPReceivedAddress = InetAddress.getByAddress(receivedIpAddress);
		DHCPServerAddress = InetAddress.getByAddress(serverIpAdress);
		byte[] options = Arrays.copyOfRange(DHCPData, 236, DHCPData.length);
		System.out.println("Options: " + Arrays.toString(options));
		System.out.println("Server Address: " + DHCPServerAddress.getHostAddress());
	}
	
	private boolean is_valid_mac(DatagramPacket packet){
		if (packet == null){
			return false;
		}
		byte[] rawData = packet.getData();
		byte[] possibleMac = Arrays.copyOfRange(rawData, 28, 44);
		return Arrays.equals(MAC, possibleMac);
	}

	public byte[] DHCP_discover(){
		byte[] op = {(byte) 0x1};
		byte[] htype = {(byte) 0x1};
		byte[] hlen = {(byte) 0x6};
		byte[] hops = {(byte) 0x0};
		byte[] xid = {(byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22};
		byte[] secs = {(byte) 0x0, (byte) 0x0 };
		byte[] flags = {(byte) 0x0, (byte) 0x0};
		byte[] ciaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] yiaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] siaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] giaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] chaddr = MAC;
		byte[] sname = new byte[64];
		Arrays.fill(sname, (byte) 0 );
		byte[] file = new byte[128];
		Arrays.fill(file, (byte) 0 );


		byte[] option_msg_type = { (byte) 53, (byte) 1, (byte) 1};// DCHP discover request, # 53
		byte[] option_ ={(byte) 55, (byte) 4, (byte) 1, (byte) 3, (byte) 15, (byte) 6 }; // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)
		
		// return one big byte array, containing all the above 
		byte[] dhcp_discover_packet = array_concatenate(op, htype, hlen, hops, xid, secs, flags, ciaddr, yiaddr, siaddr, giaddr, chaddr, sname, file, option_msg_type, option_); 
		System.out.println(Arrays.toString(dhcp_discover_packet));
		return dhcp_discover_packet; 
	}

	byte[]  array_concatenate(byte[]... array){
		int total_length = 0;
		for ( int i=0; i< array.length; i++){
			total_length += array[i].length;
		}
		byte[] concatenated = new byte[total_length];
		int index_concatenated = 0;
		for (int i = 0; i< array.length; i++){
			for (int j= 0; j < array[i].length; j++){
				concatenated[index_concatenated] = array[i][j];
				index_concatenated++;
			}
		}
		return concatenated;
	}

}
