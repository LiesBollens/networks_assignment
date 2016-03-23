package DHCPServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;



public class ServerLeaseController extends Thread {
	
	private DHCP_Server server;
	private HashMap<String, StoredConnection> StoredConnections; 
	// ip address, mac, lease time 
	private ArrayList<String[]> ClientTable;

	public ServerLeaseController(DHCP_Server server, HashMap<String, StoredConnection> StoredConnections, ArrayList<String[]> ClientTable) {
		this.server = server;
		this.StoredConnections = StoredConnections;
		this.ClientTable = ClientTable;
		
		// for a clean shutdown 
		setDaemon(true);
	}

	public void run() {
		int ratio = 0;
		while (true) {
			// print the leased addresses 
			this.server.print_leased_addresses();
			for (String[] entry: ClientTable) {
				System.out.println(Arrays.toString(entry));
			}
			// check the lease client table
			checkLeaseClientTable();
			ratio++;
			if (ratio > 3) {
				// If a DHCP-offer was sent but no DHCP-request was received, the connection would remain a stored connection forever. 
				// we need to check this less often than we need to check the client table 
				checkLeaseStoredConnections();
				ratio = 0;
			}


			try {
				// sleep for 3 seconds before checking the table again 
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// a function to check the stored connections table 
	private void checkLeaseStoredConnections() {
		// get the current time
		long current_time = System.currentTimeMillis();
		
		// iterate over all the entries from the stored connection
		Iterator<Entry<String, StoredConnection>> iter = this.StoredConnections.entrySet().iterator();
		while (iter.hasNext()) {
			// get the current entry 
			Entry<String, StoredConnection> entry = iter.next();
			// get the corresponding value
			StoredConnection currentConnection = entry.getValue();

			// get the end of lease time
			long end_of_lease = currentConnection.getStartOfLease() + currentConnection.getIpAddressLeaseTime()*1000;

			// if we're past the lease time, remove the entry from the stored connections table 
			if (current_time > end_of_lease) {
				iter.remove();
			}

		}
	}

	// a function to check the lease client table
	private void checkLeaseClientTable() {
		int i = 0;
		
		// get the current time
		long current_time = System.currentTimeMillis();
		
		// iterate over all the entries from the client table
		while (i < ClientTable.size()) {
			// if the current time is bigger than the lease time, remove the connection from the server 
			if (current_time > Long.parseLong(ClientTable.get(i)[2])) {
				server.removeConnection(ClientTable.get(i)[1]);
			}
			else {
				i++;
			}

		}
	}
}
