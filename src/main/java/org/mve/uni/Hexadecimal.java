package org.mve.uni;

public class Hexadecimal
{
	private static final byte[] HEX = "0123456789ABCDEF".getBytes();

	public static byte[] encode(byte[] data)
	{
		byte[] result = new byte[data.length << 1];
		for (int i = 0; i < data.length; i++)
		{
			result[(i << 1) | 0] = HEX[(data[i] >> 4) & 0xF];
			result[(i << 1) | 1] = HEX[(data[i] >> 0) & 0xF];
		}
		return result;
	}
}
