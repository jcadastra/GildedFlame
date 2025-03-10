package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import edu.cornell.cis3152.physics.level_player.enemies.*;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy.EnemyState;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.cis3152.physics.level_player.player.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.Stack;

public class CollisionController implements ContactListener {

    private Stack<Object[]> todos;

    public Stack<Object[]> getTodos() {
        return todos;
    }

    private AssetDirectory directory;

    public CollisionController(AssetDirectory directory) {
        this.directory = directory;
        this.todos = new Stack<Object[]>();
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects. We use this method
     * to test if it is the "right" kind of collision. In particular, we use it to test if we made
     * it to the win door.
     *
     * @param contact The two bodies that collided
     */
    @Override
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            ObstacleSprite bd1 = (ObstacleSprite) body1.getUserData();
            ObstacleSprite bd2 = (ObstacleSprite) body2.getUserData();

            if (isXandY(bd1, bd2, "bullet", Traci.class) > 0) {
                todos.push(new Object[]{"removeBullet", idX(bd1, bd2, "bullet")});
            }

            if ((isGround(bd1) || isGround(bd2)) && isX(bd1, bd2, Traci.class) == 1){
                ((Traci) idX(bd1,bd2,Traci.class)).setGrounded(true);
                todos.push(new Object[]{"traciGrounded", bd1 instanceof Traci ? fix2 : fix1});
            }

            if (isX(bd1, bd2, Torch.class) + isX(bd1,bd2,Traci.class) == 2) {
                ((Traci) idX(bd1,bd2,Traci.class)).setHasTorch(true);
                todos.push(new Object[]{"addTorch", idX(bd1, bd2, Traci.class)});
            }

            if (isXandY(bd1, bd2, "wall", Enemy.class) == 1) {
                ((Enemy) idX(bd1, bd2, Enemy.class)).changeDirection();
            }

            if (isXandY(bd1, bd2, Totem.class, Moth.class) == 1) {
                Totem totem = (Totem) idX(bd1,bd2, Totem.class);
                Moth moth = (Moth) idX(bd1,bd2, Moth.class);


                Body mothBody = moth.getObstacle().getBody();
                Body totemBody = totem.getObstacle().getBody();

                float bounceForce = 10.0f;
                float bounceDirection = (mothBody.getPosition().x < totemBody.getPosition().x) ? -1 : 1;


                if (moth.getState() == Enemy.EnemyState.OUT_OF_LIGHT){
                    moth.changeDirection();
                } else if (moth.getState() == Enemy.EnemyState.ATTACK){
                    mothBody.applyLinearImpulse(new Vector2(bounceDirection * bounceForce, 3.0f), mothBody.getWorldCenter(), true);
                    moth.changeDirection();
                }

                if (totem.getState() == Enemy.EnemyState.OUT_OF_LIGHT){
                    totem.changeDirection();
                };
            }

            if (isXandY(bd1,bd2, Light.class, Totem.class) == 1) {
                Totem totem = (Totem) idX(bd1,bd2, Totem.class);
                totem.setState(Enemy.EnemyState.IN_LIGHT);
            }

            if (isXandY(bd1, bd2, Light.class, Moth.class) == 1) {
                Light light = (Light) idX(bd1, bd2, Light.class);
                Moth moth = (Moth) idX(bd1,bd2, Moth.class);

                float lx = light.getObstacle().getX();
                float mx = moth.getObstacle().getX();

                if (lx < mx && !moth.isFacingRight()) {
                    moth.changeDirection();
                } else if (lx > mx && moth.isFacingRight()) {
                    moth.changeDirection();
                }

                moth.setState(Enemy.EnemyState.IN_LIGHT);
                moth.resetAttackTimer();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch. The main use of this method is to
     * determine when the characer is NOT on the ground. This is how we prevent double jumping.
     */
    @Override
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        ObstacleSprite bd1 = (ObstacleSprite) body1.getUserData();
        ObstacleSprite bd2 = (ObstacleSprite) body2.getUserData();


        if ((isGround(bd1) || isGround(bd2)) && isX(bd1, bd2, Traci.class) == 1){
            todos.push(new Object[]{"traciAirborne", bd1 instanceof Traci ? fix2 : fix1});
        }

        if (isXandY(bd1, bd2, Light.class, Totem.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            totem.setState(Enemy.EnemyState.OUT_OF_LIGHT);
        }

        if (isXandY(bd1, bd2, Light.class, Moth.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            moth.setState(EnemyState.OUT_OF_LIGHT);
        }
    }


    /**
     * Unused ContactListener method
     */
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }


    /**
     * Overridden preSolve method to disable collision between the avatar and Totem. This allows the
     * avatar to pass through the Totem while other collisions remain active.
     */
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        ObstacleSprite bd1 = (ObstacleSprite) body1.getUserData();
        ObstacleSprite bd2 = (ObstacleSprite) body2.getUserData();


        if (isXandY(bd1, bd2, Totem.class, Traci.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            if (totem.getState() != Enemy.EnemyState.IN_LIGHT) {
                contact.setEnabled(false);
            }
        }
    }

    private static <T,U> int isXandY (ObstacleSprite a, ObstacleSprite b, Class<T> x, Class<U> y) {
        return (x.isInstance(a) && y.isInstance(b) ? 1 : 0) + (x.isInstance(b) && y.isInstance(a) ? 1 : 0);
    }
    private static <U> int isXandY (ObstacleSprite a, ObstacleSprite b, String x, Class<U> y) {
        return (a.getName().contains(x) && y.isInstance(b) ? 1 : 0) + (b.getName().contains(x) && y.isInstance(a) ? 1 : 0);
    }
    private int isXandY (ObstacleSprite a, ObstacleSprite b, String x, String y) {
        return (a.getName().contains(x) && b.getName().contains(y) ? 1 : 0) + (b.getName().contains(x) && a.getName().contains(y) ? 1 : 0);
    }
    private static <T> int isX (ObstacleSprite a, ObstacleSprite b, Class<T> x) {
        return (x.isInstance(a) ? 1 : 0) + (x.isInstance(b) ? 1 : 0);
    }
    private int isX (ObstacleSprite a, ObstacleSprite b, String x) {
        return (a.getName().contains(x) ? 1 : 0) + (b.getName().contains(x) ? 1 : 0);
    }
    private static <T> ObstacleSprite idX (ObstacleSprite a, ObstacleSprite b, Class<T> x) {
        return (x.isInstance(a) ? a : b);
    }
    private ObstacleSprite idX (ObstacleSprite a, ObstacleSprite b, String x) {
        return (a.getName().contains(x) ? a : b);
    }
    private static boolean isGround(ObstacleSprite sprite) {
        return sprite.getName().equals("floor") ||
        sprite.getName().equals("barrier") ||
        sprite.getName().equals("spinner") ||
        sprite.getName().equals("surface")|| sprite instanceof Enemy;
    }
}
