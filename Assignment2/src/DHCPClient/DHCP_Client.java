package DHCPClient;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import DHCPShared.DHCPMessageType;
import DHCPShared.DHCP_package;





public class DHCP_Client {

	public DHCP_Client( String ipAddress, int portIncoming, int portOutgoing, byte[] macAddress ) throws Exception{
		System.out.println("making a DHCP client");
		this.ipAddress = InetAddress.getByName(ipAddress);
		
		// the port on which the client has to operate
		this.portOutgoing = portOutgoing;
		// the port on which the client gets the information
		this.portIncoming = portIncoming; 

		// the client hardware address
		byte[] chaddr_1 = macAddress; 
		byte[] chaddr_2 = new byte[10];
		Arrays.fill( chaddr_2, (byte) 0 );
		this.MAC = array_concatenate(chaddr_1, chaddr_2);
	}


	public DHCP_Client(String ipAddress, int port, byte[] macAddress) throws Exception {
		// without specification, the incoming port is equal to the outgoing port 
		this(ipAddress, port, port, macAddress);
	}


	private InetAddress ipAddress; 
	private int portOutgoing;
	private int portIncoming; 
	private DatagramPacket DHCP_packet;
	private InetAddress DHCPReceivedAddress;
	private InetAddress DHCPServerAddress;
	private byte[] receivedIpAddress;
	private byte[] serverIpAddress;
	private byte[] MAC;
	private DatagramSocket client_connection_outgoing;
	private DatagramSocket client_connection_incoming;
	private static byte[] MAGIC_COOKIE = {(byte) 99, (byte) -126, (byte) 83, (byte) 99};
	private byte[] xid = new byte[4];
	private byte DHCPMessageNumber;
	private InetAddress serverIdentifierAddress;
	private int ipAddressLeaseTime;



	public InetAddress execute_DHCP() throws Exception {
		// open a new client connection 
		setClient_connection();

		new Random().nextBytes(xid);

		// create a new DHCP packet
		DHCP_package DHCPDiscover = new DHCP_package(MAC);
		// set the xid 
		DHCPDiscover.setXid(xid);
		// set the message type to the discover message type 
		DHCPDiscover.set_message_type(DHCPMessageType.DHCPDISCOVER);
		// set the options 
		byte[] discoverOptions = {(byte) 55, (byte) 4, (byte) 1, (byte) 3, (byte) 15, (byte) 6};
		DHCPDiscover.setOption_(discoverOptions);

		// send the discover packet 
		send_packet(DHCPDiscover.get_package());
		System.out.println("sent packet: " + DHCPMessageType.DHCPDISCOVER.toString()  );
		System.out.println(Arrays.toString(DHCPDiscover.get_package()));
		
		// if the MAC address is not equal to the client hardware address,
		// the packet is destined for the wrong client 
		while (! is_valid_mac(DHCP_packet)){
			// receive a packet 
			this.DHCP_packet = receive_packet();
			System.out.println("received packet: " + DHCPMessageType.DHCPOFFER.toString());
			System.out.println(Arrays.toString(DHCP_packet.getData()));
		}

		// check the received packet, which should be a DHCPOffer 
		parse_DHCP_offer(this.DHCP_packet);

		// send the DHCPRequest packet
		DHCP_package DHCPRequest = new DHCP_package(MAC); 
		// set the xis
		DHCPRequest.setXid(xid);
		// set the server ip address
		DHCPRequest.setSiaddr(getServerIpAddress());
		// set the hardware address
		DHCPRequest.setChaddr(MAC);
		// set the correct message type 
		DHCPRequest.set_message_type(DHCPMessageType.DHCPREQUEST);
		byte[] option_requested_ip_one = {(byte) 50, (byte) 4};
		byte[] option_requested_ip = array_concatenate(option_requested_ip_one, receivedIpAddress); // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)
		byte[] option_server_ip_one = {(byte) 54, (byte) 4};
		byte[] option_server_ip = array_concatenate(option_server_ip_one, serverIpAddress);
		DHCPRequest.setOption_(array_concatenate(option_requested_ip, option_server_ip));
		
		// send the request packet 
		send_packet(DHCPRequest.get_package());
		System.out.println("sent packet: " + DHCPMessageType.DHCPREQUEST.toString()  );
		System.out.println(Arrays.toString(DHCPRequest.get_package()));

		// receive the DHCPAck packet
		DatagramPacket DHCP_ACK_packet = receive_packet();

		// check the received packet 
		parse_DHCP_offer(DHCP_ACK_packet);

		// print it out 
		System.out.println("received packet: " + DHCPMessageType.getMessageType(DHCPMessageNumber).toString());
		System.out.println(Arrays.toString(DHCP_ACK_packet.getData()));

		// if we got a not acknowledge packet instead of an acknowledge packet, 
		// we have to try again by sending a new discovery packet 
		if (DHCPMessageNumber != (byte) 5){
			if (DHCPMessageNumber == (byte) 6) {
				client_connection_outgoing.close();
				DHCP_Client client = new DHCP_Client(ipAddress.getHostAddress(), this.portOutgoing , Arrays.copyOfRange(MAC, 0, 6));
				return client.execute_DHCP();
			}
		}



		// close the client connection 
		client_connection_outgoing.close();
		client_connection_incoming.close();
		// return the IP address 
		return DHCPReceivedAddress;
	}

	public void DHCP_with_renual() throws Exception {
		while (true) {
			// send the DHCP packets to the server 
			execute_DHCP();
			while (true) {
				// sleep for half the lease time
				Thread.sleep(getIpAddressLeaseTime()/2*1000);
				// set a new client connection 
				setClient_connection();

				// send the DHCPRequest packet
				DHCP_package DHCPRequest = new DHCP_package(MAC); 
				DHCPRequest.setXid(xid);
				DHCPRequest.setSiaddr(getServerIpAddress());
				DHCPRequest.setChaddr(MAC);
				DHCPRequest.set_message_type(DHCPMessageType.DHCPREQUEST);
				byte[] option_requested_ip_one = {(byte) 50, (byte) 4};
				byte[] option_requested_ip = array_concatenate(option_requested_ip_one, receivedIpAddress); // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)
				byte[] option_server_ip_one = {(byte) 54, (byte) 4};
				byte[] option_server_ip = array_concatenate(option_server_ip_one, serverIpAddress);
				DHCPRequest.setOption_(array_concatenate(option_requested_ip, option_server_ip));
				
				send_packet(DHCPRequest.get_package());
				System.out.println("sent packet: " + DHCPMessageType.DHCPREQUEST.toString()  );
				System.out.println(Arrays.toString(DHCPRequest.get_package()));

				// receive the DHCPAck packet
				DatagramPacket DHCP_ACK_packet = receive_packet();

				// check the received packet 
				parse_DHCP_offer(DHCP_ACK_packet);

				// close the client connection 
				client_connection_outgoing.close();
				client_connection_incoming.close();

				// if the received packet was no DHCPAck, there is no use in sending another request
				if (DHCPMessageNumber != (byte) 5){
					break;
				}
			}
		}
	}

	// a function to release the ip address 
	public void release_DHCP() throws IOException{
		// if we don't have an ip address anymore, we don't have anything left to do 
		if (ipAddress == null){
			return ;
		}
		// create a new DHCP packet
		DHCP_package releasePacket = new DHCP_package(MAC);
		releasePacket.setXid(xid);
		releasePacket.set_message_type(DHCPMessageType.DHCPRELEASE);
		releasePacket.setSiaddr(getServerIpAddress());
		releasePacket.setChaddr(MAC);
		
		// send the packet
		send_packet(releasePacket.get_package());
		System.out.println("sent packet: " + DHCPMessageType.DHCPRELEASE.toString() );
		System.out.println(Arrays.toString(releasePacket.get_package()));
	}

	// a function to send the packet 
	void send_packet(byte[] packet) throws IOException {
		// we cannot send a packet without an outgoing connection
		if (this.client_connection_outgoing == null){
			System.out.println("NO outgoing connection");
			return ;
		}
		// create a new datagrampacket with the given packet 
		DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, this.ipAddress, this.portOutgoing);
		// send it via the outgoing client connection 
		client_connection_outgoing.send(sendPacket);
	}

	// a function to receive a datagram packet
	DatagramPacket receive_packet() throws IOException{
		// without an incoming connection, we cannot receive anything
		if (this.client_connection_incoming == null){
			return null;
		}
		// create a new receive packet to receive the packet
		byte[] receiveData = new byte[576];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		// let the incoming connection receive the packet 
		this.client_connection_incoming.receive(receivePacket);
		return receivePacket;
	}

	// a functin to parse a DHCP offer 
	private void parse_DHCP_offer(DatagramPacket offer) throws UnknownHostException{
		if (offer == null){
			return ;
		}
		
		// get the data
		byte[] DHCPData = offer.getData();
		
		// parse the general data
		parse_general_data(DHCPData);
		// get the received ip address
		receivedIpAddress = Arrays.copyOfRange(DHCPData, 16, 20);
		// get the server ip address
		serverIpAddress = Arrays.copyOfRange(DHCPData, 20, 24);
		
		// print it out
		DHCPReceivedAddress = InetAddress.getByAddress(receivedIpAddress);
		System.out.println(DHCPReceivedAddress.getHostAddress() + " received ip address");
		DHCPServerAddress = InetAddress.getByAddress(serverIpAddress);
		System.out.println(DHCPServerAddress.getHostAddress() + " server ip address ");
		
		// check if the offer came from the right server ( = the server we sent a request to ) 
		if (serverIdentifierAddress != null && ! DHCPServerAddress.equals(serverIdentifierAddress)){
			throw new UnknownHostException("The received ip adresses in the DHCP packet and in the options do not match.");
		}
	}

	// a function to parse some general data 
	private void parse_general_data(byte[] DHCPData)
			throws UnknownHostException {
		// get all the options from the received packet
		byte[] options = Arrays.copyOfRange(DHCPData, 236, DHCPData.length);
		
		// if the options only contain the magic cookie, return
		if (! Arrays.equals(Arrays.copyOf(options, 4), MAGIC_COOKIE)){
			return ;
		}
		int index = 4;
		while (true){
			
			// nb 255 is the end option, so we can stop parsing
			if (options[index] == (byte) 255){
				break;
			}
			// get the number of the option
			byte optionNb = options[index];
			// get the length of the option
			int optionLength = (int) options[index + 1] & 0xFF;
			// get the contents
			byte[] optionContents = Arrays.copyOfRange(options, index + 2, index + 2 + optionLength);
			
			// parse this option
			parse_option(optionNb, optionLength, optionContents);
			
			// go to the next one 
			index+= 2 + optionLength;
		}
	}

	private void parse_option(byte optionNb, int optionLength, byte[] optionContents) throws UnknownHostException {
		switch (optionNb) {
		// this option is used to convey the type of the DHCP message. its length is 1
		// legal values are:
		// 1    DHCPDISCOVER
		//        2     DHCPOFFER
		//        3     DHCPREQUEST
		//        4     DHCPDECLINE
		//        5     DHCPACK
		//        6     DHCPNAK
		//        7     DHCPRELEASE
		//        8     DHCPINFORM
		case (byte) 53:
			this.DHCPMessageNumber = optionContents[0];
		break;

		// used in DHCPOFFER and DHCPREQUEST messages, to allow the
		// client to distinguish between lease offers. 
		case (byte) 54:
			this.serverIdentifierAddress = InetAddress.getByAddress(optionContents);
		break;

		// ip address lease time
		// used in a client request to allow the client to request
		// a lease time for the IP address 
		// a server can use this option to specify the lease time it is willing to offer 
		case (byte) 51:
			this.ipAddressLeaseTime = 16777216 * (int) (optionContents[0] & 0xFF) +  65536 * (int) (optionContents[1] & 0xFF) + 256 * (int) (optionContents[2] & 0xFF) + (int) (optionContents[3] & 0xFF);
		break;

		default:
			break;
		}
	}

	// a function to check if the given mac is a valid mac address
	private boolean is_valid_mac(DatagramPacket packet){
		if (packet == null){
			return false;
		}
		
		// get the received data
		byte[] rawData = packet.getData();
		// extract the mac address from it 
		byte[] possibleMac = Arrays.copyOfRange(rawData, 28, 44);
		return Arrays.equals(MAC, possibleMac);
	}

	
	// a function to create one big array from all the given arrays
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


	public InetAddress getIpAddress() {
		return ipAddress;
	}


	public int getPort() {
		return portOutgoing;
	}


	public DatagramPacket getDHCP_discover_packet() {
		return DHCP_packet;
	}


	public InetAddress getDHCPReceivedAddress() {
		return DHCPReceivedAddress;
	}


	public InetAddress getDHCPServerAddress() {
		return DHCPServerAddress;
	}


	public byte[] getReceivedIpAddress() {
		return receivedIpAddress;
	}


	public byte[] getServerIpAddress() {
		return serverIpAddress;
	}


	public byte[] getMAC() {
		return MAC;
	}


	public DatagramSocket getClient_connection() {
		return client_connection_outgoing;
	}


	public byte getDHCPMessageType() {
		return DHCPMessageNumber;
	}


	public InetAddress getServerIdentifierAddress() {
		return serverIdentifierAddress;
	}


	public int getIpAddressLeaseTime() {
		return ipAddressLeaseTime;
	}


	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}


	public void setDHCP_discover_packet(DatagramPacket dHCP_discover_packet) {
		DHCP_packet = dHCP_discover_packet;
	}


	public void setDHCPReceivedAddress(InetAddress dHCPReceivedAddress) {
		DHCPReceivedAddress = dHCPReceivedAddress;
	}


	public void setDHCPServerAddress(InetAddress dHCPServerAddress) {
		DHCPServerAddress = dHCPServerAddress;
	}


	public void setReceivedIpAddress(byte[] receivedIpAddress) {
		this.receivedIpAddress = receivedIpAddress;
	}


	public void setServerIpAddress(byte[] serverIpAddress) {
		this.serverIpAddress = serverIpAddress;
	}


	// a function to set the client connection
	public void setClient_connection() throws SocketException {
		// create a new outgoing connection
		this.client_connection_outgoing = new DatagramSocket();
		// in case only one port was given, the incoming connectiont is equal to the outgoing
		if ( this.portIncoming == this.portOutgoing){
			this.client_connection_incoming = this.client_connection_outgoing; 
		}
		// create a new incoming connection 
		else {
			this.client_connection_incoming = new DatagramSocket(this.portIncoming); 
		}


	}


	public void setDHCPMessageType(byte dHCPMessageType) {
		DHCPMessageNumber = dHCPMessageType;
	}


	public void setServerIdentifierAddress(InetAddress serverIdentifierAddress) {
		this.serverIdentifierAddress = serverIdentifierAddress;
	}


	public void setIpAddressLeaseTime(int ipAddressLeaseTime) {
		this.ipAddressLeaseTime = ipAddressLeaseTime;
	}

}
