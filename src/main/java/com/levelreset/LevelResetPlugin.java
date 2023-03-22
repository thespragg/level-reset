package com.levelreset;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
        name = "Level Reset"
)
public class LevelResetPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LevelResetConfig config;

    @Override
    protected void startUp() throws Exception {
        clientThread.invoke(this::simulateSkillChange);
    }

    @Override
    protected void shutDown() {
        clientThread.invoke(this::simulateSkillChange);
    }

    @Subscribe
    public void onPluginChanged(PluginChanged pluginChanged) {
        // this is guaranteed to be called after the plugin has been registered by the eventbus. startUp is not.
        if (pluginChanged.getPlugin() == this) {
            clientThread.invoke(this::simulateSkillChange);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (!configChanged.getGroup().equals("levelReset")) {
            return;
        }

        clientThread.invoke(this::simulateSkillChange);
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent e) {
        final String eventName = e.getEventName();
        if (!eventName.equals("skillTabBaseLevel")) return;

        final int[] intStack = client.getIntStack();
        final int intStackSize = client.getIntStackSize();

        final int skillId = intStack[intStackSize - 2];
        final Skill skill = Skill.values()[skillId];
        final int exp = client.getSkillExperience(skill);
        final String skillName = skill.getName();

        String[] skills = parseSkills();
        if(!Arrays.stream(skills).anyMatch(skillName::equalsIgnoreCase)) return;

        // alter the local variable containing the level to show
        intStack[intStackSize - 1] = Experience.getLevelForXp(exp);
    }

    @Provides
    LevelResetConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LevelResetConfig.class);
    }

    private void simulateSkillChange() {
        for (Skill skill : Skill.values()) {
            if (skill != Skill.OVERALL) {
                client.queueChangedSkill(skill);
            }
        }
    }

    private String[] parseSkills() { return Arrays.stream(config.skills().split(",")).map(String::trim).toArray(String[]::new); }
}
