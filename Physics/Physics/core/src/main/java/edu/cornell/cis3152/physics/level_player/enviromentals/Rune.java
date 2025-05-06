package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.cis3152.physics.level_player.utils.EventAction;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Rune extends ObstacleSprite {

    static int counter = 0;
    public int ID;
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
    private TextureRegion[][] runeSigilSet;
    private float rotationDeg;
    private float units;
    private float width;
    private float height;
    private float sigilRadius;
    private float yOffsetForRuneCenter;

    public Rune (float x, float y, float width, float height, float rotationDeg, float units, float[] thresholds, Texture runeSigil) {
        super(new WheelObstacle(x,y, .61f/40 * units));
        this.sigilRadius = .61f/40 * units;
        this.rotationDeg = rotationDeg;

        getObstacle().setPhysicsUnits( units );
        getObstacle().setUserData( this );
        getObstacle().setBodyType(BodyType.StaticBody);
        getObstacle().setSensor(true);
        getObstacle().setName("rune");
        getObstacle().setAngle((float) Math.toRadians(rotationDeg));
        this.units = units;
        this.yOffsetForRuneCenter = (.23f * units);
        mesh.set(
            -width/2 * units, -height/2 * units - yOffsetForRuneCenter,
            width * units, height * units
        );
        this.width = width;
        this.height = height;
        this.eventActions = new HashSet<>();
        this.thresholds = thresholds;
        this.runeSigilSet = TextureRegion.split(runeSigil,400,520);
        ID = counter;
        counter++;
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
