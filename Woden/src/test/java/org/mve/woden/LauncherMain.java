package org.mve.woden;

import org.gradle.launcher.Main;
import org.gradle.launcher.bootstrap.ExecutionListener;
import org.gradle.launcher.cli.DefaultCommandLineActionFactory;

import java.util.Arrays;

public class LauncherMain extends Main implements ExecutionListener
{
	@Override
	public void run(String[] args)
	{
		try
		{
			// this.doAction(args, this);
			new DefaultCommandLineActionFactory().convert(Arrays.asList(args)).execute(this);
		}
		catch (Throwable e)
		{
			this.createErrorHandler().execute(e);
		}
	}

	@Override
	public void onFailure(Throwable throwable)
	{
	}
}
