package org.mve.mixin;

import net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge;
import org.mve.logging.LoggerManager;

@Mixin(MiraiLoggerFactoryImplementationBridge.class)
public class MiraiLoggerFactoryImplementationBridgeMixin
{
	@Inject(value = "createPlatformInstance()Lnet/mamoe/mirai/utils/MiraiLogger$Factory;")
	private void createPlatformInstance(Callback ci)
	{
		ci.returning(LoggerManager.FACTORY);
	}
}
