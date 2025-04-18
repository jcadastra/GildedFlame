package edu.cornell.cis3152.physics.level_player.utils;

import edu.cornell.cis3152.physics.level_player.enviromentals.EnhancedObstacleSprite;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.cis3152.physics.level_player.enviromentals.Smoke;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.ArrayList;

/**
 * consolidated data structure for fire flag class to be used to send info from fire controller to
 * game scene
 */
public class FireFlag {

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
    private EnhancedObstacleSprite subject;

    /**
     * @return the subject that will be affected by the fire flag
     */
    public EnhancedObstacleSprite getSubject() {
        return subject;
    }

    /**
     * the smoke
     */
    private ObstacleSprite smoke;

    /**
     * @return the smoke
     */
    public ObstacleSprite getSmoke() {
        return smoke;
    }

    /**
     * fire that is associated with this flag, if it is a singlet (up to user to define vs fires)
     */
    private Fire fire;

    /**
     * @return fire that is associated with this flag, if it is a singlet (up to user to define vs fires)
     */
    public Fire getFire() {
        return fire;
    }

    /**
     * fires that are associated with this flag, if it is a multi (up to user to define vs fire)
     */
    private ArrayList<Fire> fires;

    /**
     * @return fires that are associated with this flag, if it is a multi (up to user to define vs fire)
     */
    public ArrayList<Fire> getFires() {
        return fires;
    }

    /**
     * creates a fire flag relating to only one fire
     *
     * @param name name of the flag being called
     * @param subject the object that is related to the fire
     * @param fire the fire that is either created or referenced, dependent on action type
     */
    public FireFlag(String name, EnhancedObstacleSprite subject, Fire fire) {
        this.name = name;
        this.subject = subject;
        this.fire = fire;
    }

    /**
     * creates a fire flag relating to multiple fires
     *
     * @param name name of the flag being called
     * @param subject the object that is related to the fire
     * @param fires the fires that are either created or referenced, dependent on action type
     */
    public FireFlag(String name, EnhancedObstacleSprite subject, ArrayList<Fire> fires) {
        this.name = name;
        this.subject = subject;
        this.fires = fires;
    }

    public FireFlag(String name, Fire fire, ObstacleSprite smoke) {
        this.name = name;
        this.fire = fire;
        this.smoke = smoke;
    }

    public FireFlag(String name, Fire fire) {
        this.name = name;
        this.fire = fire;
    }
}
