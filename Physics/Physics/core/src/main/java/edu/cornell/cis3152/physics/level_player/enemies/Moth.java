package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Avatar;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Moth extends Enemy {
    private static class Timer {
        private int value;
        private final int max;

        Timer(int max) {
            this.max = max;
            this.value = max;
        }

        int get() { return value; }
        void reset() { value = max; }
        void decrement() { if (value > 0) value--; }
        boolean isZero() { return value == 0; }
    }

    float DETECTION_DISTANCE = 8;

    private Timer attackTimer;
    private Timer attackAnimationTimer;
    private Timer smotherTimer;
    private Timer dazedTimer;

    private static Texture smotherAnimationTexture;
    private static Texture attackAnimationTexture;
    private static Texture totemAnimationTexture;
    public static final int FRAME_SIZE = 500;
    public static final int TOTEM_FRAME_SIZE = 500;

    public static final int TOTAL_ATTACK_FRAMES = 16;
    public static final int TOTAL_SMOTHER_FRAMES = 6;
    private static final int FRAME_DURATION = 12;

    public static final int OUT_OF_LIGHT_FRAME = 1;
    public static final int ATTACK_FRAME = 13;
    public static final int IN_LIGHT_FRAME = 1;

    private int cdFrameCount = 0;
    private int frameIndex = 0;

    private void resetFrames() {
        cdFrameCount = 0;
        frameIndex = 0;
    }

    private void updateFrame(int totalFrames) {
        cdFrameCount++;
        frameIndex = (cdFrameCount / FRAME_DURATION) % totalFrames;
        if (cdFrameCount >= FRAME_DURATION * totalFrames) {
            cdFrameCount = 0;
        }
    }

    public Moth(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        rr = null;

        smotherAnimationTexture = directory.getEntry("platform-mothSMOTHERANIMATION", Texture.class);
        attackAnimationTexture = directory.getEntry("platform-mothATTACKANIMATION", Texture.class);
        totemAnimationTexture = directory.getEntry("platform-mothATTACKANIMATION", Texture.class);

        attackTimer = new Timer(data.getInt("attackTimer"));
        attackAnimationTimer = new Timer(data.getInt("attackAnimationTimer"));
        smotherTimer = new Timer(data.getInt("smotherTimer"));
        dazedTimer = new Timer(data.getInt("dazedTimer"));
    }

    public void resetAttackTimer() { attackTimer.reset(); }
    public void decrementAttackTimer() { attackTimer.decrement(); }
    public boolean isAttackTimerZero() { return attackTimer.isZero(); }
    public int getAttackTimer() { return attackTimer.get(); }

    public void resetAttackAnimationTimer() { attackAnimationTimer.reset(); }
    public void decrementAttackAnimationTimer() { attackAnimationTimer.decrement(); }
    public boolean isAttackAnimationTimerZero() { return attackAnimationTimer.isZero(); }
    public int getAttackAnimationTimer() { return attackAnimationTimer.get(); }

    public void resetSmotherTimer() { smotherTimer.reset(); }
    public void decrementSmotherTimer() { smotherTimer.decrement(); }
    public boolean isSmotherTimerZero() { return smotherTimer.isZero(); }
    public int getSmotherTimer() { return smotherTimer.get(); }

    public void resetDazedTimer() { dazedTimer.reset(); }
    public void decrementDazedTimer() { dazedTimer.decrement(); }
    public boolean isDazedTimerZero() { return dazedTimer.isZero(); }
    public int getDazedTimer() { return dazedTimer.get(); }

    @Override
    public void in_light() {
        resetAttackTimer();
        setTexture(directory.getEntry("platform-mothINLIGHT", Texture.class));
        if (rr != null) {
            if (rr.targetObject instanceof Torch && ((Torch) rr.targetObject).canBePickedUp()) {
                setState(EnemyState.TRANCE);
            } else if (rr.targetObject instanceof Avatar) {
                setState(EnemyState.CD);
            }
        }
    }

    @Override
    public void cd() {
        setTexture(directory.getEntry("platform-mothINLIGHT", Texture.class));
        if (rr == null) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            if (isAttackTimerZero()) {
                setState(EnemyState.ATTACK);
            } else {
                updateFrame(TOTAL_ATTACK_FRAMES);
                stop();
                decrementAttackTimer();
            }
        }
    }

    @Override
    public void attack() {
        resetFrames();
        if (rr != null) {
            if (isAttackAnimationTimerZero()) {
                obstacle.getBody().setType(BodyDef.BodyType.DynamicBody);
                obstacle.setBullet(true);
                Vector2 force = isFacingRight() ? new Vector2(50000, 0) : new Vector2(-50000, 0);
                obstacle.getBody().applyForceToCenter(force, true);
                obstacle.setBullet(false);
                resetAttackTimer();
                resetAttackAnimationTimer();
                setState(EnemyState.OUT_OF_LIGHT);
            } else {
                stop();
                decrementAttackAnimationTimer();
            }
        } else {
            setState(EnemyState.OUT_OF_LIGHT);
        }
    }

    @Override
    public void angry() {
        resetFrames();
        if (rr == null) {
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.OUT_OF_LIGHT);
        }
        setSpeed(3.0f);
        move();
    }

    @Override
    public void out_of_light() {
        resetFrames();
        setSpeed(2.0f);
        if (rr != null && !Float.isNaN(rr.targetDistance)) {
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Lighting) {
                setState(EnemyState.ANGRY);
            }
        }
        move();
    }

    @Override
    public void trance() {
        resetFrames();
        setTexture(directory.getEntry("platform-mothTRANCE", Texture.class));
        setSpeed(1f);
        move();
    }

    @Override
    public void smother() {
        if (isSmotherTimerZero()) {
            System.out.println("Game Over");
        } else {
            updateFrame(TOTAL_SMOTHER_FRAMES);
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
        resetFrames();
        if (isDazedTimerZero()) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            stop();
            decrementDazedTimer();
        }
    }

    private void drawAnimation(SpriteBatch batch, Texture tex, int frame, float drawX, float drawY, int frameSize, boolean flipX, boolean flipY) {
        batch.draw(tex, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(),
            frame * frameSize, 0, frameSize, frameSize, flipX, flipY);
    }

    @Override
    public void draw(SpriteBatch batch) {
        float drawX = obstacle.getX() - getWidth() / 2f;
        float drawY = obstacle.getY() - getHeight() / 2f;
        boolean flipX = isFacingRight();

        switch (getState()) {
            case IN_LIGHT:
            case TRANCE:
            case DAZED:
                drawAnimation(batch, attackAnimationTexture, IN_LIGHT_FRAME, drawX, drawY, FRAME_SIZE, flipX, getState() == EnemyState.DAZED);
                break;
            case CD:
                drawAnimation(batch, attackAnimationTexture, frameIndex, drawX, drawY, FRAME_SIZE, flipX, false);
                break;
            case ATTACK:
                drawAnimation(batch, attackAnimationTexture, ATTACK_FRAME, drawX, drawY, FRAME_SIZE, flipX, false);
                break;
            case OUT_OF_LIGHT:
            case ANGRY:
                drawAnimation(batch, totemAnimationTexture, OUT_OF_LIGHT_FRAME, drawX, drawY, TOTEM_FRAME_SIZE, flipX, false);
                break;
            case SMOTHER:
                drawAnimation(batch, smotherAnimationTexture, frameIndex, drawX, drawY, FRAME_SIZE, flipX, false);
                break;
        }
    }
}
