package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;

public class Smoke extends ObstacleSprite {
    private Fire source;
    private int lifeLimit;
    public Smoke(float x,float y, float units, Vector2 velocity) {
        super();

        float s = 0.3f;
        lifeLimit = 120;

        obstacle = new WheelObstacle(x, y, s);

        obstacle.setGravityScale(0);
        obstacle.setPhysicsUnits( units );
        obstacle.setBodyType( BodyType.DynamicBody);
        obstacle.setSensor(true);
        obstacle.setUserData( this );
        obstacle.setName("smoke");
        obstacle.setDensity(0.001f);
        obstacle.setVX(velocity.x);
        obstacle.setVY(velocity.y);
    }

    public void setSource(Fire f) {
        this.source = f;
    }

    public void updateLifeSpan() {
        lifeLimit--;
        if (lifeLimit <= 0) {
            dispose();
        }
    }

    public void dispose() {
        obstacle.markRemoved(true);
    }
}
