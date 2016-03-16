import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;



public class DHCP_main {
	public static void main(String args[]) throws Exception {
//		
//		// get the mac adddress from the computer 
//		byte[] macAddress = null;
//		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//        while(networkInterfaces.hasMoreElements())
//        {
//            NetworkInterface network1 = networkInterfaces.nextElement();
//            byte[] mac = network1.getHardwareAddress();
//            if(mac != null)
//            {
//                macAddress = mac;
//            }
//        }
//        if (macAddress == null) {
//    		InetAddress ip = InetAddress.getLocalHost();
//    		System.out.println(ip);
//    		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
//    			System.out.println(network);
//		macAddress = network.getHardwareAddress(); 
//		
//        }
//        
//        System.out.println(macAddress);
//		
//		DHCP_Client client = new DHCP_Client("192.168.1.1", 68, 67, macAddress);
//
//		System.out.println("Received IP-address: " + client.execute_DHCP().getHostAddress());		
		
		new DHCP_Server(500, "server.conf");
	
	}
}
