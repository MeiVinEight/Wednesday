package org.mve.sn.message.app;

public class UnknownApp extends BaseLightApp
{
	public UnknownApp(String content)
	{
		super(content);
	}

	@Override
	public String toString()
	{
		return "Unknown App: " + this.content;
	}
}
