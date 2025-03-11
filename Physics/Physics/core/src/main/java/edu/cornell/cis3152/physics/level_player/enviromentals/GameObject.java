package edu.cornell.cis3152.physics.level_player.enviromentals;

import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class GameObject extends ObstacleSprite {
    private boolean flammable;
    public boolean getFlammable() {
        return flammable;
    }
    public GameObject (float x, float y, float len, float height, float units) {
        super();

//        float s =2f;
//        float size = s*units;

        obstacle = new BoxObstacle(x, y, len, height);
        flammable = true;
        obstacle.setPosition(x,y);
        obstacle.setDensity( .5f );
        obstacle.setFriction( .5f);
        obstacle.setRestitution( 1.2f );
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );

//        debug = ParserUtils.parseColor( settings.get("debug"),  Color.WHITE);

        // Create a rectangular mesh the same size as the door, adjusted by
        // the physics units. For all meshes attached to a physics body, we
        // want (0,0) to be in the center of the mesh. So the method call below
        // is (x,y,w,h) where x, y is the bottom left.
        mesh.set(-(len/2.0f) * units,-(height/2.0f) * units,len*units,height*units);
    }
}
