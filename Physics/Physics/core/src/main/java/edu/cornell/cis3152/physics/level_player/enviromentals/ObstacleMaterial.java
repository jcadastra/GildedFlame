package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.utils.JsonValue;

public class ObstacleMaterial {

    private String name;

    // Chance a given flame successfully propagates per update
    private float flammability;
    private float burnTimer;
    private float burnTimerLimit;

    private float ignitionTimer;
    private float ignitionTimerLimit;


    public ObstacleMaterial (String name, JsonValue data) {
        //TODO: process from json
        this.name = name;
        this.flammability =  .05f;
        this.burnTimerLimit = 80f;
        this.ignitionTimerLimit = 15f;
        this.burnTimer = 0f;
        this.ignitionTimer = 0f;
    }

    public float getFlammability() {
        return flammability;
    }
    public void incrementBurnTimer() {
        burnTimer++;
//        System.out.println(burnTimer);
    }
    public boolean isExpiredBurnTimer() {
        return burnTimer > burnTimerLimit;
    }
    public boolean surpassIgnitionTimer(Integer v) {
        return v > ignitionTimerLimit;
    }

    public String getName() {
        return name;
    }
}
