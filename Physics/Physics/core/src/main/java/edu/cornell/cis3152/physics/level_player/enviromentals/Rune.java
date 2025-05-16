package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.cis3152.physics.level_player.utils.EventAction;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Rune extends ObstacleSprite {

    private float powerLevel = 0;
    public void setPowerLevel(float powerLevel) { this.powerLevel = powerLevel; }
    private float prevPowerLevel = 0;
    private float r = 1.5f;
    private float secondsToFullDissipation = 15;
    private float dispersalRate = 1/(60f * secondsToFullDissipation);
    private float chargeRate;

    public int inLight = 0;
    private float currentLatchThreshold = 0;
    private float[] thresholds;
    private HashSet<EventAction<?>> eventActions;
    private HashMap<Integer, Lighting> lightRefer;
    public String setTargetName;
    private TextureRegion[][] runeSigilSet;
    private float rotationDeg;
    private float units;
    private float width;
    private float height;
    private float sigilRadius;
    private float yOffsetForRuneCenter;
    private static int counter =0;
    public int ID;


    public Rune (float x, float y, float width, float height, float rotationDeg, float units, float[] thresholds, Texture runeSigil) {
        super(new WheelObstacle(x,y, .61f));
        this.sigilRadius = .61f/40 * units;
        this.rotationDeg = rotationDeg;

        getObstacle().setPhysicsUnits( units );
        getObstacle().setUserData( this );
        getObstacle().setBodyType(BodyType.StaticBody);
        getObstacle().setSensor(true);
        getObstacle().setName("rune");
        getObstacle().setAngle((float) Math.toRadians(rotationDeg));
        lightRefer = new HashMap<>();
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
        this.ID = counter;
        counter++;
    }

    public float getPowerLevel() {
        return powerLevel;
    }
    public float getPrevPowerLevel() {
        return prevPowerLevel;
    }

    private boolean triggeredPowerLevel = false;
    public void resetTriggeredPowerLevel() {triggeredPowerLevel = false;}
    public void addPowerLevel() {
        if (!triggeredPowerLevel) {
            prevPowerLevel = powerLevel;
            powerLevel += chargeRate;
            for (float th : thresholds) {
                if (powerLevel >= th) {
                    currentLatchThreshold = th;
                } else {
                    break;
                }
            }
            triggeredPowerLevel = true;
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

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        int idx;
        if (powerLevel == 0) {
            idx = 0;
        } else if (powerLevel >= 1) {
            idx = 10;
        } else {
            idx = (int) (powerLevel / .11) + 1;
        }
        TextureRegion region = runeSigilSet[0][idx];

        float texW = region.getRegionWidth();
        float texH = region.getRegionHeight();
        float drawW = width * units;
        float drawH = height * units;

        float offsetTexPx = yOffsetForRuneCenter * (texH / drawH);
        SpriteBatch.computeTransform(
            transform, texW * 0.5f, texH * 0.5f + offsetTexPx,
            obstacle.getX() * units,  obstacle.getY() * units,
            rotationDeg, drawW / texW,  drawH / texH
        );

        batch.draw(region, transform);
    }

    public float getRadius () {return r;}


    public void addInLight(Lighting l) {
        inLight++;
        lightRefer.put(l.hashCode(), l);
    }
    public void subInLight(Lighting l) {
        inLight--;
        lightRefer.remove(l.hashCode());
    }
    public void setTimeTo(float val) {chargeRate = 1/(60f * val);}
    public void setDissipateTime (float val) {secondsToFullDissipation = val; dispersalRate = 1/(60f * secondsToFullDissipation);}
    public boolean returnInLight() {
        for (Lighting l : lightRefer.values()) {
            System.out.println(l.getRadius() + sigilRadius);
            System.out.println(l.getObstacle().getPosition().add(obstacle.getPosition()).len() +  " alen");
            if (l.getRadius() + sigilRadius > l.getObstacle().getPosition().add(obstacle.getPosition()).len()) {
                inLight--;
                lightRefer.remove(l.hashCode());
            }
        }
        inLight = Math.max(0,inLight);
        return inLight > 0;
    }
    public void registerEventAction(EventAction<?> eventAction) {eventActions.add(eventAction);}
    public HashSet<EventAction<?>> getEventAction() {return eventActions;}

}
