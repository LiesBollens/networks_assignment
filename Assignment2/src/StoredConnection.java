import java.net.DatagramPacket;
import java.net.InetAddress;

public class StoredConnection {
	
	private InetAddress DHCPReceivedAddress;
	private InetAddress DHCPServerAddress;
	//private InetAddress serverIdentifierAddress;
	private byte[] macAddress; 
	private byte[] xid; 
	private byte[] hlen; 
	private byte[] opcode; 
	private DHCPMessageType DHCPMessageType;
	private int ipAddressLeaseTime;
	private int startOfLease;
	
	
	public StoredConnection(int leaseTime, int startOfLease, DHCPMessageType type,  byte[] macAddress, byte[] xid, byte[] hlen, byte[] opcode, InetAddress DHCPReceivedAddress, InetAddress DHCPServerAddress){
		setMacAddress(macAddress);
		setXid(xid);
		setHlen(hlen);
		setOpcode(opcode);
		setDHCPReceivedAddress(DHCPReceivedAddress);
		setDHCPServerAddress(DHCPServerAddress);
		setDHCPMessageType(type);
		setIpAddressLeaseTime(leaseTime); 
		setStartOfLease(startOfLease); 
	}
	
	
	public InetAddress getDHCPReceivedAddress() {
		return DHCPReceivedAddress;
	}


	public InetAddress getDHCPServerAddress() {
		return DHCPServerAddress;
	}


	public byte[] getMacAddress() {
		return macAddress;
	}


	public byte[] getXid() {
		return xid;
	}


	public byte[] getHlen() {
		return hlen;
	}


	public byte[] getOpcode() {
		return opcode;
	}


	public DHCPMessageType getDHCPMessageType() {
		return DHCPMessageType;
	}


	public int getIpAddressLeaseTime() {
		return ipAddressLeaseTime;
	}


	public int getStartOfLease() {
		return startOfLease;
	}


	public void setDHCPReceivedAddress(InetAddress dHCPReceivedAddress) {
		DHCPReceivedAddress = dHCPReceivedAddress;
	}


	public void setDHCPServerAddress(InetAddress dHCPServerAddress) {
		DHCPServerAddress = dHCPServerAddress;
	}


	public void setMacAddress(byte[] macAddress) {
		this.macAddress = macAddress;
	}


	public void setXid(byte[] xid) {
		this.xid = xid;
	}


	public void setHlen(byte[] hlen) {
		this.hlen = hlen;
	}


	public void setOpcode(byte[] opcode) {
		this.opcode = opcode;
	}


	public void setDHCPMessageType(DHCPMessageType dHCPMessageType) {
		DHCPMessageType = dHCPMessageType;
	}


	public void setIpAddressLeaseTime(int ipAddressLeaseTime) {
		this.ipAddressLeaseTime = ipAddressLeaseTime;
	}


	public void setStartOfLease(int startOfLease) {
		this.startOfLease = startOfLease;
	}


	
	
	
}


