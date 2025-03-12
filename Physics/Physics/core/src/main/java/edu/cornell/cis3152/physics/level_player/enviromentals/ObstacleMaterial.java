package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.utils.JsonValue;

public class ObstacleMaterial {

    private String name;
    private float flammability;

    public ObstacleMaterial (String name, JsonValue data) {
        //TODO: process from json
        this.name = "wood";
        this.flammability =  0;
    }

    public float getFlammability() {
        return flammability;
    }

}
