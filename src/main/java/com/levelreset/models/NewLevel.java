package com.levelreset.models;

import lombok.AccessLevel;
import lombok.Getter;

public class NewLevel {
    @Getter(AccessLevel.PUBLIC)
    int level;
    @Getter(AccessLevel.PUBLIC)
    public int xp;

    public NewLevel(int level, int xp){
        this.level = level;
        this.xp = xp;
    }
}
