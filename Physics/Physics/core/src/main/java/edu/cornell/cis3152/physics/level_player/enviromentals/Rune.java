package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.cis3152.physics.level_player.utils.Event;
import edu.cornell.cis3152.physics.level_player.utils.EventAction;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.HashSet;

public class Rune extends ObstacleSprite {

    private float powerLevel = 0;
    private float prevPowerLevel = 0;
    private float r = 1.5f;
    private float dispersalRate = 1/(60f * 10);

    private int inLight = 0;
    private boolean latch;
    private HashSet<EventAction<?>> eventActions;

    public Rune (float x, float y, float units, boolean latch) {
        obstacle = new WheelObstacle(x,y, r);
        this.latch = latch;
        this.eventActions = new HashSet<>();
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
        if (latch && powerLevel >= 1) {
            dispersalRate = 0;
        }
    }

    public void dissapatePowerLevel() {
        prevPowerLevel = powerLevel;
        if (powerLevel > 0) {
            powerLevel -= dispersalRate;
        }
    }

    public float getRadius () {return r;}

    public void addInLight() {inLight++;}
    public void subInLight() {inLight--;}
    public boolean returnInLight() {return inLight > 0;}
    public void registerEventAction(EventAction<?> eventAction) {eventActions.add(eventAction);}
    public HashSet<EventAction<?>> getEventAction() {return eventActions;}

}
