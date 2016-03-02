import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class DHCP_Client {
	public static void main(String args[]) throws Exception {
		DatagramSocket client_connection = new DatagramSocket();
		InetAddress server_adress = InetAddress.getByName("192.168.2.102");
		byte[] data = new byte[1024];
		byte[] receiveData = new byte[1024];
		String sendData = "Wij zijn mooi.";
		data = sendData.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, server_adress, 12345);
		client_connection.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		client_connection.receive(receivePacket);
		String returnString = new String(receivePacket.getData());
		System.out.println("From server: " + returnString);
		client_connection.close();
	}

}
