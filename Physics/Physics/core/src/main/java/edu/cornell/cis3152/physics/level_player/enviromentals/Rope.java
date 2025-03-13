package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import java.util.Vector;

public class Rope extends ObstacleGroup {
    public Rope(Vector2 pin1, Vector2 pin2, ) {

    }

    @Override
    protected boolean createJoints(World world) {
        return false;
    }
}
