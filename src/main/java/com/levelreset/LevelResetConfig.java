package com.levelreset;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.util.Arrays;

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

	@ConfigItem(
			keyName = "skills",
			name = "Skills",
			description = "What skills the plugin should reset. (Comma separated)"
	)
	default String skills()
	{
		return "";
	}
}
