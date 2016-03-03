

public class DHCP_main {
	public static void main(String args[]) throws Exception{
		DHCP_Client client = new DHCP_Client();
		client.send_discover();
	}
}
