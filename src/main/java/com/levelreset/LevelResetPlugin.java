package com.levelreset;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.levelreset.levelUpDisplay.LevelUpDisplayInput;
import com.levelreset.levelUpDisplay.LevelUpOverlay;
import com.levelreset.models.NewLevel;
import com.levelreset.utils.XpReset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;


// 0 credit taken for anything inside the levelUpDisplay package, it has been taken almost verbatim from https://github.com/Nightfirecat/plugin-hub-plugins/blob/virtuallevelups/src/main/java/at/nightfirec/virtuallevelups
@Slf4j
@PluginDescriptor(
        name = "Level Reset"
)
public class LevelResetPlugin extends Plugin {
    @Getter(AccessLevel.PUBLIC)
    @Inject
    private Client client;

    @Getter(AccessLevel.PUBLIC)
    @Inject
    private ClientThread clientThread;

    @Getter(AccessLevel.PUBLIC)
    @Inject
    private ChatMessageManager chatMessageManager;

    @Getter(AccessLevel.PUBLIC)
    @Inject
    private ChatboxPanelManager chatboxPanelManager;

    @Getter(AccessLevel.PUBLIC)
    @Inject
    private LevelResetConfig config;

    @Inject
    private SpriteManager spriteManager;

    private XpReset xpReset;
    private List<Skill> skillsToSet;

    @Getter(AccessLevel.PUBLIC)
    private BufferedImage reportButton;

    private LevelUpDisplayInput input;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private LevelUpOverlay overlay;

    @Getter(AccessLevel.PACKAGE)
    private final List<Skill> skillsLeveledUp = new ArrayList<>();

    @Override
    protected void startUp() throws Exception {
        spriteManager.getSpriteAsync(SpriteID.CHATBOX_REPORT_BUTTON, 0, s -> reportButton = s);
        overlayManager.add(overlay);
        performStartup();
    }

    @Override
    public void shutDown() {
        overlayManager.remove(overlay);

        if (input != null && chatboxPanelManager.getCurrentInput() == input) {
            chatboxPanelManager.close();
        }

        skillsLeveledUp.clear();
        input = null;
    }

    @Subscribe
    public void onPluginChanged(PluginChanged pluginChanged) {
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

//    @Subscribe
//    public void onScriptCallbackEvent(ScriptCallbackEvent e)
//    {
//        final String eventName = e.getEventName();
//        if(!eventName.equals("skillTabBaseLevel")) return;
//
//        for(int i = 0; i < skillsToSet.size(); i++){
//            Skill skill = skillsToSet.get(i);
//            final int exp = client.getSkillExperience(skill);
//            NewLevel newLevel = xpReset.getAdjustedLevel(exp);
//            if(updatedSkills.containsKey(skill) && updatedSkills.get(skill).Xp == newLevel.Xp) continue;
//            if(updatedSkills.containsKey(skill) && updatedSkills.get(skill).Level < newLevel.Level) skillsLeveledUp.add(skill);
//            setSkillXp(skill, newLevel);
//            updatedSkills.put(skill, newLevel);
//        }
//    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        final Skill skill = event.getSkill();
        if (!skillsToSet.contains(skill)) return;

        int exp = client.getSkillExperience(skill);
        NewLevel newLevel = xpReset.getAdjustedLevel(exp);
        setSkillXp(skill, newLevel);

        int oldLevel = updatedSkills.get(skill) != null ? updatedSkills.get(skill).getLevel() : newLevel.getLevel();
        if (oldLevel < newLevel.getLevel()) {
            skillsLeveledUp.add(skill);
        }
        updatedSkills.put(skill, newLevel);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (input != null) {
            input.closeIfTriggered();
        }

        if (skillsLeveledUp.isEmpty() || !chatboxPanelManager.getContainerWidget().isHidden()) {
            return;
        }

        final Skill skill = skillsLeveledUp.remove(0);

        input = new LevelUpDisplayInput(this, skill, xpReset);
        chatboxPanelManager.openInput(input);
    }

    private void performStartup() {
        xpReset = new XpReset(config.level());
        skillsToSet = parseSkills();
        simulateSkillChange();
        updatedSkills.clear();
    }

    private void simulateSkillChange() {
        for (Skill skill : Skill.values()) {
            if (skill != Skill.OVERALL) {
                client.queueChangedSkill(skill);
            }
        }
    }

    private void setSkillXp(Skill skill, NewLevel newLevel) {
        client.getBoostedSkillLevels()[skill.ordinal()] = newLevel.getLevel();
        client.getRealSkillLevels()[skill.ordinal()] = newLevel.getLevel();
        client.getSkillExperiences()[skill.ordinal()] = newLevel.getXp();

        client.queueChangedSkill(skill);
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
