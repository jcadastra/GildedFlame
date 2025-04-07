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
import edu.cornell.gdiac.graphics.SpriteSheet;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class Moth extends Enemy {
    /**
     * Distance in which enemy will become angry
     */
    float DETECTION_DISTANCE = 5;


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
        if (getAttackTimer() == 0) {
            setState(EnemyState.ATTACK);
        } else {
            stop();
            decrementAttackTimer();
        }
    }


    @Override
    public void attack() {
        Texture texture = directory.getEntry("platform-mothATTACK", Texture.class);
        setTexture(texture);
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
    }

    // just follows the torch around.
    @Override
    public void angry() {
        if (rr == null) {
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.OUT_OF_LIGHT);
        }
        setSpeed(3.0f);
        Texture texture = directory.getEntry("platform-mothANGRY", Texture.class);
        setTexture(texture);
        move();

    }

    @Override
    public void out_of_light() {
        Texture texture = directory.getEntry("platform-mothOUTOFLIGHT", Texture.class);
        setTexture(texture);
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
            if (rr == null) {
            } else {
                if (rr.targetObject instanceof Torch) {
                    Vector2 torchPos = ((Torch) rr.targetObject).getObstacle().getPosition();
                    if (Math.abs(getObstacle().getX() - torchPos.x) < 0.5) {
                        stop();
                    } else {
                        move_to(((Torch) rr.targetObject).getObstacle().getPosition());
                    }
                } else {
                    System.out.println(rr.targetObject);
                }
            }
            decrementSmotherTimer();
        }

    }

    @Override
    public void dazed() {
        Texture texture = directory.getEntry("platform-mothDAZED", Texture.class);
        setTexture(texture);
        if (getDazedTimer() == 0) {
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            stop();
            decrementDazedTimer();
        }
    }


}

