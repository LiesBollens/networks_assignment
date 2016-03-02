import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Server {
	public static void main(String args[]) throws Exception {
		DatagramSocket client_connection = new DatagramSocket(12345);
		byte[] data = new byte[1024];
		byte[] receiveData = new byte[1024];

		String sendData = "Wij zijn mooi.";
		data = sendData.getBytes();
		System.out.println("Voor");
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		client_connection.receive(receivePacket);
		System.out.println("Na");
		InetAddress server_adress = receivePacket.getAddress();
		int port = receivePacket.getPort();
		String returnString = new String(receivePacket.getData());
		System.out.println("From server: " + returnString);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, server_adress, port);
		client_connection.send(sendPacket);

	}
}
