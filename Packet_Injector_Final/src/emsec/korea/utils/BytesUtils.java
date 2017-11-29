package emsec.korea.utils;

public class BytesUtils {

	public static String toString(byte b)
	{
		StringBuffer sb = new StringBuffer(2);
		int i = (b & 0xF0) >> 4;
		int j = b & 0x0F;
		sb.append(new Character((char)((i > 9) ? (65 + i - 10) : (48 + i))));
		sb.append(new Character((char)((j > 9) ? (65 + j - 10) : (48 + j))));
		
		return sb.toString();
	}	 

	public static String HextoString(byte[] byteArray)
	{
		if(byteArray==null)
			return "NULL";
		return HextoString(byteArray, 0, byteArray.length);
	}
	
	public static String HextoString(byte[] byteArray, int off, int len)
	{
		StringBuffer sb = new StringBuffer(2 * len);
		for (int i = 0; i < len; i++) {
			sb.append(toString(byteArray[off + i]));
		}
		
		return sb.toString();
	}
	
	public static String HextoStringwithcol(byte[] byteArray, int off, int len)
	{
		if (byteArray.length < off + len)
			len = byteArray.length - off;
		StringBuffer sb = new StringBuffer(2 * len);
		boolean flag = true;
		for (int i = 0; i < len; i++) {
			
			if (flag)
				flag = false;
			else
				sb.append(":");
			sb.append(toString(byteArray[off + i]));
		}
		
		return sb.toString();
	}
	
	public static byte[] HexstringToHex(String str)
	{
		byte[] buffer = new byte[str.length()/2];
		
		for(int i=0;i<buffer.length;i++)
		{
			buffer[i] = (byte)Integer.parseInt(str.substring(2*i, (2*i)+2), 16);
		}
	
		return buffer;
	}
}
