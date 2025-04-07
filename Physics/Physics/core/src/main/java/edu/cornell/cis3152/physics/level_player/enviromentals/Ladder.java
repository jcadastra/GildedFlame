package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.physics2.BoxObstacle;

public class Ladder extends EnhancedObstacleSprite {

    public Ladder ( float x, float y, float height, float units) {
        float baseWidth = .75f;
        // this funky orientation is for climbing and to make sure hte player is oriented properly
        obstacle = new BoxObstacle(x+baseWidth/2,y+(height/2), height, baseWidth);
        obstacle.setAngle((float) (-Math.PI/2));
        obstacle.setBodyType(BodyType.StaticBody);
        obstacle.setSensor(true);
        obstacle.setName("ladder");
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );

        setClimbable(true);

    }
}
