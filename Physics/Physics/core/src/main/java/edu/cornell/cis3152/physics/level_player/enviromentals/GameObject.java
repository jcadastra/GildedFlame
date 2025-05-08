package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.ParticleEngine;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.Texture2D;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.PolygonObstacle;
import edu.cornell.gdiac.physics2.WheelObstacle;
import javax.swing.Box;

public class GameObject extends EnhancedObstacleSprite {
    public int tileID = -1;
    public int getTileID() {
        return tileID;
    }
    private float height;
    private float width;
    private float units;
    private Path2 sensorOutline;

    /**
     * Creates a GameObject with a hardcoded shape, generic object that is enhanced
     *
     * @param x      The world x-position.
     * @param y      The world y-position.
     * @param units  The physics unit scale.
     */
    public GameObject(float x, float y, float width, float height, float units, Boolean centerInBottomLeft) {
        super(new BoxObstacle(
            centerInBottomLeft ? x + width / 2f : x,
            centerInBottomLeft ? y + height / 2f : y,
            width,
            height
        ));
        this.width = width;
        this.height = height;
        this.units = units;

        getObstacle().setDensity(0.5f);
        getObstacle().setFriction(0.5f);
        getObstacle().setRestitution(0.1f);
        getObstacle().setPhysicsUnits(units);
        getObstacle().setFixedRotation(true);
        obstacle.setUserData(this);
        mesh.set(
            -(width  * units)/2f,
            -(height * units)/2f,
            width  * units,
            height * units
        );
    }

    public GameObject(float[] points, float x, float y, float width, float height,  float units) {
        super();
        this.width = width;
        this.height = height;
        this.units = units;

        // Construct a Poly2 object, breaking it into triangles
        Poly2 poly = new Poly2();
        PolyTriangulator triangulator = new PolyTriangulator();
        triangulator.set(points);
        triangulator.calculate();
        triangulator.getPolygon(poly);

        obstacle = new PolygonObstacle(points,x,y);
        obstacle.setDensity(0.5f);
        obstacle.setFriction(0.5f);
        obstacle.setRestitution(0.1f);
        obstacle.setPhysicsUnits(units);
        obstacle.setFixedRotation(true);
        obstacle.setUserData(this);

        mesh.set(-(width * units)/2, -(height * units)/2, width * units, height * units);
    }

    public GameObject(float x, float y, float width, float height, float units, Boolean centerInBottomLeft, int tileId) {
        super();
        this.width = width;
        this.height = height;
        this.tileID = tileId;
        this.units = units;
        String name;
        if (tileId - 5 <= 0 || tileId == 7 || tileId == 12 || tileId == 13 ||
            tileId == 14 || tileId == 15 || tileId == 29 || tileId == 31) {
            name = "platform";
        } else {
            name = "wall";
        }

        if (centerInBottomLeft) {
            x += width / 2;
            y += height / 2;
        }

        float physHeight = height * (name.equals("platform") ? 0.85f : 1f);
//        float physHeight = height * (name.equals("platform") ? 1f : 1f);
        float physCentreY = y + (physHeight - height) / 2f;

        int direction = -1;
        float widthFactor = 1f;
        //shrink to 80%, centered
        if (tileId == 0 || tileId == 12) {
//            widthFactor = .8f;
        }
        // shrink to 90%, right justified
        else if (tileId == 1 || tileId == 4 || tileId == 13) {
//            widthFactor = .9f;
        }
        // shrink to 90%, left justified
        else if (tileId == 3 || tileId == 5 || tileId == 15) {
//            widthFactor = .9f;
//            direction = 1;
        }
        float physCentreX = x - (direction* (width - width * widthFactor)) / 2f;

        BoxObstacle temp = new BoxObstacle(physCentreX,physCentreY,width * widthFactor,physHeight);

        obstacle = new ObstacleSprite(temp).getObstacle();
        obstacle.setUserData(this);
        obstacle.setDensity(0.5f);
        obstacle.setFriction(0.5f);
        obstacle.setRestitution(0f);
        obstacle.setPhysicsUnits(units);
        obstacle.setFixedRotation(true);
        obstacle.setName(name);
        //if platform ie walk on shrink height by .15 to walk on better
        mesh.set(-(width * units)/2, -(height * (name.equals("platform") ? .85f : 1) * units)/2, width * widthFactor * units, (height ) * units);
//        mesh.set(-(width * units)/2, -(height * (name.equals("platform") ? 1f : 1f) * units)/2, width * widthFactor * units, (height ) * units);
    }

    public void generateInternalCrushSensor() {
        Body body = obstacle.getBody();
        float halfW = width  / 2.6f;
        float halfH = height / 4f;

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density  = 0;
        sensorDef.isSensor = true;
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(halfW, halfH, new Vector2(0, 0), 0f);
        sensorDef.shape = sensorShape;

        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData("crushSensor");
        sensorShape.dispose();
        float u = obstacle.getPhysicsUnits();
        sensorOutline = new Path2();
        PathFactory factory = new PathFactory();

        factory.makeRect(-halfW * u, -halfH * u, 2*halfW  * u, 2*halfH * u, sensorOutline);
    }

    @Override
    public void drawDebug(SpriteBatch batch) {
        super.drawDebug(batch);

        if (sensorOutline != null) {
            batch.setTexture(Texture2D.getBlank());
            batch.setColor(Color.RED);

            Vector2 p = obstacle.getPosition();
            float a = obstacle.getAngle();
            float u = obstacle.getPhysicsUnits();

            // transform is an inherited cache variable
            transform.idt();
            transform.preRotate((float) (a * 180.0f / Math.PI));
            transform.preTranslate(p.x * u, p.y * u);

            //
            batch.outline(sensorOutline, transform);
        }
    }
}
