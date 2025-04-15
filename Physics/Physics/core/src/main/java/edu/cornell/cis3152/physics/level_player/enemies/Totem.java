package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.SpriteSheet;

import java.util.HashSet;
import java.util.Set;


import java.util.ArrayList;
import java.util.List;

public class Totem extends Enemy {


    public static final int FRAME_SIZE = 500;
    public static final int TRANSITION_TOTAL_FRAMES = 9;
    public static final int IDLE_TOTAL_FRAMES = 12;
    public static final int LIGHT_FRAME = 0;
    public static final int DARK_FRAME = 8;
    private static final int TRANSITION_FRAME_DURATION = 12;
    private static final int IDLE_FRAME_DURATION = 9;
    private final Texture transitionAnimationTexture = directory.getEntry("platform-totemLIGHTANIMATION", Texture.class);
    private final Texture idleAnimationTexture = directory.getEntry("platform-totemIDLEANIMATION", Texture.class);
    private int cdFrameCount = 0;
    private int frameIndex = 0;

    private int idleFrameCount = 0;
    private int idleFrameIndex = 0;
    private final boolean visited;

    public List<Totem> getLinkedTotems() {
        return linkedTotems;
    }
    /**
     * Time it takes for a totem to transition from a CD state to a OUT_OF_LIGHT state.
     */
    private int freezeTimer;
    private final List<Totem> linkedTotems = new ArrayList<>();

    public Totem(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        visited = false;
        setJustCollided(false);
    }

    public int getFreezeTimer() {
        return freezeTimer;
    }

    public void decrementFreezeTimer() {
        freezeTimer--;
    }

    public void resetFreeze() {
        freezeTimer = data.getInt("freezeTimer");
    }

    private void resetFrames() {
        cdFrameCount = 0;
        frameIndex = 0;
    }

    private void resetIdleFrames() {
        idleFrameCount = 0;
        idleFrameIndex = 0;
    }

    public void addLinkedTotem(Totem other) {
        if (!linkedTotems.contains(other)) {
            linkedTotems.add(other);
            other.addLinkedTotem(this);
        }
    }

    public boolean isBottomTotem() {
        float myY = obstacle.getY();
        for (Totem t : linkedTotems) {
            if (t != this && t.getObstacle().getY() < myY) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnyLinkedTotemFrozen() {
        if (this.getState() == EnemyState.CD || this.getState() == EnemyState.IN_LIGHT) {
            return true;
        }
        for (Totem linked : this.getLinkedTotems()) {
            if (linked.getState() == EnemyState.CD || linked.getState() == EnemyState.IN_LIGHT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void changeDirection() {
        if (!justCollided && isBottomTotem() && !isAnyLinkedTotemFrozen()) {
            faceRight = !faceRight;
            propagateDirection(this.faceRight, new HashSet<>());
        }
    }

    private void propagateDirection(boolean direction, Set<Totem> visited) {
        visited.add(this);
        for (Totem totem : linkedTotems) {
            if (!visited.contains(totem) && totem.getObstacle().getY() > this.getObstacle().getY()) {
                totem.faceRight = direction;
                totem.propagateDirection(direction, visited);
            }
        }
    }

    // reacting to light
    @Override
    public void in_light() {
        stop();
        resetIdleFrames();
        resetFrames();
    }

    @Override
    public void out_of_light() {
        propagateDirection(this.faceRight, new HashSet<>());
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        move();
        idleFrameCount++;
        idleFrameIndex = (idleFrameCount / IDLE_FRAME_DURATION) % IDLE_TOTAL_FRAMES;
        if (idleFrameCount >= IDLE_FRAME_DURATION * IDLE_TOTAL_FRAMES) {
            idleFrameCount = 0;
        }
        resetFrames();
    }

    @Override
    public void draw(SpriteBatch batch) {
        float drawX = obstacle.getX() - getWidth() / 2f;
        float drawY = obstacle.getY() - getHeight() / 2f;
        int srcIndex;
        switch (getState()) {
            case CD:
                srcIndex = frameIndex * FRAME_SIZE;
                batch.draw(transitionAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, !isFacingRight(), false);
                break;
            case IN_LIGHT:
                srcIndex = LIGHT_FRAME * FRAME_SIZE;
                batch.draw(transitionAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, !isFacingRight(), false);
                break;
            case OUT_OF_LIGHT:
                srcIndex = idleFrameIndex * FRAME_SIZE;
                batch.draw(idleAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, !isFacingRight(), false);
                break;
        }
    }

    @Override
    public void cd() {
        resetIdleFrames();

        cdFrameCount++;
        frameIndex = (cdFrameCount / TRANSITION_FRAME_DURATION) % TRANSITION_TOTAL_FRAMES;
        if (cdFrameCount >= TRANSITION_FRAME_DURATION * TRANSITION_TOTAL_FRAMES) {
            cdFrameCount = 0;
        }
        decrementFreezeTimer();
        if (getFreezeTimer() <= 0) {
            setState(EnemyState.OUT_OF_LIGHT);
            resetIdleFrames();
            resetFrames();
        }
    }


}
