package edu.cornell.cis3152.physics.level_player.enemies;

import box2dLight.Light;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.cis3152.physics.level_player.enviromentals.GameObject;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.enviromentals.Smoke;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Avatar;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class Moth extends Enemy {
    public static final int FRAME_SIZE = 500;
    // Frame counts
    public static final int TOTAL_ATTACK_FRAMES = 1;
    public static final int TOTAL_ANGRY_FRAMES = 6;
    public static final int TOTAL_IN_LIGHT_FRAMES = 4; // same as frustrated
    public static final int TOTAL_OUT_OF_LIGHT_FRAMES = 6;
    public static final int TOTAL_JUMP_FRAMES = 6;
    private static final int TOTAL_TRANCE_FRAMES = 4;
    public static final int TOTAL_SMOTHER_FRAMES = 6;
    public static final int TOTAL_CD_FRAMES = 18;
    public static final int TOTAL_DAZED_FRAMES = 4;
    public static final int TOTAL_FRUSTRATED_FRAMES = 4;
    public static final int TOTAL_DUST_FRAMES = 8;
    // Frame durations
    private static final int ATTACK_FRAME_DURATION = 12;
    private static final int ANGRY_FRAME_DURATION = 12;
    private static final int IN_LIGHT_FRAME_DURATION = 12;
    private static final int OUT_OF_LIGHT_FRAME_DURATION = 12;
    private static final int JUMP_FRAME_DURATION = 12;
    private static final int TRANCE_FRAME_DURATION = 36;
    private static final int SMOTHER_FRAME_DURATION = 12;
    private static final int CD_FRAME_DURATION = 6;
    private static final int DAZED_FRAME_DURATION = 12;
    private static final int FRUSTRATED_FRAME_DURATION = 12;
    private static final int DUST_FRAME_DURATION = 12;
    private static Texture outOfLightAnimationTexture;
    private static Texture angryAnimationTexture;
    private static Texture inLightAnimationTexture;
    private static Texture jumpAnimationTexture;
    private static Texture tranceAnimationTexture;
    private static Texture smotherAnimationTexture;
    private static Texture cdAnimationTexture;
    private static Texture attackAnimationTexture;
    private static Texture dazedAnimationTexture;
    private static Texture frustratedAnimationTexture;
    private static Texture dustAnimationTexture;

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
    private final Timer tranceTimer;
    float DETECTION_DISTANCE = 8;
    private int frameCount = 0;
    private int frameIndex = 0;
    private int dustCount = 0;
    private int dustIndex = 0;
    private float initialX;
    private float initialY;
    private boolean doOnceAttack = true;
    private EnemyState nextFrameState;
    private boolean doNextFrameState = false;

    Vector2 torchPos;

    private boolean hasJumped = false;
    private void setHasJumped(boolean val) {
        hasJumped = val;
    }
    public Moth(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        rr = null;

        outOfLightAnimationTexture = directory.getEntry("platform-mothOUTOFLIGHTANIMATION", Texture.class);
        angryAnimationTexture = directory.getEntry("platform-mothANGRYANIMATION", Texture.class);
        inLightAnimationTexture = directory.getEntry("platform-mothFRUSTRATEDANIMATION", Texture.class);
        jumpAnimationTexture = directory.getEntry("platform-mothJUMPANIMATION", Texture.class);
        tranceAnimationTexture = directory.getEntry("platform-mothTRANCEANIMATION", Texture.class);
        smotherAnimationTexture = directory.getEntry("platform-mothSMOTHERANIMATION", Texture.class);
        dazedAnimationTexture = directory.getEntry("platform-mothDAZEDANIMATION", Texture.class);
        cdAnimationTexture = directory.getEntry("platform-mothCDANIMATION", Texture.class);
        attackAnimationTexture = directory.getEntry("platform-mothATTACKANIMATION", Texture.class);
        frustratedAnimationTexture = directory.getEntry("platform-mothFRUSTRATEDANIMATION", Texture.class);
        dustAnimationTexture = directory.getEntry("platform-mothDUSTANIMATION", Texture.class);
        attackTimer = new Timer(data.getInt("attackTimer"));
//        attackAnimationTimer = new Timer(data.getInt("attackAnimationTimer"));
        attackAnimationTimer = new Timer(50);
        tranceTimer = new Timer(100);
        smotherTimer = new Timer(data.getInt("smotherTimer"));
        dazedTimer = new Timer(data.getInt("dazedTimer"));
        obstacle.setRestitution(data.getFloat("restitution", 0));
        setGrounded(true);
    }

    private void resetFrames() {
        frameCount = 0;
        frameIndex = 0;
    }

    private void resetDustFrames() {
        dustCount = 0;
        dustIndex = 0;
    }

    private void updateFrame(int frameDuration, int totalFrames) {
        frameCount++;
        frameIndex = (frameCount / frameDuration) % totalFrames;
        if (frameCount >= frameDuration * totalFrames) {
            frameCount = 0;
        }
    }

    private void updateDustFrame(){
        dustCount++;
        dustIndex = (dustCount / DUST_FRAME_DURATION) % TOTAL_DUST_FRAMES;
        if (dustCount >= DUST_FRAME_DURATION * TOTAL_DUST_FRAMES){
            dustCount = 0;
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

    public void resetTranceTimer() {
        tranceTimer.reset();
    }

    public void decrementTranceTimer() {
        tranceTimer.decrement();
    }

    public boolean isTranceTimerZero() {
        return tranceTimer.isZero();
    }

    public int getTranceTimer() {
        return tranceTimer.get();
    }

    @Override
    public void in_light() {
        updateFrame(IN_LIGHT_FRAME_DURATION, TOTAL_IN_LIGHT_FRAMES);
        resetAttackTimer();
        if (rr != null) {
            System.out.println("Target is of type " + rr.targetObject);
//            if (rr.targetObject instanceof GameObject){
//                System.out.println(((ObstacleSprite) rr.targetObject).getName().contains("infburnable"));
//            }
            if (rr.targetObject instanceof Torch && !Avatar.getHasTorch()){
                setState(EnemyState.TRANCE);
                resetTranceTimer();
            } else if (rr.targetObject instanceof Avatar && Avatar.getHasTorch()) {
                setState(EnemyState.CD);
            } else if (rr.targetObject instanceof GameObject && ((ObstacleSprite) rr.targetObject).getName().contains("infburnable")) {
                setState(EnemyState.CD);
            } else if (rr.targetObject instanceof Fire){
                setState(EnemyState.TRANCE);
            } else {
                setState(EnemyState.FRUSTRATED);
            }
        } else {
//            System.out.println("rr is null");
            stop();
        }
    }

    @Override
    public void cd() {
        if (rr == null || (!(rr.targetObject instanceof Avatar) && !(rr.targetObject instanceof GameObject))) {
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
    public void stop() {
        float currY = obstacle.getLinearVelocity().y;
        obstacle.getBody().setLinearVelocity(0, currY);
    }

    @Override
    public void attack() {
        resetFrames();
        setSpeed(4.5f);
        if (isAttackAnimationTimerZero()) {
            setState(EnemyState.OUT_OF_LIGHT);
            doOnceAttack = true;
            resetSpeed();
            resetFrames();
            resetDustFrames();
            resetAttackAnimationTimer();
        } else {
            move();
            updateFrame(ATTACK_FRAME_DURATION, TOTAL_ATTACK_FRAMES);
            updateDustFrame();
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
        resetTranceTimer();
        updateFrame(OUT_OF_LIGHT_FRAME_DURATION, TOTAL_OUT_OF_LIGHT_FRAMES);
        if (rr != null && !Float.isNaN(rr.targetDistance)) {
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Lighting) {
                setState(EnemyState.ANGRY);
            }
        }
        move();
    }

    @Override
    public void trance() {
        updateFrame(TRANCE_FRAME_DURATION, TOTAL_TRANCE_FRAMES);
        if (isTranceTimerZero()){
            setState(EnemyState.JUMP);
        } else {
            decrementTranceTimer();
            stop();
        }

    }

    @Override
    public void jump() {
        updateFrame(JUMP_FRAME_DURATION, TOTAL_JUMP_FRAMES);
        Body body = obstacle.getBody();
        if (hasJumped) {
            return;
        }
            if (rr != null && rr.targetObject instanceof Torch){
                Torch torch = (Torch) rr.targetObject;
                torchPos = torch.getObstacle().getPosition();
                body.setType(BodyDef.BodyType.DynamicBody);
                body.setAwake(true);
                body.setGravityScale(0.5f);
                float jumpVy  = 2.5f;
                float gEff    = Math.abs(body.getWorld().getGravity().y * body.getGravityScale());
                float T       = (1.5f * jumpVy) / gEff;
                float dx      = torchPos.x - body.getPosition().x;
                float jumpVx  = dx / T;
                body.setLinearVelocity(jumpVx, jumpVy);
                setHasJumped(true);
            } else {
                if (rr == null) {
                    System.out.println("null check");
                }
            }

    }

    @Override
    public void update(){
        super.update();
    }

    @Override
    public void smother() {
        setHasJumped(false);
//        stop();
        if (isSmotherTimerZero()) {
            System.out.println("Game Over");
        } else {
            updateFrame(SMOTHER_FRAME_DURATION, TOTAL_SMOTHER_FRAMES);
            decrementSmotherTimer();
        }
    }

    @Override
    public void dazed() {
        Body body = obstacle.getBody();
//        resetFrames();
        if (isDazedTimerZero()) {
            setState(EnemyState.OUT_OF_LIGHT);
            onExitDazed();
            return;
        }
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        updateFrame(DAZED_FRAME_DURATION, TOTAL_DAZED_FRAMES);
        body.setGravityScale(1.0f);
//        Vector2 vel = body.getLinearVelocity();
//        body.setLinearVelocity(0f, vel.y);
        decrementDazedTimer();

    }

    private void onExitDazed() {
        if (rr != null && rr.targetObject instanceof Torch && ((Torch)rr.targetObject).canBePickedUp()) {
            resetSmotherTimer();
            setState(EnemyState.SMOTHER);
            return;
        }

        if (rr != null && rr.targetObject instanceof Lighting) {
            setState(EnemyState.IN_LIGHT);
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
            case JUMP:
                drawAnimation(batch, jumpAnimationTexture, 3, drawX, drawY, flipX);
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
                if (doOnceAttack){
                    initialX = drawX;
                    initialY = drawY;
                    doOnceAttack = false;
                }
                drawAnimation(batch, dustAnimationTexture, dustIndex, initialX, initialY, flipX);
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
