package edu.cornell.cis3152.physics.level_player.utils;

import edu.cornell.cis3152.physics.level_player.enviromentals.EnhancedObstacleSprite;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import java.util.ArrayList;

public class FireFlag {

    private String name;

    public String getName() {
        return name;
    }

    private EnhancedObstacleSprite subject;

    public EnhancedObstacleSprite getSubject() {
        return subject;
    }

    private Fire fire;

    public Fire getFire() {
        return fire;
    }
    private ArrayList<Fire> fires;

    public ArrayList<Fire> getFires() {
        return fires;
    }

    public FireFlag(String name, EnhancedObstacleSprite subject, Fire fire) {
        this.name = name;
        this.subject = subject;
        this.fire = fire;
    }

    public FireFlag(String name, EnhancedObstacleSprite subject, ArrayList<Fire> fires) {
        this.name = name;
        this.subject = subject;
        this.fires = fires;
    }

}
