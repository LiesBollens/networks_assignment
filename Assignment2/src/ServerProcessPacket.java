import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;



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
	
	
	public ServerProcessPacket(DHCP_Server server, DatagramPacket packet){
		this.server = server;
		this.packet = packet; 
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



	
}
