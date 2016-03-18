package DHCPServer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.omg.CORBA.DATA_CONVERSION;

import com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl;

import DHCPShared.DHCPHelper;
import DHCPShared.DHCPMessageType;
import DHCPShared.DHCP_package;
import sun.java2d.cmm.kcms.KcmsServiceProvider;



public class ServerProcessPacket implements  Runnable {
	private byte[] clientIpAddress;
	private byte[] serverIpAddress;
	private DHCP_Server server;
	private DatagramPacket packet; 
	private InetAddress DHCPReceivedAddress;
	private InetAddress DHCPServerAddress;
	private InetAddress serverIdentifierAddress;
	private byte[] macAddress; 
	private byte[] xid; 
	private byte[] hlen; 
	private byte[] opcode; 
	private DHCPMessageType DHCPMessageNumber;
	private int ipAddressLeaseTime;
	private static InetAddress DISCOVER_ADDRESS ;
	private static byte[] MAGIC_COOKIE = {(byte) 99, (byte) -126, (byte) 83, (byte) 99};
	private StoredConnection storedConnection; 
	private DatagramSocket clientConnection; 
	
	
	public ServerProcessPacket(DHCP_Server server, DatagramPacket packet, DatagramSocket clientConnection){
		this.server = server;
		this.packet = packet; 
		this.clientConnection = clientConnection; 
		try {
			this.DISCOVER_ADDRESS =  InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	public void run() {
		try {
			int parseReturnCode = parse_DHCP_packet(packet);
			if ( parseReturnCode < 0 ){
				return; 
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return; 
		}
		System.out.println("received packet: " + DHCPMessageNumber.getNumber());
		System.out.println(Arrays.toString(packet.getData()));
		try {
			updateStoredConnection();
			sendReply();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private int parse_DHCP_packet(DatagramPacket offer) throws UnknownHostException{
		if (offer == null){
			return -1;
		}
		byte[] DHCPData = offer.getData();
		
		serverIpAddress = Arrays.copyOfRange(DHCPData, 20, 24);
		DHCPServerAddress = InetAddress.getByAddress(serverIpAddress);
		System.out.println(DHCPServerAddress.getHostAddress() + " server ip address ");
		if (!DHCPServerAddress.equals(this.server.getAddress())){
			if (! DHCPServerAddress.equals(DISCOVER_ADDRESS)){
				return -1;
			}
		}
		
		clientIpAddress = Arrays.copyOfRange(DHCPData, 16, 20);
		DHCPReceivedAddress = InetAddress.getByAddress(clientIpAddress);
		System.out.println(DHCPReceivedAddress.getHostAddress() + " received ip address");
		parse_general_data(DHCPData);
		// if the address is not equal to the server address and the message is not a discover message,
		// return
		if (!DHCPServerAddress.equals(this.server.getAddress())){
			if( ! (DHCPServerAddress.equals(DISCOVER_ADDRESS) && DHCPMessageNumber == DHCPMessageNumber.DHCPDISCOVER)){
				return -1; 
			}
		}
		
		
		
		opcode = Arrays.copyOfRange(DHCPData, 0, 1);
		hlen = Arrays.copyOfRange(DHCPData, 2, 3);
		xid = Arrays.copyOfRange(DHCPData, 4, 8);
		macAddress = Arrays.copyOfRange(DHCPData, 28, 28 + (int) hlen[0] & 0xFF);
		return 0; 
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
			DHCPMessageNumber = DHCPMessageNumber.getMessageType(optionContents[0]);
		break;
		
		case (byte) 55:
			System.out.println("parameters requested");
		break; 
		
		
		
		// used in DHCPOFFER and DHCPREQUEST messages, to allow the
		// client to distinguish between lease offers. 
		case (byte) 54:
			serverIdentifierAddress = InetAddress.getByAddress(optionContents);
		break;
		
		// ip address lease time
		// used in a client request to allow the client to request
		// a lease time for the IP address 
		// a server can use this option to specify the lease time it is willing to offer 
		case (byte) 51:
			ipAddressLeaseTime = 16777216 * (int) (optionContents[0] & 0xFF) +  65536 * (int) (optionContents[1] & 0xFF) + 256 * (int) (optionContents[2] & 0xFF) + (int) (optionContents[3] & 0xFF);
		break;

		default:
			break;
		}
	}
	
	private void updateStoredConnection() throws UnknownHostException{
		System.out.println(Arrays.toString(macAddress));
		storedConnection = this.server.getConnection(macAddress);
		if (DHCPMessageNumber == DHCPMessageType.DHCPDISCOVER){
			System.out.println("Recieved discover");
			if( storedConnection!= null ){
				// remove the connection from the server file
				this.server.removeConnection(macAddress);
			}
			StoredConnection connection = new StoredConnection(this.server.getLease_time(), System.currentTimeMillis(), DHCPMessageType.DHCPDISCOVER, macAddress, xid, hlen, opcode, InetAddress.getByAddress(this.server.get_next_available_ip()), DHCPServerAddress);
			this.server.addConnection(connection);
			storedConnection = connection;
		}
		else if (DHCPMessageNumber == DHCPMessageType.DHCPREQUEST){
			System.out.println("Recieved request");
			if ( storedConnection == null){
				return;
			} else {
				storedConnection.setXid(xid);
				storedConnection.setDHCPMessageType(DHCPMessageType.DHCPREQUEST);
				storedConnection.setStartOfLease(System.currentTimeMillis());
			}
		}
		else if (DHCPMessageNumber == DHCPMessageType.DHCPRELEASE){
			System.out.println("Received release");
			this.server.removeConnection(macAddress);
		}
	}
	
	private void sendReply() throws IOException{
		

		DHCP_package offerPackage = new DHCP_package(macAddress);
		if(DHCPMessageNumber == DHCPMessageType.DHCPDISCOVER){
			setGenericParameters(offerPackage, DHCPMessageType.DHCPOFFER);
			DatagramPacket sendPacket = new DatagramPacket(offerPackage.get_package(), offerPackage.get_package().length, this.packet.getAddress(), this.server.getOutgoingPort());
			clientConnection.send(sendPacket);
			this.storedConnection.setStartOfLease(System.currentTimeMillis());
			this.storedConnection.setDHCPMessageType(DHCPMessageType.DHCPOFFER);
			System.out.println("Offer was sent");
		}
		else if (DHCPMessageNumber == DHCPMessageType.DHCPREQUEST){
			byte[] ipAddress = storedConnection.getDHCPReceivedAddress().getAddress();
			String[] tableEntry = this.server.getByIpAddress(ipAddress);
			System.out.println(Arrays.toString(tableEntry));
			
			if (tableEntry != null && ! tableEntry[1].equals(Arrays.toString(macAddress)) ){
				setGenericParameters(offerPackage, DHCPMessageType.DHCPNAK);
				DatagramPacket sendPacket = new DatagramPacket(offerPackage.get_package(), offerPackage.get_package().length, this.packet.getAddress(), this.server.getOutgoingPort());
				clientConnection.send(sendPacket);
				System.out.println("NACK was sent");
				
			}
			else {
				this.server.refreshClientTable(clientIpAddress, macAddress, System.currentTimeMillis() + 1000*storedConnection.getIpAddressLeaseTime());
				setGenericParameters(offerPackage, DHCPMessageType.DHCPACK);
				DatagramPacket sendPacket = new DatagramPacket(offerPackage.get_package(), offerPackage.get_package().length, this.packet.getAddress(), this.server.getOutgoingPort());
				clientConnection.send(sendPacket);
				this.storedConnection.setStartOfLease(System.currentTimeMillis());
				this.storedConnection.setDHCPMessageType(DHCPMessageType.DHCPACK);
				System.out.println("ACK was sent");
			}
			
		} 
		else if (DHCPMessageNumber == DHCPMessageType.DHCPRELEASE){
			return; 
		}
	}
	
	private void setGenericParameters(DHCP_package packet, DHCPMessageType DHCPType) throws UnknownHostException{
		byte[] op = {(byte) 2};
		packet.setOp(op);
		packet.setXid(this.xid);
		packet.setYiaddr(storedConnection.getDHCPReceivedAddress().getAddress());
		packet.setSiaddr(this.server.getAddress().getAddress());
		byte[] chaddr2  = new byte[10];
		packet.setChaddr(DHCP_package.array_concatenate(storedConnection.getMacAddress() , chaddr2));
		byte[] option51_1 = { (byte) 51, (byte) 4};
		byte[] option51_2 = DHCPHelper.long_to_byte(storedConnection.getIpAddressLeaseTime());
		byte[] option54_1 = { (byte) 54, (byte) 4} ;
		byte[] option54_2 = this.server.getAddress().getAddress();
		packet.setOption_(DHCP_package.array_concatenate(option51_1, option51_2, option54_1, option54_2));
		packet.set_message_type(DHCPType);
		
	}

	
		


	
}
