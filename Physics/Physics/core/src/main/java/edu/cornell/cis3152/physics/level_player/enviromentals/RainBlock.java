package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class RainBlock extends ObstacleSprite {
    public RainBlock ( float x, float y, float width, float height, float units, float fireRadius) {
        obstacle = new BoxObstacle(x+width/2 + fireRadius,y+(height/2), width - fireRadius * 2, height);
        obstacle.setBodyType(BodyType.StaticBody);
        obstacle.setSensor(true);
        obstacle.setName("rainBlock");
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );
    }
}
