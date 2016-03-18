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
		setDaemon(true);
	}

	public void run() {
		int ratio = 0;
		while (true) {
			for (String[] entry: ClientTable) {
				System.out.println(Arrays.toString(entry));
			}
			checkLeaseClientTable();
			ratio++;
			if (ratio > 3) {
				// If a DHCP-offer was sent but no DHCP-request was received, the connection would remain a stored connection forever. 
				checkLeaseStoredConnections();
				ratio = 0;
			}


			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void checkLeaseStoredConnections() {
		long current_time = System.currentTimeMillis();
		Iterator<Entry<String, StoredConnection>> iter = this.StoredConnections.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, StoredConnection> entry = iter.next();
			StoredConnection currentConnection = entry.getValue();
			long end_of_lease = currentConnection.getStartOfLease() + currentConnection.getIpAddressLeaseTime()*1000;
			if (current_time > end_of_lease) {
				iter.remove();
			}

		}
	}

	private void checkLeaseClientTable() {
		int i = 0;
		long current_time = System.currentTimeMillis();
		while (i < ClientTable.size()) {
			if (current_time > Long.parseLong(ClientTable.get(i)[2])) {
				server.removeConnection(ClientTable.get(i)[1]);
			}
			else {
				i++;
			}

		}
	}
}
