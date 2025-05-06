package edu.cornell.cis3152.physics.level_player.enviromentals;

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

    private int smokeTimerLimit;


    public ObstacleMaterial (String name) {
        this.name = name;
        switch (name) {
            case "torch":
                this.flammability = 0f;
                this.burnTimerLimit = 0f;
                this.ignitionTimerLimit = 0f;
                this.smokeTimerLimit = 5;
                break;
            case "wood":
                this.flammability = 0.05f;
                this.burnTimerLimit = 80f;
                this.ignitionTimerLimit = 15f;
                this.smokeTimerLimit = 10;
                break;

            case "rope":
                this.flammability = 1f;
                this.burnTimerLimit = 24f;
                this.ignitionTimerLimit = 22f;
                this.smokeTimerLimit = 3;
                break;

            case "infinite":
                this.flammability = 1f;
                this.burnTimerLimit = 0f;
                this.ignitionTimerLimit = 1f;
                this.smokeTimerLimit = 10;
                break;
            case "stone":
            case "iron":
            default:
                this.flammability = 0f;
                this.burnTimerLimit = 0f;
                this.ignitionTimerLimit = 0f;
                this.smokeTimerLimit = -1;
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
        return burnTimer > burnTimerLimit && burnTimerLimit > 0;
    }
    public void resetBurnTimer() {burnTimer = 0f;}
    public boolean surpassIgnitionTimer(Integer v) {
        return v > ignitionTimerLimit;
    }
    public boolean makesSmoke() { return smokeTimerLimit > -1;}

    public boolean checkSmokeTime(int val) {
        return val >= smokeTimerLimit;
    }

    public String getName() {
        return name;
    }
}
