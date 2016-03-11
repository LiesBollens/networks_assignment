import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;



public class DHCP_main {
	public static void main(String args[]) throws Exception {
		// get the mac adddress from the computer 
		InetAddress ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] macAddress = network.getHardwareAddress(); 
		
		DHCP_Client client = new DHCP_Client("192.168.2.1", 67,macAddress);
		System.out.println("Received IP-address: " + client.execute_DHCP().getHostAddress());		
	}
}
