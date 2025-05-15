package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import edu.cornell.cis3152.physics.level_player.enemies.*;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy.EnemyState;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.cis3152.physics.level_player.player.*;
import edu.cornell.cis3152.physics.level_player.player.Avatar.GroundState;
import edu.cornell.cis3152.physics.level_player.utils.CollisionFlag;
import edu.cornell.cis3152.physics.level_player.utils.ContactKey;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics2.ObstacleSprite;

import java.util.*;

public class CollisionController implements ContactListener {

    private final Stack<CollisionFlag> collisionFlags;
    private final List<Totem[]> pendingTotemMerges = new ArrayList<>();
    private final AssetDirectory directory;
    private final FireController fireController;
    private boolean directionFlag;
    private Map<ContactKey, Integer> sustainedContacts = new HashMap<>();
    private boolean beginSmother = false;


    public CollisionController(AssetDirectory directory, FireController fireController) {
        this.directory = directory;
        this.fireController = fireController;
        this.collisionFlags = new Stack<>();
        this.sustainedContacts = new HashMap<>();
    }

    public boolean beginSmother(){return beginSmother;}

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

    public boolean isGround(ObstacleSprite sprite) {
        if (sprite instanceof Totem) {
            return ((Totem) (sprite)).getState() != EnemyState.OUT_OF_LIGHT;
        }
        return sprite.getName().contains("floor") || sprite.getName().contains("platform") ||
               sprite.getName().contains("barrier") || sprite.getName().contains("spinner") ||
               sprite.getName().contains("button") || sprite.getName().contains("burnable");
    }

    public Stack<CollisionFlag> getCollisionFlags() {
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

            if (isX(bd1, bd2, Fire.class) == 2) {
                return;
            }
            if (isX(bd1, bd2, Smoke.class) == 2) {
                return;
            }
            if (isX(bd1, bd2, "rain") == 2) {
                return;
            }

            if (isX(bd1, bd2, "rain") == 1) {
                ObstacleSprite nonRain = bd1.getObstacle().getName().contains("rain") ? bd2 : bd1;
                if ((nonRain.getObstacle().getBodyType() == BodyType.KinematicBody || nonRain.getObstacle().getBodyType() == BodyType.StaticBody)
                    && !nonRain.getObstacle().isSensor() && !nonRain.getName().contains("grate") ) {
                    collisionFlags.add(new CollisionFlag("resetRain",idX(bd1, bd2, "rain") ));

                } else if ((nonRain instanceof Enemy || nonRain instanceof Avatar || nonRain.getName().contains("platform") || nonRain.getName().contains("burnable"))&& !nonRain.getName().contains("grate")) {
                    collisionFlags.add(new CollisionFlag("resetRain",idX(bd1, bd2, "rain") ));
                }
            }

            if (isXandY(bd1,bd2, Fire.class, Moth.class) == 1){
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                Fire fire = (Fire) idX(bd1, bd2, Fire.class);
                Vector2 v = moth.getObstacle().getLinearVelocity();
                moth.getObstacle().setLinearVelocity(new Vector2 (v.x * 0.8f, v.y * 0.8f));
            }

            /**
             * Avatar and Torch collision.
             * If the player is not climbing then the avatar picks up the torch
             */
            if (isXandY(bd1, bd2, Avatar.class, Torch.class )== 1) {
                if (((Avatar)idX(bd1,bd2, Avatar.class)).getGroundedState() != GroundState.CLIMBING) {
                    collisionFlags.push(new CollisionFlag("addTorch", idX(bd1, bd2, Avatar.class)));
                }
            }

//            if (isXandY(bd1, bd2, "platform", Enemy.class) == 1) {
//                Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
//                ObstacleSprite platform = idX(bd1, bd2, "platform");
//                Vector2 enemyPos = enemy.getObstacle().getBody().getPosition();
//                Vector2 platformPos = platform.getObstacle().getBody().getPosition();
//
//                float xDiff = Math.abs(enemyPos.x - platformPos.x);
//                float yDiff = Math.abs(enemyPos.y - platformPos.y);
//
//                if (xDiff > yDiff) {
//                    System.out.println("LEFT RIGHT COLLISION");
//                    enemy.changeDirection();
//                    enemy.setJustCollided(true);
//                }
//            }


            if (isXandY(bd1, bd2, "wall", Enemy.class) == 1) {
                Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
                enemy.changeDirection();
                enemy.setJustCollided(true);
            }



            if (isXandY(bd1, bd2, "nonburnable", Moth.class) == 1) {
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                moth.changeDirection();
                moth.setJustCollided(true);
            }

            if (isXandY(bd1, bd2, "infburnable", Moth.class) == 1) {
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                Object other = (bd1.getObstacle().getUserData() instanceof Moth) ? bd2.getObstacle().getUserData() : bd1.getObstacle().getUserData();
//                if (moth.getState() == EnemyState.ATTACK){
                    EnhancedObstacleSprite inf = (GameObject) other;
                    if (fireController.isBodyOnFire(inf)){
                        fireController.extinguishFireObject(inf);
                    }
//                }
                moth.changeDirection();
                moth.setJustCollided(true);
            }

//            if (isXandY(bd1, bd2, "ground", Enemy.class) == 1) {
//                Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
//                enemy.changeDirection();
//                enemy.setJustCollided(true);
//            }


//            if (isX(bd1,bd2, Enemy.class) == 1) {
//                Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
//                if (isGround(bd1) || isGround(bd2)) {
//                    enemy.setGrounded(true);
//                }
//            }

            if (isXandY(bd1, bd2, Moth.class, Totem.class) == 1) {
                Totem totem = (Totem) idX(bd1, bd2, Totem.class);
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                if (totem.getState() != Enemy.EnemyState.OUT_OF_LIGHT) {
                    moth.changeDirection();
                }
            }

            /**
             * Totem to Totem collision:
             * If the totem collision is left to right, the totems just bounce off and switch direction
             * If the totem collision is top to bottom, the totems combine through a joint and become a totem stack.
             */
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
                    topTotem.addLinkedTotem(bottomTotem);
                } else if (xDiff > 0.5f && yDiff < 0.5f) {
                    if (totem1.getState() != EnemyState.CD && totem1.getState() != EnemyState.IN_LIGHT) {
                        totem1.changeDirection();
                        totem1.setJustCollided(true);
                    }
                    if (totem2.getState() != EnemyState.CD && totem2.getState() != EnemyState.IN_LIGHT) {
                        totem2.changeDirection();
                        totem2.setJustCollided(true);
                    }
                }

            }

            /**
             * Totem and Light collision
             * While the totem is in light, it changes to the IN_LIGHT state.
             */
            if (isXandY(bd1, bd2, Lighting.class, Totem.class) == 1) {
                Lighting light = (Lighting) idX(bd1, bd2, Lighting.class);
                Totem totem = (Totem) idX(bd1, bd2, Totem.class);
                totem.stopTimer();
                totem.resetCooldownTimer();

                    if (light.getObstacle().getX() < totem.getObstacle().getX()) {
                        totem.setFreezeRight(true);
                        totem.lockFreeze();
                    } else {
                        totem.setFreezeRight(false);
                        totem.lockFreeze();
                    }
                    totem.resetFreeze();
                    totem.setState(EnemyState.IN_LIGHT);


            }

            /**
             * Moth and Torch collision
             * If the moth comes into contact with the torch, then the moth changes to the SMOTHER state.
             */
//            if (isXandY(bd1, bd2, Moth.class, Fire.class) == 1) {
//                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
//                Fire fire = (Fire) idX(bd1, bd2, Fire.class);
//                if (moth.getState() != EnemyState.DAZED && !Avatar.getHasTorch()) {
//                    float mothX  = moth.getObstacle().getBody().getPosition().x;
//                    float fireX = fire.getObstacle().getBody().getPosition().x;
//                    moth.resetSmotherTimer();
//                    moth.setState(EnemyState.SMOTHER);
//                    beginSmother = true;
//                    beginSmother();
//                } else if (moth.getState() != EnemyState.DAZED && Avatar.getHasTorch()) {
//                    moth.setState(EnemyState.CD);
//                } else {
//                    // still stay in the smother state;
//                }
//            }

            if (isXandY(bd1, bd2, Moth.class, Torch.class) == 1) {
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);

                Torch torch = (Torch) idX(bd1, bd2, Torch.class);
                if (moth.getState() != EnemyState.DAZED && !Avatar.getHasTorch()) {
                    float mothX  = moth.getObstacle().getBody().getPosition().x;
                    float torchX = torch.getObstacle().getBody().getPosition().x;
                    moth.resetSmotherTimer();
                    moth.setState(EnemyState.SMOTHER);
                    beginSmother = true;
                } else if (moth.getState() != EnemyState.DAZED && Avatar.getHasTorch()) {
                    moth.setState(EnemyState.CD);
                } else {
                    // still stay in the smother state;
                }
                if (moth.getState()!=EnemyState.SMOTHER){
                    beginSmother = false;
                }

            }

//            if (isXandY(bd1, bd2, Moth.class, Moth.class) == 2) {
//                Moth moth1 = (Moth) bd1;
//                Moth moth2 = (Moth) bd2;
//                if (moth1.getState() != EnemyState.IN_LIGHT) {
//                    moth1.changeDirection();
//                    moth1.setJustCollided(true);
//                }
//                if (moth2.getState() != EnemyState.IN_LIGHT) {
//                    moth2.changeDirection();
//                    moth2.setJustCollided(true);
//                }
//
//            }

            /**
             * Moth and Light Collision
             * Whenever the moth is in the light radius, the moth changes in to an IN_LIGHT stage, and follows the
             * light.
             */
            if (isXandY(bd1, bd2, Lighting.class, Moth.class) == 1) {
                Lighting light = (Lighting) idX(bd1, bd2, Lighting.class);
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                if (moth.getState() != EnemyState.JUMP && moth.getState() != EnemyState.SMOTHER && moth.getState() != EnemyState.DAZED && moth.getState() != EnemyState.ATTACK && moth.getState() != EnemyState.TRANCE) {
                    moth.setState(EnemyState.IN_LIGHT);

                    float lx = light.getObstacle().getX();
                    float mx = moth.getObstacle().getX();

                    if ((lx < mx && moth.isFacingRight()) || lx > mx && !moth.isFacingRight()) {
                        moth.changeDirection();
                    }
                }

            }

            /**
             * Player and Moth Collision
             * Whenever the player hits a moth entity, they die.
             */
            if (isXandY(bd1, bd2, Moth.class, Avatar.class) == 1) {
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                Avatar player = (Avatar) idX(bd1, bd2, Avatar.class);

                if (moth.getState() != EnemyState.DAZED) {

                    Vector2 mothPos = moth.getObstacle().getBody().getPosition();
                    Vector2 playerPos = player.getObstacle().getBody().getPosition();

                    float xDiff = Math.abs(mothPos.x - playerPos.x);
                    float yDiff = Math.abs(mothPos.y - playerPos.y);

                    if (yDiff > xDiff) {
                        if ((moth.getState() == EnemyState.SMOTHER) && playerPos.y > mothPos.y) {
                            moth.resetDazedTimer();
                            moth.setState(EnemyState.DAZED);
                            //beginSmother = false;
                            Body playerBody = player.getObstacle().getBody();
                            float bounceImpulse = 0.15f * player.getUnits();
                            playerBody.applyLinearImpulse(
                                new Vector2(0, bounceImpulse),
                                playerBody.getWorldCenter(),
                                true
                            );
                        } else {
                            player.die();
                            collisionFlags.push(new CollisionFlag("queueFailure"));
                        }
                    }else {
                        player.die();
                        collisionFlags.push(new CollisionFlag("queueFailure"));

                    }
                }
            }


            if (isXandY(bd1, bd2, EnhancedObstacleSprite.class, Fire.class) == 1 ) {
                Fire f = (Fire) idX(bd1, bd2, Fire.class);
                EnhancedObstacleSprite b = (EnhancedObstacleSprite) idX(bd1, bd2, EnhancedObstacleSprite.class);
                if (b.getMaterial().getFlammability() > 0 && !fireController.testIfFullyBurning(b)) {
                    ContactKey key = new ContactKey(fix1, fix2);
                    sustainedContacts.put(key, 1);
                }
            }

            if (isXandY(bd1, bd2, Moth.class, Torch.class) == 1) {
                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
                Torch torch = (Torch) idX(bd1, bd2, Torch.class);
                if (moth.getState() == EnemyState.SMOTHER) {
                    ContactKey key2 = new ContactKey(fix1, fix2);
                    sustainedContacts.put(key2, 300);
                    beginSmother = true;
                }
            }

            if (isX(bd1, bd2, Avatar.class) == 1 && isX(bd1,bd2,"goalDoor") == 1) {
                ContactKey key = new ContactKey(fix1, fix2);
                sustainedContacts.put(key, -1);
            }

            /**
             * Collision detection to see if the player is overlapping with any climbable entities
             * then stores them within the player for future joint creation
             */
            if (isXandY(bd1, bd2, Avatar.class, EnhancedObstacleSprite.class) == 1) {
                EnhancedObstacleSprite eos = (EnhancedObstacleSprite) idX(bd1, bd2, EnhancedObstacleSprite.class);
                Avatar traci = (Avatar) idX(bd1, bd2, Avatar.class);
                Fixture subjectFixture = (bd1.getClass().equals(Avatar.class)) ? fix1 : fix2;

                if (eos.getClimbable() && (subjectFixture.getUserData() == null || !subjectFixture.getUserData().equals(traci.getSensorName()))) {
                    collisionFlags.add(new CollisionFlag("addClimbingJoint", eos));
                }
            }
            if (isXandY(bd1, bd2, Avatar.class, Coin.class) == 1) {
                Coin coin = (Coin) idX(bd1, bd2, Coin.class);
                collisionFlags.push(new CollisionFlag("collect_coin", coin));
            }

            if (isX(bd1,bd2, "trackerBall") == 1) {
                if (isGround(bd1) || isGround(bd2)) {
                    ((ObstacleSprite) idX(bd1,bd2,"trackerBall")).getObstacle().markRemoved(true);
                }
            }

            if (isXandY(bd1,bd2, Lighting.class, Rune.class) == 1) {
                ((Rune) idX(bd1,bd2,Rune.class)).addInLight();
                ContactKey key = new ContactKey(fix1, fix2);
                sustainedContacts.put(key, -1);
            }

            if (isX(bd1, bd2, Torch.class) == 1) {
                ObstacleSprite nonTorch = bd1 instanceof Torch ? bd2 : bd1;
                // ObstacleSprite torch = idX(bd1, bd2, Torch.class);
                Torch torch = (Torch) idX(bd1, bd2, Torch.class);
                if (isGround(nonTorch)) {
                    float av = torch.getObstacle().getAngularVelocity();
                    torch.getObstacle().setAngularVelocity(Math.signum(av) * .8f);
                    torch.getObstacle().setLinearVelocity(Vector2.Zero);

                    if(!torch.hasLanded() && !torch.isBeingHeld() && torch.getLandCooldownTimer() <= 0) {
                        torch.setHasLanded(true);
                        torch.setLandCooldownTimer(60);
                        collisionFlags.push(new CollisionFlag("torchLand", torch));
                    }
                }
            }

            if (isXandY(bd1, bd2, Avatar.class, GameObject.class) == 1) {
                Fixture subjectFixture = (bd1.getClass().equals(GameObject.class)) ? fix1 : fix2;

                if (subjectFixture.getUserData() != null && subjectFixture.getUserData().equals("crushSensor")) {
                    collisionFlags.add(new CollisionFlag("queueFailure"));
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

                Vector2 f_pos = f.getObstacle().getPosition().cpy();
                Vector2 b_pos = b.getObstacle().getPosition().cpy();
                Vector2 delta = (b_pos.cpy()).sub(f_pos);
                if (b.getMaterial().surpassIgnitionTimer(contactTime) && !fireController.testIfFullyBurning(b)) {

                    Vector2 newFirePos;
                    if (b.getName().contains("rope")) {
                        newFirePos = b_pos;
                    } else {
                        b_pos.sub(f_pos);
                        b_pos.nor().scl(f.getRadius());
                        newFirePos = delta.len() < b_pos.len() ? f_pos.add(delta) : f_pos.add(b_pos);
                    }

                    fireController.lightAnew(b, newFirePos);
                    it.remove();
                } else {
                    float modif;
                    if (Objects.equals(b.getMaterial().getName(), "rope")) {
                        modif = (float) (1 / (Math.pow(delta.len(),1)));
                    } else {
                        modif = 1;
                    }
                    sustainedContacts.put(key, (int) (contactTime + modif));
                }
            }

//            if (isXandY(bd1, bd2, Torch.class, Moth.class) == 1) {
//                Moth moth = (Moth) idX(bd1, bd2, Moth.class);
//                Torch torch = (Torch) idX(bd1, bd2, Torch.class);
//                int contactTime = sustainedContacts.get(key);
//                if (moth.getState() == EnemyState.SMOTHER) {
//                    if (contactTime == 0) {
//                        beginSmother = false;
//                        collisionFlags.push(new CollisionFlag("queueFailure"));
//                    } else {
//                        sustainedContacts.put(key, contactTime - 1);
//                    }
//                }
//            }

            if (isXandY(bd1, bd2, Torch.class, Moth.class) == 1) {
                Moth moth   = (Moth)  idX(bd1, bd2, Moth.class);
                Torch torch = (Torch) idX(bd1, bd2, Torch.class);
                int contactTime = sustainedContacts.get(key);

                if (moth.getState() == EnemyState.SMOTHER) {
                    if (contactTime == 0) {
                        beginSmother = false;
                        float mothX  = moth.getObstacle().getBody().getPosition().x;
                        float torchX = torch.getObstacle().getBody().getPosition().x;
                        if (torchX < mothX && moth.isFacingRight()) {
                            moth.changeDirection();
                        } else if (torchX > mothX && !moth.isFacingRight()) {
                            moth.changeDirection();
                        }
                        collisionFlags.push(new CollisionFlag("queueFailureTorchOff"));
                    } else {
                        sustainedContacts.put(key, contactTime - 1);
                    }
                }
                if (moth.getState() == EnemyState.IN_LIGHT){
                    moth.setState(EnemyState.SMOTHER);
                }
            }


            if (isX(bd1, bd2, Avatar.class) == 1 && isX(bd1,bd2,"goalDoor") == 1) {
                if (Avatar.getHasTorch()) {
                    collisionFlags.push(new CollisionFlag("queueWin"));
                    it.remove();
                } else {
                    sustainedContacts.put(key, -1);
                }
            }

            if (isXandY(bd1,bd2, Lighting.class, Rune.class) == 1) {
                Rune rune = (Rune) idX(bd1,bd2,Rune.class);
                Lighting lighting = (Lighting) idX(bd1,bd2,Lighting.class);
                float distance = Math.abs((new Vector2(lighting.getObstacle().getPosition()).sub(rune.getObstacle().getPosition())).len());
                if (rune.getPowerLevel() < 1) {
                    rune.addPowerLevel();
                }
                sustainedContacts.put(key, -1);
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


//        if (isX(bd1, bd2, Avatar.class) == 1) {
//            Avatar t = (Avatar) idX(bd1, bd2, Avatar.class);
//            if ((isGround(bd1) || isGround(bd2)) && (((t.getSensorName().equals(fd2) && t != bd1) || (t.getSensorName().equals(fd1) && t != bd2)) && t.getGroundedState().equals(GroundState.GROUNDED))) {
//                collisionFlags.push(new CollisionFlag("traciAirborne", bd1 instanceof Avatar ? fix2 : fix1));
//            }
//        }
//

        if (isX(bd1,bd2, Enemy.class) == 1) {
            Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
            if (isGround(bd1) || isGround(bd2)) {
                enemy.setGrounded(false);
            }
        }


        /**
         * Totem and Light collision:
         * When the totem leaves the light radius, it will go to the CD stage for FreezeTimer seconds, then it
         * transitions to the OUT_OF_LIGHT stage. While in the CD stage, it behaves exactly like IN_LIGHT stage.
         */

        if (isXandY(bd1, bd2, Lighting.class, Totem.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            totem.beginTimer();
        }

        /**
         * Moth and Light collision:
         * When the moth leaves the light radius, it changes back to OUT_OF_LIGHT stage.
         */
        if (isXandY(bd1, bd2, Lighting.class, Moth.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            if (moth.getState() != EnemyState.DAZED && moth.getState() != EnemyState.ATTACK && moth.getState() != EnemyState.SMOTHER && moth.getState() != EnemyState.JUMP) {
                moth.setState(EnemyState.OUT_OF_LIGHT);

            }
        }
        if (isXandY(bd1, bd2, Fire.class, EnhancedObstacleSprite.class) == 1) {
            ContactKey key = new ContactKey(fix1, fix2);
            sustainedContacts.remove(key);
        }

        if (isX(bd1, bd2, Avatar.class) == 1 && isX(bd1,bd2,"goalDoor") == 1) {
            ContactKey key = new ContactKey(fix1, fix2);
            sustainedContacts.remove(key);
        }

        if (isXandY(bd1,bd2, Lighting.class, Rune.class) == 1) {
            ((Rune) idX(bd1,bd2,Rune.class)).subInLight();
            ContactKey key = new ContactKey(fix1, fix2);
            sustainedContacts.remove(key);
        }

        /**
         * Whenever enemies bump into each other, they just turn the other way
         */
        if (isXandY(bd1, bd2, Moth.class, Moth.class) == 2) {
            Moth enemy1 = (Moth) bd1;
            Moth enemy2 = (Moth) bd2;
            enemy1.setJustCollided(false);
            enemy2.setJustCollided(false);
        }

        if (isXandY(bd1, bd2, Moth.class, Totem.class) == 2){
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            moth.setJustCollided(false);
        }

        if (isXandY(bd1, bd2, "wall", Enemy.class) == 1) {
            Enemy enemy = (Enemy) idX(bd1, bd2, Enemy.class);
            enemy.setJustCollided(false);

        }

        if (isXandY(bd1, bd2, "burnable", Moth.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            moth.setJustCollided(false);
        }


        /**
         * Moth and Torch collision
         * If the moth comes out of contact with the torch, then the moth changes to the SMOTHER state.
         */
        if (isXandY(bd1, bd2, Moth.class, Torch.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            Torch  torch = (Torch) idX(bd1, bd2, Torch.class);
            if (moth.getState() != EnemyState.DAZED) {
//                if (moth.getState() == EnemyState.SMOTHER) {
                    float mothX = moth.getObstacle().getBody().getPosition().x;
                    float torchX = torch.getObstacle().getBody().getPosition().x;

                    if ((torchX < mothX && moth.isFacingRight()) ||
                        (torchX > mothX && !moth.isFacingRight())) {
                        moth.changeDirection();
                    }
                moth.setState(EnemyState.OUT_OF_LIGHT);
            }else {
                beginSmother = false;
            }

            ContactKey key = new ContactKey(fix1, fix2);
            sustainedContacts.remove(key);
//                collisionFlags.push(new Object[]{"addTorch", idX(bd1, bd2, Traci.class)});
        }

        /**
         * Ground and Torch collision
         * If the torch leaves the ground, set the hasLanded state back to false.
         */
        if (isX(bd1, bd2, Torch.class) == 1) {
            Torch torch = (Torch) idX(bd1, bd2, Torch.class);
            ObstacleSprite other = (bd1 instanceof Torch) ? bd2 : bd1;

            if (isGround(other)) {
                torch.setHasLanded(false);
            }
        }

        /**
         * End contact collision detection to remove a climbable object from the player's
         * repository of such, mainly to handle joint creation/destruction as its sister
         * method in begin contact
         */
        if (isXandY(bd1, bd2, Avatar.class, EnhancedObstacleSprite.class) == 1) {
            EnhancedObstacleSprite eos = (EnhancedObstacleSprite) idX(bd1, bd2, EnhancedObstacleSprite.class);
            Avatar traci = (Avatar) idX(bd1,bd2, Avatar.class);
            Fixture subjectFixture = (bd1.getClass().equals(Avatar.class)) ? fix1 : fix2;

            if (eos.getClimbable() && (subjectFixture.getUserData() == null || !subjectFixture.getUserData().equals(traci.getSensorName()))) {
                collisionFlags.add(new CollisionFlag("removeClimbingJoint", eos));
            }
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


        if (isXandY(bd1, bd2, Avatar.class, Enemy.class) == 1) {
            if (((Avatar)idX(bd1,bd2,Avatar.class)).isDead()) {
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, Moth.class, Avatar.class) == 1 || isXandY(bd1, bd2, "platform", Avatar.class) == 1) {
            Avatar avatar = (Avatar) idX(bd1, bd2, Avatar.class);
            if (avatar.isDead() || avatar.getGroundedState() == GroundState.DEAD){
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, Totem.class, Avatar.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            if (totem.getState() == EnemyState.OUT_OF_LIGHT) {
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, "burnable", Totem.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            if (totem.getState() == EnemyState.OUT_OF_LIGHT) {
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, Moth.class, Totem.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            if (totem.getState() == EnemyState.OUT_OF_LIGHT) {
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, Totem.class, Totem.class) == 1) {
            Totem totem = (Totem) idX(bd1, bd2, Totem.class);
            if (totem.getState() == EnemyState.OUT_OF_LIGHT) {
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, Moth.class, Torch.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
//            if (moth.getState() == EnemyState.DAZED) {
                contact.setEnabled(false);
//            }
        }


        if (isXandY(bd1, bd2, Moth.class, Avatar.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            if (moth.getState() == EnemyState.DAZED || moth.getState() == EnemyState.SMOTHER || moth.getState() == EnemyState.JUMP) {
                contact.setEnabled(false);
            }
        }

        if (isXandY(bd1, bd2, Moth.class, Lighting.class) == 1) {
            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
            if (moth.getState() == EnemyState.DAZED) {
                contact.setEnabled(false);
            }
        }

                    if (isXandY(bd1, bd2, Moth.class, Moth.class) == 2) {
                        Moth moth1 = (Moth) bd1;
                        Moth moth2 = (Moth) bd2;
                        contact.setEnabled(false);
                    }

//        if (isXandY(bd1, bd2, Moth.class, Torch.class) == 1) {
//            Moth moth = (Moth) idX(bd1, bd2, Moth.class);
//            if (moth.getState() == EnemyState.SMOTHER || moth.getState() == EnemyState.JUMP) {
//                contact.setEnabled(false);
//            }
//        }
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

    public void reset(){//resets properties
        // imma not proud of this im sorry
        beginSmother = false;
    }
}
