package edu.cornell.cis3152.physics.platform;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.List;
public class AIController {

    // Constants
    /**
     * How close the light must be for the moth to chase it
     */
    private static final int CHASE_DIST = 3;
    /**
     * How close the player must be for the moth to attack it
     */
    private static final int ATTACK_DIST = 1;

    /**
     * The number of ticks since we started this controller
     */
    private long ticks;


    private static final float LIGHT_RADIUS = 3;

    private List<Enemy> enemies;
    // CHANGE LATER
    public static Vector2 lightPosition;
    public static Vector2 playerPosition;

    public Traci traci;
    public AIController(Torch_playground playground){
        this.enemies = playground.getEnemies();
        this.traci = playground.getAvatar();
    }

    public void update(){
        playerPosition = traci.getLocation();
        for (Enemy enemy : enemies){
            if (lightDistance(enemy) < LIGHT_RADIUS){
                enemy.setState(Enemy.EnemyState.IN_LIGHT);
            } else if (lightDistance(enemy) < CHASE_DIST && enemy instanceof Moth){
                enemy.setState(Enemy.EnemyState.ATTRACTED);
            }
            enemy.update();
        }
    }

    public int lightDistance(Enemy enemy){
//        return (int) Math.sqrt(Math.pow((enemy.getX() - lightPosition.x),2) + Math.pow((enemy.getY() - lightPosition.y), 2));
        return (int) Math.sqrt(Math.pow((enemy.getX() - playerPosition.x),2) + Math.pow((enemy.getY() - playerPosition.y), 2));
    }
}
