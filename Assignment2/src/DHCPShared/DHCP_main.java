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
	static boolean IsClient = true; 
	public static void main(String args[]) throws Exception , FileNotFoundException{
//		
//		// code to be executed when running as a client
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
			
	        // create a new DHCP client 
			DHCP_Client client = new DHCP_Client("172.20.10.2", 1238, 1237, macAddress);

			// start the process of getting an IP-address, with renewal when the lease time is up
			client.DHCP_with_renual();
			
		} else {
			// create a new server, with the configurations from the config file 
			DHCP_Server server = new DHCP_Server("/server.conf");
			// start the server 
			server.start();
			while (true) {
				Thread.sleep(10000);
			}
		}
		
		
		
		
	}
}
