package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.utils.JsonValue;

public class ObstacleMaterial {

    private String name;

    // Chance a given flame successfully propagates per update
    private float flammability;


    public ObstacleMaterial (String name, JsonValue data) {
        //TODO: process from json
        this.name = "wood";
        this.flammability =  .005f;
    }

    public float getFlammability() {
        return flammability;
    }

}
