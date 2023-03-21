package com.levelreset;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("levelReset")
public interface LevelResetConfig extends Config
{
	@ConfigItem(
		keyName = "level",
		name = "Level",
		description = "At what level the plugin should reset back to 1."
	)
	default int level()
	{
		return 92;
	}
}
