package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.cis3152.physics.level_player.utils.EventAction;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;
import java.util.HashSet;

public class Rune extends ObstacleSprite {

    private float powerLevel = 0;
    private float prevPowerLevel = 0;
    private float r = 1.5f;
    private float secondsToFullDissipation = 15;
    private float dispersalRate = 1/(60f * secondsToFullDissipation);

    private int inLight = 0;
    private float currentLatchThreshold = 0;
    private float[] thresholds;
    private HashSet<EventAction<?>> eventActions;
    public String setTargetName;

    public Rune (float x, float y, float units, float[] thresholds) {
        obstacle = new WheelObstacle(x,y, r);
        this.eventActions = new HashSet<>();
        this.thresholds = thresholds;
        obstacle.setBodyType(BodyType.StaticBody);
        obstacle.setSensor(true);
        obstacle.setName("rune");
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );
    }

    public float getPowerLevel() {
        return powerLevel;
    }
    public float getPrevPowerLevel() {
        return prevPowerLevel;
    }

    public void addPowerLevel(float val) {
        prevPowerLevel = powerLevel;
        powerLevel += val;
        for (float th : thresholds) {
            if (powerLevel >= th) {
                currentLatchThreshold = th;
            } else {
                break;
            }
        }
    }

    public void dissapatePowerLevel() {
        prevPowerLevel = powerLevel;
        if (powerLevel > 0) {
            powerLevel -= dispersalRate;
            if (powerLevel < currentLatchThreshold) {
                powerLevel = currentLatchThreshold;
            }
        }
    }

    public float getRadius () {return r;}

    public void addInLight() {inLight++;}
    public void subInLight() {inLight--;}
    public boolean returnInLight() {return inLight > 0;}
    public void registerEventAction(EventAction<?> eventAction) {eventActions.add(eventAction);}
    public HashSet<EventAction<?>> getEventAction() {return eventActions;}

}
