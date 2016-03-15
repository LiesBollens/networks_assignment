import java.io.BufferedReader;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.sun.security.auth.login.ConfigFile;


public class DHCP_Server {
	
	private int port; 
	
	public DHCP_Server(int port, String config){
		this.port = port;
		File configFile = null; 
		try {
			configFile = new File(config);
		} catch( NullPointerException e ){
			return; 
		}
		BufferedReader bufferedReader = new BufferedReader(ConfigFile);
		
		
		
		
	}
	
}
