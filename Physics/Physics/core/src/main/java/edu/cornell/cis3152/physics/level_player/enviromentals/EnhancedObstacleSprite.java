package edu.cornell.cis3152.physics.level_player.enviromentals;

import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class EnhancedObstacleSprite extends ObstacleSprite {

    private ObstacleMaterial material;

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
}
