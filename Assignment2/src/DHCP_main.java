import java.util.Arrays;



public class DHCP_main {
	public static void main(String args[]) throws Exception {
		DHCP_Client client = new DHCP_Client("10.33.14.246", 1234);
		System.out.println("Received IP-address: " + client.execute_DHCP().getHostAddress());		
	}
}
