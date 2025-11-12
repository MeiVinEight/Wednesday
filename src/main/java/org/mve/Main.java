package org.mve;

public class Main
{
	public static void main(String[] args)
	{
		Wednesday wednesday = new Wednesday();
		Runtime.getRuntime().addShutdownHook(new Thread(wednesday::close));
	}
}
