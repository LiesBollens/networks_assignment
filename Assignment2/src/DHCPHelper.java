
public class DHCPHelper {
	public static long byte_to_long(byte[] bytes) {
		long val = 0;
		for (int i = 0; i < bytes.length; i++) {
			val <<= 8;
			val |= bytes[i] & 0xFF;
		}
		return val;
	}
}
