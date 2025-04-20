package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Avatar;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Moth extends Enemy {
    public static final int FRAME_SIZE = 500;
    // Frame counts
    public static final int TOTAL_ATTACK_FRAMES = 1;
    public static final int TOTAL_ANGRY_FRAMES = 6; // same as out of light for now
    public static final int TOTAL_IN_LIGHT_FRAMES = 1; // lowk doesn't need to exist bc inlight is transition
    public static final int TOTAL_OUT_OF_LIGHT_FRAMES = 6;
    public static final int TOTAL_TRANCE_FRAMES = 6; // same as out of light for now
    public static final int TOTAL_SMOTHER_FRAMES = 6;
    public static final int TOTAL_CD_FRAMES = 18;
    public static final int TOTAL_DAZED_FRAMES = 4;

    public static final int TOTAL_FRUSTRATED_FRAMES = 1; // for now this is just the in light animation.
    // Frame durations
    private static final int ATTACK_FRAME_DURATION = 12;
    private static final int ANGRY_FRAME_DURATION = 12;
    private static final int IN_LIGHT_FRAME_DURATION = 12;
    private static final int OUT_OF_LIGHT_FRAME_DURATION = 12;
    private static final int TRANCE_FRAME_DURATION = 12;
    private static final int SMOTHER_FRAME_DURATION = 12;
    private static final int CD_FRAME_DURATION = 5;
    private static final int DAZED_FRAME_DURATION = 12;
    private static final int FRUSTRATED_FRAME_DURATION = 12;
    private static Texture outOfLightAnimationTexture;
    private static Texture angryAnimationTexture;
    private static Texture inLightAnimationTexture;
    private static Texture tranceAnimationTexture;
    private static Texture smotherAnimationTexture;
    private static Texture cdAnimationTexture;
    private static Texture attackAnimationTexture;
    private static Texture dazedAnimationTexture;
    private static Texture frustratedAnimationTexture;

    /**
     * This is the amount of time until the moth attacks after being in the light.
     */
    private final Timer attackTimer;
    /**
     * This is the amount of time until the moth attacks after being in the light.
     */
    private final Timer attackAnimationTimer;
    private final Timer smotherTimer;
    private final Timer dazedTimer;
    float DETECTION_DISTANCE = 8;
    private int frameCount = 0;
    private int frameIndex = 0;

    public Moth(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        rr = null;

        outOfLightAnimationTexture = directory.getEntry("platform-mothOUTOFLIGHTANIMATION", Texture.class);
        angryAnimationTexture = directory.getEntry("platform-mothOUTOFLIGHTANIMATION", Texture.class); // same as out of light
        inLightAnimationTexture = directory.getEntry("platform-mothINLIGHTANIMATION", Texture.class);
        tranceAnimationTexture = directory.getEntry("platform-mothOUTOFLIGHTANIMATION", Texture.class); // same as out of light
        smotherAnimationTexture = directory.getEntry("platform-mothSMOTHERANIMATION", Texture.class);
        dazedAnimationTexture = directory.getEntry("platform-mothDAZEDANIMATION", Texture.class);
        cdAnimationTexture = directory.getEntry("platform-mothCDANIMATION", Texture.class);
        attackAnimationTexture = directory.getEntry("platform-mothATTACKANIMATION", Texture.class);
        frustratedAnimationTexture = directory.getEntry("platform-mothINLIGHTANIMATION", Texture.class); // just in light for now

        attackTimer = new Timer(data.getInt("attackTimer"));
//        attackAnimationTimer = new Timer(data.getInt("attackAnimationTimer"));
        attackAnimationTimer = new Timer(50);
        smotherTimer = new Timer(data.getInt("smotherTimer"));
        dazedTimer = new Timer(data.getInt("dazedTimer"));
    }

    private void resetFrames() {
        frameCount = 0;
        frameIndex = 0;
    }

    private void updateFrame(int frameDuration, int totalFrames) {
        frameCount++;
        frameIndex = (frameCount / frameDuration) % totalFrames;
        if (frameCount >= frameDuration * totalFrames) {
            frameCount = 0;
        }
    }

    private void resetSpeed() {
        setSpeed(2.0f);
    }

    public void resetAttackTimer() {
        attackTimer.reset();
    }

    public void decrementAttackTimer() {
        attackTimer.decrement();
    }

    public boolean isAttackTimerZero() {
        return attackTimer.isZero();
    }

    public int getAttackTimer() {
        return attackTimer.get();
    }

    public void resetAttackAnimationTimer() {
        attackAnimationTimer.reset();
    }

    public void decrementAttackAnimationTimer() {
        attackAnimationTimer.decrement();
    }

    public boolean isAttackAnimationTimerZero() {
        return attackAnimationTimer.isZero();
    }

    public int getAttackAnimationTimer() {
        return attackAnimationTimer.get();
    }

    public void resetSmotherTimer() {
        smotherTimer.reset();
    }

    public void decrementSmotherTimer() {
        smotherTimer.decrement();
    }

    public boolean isSmotherTimerZero() {
        return smotherTimer.isZero();
    }

    public int getSmotherTimer() {
        return smotherTimer.get();
    }

    public void resetDazedTimer() {
        dazedTimer.reset();
    }

    public void decrementDazedTimer() {
        dazedTimer.decrement();
    }

    public boolean isDazedTimerZero() {
        return dazedTimer.isZero();
    }

    public int getDazedTimer() {
        return dazedTimer.get();
    }

    @Override
    public void in_light() {
        updateFrame(IN_LIGHT_FRAME_DURATION, TOTAL_IN_LIGHT_FRAMES);
        resetAttackTimer();
        if (rr != null) {
            System.out.println(rr.targetObject);
            if (rr.targetObject instanceof Torch && ((Torch) rr.targetObject).canBePickedUp()) {
                setState(EnemyState.TRANCE);
            } else if (rr.targetObject instanceof Avatar) {
                setState(EnemyState.CD);
            } else {
//                System.out.println("Frustrated");
                setState(EnemyState.FRUSTRATED);
            }
        } else {
            stop();
            System.out.println("rr = null");
        }
    }

    @Override
    public void cd() {
        if (rr == null) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            if (isAttackTimerZero()) {
                setState(EnemyState.ATTACK);
            } else { // loops through cd state
                updateFrame(CD_FRAME_DURATION, TOTAL_CD_FRAMES);
                stop();
                decrementAttackTimer();
            }
        }
    }

    @Override
    public void attack() {
        resetFrames();
        setSpeed(4.5f);
        System.out.println(getAttackAnimationTimer());
        if (isAttackAnimationTimerZero()) {
//                obstacle.getBody().setType(BodyDef.BodyType.DynamicBody);
//                obstacle.setBullet(true);
//                Vector2 force = isFacingRight() ? new Vector2(50000, 0) : new Vector2(-50000, 0);
//                obstacle.getBody().applyForceToCenter(force, true);
//                obstacle.setBullet(false);
//                resetAttackTimer();
//                resetAttackAnimationTimer();
            setState(EnemyState.OUT_OF_LIGHT);
            resetSpeed();
            resetFrames();
            resetAttackAnimationTimer();
        } else {
            move();
            updateFrame(ATTACK_FRAME_DURATION, TOTAL_ATTACK_FRAMES);
            decrementAttackAnimationTimer();
        }
    }

    @Override
    public void angry() {
        setSpeed(3.0f);
        updateFrame(ANGRY_FRAME_DURATION, TOTAL_ANGRY_FRAMES);
        if (rr == null) {
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.OUT_OF_LIGHT);
            resetSpeed();
        }
        move();
    }

    @Override
    public void out_of_light() {
        setSpeed(2.0f);
        updateFrame(OUT_OF_LIGHT_FRAME_DURATION, TOTAL_OUT_OF_LIGHT_FRAMES);
        if (rr != null && !Float.isNaN(rr.targetDistance)) {
            System.out.println(rr.targetObject);
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Lighting) {
                System.out.println("OUTOFLIGHT > ANGRY");
                setState(EnemyState.ANGRY);
            } else {
                System.out.println((rr.targetDistance < DETECTION_DISTANCE) + " ");
            }
        }
        move();
    }

    @Override
    public void trance() {
        setSpeed(1f);
        move();
        updateFrame(TRANCE_FRAME_DURATION, TOTAL_TRANCE_FRAMES);
    }

    @Override
    public void smother() {
        if (isSmotherTimerZero()) {
            System.out.println("Game Over");
        } else {
            updateFrame(SMOTHER_FRAME_DURATION, TOTAL_SMOTHER_FRAMES);
            if (rr != null && rr.targetObject instanceof Torch) {
                Torch torch = (Torch) rr.targetObject;
                Vector2 torchPos = torch.getObstacle().getPosition();
                if (Math.abs(getObstacle().getX() - torchPos.x) < 0.5) {
                    stop();
                } else {
                    move_to(torchPos);
                }
            }
            decrementSmotherTimer();
        }
    }

    @Override
    public void dazed() {
//        resetFrames();
        if (isDazedTimerZero()) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            updateFrame(DAZED_FRAME_DURATION, TOTAL_DAZED_FRAMES);
            stop();
            decrementDazedTimer();
        }
    }

    @Override
    public void frustrated() {
            stop();
            updateFrame(FRUSTRATED_FRAME_DURATION, TOTAL_FRUSTRATED_FRAMES);
    }

    private void drawAnimation(SpriteBatch batch, Texture tex, int frame, float drawX, float drawY, boolean flipX) {
        batch.draw(tex, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), frame * Moth.FRAME_SIZE, 0, Moth.FRAME_SIZE, Moth.FRAME_SIZE, flipX, false);
    }

    @Override
    public void draw(SpriteBatch batch) {
        float drawX = obstacle.getX() - getWidth() / 2f;
        float drawY = obstacle.getY() - getHeight() / 2f;
        boolean flipX = isFacingRight();

        switch (getState()) {
            case IN_LIGHT:
                drawAnimation(batch, inLightAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case TRANCE:
                drawAnimation(batch, tranceAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case DAZED:
                drawAnimation(batch, dazedAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case CD:
                drawAnimation(batch, cdAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case ATTACK:
                drawAnimation(batch, attackAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case OUT_OF_LIGHT:
                drawAnimation(batch, outOfLightAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case ANGRY:
                drawAnimation(batch, angryAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case SMOTHER:
                drawAnimation(batch, smotherAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
            case FRUSTRATED:
                drawAnimation(batch, frustratedAnimationTexture, frameIndex, drawX, drawY, flipX);
                break;
        }
    }

    private static class Timer {
        private final int max;
        private int value;

        Timer(int max) {
            this.max = max;
            this.value = max;
        }

        int get() {
            return value;
        }

        void reset() {
            value = max;
        }

        void decrement() {
            if (value > 0) value--;
        }

        boolean isZero() {
            return value == 0;
        }
    }
}
