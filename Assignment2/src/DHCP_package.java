import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class DHCP_package {
	public DHCP_package(byte[] MAC) throws UnknownHostException, SocketException{
		this.chaddr = MAC; 
	}
	

	public byte[] getOp() {
		return op;
	}

	public byte[] getHtype() {
		return htype;
	}

	public byte[] getHlen() {
		return hlen;
	}

	public byte[] getHops() {
		return hops;
	}

	public byte[] getXid() {
		return xid;
	}

	public byte[] getSecs() {
		return secs;
	}

	public byte[] getFlags() {
		return flags;
	}

	public byte[] getCiaddr() {
		return ciaddr;
	}

	public byte[] getYiaddr() {
		return yiaddr;
	}

	public byte[] getSiaddr() {
		return siaddr;
	}

	public byte[] getGiaddr() {
		return giaddr;
	}

	public byte[] getChaddr() {
		return chaddr;
	}

	public byte[] getSname() {
		return sname;
	}

	public byte[] getFile() {
		return file;
	}

	public byte[] getOption_msg_type() {
		return option_msg_type;
	}

	public byte[] getOption_() {
		return option_;
	}

	public void setOp(byte[] op) {
		this.op = op;
	}

	public void setHtype(byte[] htype) {
		this.htype = htype;
	}

	public void setHlen(byte[] hlen) {
		this.hlen = hlen;
	}

	public void setHops(byte[] hops) {
		this.hops = hops;
	}

	public void setXid(byte[] xid) {
		this.xid = xid;
	}

	public void setSecs(byte[] secs) {
		this.secs = secs;
	}

	public void setFlags(byte[] flags) {
		this.flags = flags;
	}

	public void setCiaddr(byte[] ciaddr) {
		this.ciaddr = ciaddr;
	}

	public void setYiaddr(byte[] yiaddr) {
		this.yiaddr = yiaddr;
	}

	public void setSiaddr(byte[] siaddr) {
		this.siaddr = siaddr;
	}

	public void setGiaddr(byte[] giaddr) {
		this.giaddr = giaddr;
	}

	public void setChaddr(byte[] chaddr) {
		this.chaddr = chaddr;
	}

	public void setSname(byte[] sname) {
		this.sname = sname;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public void setOption_msg_type(byte[] option_msg_type) {
		this.option_msg_type = option_msg_type;
	}

	public void setOption_(byte[] option_) {
		this.option_ = option_;
	}


	// 1 octet, message operation code/message type
	// 1 = BOOTREQUEST, 2 = BOOTREPLY
	byte[] op = {(byte) 0x1};

	// 1 byte, hardware address type
	byte[] htype = {(byte) 0x1};

	// 1 byte, hardware address length
	byte[] hlen = {(byte) 0x6};

	// 1byte, client sets to zero
	byte[] hops = {(byte) 0x0};

	// 4 bytes, Transaction ID, a random number chosen by the
	//client, used by the client and server to associate
	//messages and responses between a client and a
	//server.
	byte[] xid = {(byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22};

	// 2 bytes, 
	//Filled in by client, seconds elapsed since client
	//began address acquisition or renewal process.
	byte[] secs = {(byte) 0x0, (byte) 0x0 };

	// Z bytes, flags
	byte[] flags = {(byte) 0x0, (byte) 0x0};

	// 4 bytes
	// client ip address
	byte[] ciaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};

	// 4 bytes
	// client IP address
	byte[] yiaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};

	// 4 bytes
	// IP address of next server to use in bootstrap;
	//returned in DHCPOFFER, DHCPACK by server.
	byte[] siaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};

	// 4 bytes
	// relay agent IP address, used in booting
	// via a relay agent 
	byte[] giaddr = {(byte) 0x0,(byte) 0x0,(byte) 0x0,(byte) 0x0};

	// 16 bytes, client hardware ( = MAC) address
	byte[] chaddr;


	private static byte[] MAGIC_COOKIE = {(byte) 99, (byte) -126, (byte) 83, (byte) 99};

	// 64 bytes
	// optional server host name, null terminated string
	byte[] sname = new byte[64];

	// 128 bytes
	// boot file name, null terminated string.
	//"generic" name or null in DHCPDISCOVER, fully qualified
	//directory-path name in DHCPOFFER.
	byte[] file = new byte[128];

	byte[] option_end = { (byte) 255};

	// the message type 
	byte[] option_msg_type = { (byte) 53, (byte) 1, (byte) 1};// DCHP discover request, # 53

	// optional data 
	byte[] option_ ={(byte) 55, (byte) 4, (byte) 1, (byte) 3, (byte) 15, (byte) 6}; // parameter request list, list # 55, then request subnet mask(1), router(3), domain name(15), domain name server(6)


	public byte[] get_package(){
		return array_concatenate(op, htype, hlen, hops, xid, secs, flags, ciaddr, yiaddr, siaddr, giaddr, chaddr, sname, file, MAGIC_COOKIE, option_msg_type, option_, option_end);
	}

	// a function to create one big array from all the given arrays
	byte[]  array_concatenate(byte[]... array){
		int total_length = 0;
		for ( int i=0; i< array.length; i++){
			total_length += array[i].length;
		}
		byte[] concatenated = new byte[total_length];
		int index_concatenated = 0;
		for (int i = 0; i< array.length; i++){
			for (int j= 0; j < array[i].length; j++){
				concatenated[index_concatenated] = array[i][j];
				index_concatenated++;

			}
		}
		return concatenated;
	}

	public void set_message_type(DHCPMessageType type){
		byte[] message_type = { (byte) 53, (byte) 1, (byte) type.getNumber()};
		setOption_msg_type(message_type );
	}
}
