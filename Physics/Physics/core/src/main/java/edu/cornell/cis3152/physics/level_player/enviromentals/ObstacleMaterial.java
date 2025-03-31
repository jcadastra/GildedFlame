package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.utils.JsonValue;
import java.util.Objects;

/**
 * ObstacleMaterial is a material that is associated with a given obstacle
 * holds some general values and used in conjunction with ENhancedObstacleSPrite to hold
 * flammability values for an item
 */
public class ObstacleMaterial {

    private String name;

    // Chance a given flame successfully propagates per update, if > 0 then can catch fire
    private float flammability;

    // internal timer to track for how long an object has been fully engulfed in fire, if passes
    // limit then expires
    private float burnTimer;
    private float burnTimerLimit;

    // internal timer to track for how long an object has been in contact with a fire
    // the implementation is such that if there is more than one fire source touching an obj
    // then it increases in speed of catching fire and should burst in flames both times
    // if greater then limit, the obj is marked for destruction
    private float ignitionTimer;
    private float ignitionTimerLimit;


    public ObstacleMaterial (String name, JsonValue data) {
        this.name = name;
        switch (name) {
            case "wood":
                this.flammability = 0.05f;
                this.burnTimerLimit = 80f;
                this.ignitionTimerLimit = 15f;
                break;

            case "rope":
                this.flammability = 0.05f;
                this.burnTimerLimit = 600f;
                this.ignitionTimerLimit = 8f;
                break;

            default:
                this.flammability = 0f;
                this.burnTimerLimit = 0f;
                this.ignitionTimerLimit = 0f;
                break;
        }
        this.burnTimer = 0f;
        this.ignitionTimer = 0f;
    }

    public float getFlammability() {
        return flammability;
    }
    public void incrementBurnTimer() {
        burnTimer++;
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
