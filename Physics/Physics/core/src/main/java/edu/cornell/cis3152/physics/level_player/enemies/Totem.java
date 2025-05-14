package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;

import java.util.HashSet;
import java.util.Set;


import java.util.ArrayList;
import java.util.List;

public class Totem extends Enemy {


    public static final int FRAME_SIZE = 500;
    public static final int TRANSITION_TOTAL_FRAMES = 9;
    public static final int IDLE_TOTAL_FRAMES = 7;
    public static final int FREEZE_TOTAL_FRAMES = 9;
    private static final int TRANSITION_FRAME_DURATION = 12;
    private static final int REVERSE_FRAME_DURATION = 12;
    private static final int IDLE_FRAME_DURATION = 12;
    private static final int FREEZE_FRAME_DURATION = 3;
    private final Texture transitionAnimationTexture = directory.getEntry("platform-totemLIGHTANIMATION", Texture.class);
    private final Texture reverseTransitionTexture = directory.getEntry("platform-totemOUTLIGHTANIMATION", Texture.class);
    private final Texture idleAnimationTexture = directory.getEntry("platform-totemIDLEANIMATION", Texture.class);
    private final Texture freezeLeftAnimationTexture = directory.getEntry("platform-totemFREEZELEFTANIMATION", Texture.class);
    private final Texture freezeRightAnimationTexture = directory.getEntry("platform-totemFREEZERIGHTANIMATION", Texture.class);
    private final boolean visited;
    private final List<Totem> linkedTotems = new ArrayList<>();
    protected EnemyState previousState = EnemyState.OUT_OF_LIGHT;
    private int cdFrameCount = 0;
    private int frameIndex = 0;
    private int idleFrameCount = 0;
    private int idleFrameIndex = 0;
    /**
     * Time it takes for a totem to transition from a CD state to a OUT_OF_LIGHT state.
     */
    private int freezeTimer;
    private boolean freezeRight = false;
    private int reverseFrameCount = 0;
    private int reverseFrameIndex = 0;
    private int cooldownTimer = 30;
    private boolean timerStart = false;
    public void resetCooldownTimer() {
        cooldownTimer = 30;
    }
    public int getCooldownTimer() {
        return cooldownTimer;
    }
    public void decrementCooldownTimer() {
        if (getTimerStart()){
            cooldownTimer--;
        }
        if (cooldownTimer == 0){
            resetFrames();
            setState(EnemyState.CD);
            resetCooldownTimer();
            stopTimer();
        }
    }

    public boolean getTimerStart(){
        return timerStart;
    }

    public void beginTimer(){
        timerStart = true;
    }

    public void stopTimer(){
        timerStart = false;
    }
    public Totem(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position, "totem");
        visited = false;
        setJustCollided(false);
    }

    @Override
    public void update() {
        decrementCooldownTimer();
        super.update();
    }

    public int getCdFrameCount() {
        return cdFrameCount;
    }

    public int getIdleFrameCount() {
        return idleFrameCount;
    }

    public int getReverseFrameCount() {
        return reverseFrameCount;
    }

    public EnemyState getPreviousState() {
        return previousState;
    }

    @Override
    public void setState(EnemyState newState) {
        if (this.getState() != EnemyState.IN_LIGHT){
            previousState = this.getState();
        }
        super.setState(newState);
    }
public boolean locked = false;
    public void lockFreeze(){
        locked = true;
    }

    public void unlockFreeze(){
        locked = false;
    }
    public void setFreezeRight(boolean freezeRight) {
        if (!locked){
            this.freezeRight = freezeRight;
        }
    }

    public List<Totem> getLinkedTotems() {
        return linkedTotems;
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

    private void resetReverseFrames() {
        reverseFrameCount = 0;
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
        if (getPreviousState() == EnemyState.CD) {
            if (reverseFrameIndex > 0) {
                reverseFrameCount++;
                if (reverseFrameCount > REVERSE_FRAME_DURATION){
                    reverseFrameCount = 0;
                    reverseFrameIndex--;
                }
                frameIndex = reverseFrameIndex;
            } else {
                previousState = EnemyState.IN_LIGHT;
            }
        } else {
            cdFrameCount++;
            frameIndex = (cdFrameCount / FREEZE_FRAME_DURATION);
            if (frameIndex >= FREEZE_TOTAL_FRAMES) {
                frameIndex = FREEZE_TOTAL_FRAMES - 1;
            }
        }
    }

    @Override
    public void out_of_light() {
        unlockFreeze();
        propagateDirection(this.faceRight, new HashSet<>());
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        move();
        idleFrameCount++;
        idleFrameIndex = (idleFrameCount / IDLE_FRAME_DURATION) % IDLE_TOTAL_FRAMES;
        if (idleFrameCount >= IDLE_FRAME_DURATION * IDLE_TOTAL_FRAMES) {
            idleFrameCount = 0;
        }
        resetFrames();
        resetReverseFrames();
    }



    @Override
    public void draw(SpriteBatch batch) {
        float drawX = obstacle.getX() - getWidth() / 2f;
        float drawY = obstacle.getY() - getHeight() / 2f;
        int srcIndex;
        switch (getState()) {
            case CD:
                srcIndex = frameIndex * FRAME_SIZE;
                batch.draw(transitionAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                break;

            case IN_LIGHT:
                if (getPreviousState() == EnemyState.CD) {

                    srcIndex = reverseFrameIndex * FRAME_SIZE;
                    batch.draw(transitionAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                } else {
                    srcIndex = Math.min(frameIndex, FREEZE_TOTAL_FRAMES - 1) * FRAME_SIZE;
                    if (freezeRight && isFacingRight()){
                        batch.draw(freezeLeftAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, false, false);
                    } else if (freezeRight && !isFacingRight()){
                        batch.draw(freezeRightAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, true, false);
                    } else if (!freezeRight && isFacingRight()){
                        batch.draw(freezeRightAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, false, false);
                    } else {
                        batch.draw(freezeLeftAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, true, false);

                    }
                }
                break;

            case OUT_OF_LIGHT:
                srcIndex = idleFrameIndex * FRAME_SIZE;
                batch.draw(idleAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, !isFacingRight(), false);
                break;
        }
    }


    @Override
    public void cd() {
        unlockFreeze();
        resetIdleFrames();
        resetReverseFrames();
        cdFrameCount++;
        frameIndex = (cdFrameCount / TRANSITION_FRAME_DURATION) % TRANSITION_TOTAL_FRAMES;
        reverseFrameIndex = frameIndex; // frame that the thing ends with - so total frames
        if (cdFrameCount >= TRANSITION_FRAME_DURATION * TRANSITION_TOTAL_FRAMES) {
            cdFrameCount = 0; // this resets the cdFrame count
        }
        decrementFreezeTimer();
        if (getFreezeTimer() <= 0) {
            setState(EnemyState.OUT_OF_LIGHT);
            resetIdleFrames();
            resetFrames();
        }
    }


}
