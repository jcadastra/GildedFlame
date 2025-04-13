package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Traci;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.SpriteSheet;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class Moth extends Enemy {
    /**
     * Distance in which enemy will become angry
     */
    float DETECTION_DISTANCE = 8;
    /**
     * Time it takes for a moth to transition from a CD state to an ATTACK state.
     */
    private int attackTimer;

    /**
     * Time it takes between moth attacks.
     */
    private int attackAnimationTimer;

    /**
     * Time it takes to smother the torch.
     */
    private int smotherTimer;

    /**
     * Time it takes for the moth to come back alive.
     */
    private int dazedTimer;

    private static Texture smotherAnimationTexture;
    private static Texture attackAnimationTexture;
    private static Texture totemAnimationTexture;
    public static final int FRAME_SIZE = 500;
//    public static final int TOTEM_FRAME_SIZE = 1080;
    public static final int TOTEM_FRAME_SIZE = 500;

    public static final int TOTAL_ATTACK_FRAMES = 16;
    public static final int TOTAL_SMOTHER_FRAMES = 6;
    private static final int FRAME_DURATION = 12;

//    /**
//     * 8th frame of the totem animation texture
//     */
//    public static final int OUT_OF_LIGHT_FRAME = 8;
        public static final int OUT_OF_LIGHT_FRAME = 1;


    /**
     * 13th frame of the attack animation texture
     */
    public static final int ATTACK_FRAME = 13;

    /**
     * 1st frame of the attack animation
     */
    public static final int IN_LIGHT_FRAME = 1;
    private int cdFrameCount = 0;
    private int frameIndex = 0;

    private void resetFrames() {
        cdFrameCount = 0;
        frameIndex = 0;
    }

    /**
     * @param id
     * @param units
     * @param value
     * @param directory
     * @param position
     */
    public Moth(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        rr = null;

        smotherAnimationTexture = directory.getEntry("platform-mothSMOTHERANIMATION", Texture.class);
        attackAnimationTexture = directory.getEntry("platform-mothATTACKANIMATION", Texture.class);
//        totemAnimationTexture = directory.getEntry("platform-totemLIGHTANIMATION", Texture.class);
        totemAnimationTexture = directory.getEntry("platform-mothATTACKANIMATION", Texture.class);

    }

    public int getAttackTimer() {
        return attackTimer;
    }

    public void decrementAttackTimer() {
        attackTimer--;
    }

    public void resetAttackTimer() {
        attackTimer = data.getInt("attackTimer");
    }

    public int getAttackAnimationTimer() {
        return attackAnimationTimer;
    }

    public void decrementAttackAnimationTimer() {
        attackAnimationTimer--;
    }

    public void resetAttackAnimationTimer() {
        attackAnimationTimer = data.getInt("attackAnimationTimer");
    }

    public int getSmotherTimer() {
        return smotherTimer;
    }

    public void decrementSmotherTimer() {
        smotherTimer--;
    }

    public void resetSmotherTimer() {
        smotherTimer = data.getInt("smotherTimer");
    }

    public int getDazedTimer() {
        return dazedTimer;
    }

    public void decrementDazedTimer() {
        dazedTimer--;
    }

    public void resetDazedTimer() {
        dazedTimer = data.getInt("dazedTimer");
    }

    /**
     * in_light() is the state of the moth upon contact with the light.
     * If the moth detects a torch entity, then it becomes intranced.
     * If the moth detects a player entity, then it changes into a cooldown, which will lead to an angry state.
     */
    @Override
    public void in_light() {
        resetAttackTimer();
        Texture texture = directory.getEntry("platform-mothINLIGHT", Texture.class);
        setTexture(texture);
        if (rr != null) {
            if (rr.targetObject instanceof Torch && ((Torch) rr.targetObject).canBePickedUp()) {
                setState(EnemyState.TRANCE);
            } else if (rr.targetObject instanceof Traci) {
                setState(EnemyState.CD);
            }
        }

    }

    @Override
    public void cd() {
        if (rr == null) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            if (getAttackTimer() == 0) {
                setState(EnemyState.ATTACK);
            } else {
                System.out.println(rr.targetObject);
                cdFrameCount++;
                frameIndex = (cdFrameCount / FRAME_DURATION) % TOTAL_ATTACK_FRAMES;
                if (cdFrameCount >= FRAME_DURATION * TOTAL_ATTACK_FRAMES) {
                    cdFrameCount = 0;
                }
                stop();
                decrementAttackTimer();
            }
        }

    }


    @Override
    public void attack() {
        resetFrames();
        if (rr != null) {
            if (getAttackAnimationTimer() == 0) {
                obstacle.getBody().setType(BodyDef.BodyType.DynamicBody);
                obstacle.setBullet(true);
                if (isFacingRight()) {
                    obstacle.getBody().applyForceToCenter(new Vector2(50000, 0), true);
                } else {
                    obstacle.getBody().applyForceToCenter(new Vector2(-50000, 0), true);
                }
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

    // just follows the torch around.
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
        if (!(rr == null) && (!Float.isNaN(rr.targetDistance))) {
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Lighting) {
                setState(EnemyState.ANGRY);
            } else {
                setState(EnemyState.OUT_OF_LIGHT);
            }
        }
        move();
    }

    @Override
    public void trance() {
        resetFrames();
        Texture texture = directory.getEntry("platform-mothTRANCE", Texture.class);
        setTexture(texture);
        setSpeed(1f);
        move();
    }

    @Override
    public void smother() {
        if (getSmotherTimer() == 0) {
            System.out.println("Game Over");
        } else {
            cdFrameCount++;
            frameIndex = (cdFrameCount / FRAME_DURATION) % TOTAL_SMOTHER_FRAMES;
            if (cdFrameCount >= FRAME_DURATION * TOTAL_SMOTHER_FRAMES) {
                cdFrameCount = 0;
            }
            if (rr != null) {
                if (rr.targetObject instanceof Torch) {
                    Vector2 torchPos = ((Torch) rr.targetObject).getObstacle().getPosition();
                    if (Math.abs(getObstacle().getX() - torchPos.x) < 0.5) {
                        stop();
                    } else {
                        move_to(((Torch) rr.targetObject).getObstacle().getPosition());
                    }
                }
            }
            decrementSmotherTimer();
        }

    }

    @Override
    public void dazed() {
        resetFrames();
        if (getDazedTimer() == 0) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            stop();
            decrementDazedTimer();
        }
    }



    @Override
    public void draw(SpriteBatch batch) {
        float drawX = obstacle.getX() - getWidth() / 2f;
        float drawY = obstacle.getY() - getHeight() / 2f;
        int srcIndex;
        switch (getState()) {
            case IN_LIGHT:
                srcIndex = IN_LIGHT_FRAME * FRAME_SIZE;
                batch.draw(attackAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                break;
            case CD:
                srcIndex = frameIndex * FRAME_SIZE;
                batch.draw(attackAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                break;
            case OUT_OF_LIGHT:
                srcIndex = OUT_OF_LIGHT_FRAME * TOTEM_FRAME_SIZE;
                batch.draw(totemAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, TOTEM_FRAME_SIZE, TOTEM_FRAME_SIZE, isFacingRight(), false);
                break;
            case ANGRY:
                srcIndex = OUT_OF_LIGHT_FRAME * TOTEM_FRAME_SIZE;
                batch.draw(totemAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, TOTEM_FRAME_SIZE, TOTEM_FRAME_SIZE, isFacingRight(), false);
                break;
            case ATTACK:
                srcIndex = ATTACK_FRAME * FRAME_SIZE;
                batch.draw(attackAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                break;
            case SMOTHER:
                srcIndex = frameIndex * FRAME_SIZE;
                batch.draw(smotherAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                break;
            case TRANCE:
                srcIndex = IN_LIGHT_FRAME * FRAME_SIZE;
                batch.draw(attackAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
                break;
            case DAZED:
                srcIndex = IN_LIGHT_FRAME * FRAME_SIZE;
                batch.draw(attackAnimationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), true);
                break;
        }
    }


}

