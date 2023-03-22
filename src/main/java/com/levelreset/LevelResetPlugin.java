package com.levelreset;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.levelreset.models.NewLevel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@Slf4j
@PluginDescriptor(
        name = "Level Reset"
)
public class LevelResetPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LevelResetConfig config;

    private XpReset xpReset;
    private List<Skill> skillsToSet;

    @Override
    protected void startUp() throws Exception {
        performStartup();
    }

    @Override
    protected void shutDown() {
        performStartup();
    }

    @Subscribe
    public void onPluginChanged(PluginChanged pluginChanged) {
        // this is guaranteed to be called after the plugin has been registered by the eventbus. startUp is not.
        if (pluginChanged.getPlugin() == this) {
            performStartup();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (!configChanged.getGroup().equals("levelReset")) {
            return;
        }

        performStartup();
    }

    @Provides
    LevelResetConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LevelResetConfig.class);
    }

    private HashMap<Skill, NewLevel> updatedSkills = new HashMap<>();

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent e)
    {
        final String eventName = e.getEventName();
        if(!eventName.equals("skillTabBaseLevel")) return;

        for(int i = 0; i < skillsToSet.size(); i++){
            Skill skill = skillsToSet.get(i);
            final int exp = client.getSkillExperience(skill);
            NewLevel newLevel = xpReset.getAdjustedLevel(exp);
            if(updatedSkills.containsKey(skill) && updatedSkills.get(skill).Xp == newLevel.Xp) continue;
            setSkillXp(skill, newLevel);
            updatedSkills.put(skill, newLevel);
        }
    }

    private void performStartup(){
        xpReset = new XpReset(config.level());
        skillsToSet = parseSkills();
        simulateSkillChange();
        updatedSkills.clear();
    }

    private void simulateSkillChange()
    {
        for (Skill skill : Skill.values())
        {
            if (skill != Skill.OVERALL)
            {
                client.queueChangedSkill(skill);
            }
        }
    }

    private void setSkillXp(Skill skill, NewLevel newLevel){
        client.getBoostedSkillLevels()[skill.ordinal()] = newLevel.Level;
        client.getRealSkillLevels()[skill.ordinal()] = newLevel.Level;
        client.getSkillExperiences()[skill.ordinal()] = newLevel.Xp;

        client.queueChangedSkill(skill);

        StatChanged statChanged = new StatChanged(
                skill,
                newLevel.Xp,
                newLevel.Level,
                newLevel.Level
        );
        eventBus.post(statChanged);
    }

    private List<Skill> parseSkills() {
        return Arrays.stream(config.skills().split(","))
                .map(String::trim)
                .map(skillName -> {
                    try {
                        return Skill.valueOf(skillName.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
