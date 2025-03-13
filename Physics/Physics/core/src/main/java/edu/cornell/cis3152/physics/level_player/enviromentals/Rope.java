package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.FloatArray;
import edu.cornell.cis3152.physics.level_player.Torch_playground;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;

public class Rope extends ObstacleGroup {
    private Vector2 pin1;
    private Vector2 pin2;
    private float d;
    private float units;
    private float lenOfCurve;

    private float ropeThickness;
    private float ropePieceLen;
    private ArrayList <EnhancedObstacleSprite> bottomEntities;
    private ArrayList <EnhancedObstacleSprite> topEntities;
    private ArrayList <EnhancedObstacleSprite> anchors;
    private float h;

    /**
     * Helper method to create a whole rope from two points
     * Ensure that pin1 comes before pin2 in that pin1.x < pin2.x and same with .y
     *
     * provide PIN1 and PIN2 cords in terms of global cords, ie not box2d, ie * units
     * pin2.x - pin1.x >= 2
     *
     * @param pin1
     * @param pin2
     * @param depthOfCurve
     * @param units
     */
    public Rope(Vector2 pin1, Vector2 pin2, float depthOfCurve, float units) {
        this.h = depthOfCurve;
        this.pin1 = pin1;
        this.pin2 = pin2;
        this.d = pin2.x - pin1.x;
        this.units = units;

//        from json
        this.ropeThickness = 3;
        this.ropePieceLen = 3;


        float[] bottomVertices = genOneRow(-ropeThickness/2);
        float[] topVertices = genOneRow(ropeThickness/2);
        lenOfCurve = lengthOfCurve(bottomVertices);

        anchors = genAnchors();
        bottomEntities = genFixtures(bottomVertices);
        topEntities = genFixtures(topVertices);

    }


    public static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Given an x value, returns a y value at that point on the curve
     * @return
     */
    private float caternaryCurveFunc(float x_pos, float y_offset) {
        return (float) (h * (Math.cosh(x_pos/1000) - 1 / Math.cosh(d/1000) - 1)) + y_offset;
    }

    private float[] genOneRow(float y_offset) {
        FloatArray vertexSet = new FloatArray();
        for (float i = pin1.x + 1f; i < pin2.x - 1f; i += .5f) {
            vertexSet.add(i);
            vertexSet.add( + caternaryCurveFunc(i, y_offset));
            System.out.println(new Vector2(i,caternaryCurveFunc(i, y_offset)));
        }
        return vertexSet.toArray();
    }

    private float lengthOfCurve(float[] vertexSet) {
        Polyline temp = new Polyline(vertexSet);
        return temp.getLength();
    }

    private ArrayList<EnhancedObstacleSprite> genAnchors() {
        ArrayList<EnhancedObstacleSprite> returnSet = new ArrayList<>();

        WheelObstacle wheel = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness/2);
        wheel.setBodyType(BodyType.DynamicBody);
        wheel.setMass(0.0001f);
        wheel.setPhysicsUnits(units);
        wheel.setName("leftRopeAnchor");
        EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
        returnSet.add(s);
        sprites.add(s);

        wheel = new WheelObstacle(pin2.x / units, pin2.y / units, ropeThickness/2);
        wheel.setBodyType(BodyType.DynamicBody);
        wheel.setMass(0.0001f);
        wheel.setPhysicsUnits(units);
        wheel.setName("rightRopeAnchor");
        s = new EnhancedObstacleSprite(wheel);
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);

        return returnSet;
    }

    private ArrayList<EnhancedObstacleSprite> genFixtures(float[] vertices) {
        int tempLen = vertices.length;
        ArrayList<EnhancedObstacleSprite> returnSet = new ArrayList<>();
        for (int i = 0; i < tempLen; i += 2) {
            WheelObstacle wheel = new WheelObstacle(vertices[i] / units, vertices[i + 1] / units, ropeThickness/2);
//            System.out.println(new Vector2(vertices[i] / units, vertices[i + 1] / units));
            wheel.setBodyType(BodyType.DynamicBody);
            wheel.setMass(0.0001f);
            wheel.setName("ropeSegment");

            EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
            returnSet.add(s);
            s.setDebugColor( Color.PURPLE );
            sprites.add(s);
        }
        return returnSet;
    }

    @Override
    protected boolean createJoints(World world) {

        DistanceJointDef keyJoint = new DistanceJointDef();
        keyJoint.frequencyHz = .5f;
        keyJoint.dampingRatio = .5f;
        int totalNumOfUnits = topEntities.size();

        for (int i = 0; i < totalNumOfUnits; i++) {
            if (i == totalNumOfUnits - 1) {
                Body rightAnchor = anchors.get(1).getObstacle().getBody();
                Body top = topEntities.get(i).getObstacle().getBody();
                Body bottom = bottomEntities.get(i).getObstacle().getBody();

                keyJoint.initialize(rightAnchor, top, rightAnchor.getWorldCenter(), top.getWorldCenter());
                joints.add(world.createJoint(keyJoint));
                keyJoint.initialize(rightAnchor, bottom, rightAnchor.getWorldCenter(), bottom.getWorldCenter());
                joints.add(world.createJoint(keyJoint));
            } else {
                if (i == 0) {
                    Body leftAnchor = anchors.get(0).getObstacle().getBody();
                    Body top = topEntities.get(i).getObstacle().getBody();
                    Body bottom = bottomEntities.get(i).getObstacle().getBody();

                    keyJoint.initialize(leftAnchor, top, leftAnchor.getWorldCenter(), top.getWorldCenter());
                    joints.add(world.createJoint(keyJoint));
                    keyJoint.initialize(leftAnchor, bottom, leftAnchor.getWorldCenter(), bottom.getWorldCenter());
                    joints.add(world.createJoint(keyJoint));
                }
                Body central = topEntities.get(i).getObstacle().getBody();
                Body bodyRight = topEntities.get(i+1).getObstacle().getBody();
                Body bodyBelow = bottomEntities.get(i).getObstacle().getBody();
                Body bodyBelowRight = bottomEntities.get(i+1).getObstacle().getBody();
                keyJoint.initialize(central, bodyRight, central.getWorldCenter(), bodyRight.getWorldCenter());
                joints.add(world.createJoint(keyJoint));
                keyJoint.initialize(central, bodyRight, central.getWorldCenter(), bodyBelow.getWorldCenter());
                joints.add(world.createJoint(keyJoint));
                keyJoint.initialize(central, bodyRight, central.getWorldCenter(), bodyBelowRight.getWorldCenter());
                joints.add(world.createJoint(keyJoint));

                central = bottomEntities.get(i).getObstacle().getBody();
                Body topRight = topEntities.get(i+1).getObstacle().getBody();
                bodyRight = bottomEntities.get(i+1).getObstacle().getBody();
                keyJoint.initialize(central, topRight, central.getWorldCenter(), topRight.getWorldCenter());
                joints.add(world.createJoint(keyJoint));
                keyJoint.initialize(central, bodyRight, central.getWorldCenter(), bodyRight.getWorldCenter());
                joints.add(world.createJoint(keyJoint));
            }
        }

        return true;
    }

}

/**
 * make the hypotehtical curve
 * find length to see how long it would be
 *
 * create rope points along the curve to create rope x 2
 * join each set of rope points with a distance joint constraint
 * attach the ends to a revolute at the end
 *
 * rope needs to be made of enhanced
 */
