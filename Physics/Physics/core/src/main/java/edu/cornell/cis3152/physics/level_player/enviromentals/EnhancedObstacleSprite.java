package edu.cornell.cis3152.physics.level_player.enviromentals;

import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.SpriteMesh;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.awt.Shape;

/**
 * Enhanced obstacle sprite is for any Obstacle Sprite that needs
 * additional functionality, in the beginning this is just to attach
 * a material type to an object (which currently just holds fire) but this
 * can be extended out to chnage a floor to ice or other values as needed
 */

public class EnhancedObstacleSprite extends ObstacleSprite {

    private ObstacleMaterial material;
    private boolean climbable;

    public EnhancedObstacleSprite() {
        super();
    }
    public EnhancedObstacleSprite(Obstacle obstacle) {
        super(obstacle);
    }
    public EnhancedObstacleSprite(Obstacle obstacle, boolean mesh) {
        super(obstacle, mesh);
    }

    public void setMaterial(ObstacleMaterial material) {
        this.material = material;
    }
    public ObstacleMaterial getMaterial() {
        return material;
    }
    public void setClimbable(boolean value) {climbable = value;}
    public boolean getClimbable() {return climbable;}

}
