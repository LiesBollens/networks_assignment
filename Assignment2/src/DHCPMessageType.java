
public enum DHCPMessageType {
	
	DHCPDISCOVER(1),
	DHCPOFFER(2),
	DHCPREQUEST(3),
	DHCPDECLINE(4),
	DHCPACK(5),
	DHCPNAK(6),
	DHCPRELEASE(7),
	DHCPINFORM(8);
	
	private int number;

	DHCPMessageType(int number) {
		this.number = number; 
		
	}
	
	public int getNumber(){
		return this.number; 
	}
	
	public static DHCPMessageType getMessageType(byte number){
		int num = (int) number & 0xFF; 
		for (DHCPMessageType type : DHCPMessageType.values()){
			if ( type.getNumber() == num){
				return type;
			}
		}
		return null; 
	}
	
}
