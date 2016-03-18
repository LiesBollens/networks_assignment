package DHCPShared;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DHCPHelper {
	public static long byte_to_long(byte[] bytes) {
		long val = 0;
		for (int i = 0; i < bytes.length; i++) {
			val <<= 8;
			val |= bytes[i] & 0xFF;
		}
		return val;
	}
	
	public static byte[] long_to_byte(long ip){
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(ip);
		return Arrays.copyOfRange(buffer.array(),4,8);
	}
}
