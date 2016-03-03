import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;




public class DHCP_Client {
	public void send_discover() throws Exception {
		byte bin = (byte) 0xA2;
		System.out.println(bin);

		DatagramSocket client_connection = new DatagramSocket();
		InetAddress server_adress = InetAddress.getByName("192.168.2.101");
		
		byte[] receiveData = new byte[1024];
		byte[] data = DHCP_discover();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, server_adress, 12345);
		client_connection.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		client_connection.receive(receivePacket);
		String returnString = new String(receivePacket.getData());
		System.out.println("From server: " + returnString);
		client_connection.close();
	}

	public byte[] DHCP_discover(){
		byte[] op = {(byte) 0x1};
		byte[] htype = {(byte) 0x1};
		byte[] hlen = {(byte) 0x6};
		byte[] hops = {(byte) 0x0};
		byte[] xid = {(byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22};
		byte[] secs = {(byte) 0x0, (byte) 0x0 };
		byte[] ciaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] yiaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] siaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] giaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};
		byte[] chaddr_1 = {(byte) 0xac, (byte) 0xbc, (byte) 0x32, (byte) 0xc4, (byte) 0xcd, (byte) 0x27};
		byte[] chaddr_2 = new byte[10];
		Arrays.fill( chaddr_2, (byte) 0 );
		byte[] chaddr = array_concatenate(chaddr_1, chaddr_2);
		byte[] sname = new byte[64];
		Arrays.fill(sname, (byte) 0 );
		byte[] file = new byte[128];
		Arrays.fill(file, (byte) 0 );


		byte[] option_msg_type = { (byte) 53, (byte) 1, (byte) 1};// DCHP discover request, # 53
		byte[] option_ ={(byte) 55, (byte) 4, (byte) 1, (byte) 3, (byte) 15, (byte) 6 }; // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)
		
		// return one big byte array, containing all the above 
		byte[] dhcp_discover_packet = array_concatenate(op, htype, hlen, hops, xid, secs, ciaddr, yiaddr, siaddr, giaddr, chaddr, sname, file, option_msg_type, option_); 
		System.out.println(dhcp_discover_packet);
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
