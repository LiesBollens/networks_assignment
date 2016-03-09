import java.io.IOException;
import java.math.BigInteger;
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
	private byte[] serverIpAddress;
	private byte[] MAC;
	private DatagramSocket client_connection;
	private static byte[] MAGIC_COOKIE = {(byte) 99, (byte) -126, (byte) 83, (byte) 99};
	
	private byte DHCPMessageType;
	private InetAddress serverIdentifierAddress;
	private int ipAddressLeaseTime;
	
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
		send_packet(DHCP_request());
		DatagramPacket DHCP_ACK_packet = receive_packet();
		parse_DHCP_offer(DHCP_ACK_packet);
		client_connection.close();
	}
	
	private void parse_DHCP_offer(DatagramPacket offer) throws UnknownHostException{
		if (offer == null){
			return ;
		}
		byte[] DHCPData = offer.getData();
		parse_general_data(DHCPData);
		System.out.println("Ip lease time: " + ipAddressLeaseTime);
		System.out.println("Message type: " + DHCPMessageType);
		receivedIpAddress = Arrays.copyOfRange(DHCPData, 16, 20);
		serverIpAddress = Arrays.copyOfRange(DHCPData, 20, 24);
		DHCPReceivedAddress = InetAddress.getByAddress(receivedIpAddress);
		DHCPServerAddress = InetAddress.getByAddress(serverIpAddress);
		if (serverIdentifierAddress != null && ! DHCPServerAddress.equals(serverIdentifierAddress)){
			throw new UnknownHostException("The received ip adresses in the DHCP packet and in the options do not match.");
		}
		System.out.println("Server Address: " + DHCPServerAddress.getHostAddress());
	}

	private void parse_general_data(byte[] DHCPData)
			throws UnknownHostException {
		byte[] options = Arrays.copyOfRange(DHCPData, 236, DHCPData.length);
		if (! Arrays.equals(Arrays.copyOf(options, 4), MAGIC_COOKIE)){
			return ;
		}
		int index = 4;
		while (true){
			if (options[index] == (byte) 255){
				break;
			}
			byte optionNb = options[index];
			int optionLength = (int) options[index + 1] & 0xFF;
			byte[] optionContents = Arrays.copyOfRange(options, index + 2, index + 2 + optionLength);
			parse_option(optionNb, optionLength, optionContents);
			index+= 2 + optionLength;
		}
	}
	
	private void parse_option(byte optionNb, int optionLength, byte[] optionContents) throws UnknownHostException {
		switch (optionNb) {
		case (byte) 53:
			DHCPMessageType = optionContents[0];
			break;
		case (byte) 54:
			serverIdentifierAddress = InetAddress.getByAddress(optionContents);
			break;
		case (byte) 51:
			ipAddressLeaseTime = 16777216 * (int) (optionContents[0] & 0xFF) +  65536 * (int) (optionContents[1] & 0xFF) + 256 * (int) (optionContents[2] & 0xFF) + (int) (optionContents[3] & 0xFF);
			break;
			
		default:
			break;
		}
	}
	
	private boolean is_valid_mac(DatagramPacket packet){
		if (packet == null){
			return false;
		}
		byte[] rawData = packet.getData();
		byte[] possibleMac = Arrays.copyOfRange(rawData, 28, 44);
		return Arrays.equals(MAC, possibleMac);
	}

	public byte[] DHCP_request(){
		byte[] op = {(byte) 0x1};
		byte[] htype = {(byte) 0x1};
		byte[] hlen = {(byte) 0x6};
		byte[] hops = {(byte) 0x0};
		byte[] xid = {(byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22};
		byte[] secs = {(byte) 0x0, (byte) 0x0 };
		byte[] flags = {(byte) 0x0, (byte) 0x0};
		byte[] ciaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] yiaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] siaddr = serverIpAddress;
		byte[] giaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] chaddr = MAC;
		byte[] sname = new byte[64];
		Arrays.fill(sname, (byte) 0 );
		byte[] file = new byte[128];
		Arrays.fill(file, (byte) 0 );

		byte[] option_msg_type = { (byte) 53, (byte) 1, (byte) 3};// DCHP discover request, # 53
		byte[] option_requested_ip_one = {(byte) 50, (byte) 4};
		byte[] option_requested_ip = array_concatenate(option_requested_ip_one, receivedIpAddress); // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)
		byte[] option_server_ip_one = {(byte) 54, (byte) 4};
		byte[] option_server_ip = array_concatenate(option_server_ip_one, serverIpAddress);
		byte[] option_end = { (byte) 255};
		
		// return one big byte array, containing all the above 
		byte[] dhcp_discover_packet = array_concatenate(op, htype, hlen, hops, xid, secs, flags, ciaddr, yiaddr, siaddr, giaddr, chaddr, sname, file, MAGIC_COOKIE, option_msg_type, option_requested_ip, option_server_ip, option_end); 
		System.out.println(Arrays.toString(dhcp_discover_packet));
		return dhcp_discover_packet; 
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
		byte[] option_ ={(byte) 55, (byte) 4, (byte) 1, (byte) 3, (byte) 15, (byte) 6, (byte) 255}; // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)
		
		// return one big byte array, containing all the above 
		byte[] dhcp_discover_packet = array_concatenate(op, htype, hlen, hops, xid, secs, flags, ciaddr, yiaddr, siaddr, giaddr, chaddr, sname, file, MAGIC_COOKIE, option_msg_type, option_); 
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
