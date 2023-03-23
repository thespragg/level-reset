/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.levelreset.levelUpDisplay;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;

import com.levelreset.LevelResetPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Experience;
import net.runelite.api.FontID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.chatbox.ChatboxInput;
import net.runelite.client.input.KeyListener;

public class LevelUpDisplayInput extends ChatboxInput implements KeyListener
{
    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 16;

    private final LevelResetPlugin plugin;
    private final Skill skill;

    @Getter
    private boolean closeMessage;

    public LevelUpDisplayInput(LevelResetPlugin plugin, Skill skill)
    {
        this.plugin = plugin;
        this.skill = skill;
    }

    @Override
    public void open()
    {
        plugin.getClientThread().invoke(this::setFireworksGraphic);

        final Widget chatboxContainer = plugin.getChatboxPanelManager().getContainerWidget();

        final String skillName = skill.getName();
        final int skillExperience = plugin.getClient().getSkillExperience(skill);
        final int skillLevel = Experience.getLevelForXp(skillExperience);
        final List<SkillModel> skillModels = SkillModel.getSKILL_MODELS(skill);
        final String prefix = (skill == Skill.AGILITY || skill == Skill.ATTACK) ? "an " : "a ";

        final Widget levelUpLevel = chatboxContainer.createChild(-1, WidgetType.TEXT);
        final Widget levelUpText = chatboxContainer.createChild(-1, WidgetType.TEXT);
        final Widget levelUpContinue = chatboxContainer.createChild(-1, WidgetType.TEXT);

        final String levelUpLevelString;
        if (skillExperience == Experience.MAX_SKILL_XP)
        {
            levelUpLevelString = "Congratulations, you just maxed your " + skillName + " skill.";
        }
        else
        {
            levelUpLevelString = "Congratulations, you just advanced " + prefix + skillName + " level.";
        }
        levelUpLevel.setText(levelUpLevelString);
        levelUpLevel.setTextColor(Color.BLACK.getRGB());
        levelUpLevel.setFontId(FontID.QUILL_8);
        levelUpLevel.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpLevel.setOriginalX(73 + X_OFFSET);
        levelUpLevel.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpLevel.setOriginalY(15 + Y_OFFSET);
        levelUpLevel.setOriginalWidth(390);
        levelUpLevel.setOriginalHeight(30);
        levelUpLevel.setXTextAlignment(WidgetTextAlignment.CENTER);
        levelUpLevel.setYTextAlignment(WidgetTextAlignment.LEFT);
        levelUpLevel.setWidthMode(WidgetSizeMode.ABSOLUTE);
        levelUpLevel.revalidate();

        final String levelUpTextString;
        if (skillExperience == Experience.MAX_SKILL_XP)
        {
            levelUpTextString = "You have reached maximum experience in " + skillName;
        }
        else
        {
            levelUpTextString = (skill == Skill.HITPOINTS
                    ? "Your Hitpoints are now " + skillLevel
                    : "Your " + skillName + " level is now " + skillLevel)
                    + '.';
        }
        levelUpText.setText(levelUpTextString);
        levelUpText.setFontId(FontID.QUILL_8);
        levelUpText.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpText.setOriginalX(73 + X_OFFSET);
        levelUpText.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpText.setOriginalY(44 + Y_OFFSET);
        levelUpText.setOriginalWidth(390);
        levelUpText.setOriginalHeight(30);
        levelUpText.setXTextAlignment(WidgetTextAlignment.CENTER);
        levelUpText.setYTextAlignment(WidgetTextAlignment.LEFT);
        levelUpText.setWidthMode(WidgetSizeMode.ABSOLUTE);
        levelUpText.revalidate();

        levelUpContinue.setText("Click here to continue");
        levelUpContinue.setTextColor(Color.BLUE.getRGB());
        levelUpContinue.setFontId(FontID.QUILL_8);
        levelUpContinue.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpContinue.setOriginalX(73 + X_OFFSET);
        levelUpContinue.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpContinue.setOriginalY(74 + Y_OFFSET);
        levelUpContinue.setOriginalWidth(390);
        levelUpContinue.setOriginalHeight(17);
        levelUpContinue.setXTextAlignment(WidgetTextAlignment.CENTER);
        levelUpContinue.setYTextAlignment(WidgetTextAlignment.LEFT);
        levelUpContinue.setWidthMode(WidgetSizeMode.ABSOLUTE);
        levelUpContinue.setAction(0, "Continue");
        levelUpContinue.setOnOpListener((JavaScriptCallback) ev -> triggerCloseViaMessage());
        levelUpContinue.setOnMouseOverListener((JavaScriptCallback) ev -> levelUpContinue.setTextColor(Color.WHITE.getRGB()));
        levelUpContinue.setOnMouseLeaveListener((JavaScriptCallback) ev -> levelUpContinue.setTextColor(Color.BLUE.getRGB()));
        levelUpContinue.setHasListener(true);
        levelUpContinue.revalidate();

        for (SkillModel skillModel : skillModels)
        {
            buildWidgetModel(chatboxContainer, skillModel);
        }

        plugin.getChatMessageManager().queue(QueuedMessage.builder()
                .type(ChatMessageType.GAMEMESSAGE)
                .runeLiteFormattedMessage(skillExperience == Experience.MAX_SKILL_XP
                        ? "Congratulations, you've just reached max experience in " + skillName + '!'
                        : "Congratulations, you've just advanced your " + skillName + " level. You are now virtual level " + skillLevel + '.')
                .build());
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        if (e.getKeyChar() != ' ')
        {
            return;
        }

        triggerCloseViaMessage();

        e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    public void closeIfTriggered()
    {
        if (closeMessage && plugin.getChatboxPanelManager().getCurrentInput() == this)
        {
            plugin.getChatboxPanelManager().close();
        }
    }

    void triggerClose()
    {
        closeMessage = true;
    }

    private void triggerCloseViaMessage()
    {
        final Widget levelUpContinue = plugin.getClient().getWidget(WidgetInfo.CHATBOX_CONTAINER).getChild(2);
        levelUpContinue.setText("Please wait...");

        closeMessage = true;
    }

    private static void buildWidgetModel(Widget chatboxContainer, SkillModel model)
    {
        final Widget levelUpModel = chatboxContainer.createChild(-1, WidgetType.MODEL);

        levelUpModel.setModelId(model.getModelID());
        levelUpModel.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpModel.setOriginalX(model.getOriginalX() + X_OFFSET);
        levelUpModel.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpModel.setOriginalY(model.getOriginalY() + Y_OFFSET);
        levelUpModel.setOriginalWidth(model.getIconWidth());
        levelUpModel.setOriginalHeight(model.getIconHeight());
        levelUpModel.setRotationX(model.getRotationX());
        levelUpModel.setRotationY(model.getRotationY());
        levelUpModel.setRotationZ(model.getRotationZ());
        levelUpModel.setModelZoom(model.getModelZoom());
        levelUpModel.revalidate();
    }

    private void setFireworksGraphic()
    {
        final Player localPlayer = plugin.getClient().getLocalPlayer();
        if (localPlayer == null)
        {
            return;
        }

        final int fireworksGraphic = LevelUpFireworks.NINETY_NINE.getGraphicId();

        if (fireworksGraphic == -1)
        {
            return;
        }

        localPlayer.setGraphic(fireworksGraphic);
        localPlayer.setSpotAnimFrame(0);
    }
}