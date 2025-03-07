package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import edu.cornell.cis3152.physics.GameplayScene;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy;
import edu.cornell.cis3152.physics.level_player.enemies.Moth;
import edu.cornell.cis3152.physics.level_player.enemies.Totem;
import edu.cornell.cis3152.physics.level_player.enviromentals.Light;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class CollisionController implements ContactListener {

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */

    private AssetDirectory directory;
    private Torch_playground scene;
    public CollisionController(AssetDirectory d, Torch_playground s) {
        directory = d;
        scene = s;
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

            // Test bullet collision with world
            if (bd1.getName().equals("bullet") && bd2 != scene.avatar && !bd2.getName().equals("goal")) {
                scene.removeBullet(bd1);
            }

            if (bd2.getName().equals("bullet") && bd1 != scene.avatar && !bd1.getName().equals("goal")) {
                scene.removeBullet(bd2);
            }

            // See if we have landed on a platform.
            if ((scene.avatar.getSensorName().equals(fd2) && scene.avatar != bd1 && (
                bd1.getName().equals("floor") || (bd1 instanceof Enemy
                    || bd1.getName().contains("barrier"))) ||
                (scene.avatar.getSensorName().equals(fd1) && scene.avatar != bd2 && (
                    bd2.getName().equals("floor") || (bd2 instanceof Enemy || bd2.getName()
                        .contains("barrier")))
                ))) {
                scene.avatar.setGrounded(true);
                scene.sensorFixtures.add(scene.avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((scene.avatar.getSensorName().equals(fd2) && scene.avatar != bd1 && (
                bd1.getName().equals("floor") || bd1 instanceof Totem) ||
                (scene.avatar.getSensorName().equals(fd1) && scene.avatar != bd2 && (
                    bd2.getName().equals("floor") || bd2 instanceof Totem)
                ))) {
                scene.avatar.setGrounded(true);
                scene.sensorFixtures.add(scene.avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            if (bd1 == scene.torch && bd2 == scene.avatar && scene.torch.canBePickedUp()) {
                scene.avatar.setHasTorch(true);
                scene.queueAddTorch = true;
            }

            if ((bd1 instanceof Enemy && bd2.getName().startsWith("wall")) ||
                (bd2 instanceof Enemy && bd1.getName().startsWith("wall"))) {
                Enemy enemy = (bd1 instanceof Enemy) ? (Enemy) bd1 : (Enemy) bd2;
                enemy.changeDirection();
            }

            if ((bd2 instanceof Totem && bd1 instanceof Moth) || (bd1 instanceof Totem
                && bd2 instanceof Moth)) {
                ((Enemy) bd2).changeDirection();
                ((Enemy) bd1).changeDirection();
            }

            if ((bd2 instanceof Light && bd1 instanceof Totem)) {
                Texture texture = directory.getEntry("rocket-totem03", Texture.class);
                scene.totem.setTexture(texture);
                scene.totem.setState(Enemy.EnemyState.IN_LIGHT);
                scene.totem.resetFreeze();
            }

            if ((bd2 instanceof Light && bd1 instanceof Moth) || (bd2 instanceof Moth
                && bd1 instanceof Light)) {
                Light light = (bd1 instanceof Light) ? (Light) bd1 : (Light) bd2;
                Enemy moth = (bd1 instanceof Enemy) ? (Enemy) bd1 : (Enemy) bd2;

                float lx = light.getObstacle().getX();
                float mx = moth.getObstacle().getX();

                Texture texture = directory.getEntry("rocket-moth03", Texture.class);

                if (lx < mx && !moth.isFacingRight()) {
                    moth.changeDirection();
                } else if (lx > mx && moth.isFacingRight()) {
                    moth.changeDirection();
                }

                moth.setTexture(texture);
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

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((scene.avatar.getSensorName().equals(fd2) && scene.avatar != bd1) ||
            (scene.avatar.getSensorName().equals(fd1) && scene.avatar != bd2)) {
            scene.sensorFixtures.remove(scene.avatar == bd1 ? fix2 : fix1);
            if (scene.sensorFixtures.size == 0) {
                scene.avatar.setGrounded(false);
            }
        }

        if ((bd2 instanceof Light && bd1 instanceof Totem)) {
            Texture texture = directory.getEntry("rocket-totem01", Texture.class);
            scene.totem.setTexture(texture);
            scene.totem.setState(Enemy.EnemyState.OUT_OF_LIGHT);
        }

        if ((bd2 instanceof Light && bd1 instanceof Moth)) {
            Texture texture = directory.getEntry("rocket-moth01", Texture.class);
            scene.moth.setTexture(texture);
            scene.moth.setState(Enemy.EnemyState.OUT_OF_LIGHT);
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
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        Body bodyA = fixA.getBody();
        Body bodyB = fixB.getBody();

        // Retrieve the user data from the bodies.
        Object dataA = bodyA.getUserData();
        Object dataB = bodyB.getUserData();

        // If either user data is null, do nothing.
        if (dataA == null || dataB == null) {
            return;
        }

        // Disable collision if one body is Totem and the other is the avatar.
        if (scene.totem.getState() != Enemy.EnemyState.IN_LIGHT) {
            if ((dataA instanceof Totem && dataB == scene.avatar) ||
                (dataB instanceof Totem && dataA == scene.avatar)) {
                contact.setEnabled(false);
            }
        }
    }

}
