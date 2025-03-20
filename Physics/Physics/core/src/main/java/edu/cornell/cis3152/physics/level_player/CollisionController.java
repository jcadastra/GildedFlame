package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import edu.cornell.cis3152.physics.level_player.enemies.*;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy.EnemyState;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.cis3152.physics.level_player.player.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics2.ObstacleSprite;

import java.util.*;

public class CollisionController implements ContactListener {

    private final Stack<Object[]> collisionFlags;

    private boolean directionFlag;
    private final List<Totem[]> pendingTotemMerges = new ArrayList<>();
    private Map<ContactKey, Integer> sustainedContacts = new HashMap<>();
    private final AssetDirectory directory;
    private final FireController fireController;
    public CollisionController(AssetDirectory directory, FireController fireController) {
        this.directory = directory;
        this.fireController = fireController;
        this.collisionFlags = new Stack<>();
        this.sustainedContacts = new HashMap<>();
    }

    /**
     * Next three are for querying if a is of type x or y and b is of the other type
     * <p>
     * 0 means no combo works ie a and b are neither x or y
     * 1 means that there is a comb that works ie a is x and b is y || a is y and b is x
     * 2 means that both combos work ie a == b == x == y
     */
    private static <T, U> int isXandY(ObstacleSprite a, ObstacleSprite b, Class<T> x, Class<U> y) {
        return (x.isInstance(a) && y.isInstance(b) ? 1 : 0) + (x.isInstance(b) && y.isInstance(a) ? 1 : 0);
    }

    private static <U> int isXandY(ObstacleSprite a, ObstacleSprite b, String x, Class<U> y) {
        return (a.getName().contains(x) && y.isInstance(b) ? 1 : 0) + (b.getName().contains(x) && y.isInstance(a) ? 1 : 0);
    }

    /**
     * Next two are for querying if either a or b is of type x
     * <p>
     * 0 means none are of x, 1 means one is, 2 means both are
     */
    private static <T> int isX(ObstacleSprite a, ObstacleSprite b, Class<T> x) {
        return (x.isInstance(a) ? 1 : 0) + (x.isInstance(b) ? 1 : 0);
    }

    /**
     * Next two are used mainly when you know either a or b is x but not both
     * <p>
     * returns whichever is of X; undefined behavior when a.class == b.class == x.class
     */
    private static <T> ObstacleSprite idX(ObstacleSprite a, ObstacleSprite b, Class<T> x) {
        return (x.isInstance(a) ? a : b);
    }

    private static boolean isGround(ObstacleSprite sprite) {
        return sprite.getName().equals("floor") || sprite.getName().equals("platform") || sprite.getName().equals("barrier") || sprite.getName().equals("spinner") || sprite.getName().equals("surface") || sprite instanceof Totem;
    }

    public Stack<Object[]> getCollisionFlags() {
        return collisionFlags;
    }

    private void createTotemJoint(Totem topTotem, Totem bottomTotem) {
        Body topBody = topTotem.getObstacle().getBody();
        Body bottomBody = bottomTotem.getObstacle().getBody();

        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = topBody;
        jointDef.bodyB = bottomBody;
        jointDef.localAnchorA.set(0, -0.5f);
        jointDef.localAnchorB.set(0, 0.5f);
        jointDef.collideConnected = false;

        topBody.getWorld().createJoint(jointDef);
        topTotem.getObstacle().getBody().setType(BodyDef.BodyType.StaticBody);
    }

    public void processPendingMerges() {
        for (Totem[] pair : pendingTotemMerges) {
            createTotemJoint(pair[0], pair[1]);
        }
        pendingTotemMerges.clear();
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

            if (isXandY(bd1, bd2, Fire.class, Fire.class) == 2) {
                return;
            }

            if ((isGround(bd1) || isGround(bd2)) && isX(bd1, bd2, Traci.class) == 1) {
                ((Traci) idX(bd1, bd2, Traci.class)).setGrounded(true);
            }

            if (isX(bd1, bd2, Traci.class) == 1) {
                Traci t = (Traci) idX(bd1, bd2, Traci.class);
                if ((t.getSensorName().equals(fd2) && t != bd1 && isGround(bd1)) || (t.getSensorName().equals(fd1) && t != bd2 && isGround(bd2))) {
                    t.setGrounded(true);
                    collisionFlags.push(new Object[]{"traciGrounded", bd1 instanceof Traci ? fix2 : fix1});
                }
            }

            if (isX(bd1, bd2, Torch.class) + isX(bd1, bd2, Traci.class) == 2) {
                ((Traci) idX(bd1, bd2, Traci.class)).setHasTorch(true);
                collisionFlags.push(new Object[]{"addTorch", idX(bd1, bd2, Traci.class)});
            }

            if (isXandY(bd1, bd2, "wall", Enemy.class) == 1) {
                Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
                enemy.setJustCollided(false);
                enemy.changeDirection();
            }

            if (isXandY(bd1, bd2, "floor", Enemy.class) == 1) {
                Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
                enemy.setGrounded(true);
            }
            if (isXandY(bd1, bd2, Totem.class, Moth.class) == 1) {
                Totem totem = (Totem) idX(bd1, bd2, Totem.class);
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);


                Body mothBody = moth.getObstacle().getBody();
                Body totemBody = totem.getObstacle().getBody();

                float bounceForce = 10.0f;
                float bounceDirection = (mothBody.getPosition().x < totemBody.getPosition().x) ? -1 : 1;


                if (moth.getState() == Enemy.EnemyState.OUT_OF_LIGHT) {
                    moth.changeDirection();
                } else if (moth.getState() == Enemy.EnemyState.ATTACK) {
                    mothBody.applyLinearImpulse(new Vector2(bounceDirection * bounceForce, 3.0f), mothBody.getWorldCenter(), true);
                    moth.changeDirection();
                }

                if (totem.getState() == Enemy.EnemyState.OUT_OF_LIGHT) {
                    totem.changeDirection();
                }
            }
            if (isXandY(bd1, bd2, Totem.class, Totem.class) == 2) {
                Totem totem1 = (Totem) bd1;
                Totem totem2 = (Totem) bd2;

                    Vector2 pos1 = body1.getPosition();
                    Vector2 pos2 = body2.getPosition();

                    float xDiff = Math.abs(pos1.x - pos2.x);
                    float yDiff = Math.abs(pos1.y - pos2.y);

                    if (xDiff < 0.5f && yDiff > 0.5f) {
                        Totem topTotem = (pos1.y > pos2.y) ? totem1 : totem2;
                        Totem bottomTotem = (topTotem == totem1) ? totem2 : totem1;
                        pendingTotemMerges.add(new Totem[]{topTotem, bottomTotem});
                    } else if (xDiff > 0.5f && yDiff < 0.5f) {
                        totem1.setJustCollided(false);
                        totem2.setJustCollided(false);
                        totem1.changeDirection();
                        totem2.changeDirection();
                        totem1.setJustCollided(true);
                        totem2.setJustCollided(true);
                    }

            }
            if (isXandY(bd1, bd2, Lighting.class, Totem.class) == 1) {
                Totem totem = (Totem) idX(bd1, bd2, Totem.class);
                totem.setState(Enemy.EnemyState.IN_LIGHT);
            }

            if (isXandY(bd1, bd2, Moth.class, Torch.class) == 1) {
                collisionFlags.push(new Object[]{"queueFailure"});
            }


            if (isXandY(bd1, bd2, Lighting.class, Moth.class) == 1) {
                Lighting light = (Lighting) idX(bd1, bd2, Lighting.class);
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);

                moth.setState(EnemyState.IN_LIGHT);


                float lx = light.getObstacle().getX();
                float mx = moth.getObstacle().getX();

                if ((lx < mx && moth.isFacingRight()) || lx > mx && !moth.isFacingRight()) {
                    moth.changeDirection();
//                } else if (lx < mx && !moth.isFacingRight()){
//                    System.out.println("Moth is correctly facing left");
//                } else {
//                    System.out.println("Moth is correctly facing right");
                }



            }

            if (isXandY(bd1, bd2, Moth.class, Traci.class) == 1) {
                collisionFlags.push(new Object[]{"queueFailure"});
            }

            if (isXandY(bd1, bd2, EnhancedObstacleSprite.class, Fire.class) == 1) {
                Fire f = (Fire) idX(bd1, bd2, Fire.class);
                EnhancedObstacleSprite b = (EnhancedObstacleSprite) idX(bd1, bd2, EnhancedObstacleSprite.class);
                if (b.getMaterial().getFlammability() > 0 && !fireController.testIfFullyBurning(b)) {
                    ContactKey key = new ContactKey(fix1, fix2);
                    sustainedContacts.put(key, 1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sustainedContact() {
        for (Iterator<ContactKey> it = sustainedContacts.keySet().iterator(); it.hasNext(); ) {
            ContactKey key = it.next();
            Fixture fix1 = key.fix1;
            Fixture fix2 = key.fix2;

            Body body1 = fix1.getBody();
            Body body2 = fix2.getBody();

            Object fd1 = fix1.getUserData();
            Object fd2 = fix2.getUserData();

            ObstacleSprite bd1 = (ObstacleSprite) body1.getUserData();
            ObstacleSprite bd2 = (ObstacleSprite) body2.getUserData();

            if (isXandY(bd1, bd2, EnhancedObstacleSprite.class, Fire.class) == 1) {
                Fire f = (Fire) idX(bd1, bd2, Fire.class);
                EnhancedObstacleSprite b = (EnhancedObstacleSprite) idX(bd1, bd2, EnhancedObstacleSprite.class);
                int contactTime = sustainedContacts.get(key);

                Vector2 delta = ((b.getObstacle().getBody().getPosition().cpy()).sub(f.getObstacle().getBody().getPosition()));

                if (b.getMaterial().getFlammability() > 0 && b.getMaterial().surpassIgnitionTimer(contactTime) && !fireController.testIfFullyBurning(b)) {

                    Vector2 f_pos = f.getObstacle().getPosition().cpy();
                    Vector2 b_pos = b.getObstacle().getPosition().cpy();
                    b_pos.sub(f_pos);
                    b_pos.nor().scl(f.getRadius());
                    fireController.lightAnew(b, delta.len() < b_pos.len() ? f_pos.add(delta) : f_pos.add(b_pos));
                    it.remove();
                } else {
                    float modif;
                    if (Objects.equals(b.getMaterial().getName(), "rope")) {
                        modif = (float) (1 / (Math.PI * Math.pow(delta.len(), 2.3) * 4));
                    } else {
                        modif = 1;
                    }
                    sustainedContacts.put(key, (int) (contactTime + modif));
                }
            }
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


        if (isX(bd1, bd2, Traci.class) == 1) {
            Traci t = (Traci) idX(bd1, bd2, Traci.class);
            if ((t.getSensorName().equals(fd2) && t != bd1) || (t.getSensorName().equals(fd1) && t != bd2)) {
                collisionFlags.push(new Object[]{"traciAirborne", bd1 instanceof Traci ? fix2 : fix1});
            }
        }

        if (isXandY(bd1, bd2, Lighting.class, Totem.class) == 1) {
//            System.out.println("CHECK");
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            totem.resetFreeze();
            totem.setState(Enemy.EnemyState.OUT_OF_LIGHT);
        }

        if (isXandY(bd1, bd2, Lighting.class, Moth.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            moth.setState(EnemyState.OUT_OF_LIGHT);
        }
        if (isXandY(bd1, bd2, Fire.class, EnhancedObstacleSprite.class) == 1) {
            ContactKey key = new ContactKey(fix1, fix2);
            sustainedContacts.remove(key);

        }

        if (isXandY(bd1, bd2, Totem.class, Totem.class) == 2) {
            Totem totem1 = (Totem) bd1;
            Totem totem2 = (Totem) bd2;
            totem1.setJustCollided(false);
            totem2.setJustCollided(false);
        }

        if (isXandY(bd1, bd2, "wall", Enemy.class) == 1){
            Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
            enemy.setJustCollided(false);

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

    private int isXandY(ObstacleSprite a, ObstacleSprite b, String x, String y) {
        return (a.getName().contains(x) && b.getName().contains(y) ? 1 : 0) + (b.getName().contains(x) && a.getName().contains(y) ? 1 : 0);
    }

    private int isX(ObstacleSprite a, ObstacleSprite b, String x) {
        return (a.getName().contains(x) ? 1 : 0) + (b.getName().contains(x) ? 1 : 0);
    }

    private ObstacleSprite idX(ObstacleSprite a, ObstacleSprite b, String x) {
        return (a.getName().contains(x) ? a : b);
    }

}

//Exception in thread "main" java.lang.IllegalArgumentException: key cannot be null.
//at com.badlogic.gdx.utils.ObjectMap.locateKey(ObjectMap.java:128)
//at com.badlogic.gdx.utils.ObjectMap.get(ObjectMap.java:183)
//at edu.cornell.gdiac.assets.AssetDirectory.getEntry(AssetDirectory.java:327)
//at edu.cornell.cis3152.physics.GameplayScene.loadLevel(GameplayScene.java:523)
//at edu.cornell.cis3152.physics.GDXRoot.exitScreen(GDXRoot.java:199)
//at edu.cornell.cis3152.physics.GameplayScene.preUpdate(GameplayScene.java:692)
//at edu.cornell.cis3152.physics.GameplayScene.render(GameplayScene.java:1008)
//at com.badlogic.gdx.Game.render(Game.java:48)
//at com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window.update(Lwjgl3Window.java:387)
//at com.badlogic.gdx.backends.lwjgl3.Lwjgl3AppShiv.loop(Lwjgl3AppShiv.java:197)
//at com.badlogic.gdx.backends.lwjgl3.Lwjgl3AppShiv.start(Lwjgl3AppShiv.java:171)
//at edu.cornell.gdiac.backend.GDXApp.<init>(GDXApp.java:73)
//at edu.cornell.cis3152.physics.lwjgl3.DesktopLauncher.main(DesktopLauncher.java:44)
//
//> Task :lwjgl3:DesktopLauncher.main() FAILED
//[Incubating] Problems report is available at: file:///C:/Users/harve/Documents/cs3152/The-Gilded-Flame/Physics/Physics/build/reports/problems/problems-report.html
//
//Execution failed for task ':lwjgl3:DesktopLauncher.main()'.
//    > Process 'command 'C:\Users\harve\.jdks\temurin-21.0.2\bin\java.exe'' finished with non-zero exit value 1
//
//    * Try:
//    > Run with --stacktrace option to get the stack trace.
//> Run with --info or --debug option to get more log output.
//> Run with --scan to get full insights.
//> Get more help at https://help.gradle.org.
//Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.
//You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
//For more on this, please refer to https://docs.gradle.org/8.11.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
//BUILD FAILED in 10m 9s
//5 actionable tasks: 1 executed, 4 up-to-date
//
//
