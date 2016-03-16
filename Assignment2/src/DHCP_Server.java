import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;


public class DHCP_Server {

	private int port; 
	private int max_connections;

	public DHCP_Server(int port, String config){
		this.port = port;
		config = convert_config_path(config);
		parse_config_file(config);
		System.out.println(max_connections);




	}

	private String convert_config_path(String config) {
		URL config_url = getClass().getResource(config);
		if (config_url != null) {
			return config_url.getPath();
		}
		return config;
	}

	private void parse_config_file(String config_path) {
		boolean file_load_succesfull = true;
		
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream(config_path);
			prop.load(input);

		} catch( Exception e ){
			System.out.println(e);
			file_load_succesfull = false; 
		}
		
		String max_conn = null;
		
		if (file_load_succesfull) {
			max_conn = prop.getProperty("max_connections");
		}
		
		if (max_conn != null) {
			this.max_connections = Integer.parseInt(max_conn);
		}
		else {
			this.max_connections = 3;
		}
	}
}
