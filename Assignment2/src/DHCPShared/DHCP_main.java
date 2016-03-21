package DHCPShared;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;

import DHCPClient.DHCP_Client;
import DHCPServer.DHCP_Server;





public class DHCP_main {
	static boolean IsClient = false; 
	public static void main(String args[]) throws Exception , FileNotFoundException{
//		
//		// get the mac adddress from the computer 
		if (IsClient){
		
			byte[] macAddress = null;
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
	        while(networkInterfaces.hasMoreElements())
	        {
	            NetworkInterface network1 = networkInterfaces.nextElement();
	            byte[] mac = network1.getHardwareAddress();
	            if(mac != null)
	            {
	                macAddress = mac;
	            }
	        }
	        if (macAddress == null) {
	    		InetAddress ip = InetAddress.getLocalHost();
	    		System.out.println(ip);
	    		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	    			System.out.println(network);
			macAddress = network.getHardwareAddress(); 
			
	        }
	        
	        System.out.println(macAddress);
			
			DHCP_Client client = new DHCP_Client("172.20.10.2", 1234, 1234, macAddress);

			System.out.println("Received IP-address: " + client.execute_DHCP().getHostAddress());		
			
		} else {
			DHCP_Server server = new DHCP_Server("/server.conf");
			server.start();
			while (true) {
				Thread.sleep(10000);
			}
		}
		
		
		
		
	}
}
