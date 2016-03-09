import java.util.Arrays;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class DHCP_main {
	public static void main(String args[]) throws Exception {
		DHCP_Client client = new DHCP_Client();
		byte[] packet = client.DHCP_discover();
		client.send_packet(packet);
		
		
		
		
		
	}
}
