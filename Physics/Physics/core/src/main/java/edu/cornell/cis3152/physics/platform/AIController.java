package edu.cornell.cis3152.physics.platform;

public class AIController {
    // public class AIController implements InputController

    // Constants for chase algorithms
    /**
     * How close the light must be for the moth to chase it
     */
    private static final int CHASE_DIST = 3;
    /**
     * How close the player must be for the moth to attack it
     */
    private static final int ATTACK_DIST = 1;

    /**
     * The number of ticks since we started this controller
     */
    private long ticks;
}
