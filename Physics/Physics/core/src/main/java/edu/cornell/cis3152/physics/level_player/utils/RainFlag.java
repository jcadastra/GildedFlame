package edu.cornell.cis3152.physics.level_player.utils;

import edu.cornell.cis3152.physics.level_player.enviromentals.EnhancedObstacleSprite;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.ArrayList;

/**
 * consolidated data structure for fire flag class to be used to send info from fire controller to
 * game scene
 */
public class RainFlag {

    /**
     * name of the action associated with the flag
     */
    private String name;

    /**
     * @return name of the action being taken
     */
    public String getName() {
        return name;
    }

    /**
     * the EOS, not obstaclesprite cause EOS can take a material, that the fire is affecting
     */
    private ObstacleSprite subject;
    public int rainNum;

    /**
     * @return the subject that will be affected by the fire flag
     */
    public ObstacleSprite getSubject() {
        return subject;
    }

    /**
     * creates a fire flag relating to only one fire
     *
     * @param name name of the flag being called
     * @param subject the object that is related to the fire
     */
    public RainFlag(String name, ObstacleSprite subject, int rainNum) {
        this.name = name;
        this.subject = subject;
        this.rainNum = rainNum;
    }

}
