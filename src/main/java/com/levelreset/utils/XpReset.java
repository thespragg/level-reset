package com.levelreset.utils;

import com.levelreset.models.NewLevel;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Experience;

import java.util.Arrays;

public class XpReset {
    private static final int[] XP_LEVELS = {0,83,174,276,388,512,650,801,969,1154,1358,1584,1833,2107,2411,2746,3115,3523,3973,4470,5018,5624,6291,7028,7842,8740,9730,10824,12031,13363,14833,16456,18247,20224,22406,24815,27473,30408,33648,37224,41171,45529,50339,55649,61512,67983,75127,83014,91721,101333,111945,123660,136594,150872,166636,184040,203254,224466,247886,273742,302288,333804,368599,407015,449428,496254,547953,605032,668051,737627,814445,899257,992895,1096278,1220421,1336443,1475581,1629200,1798808,1986068,2192818,2421087,2673114,2951373,3258594,3597792,3972294,4385776,4842295,5346332,5902831,6517253,7195629,7944614,8771558,9684577,10692629,11805606,13034431};
    private int[] adjustedXpLevels;
    private int resetLevel;
    @Getter(AccessLevel.PUBLIC)
    float ratio;

    public XpReset(int resetLevel){
        this.resetLevel = resetLevel;
        int newTotalXp = XP_LEVELS[98] - XP_LEVELS[resetLevel - 1];
        float ratio = (float)XP_LEVELS[98] / (float)newTotalXp;
        adjustedXpLevels = Arrays.stream(XP_LEVELS).map(val -> Math.round(val / ratio)).toArray();
    }

    public NewLevel getAdjustedLevel(int currentXP) {
        int currentLevel = Experience.getLevelForXp(currentXP);
        if(currentLevel < resetLevel || currentLevel >= 99) return new NewLevel(currentLevel, currentXP);

        int newTotalXp = currentXP - XP_LEVELS[resetLevel - 1];
        int adjustedLevel = getNewLevel(newTotalXp);
        return new NewLevel(adjustedLevel == -1 ? currentLevel : adjustedLevel, newTotalXp);
    }

    public int getNewLevel(int xp){
        for(int i = 0; i < adjustedXpLevels.length; i++){
            if(i == adjustedXpLevels.length - 1) return 99;
            if(xp >= adjustedXpLevels[i] && xp < adjustedXpLevels[i + 1]) return i + 1;
        }
        return -1;
    }
}
