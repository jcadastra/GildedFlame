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
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.Torch_playground;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;
import java.util.Arrays;

public class Rope extends ObstacleGroup {
    private Vector2 pin1;
    private Vector2 pin2;
    private float d;
    private float units;
    private JsonValue data;
    private float lenOfCurve;

    private float ropeThickness;
    private float ropePieceLen;
    private ArrayList <EnhancedObstacleSprite> bottomEntities;
    private ArrayList <EnhancedObstacleSprite> topEntities;
    private ArrayList <EnhancedObstacleSprite> anchors;
    private float h;

    public float[] topVertices;
    public float[] bottomVertices;

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
    public Rope(Vector2 pin1, Vector2 pin2, float depthOfCurve, float units, JsonValue data) {
        this.h = depthOfCurve;
        this.pin1 = pin1;
        this.pin2 = pin2;
        this.d = (pin2.x - pin1.x)/2;
        this.units = units;
        this.data = data;

//        from json
        this.ropeThickness = 10;
        this.ropePieceLen = 20f;

        bottomVertices = genOneRow(-ropeThickness/2);
        topVertices = genOneRow(ropeThickness/2);
        lenOfCurve = lengthOfCurve(bottomVertices);
        anchors = genAnchors();
//        System.out.println("l anchors"+anchors.get(0).getObstacle().getPosition());
//        System.out.println("r anchors"+anchors.get(1).getObstacle().getPosition());
        bottomEntities = genFixtures(bottomVertices);
//        System.out.println("l anchors"+anchors.get(0).getObstacle().getPosition());
//        System.out.println("r anchors"+anchors.get(1).getObstacle().getPosition());
        topEntities = genFixtures(topVertices);
//        System.out.println("l anchors"+anchors.get(0).getObstacle().getPosition());
//        System.out.println("r anchors"+anchors.get(1).getObstacle().getPosition());
        fixAnchors();
//        System.out.println("l anchors"+anchors.get(0).getObstacle().getPosition());
//        System.out.println("r anchors"+anchors.get(1).getObstacle().getPosition());

//        System.out.println(h + ", "+ pin1 + ", "+ pin2 + ", "+ d);
    }


    public static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Given an x value, returns a y value at that point on the curve
     * @return
     */
    private float caternaryCurveFunc(float x_pos, float y_offset) {
        //TODO: FIX CATERNARY EQ
//        return (float) (h * ((Math.cosh((x_pos - (pin1.x + d)) / 1000) - 1) / (Math.cosh(d / 1000) - 1))) + y_offset;
        return y_offset;
    }

    private float[] genOneRow(float y_offset) {
//        System.out.println("gen one caternary row ------------------------------");
        FloatArray vertexSet = new FloatArray();
        for (float i = pin1.x; i < pin2.x; i += ropePieceLen/2) {
            vertexSet.add(i);
//            System.out.println(i);
            vertexSet.add(pin1.y + caternaryCurveFunc(i, y_offset));
//            System.out.println(i + "," + caternaryCurveFunc(i, y_offset));
        }
        return vertexSet.toArray();
    }

    private float lengthOfCurve(float[] vertexSet) {
        Polyline temp = new Polyline(vertexSet);
        return temp.getLength();
    }

    private ArrayList<EnhancedObstacleSprite> genAnchors() {
        ArrayList<EnhancedObstacleSprite> returnSet = new ArrayList<>();

        WheelObstacle wheel = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness/(2 * units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setPhysicsUnits(units);
        wheel.setName("leftRopeAnchor");
        EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
        s.setMaterial(new ObstacleMaterial("spinalFluid", data.get(1)));
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);

        wheel = new WheelObstacle(pin2.x / units, pin2.y / units, ropeThickness/(2 * units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setPhysicsUnits(units);
        wheel.setName("rightRopeAnchor");
        s = new EnhancedObstacleSprite(wheel);
        s.setMaterial(new ObstacleMaterial("spinalFluid", data.get(1)));
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);
        return returnSet;
    }

    private ArrayList<EnhancedObstacleSprite> genFixtures(float[] vertices) {
        int tempLen = vertices.length;
        ArrayList<EnhancedObstacleSprite> returnSet = new ArrayList<>();
        for (int i = 0; i < tempLen; i += 2) {
//            System.out.println(vertices[i] + "," + vertices[i+1]);
            WheelObstacle wheel = new WheelObstacle(vertices[i] / units, vertices[i+1] / units, ropeThickness/(2 * units));
            wheel.setBodyType(BodyType.DynamicBody);
            wheel.setMass(0.025f);
            wheel.setPhysicsUnits(units);
            wheel.setName("ropeSegment");
            EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
            s.setMaterial(new ObstacleMaterial("spinalFluid", data.get(1)));
            returnSet.add(s);
//            System.out.println("Physics Body Pos: " + wheel.getPosition());
            s.setDebugColor( Color.PURPLE );
            sprites.add(s);
        }
        return returnSet;
    }

    @Override
    protected boolean createJoints(World world) {
        DistanceJointDef externalJoint = new DistanceJointDef();
        externalJoint.collideConnected = false;
        externalJoint.length = (ropePieceLen * 2) / units;

//        DistanceJointDef internalJoint = new DistanceJointDef();
//        internalJoint.frequencyHz = 15f;  // Stiffer than external
//        internalJoint.dampingRatio = 5f;  // Less oscillation
//        internalJoint.length = (ropePieceLen / 2) / units; // Shorter constraint

        int totalNumOfUnits = topEntities.size();

        for (int i = 0; i < totalNumOfUnits; i++) {
            Body top = topEntities.get(i).getObstacle().getBody();
            Body bottom = bottomEntities.get(i).getObstacle().getBody();

            externalJoint.initialize(top, bottom, top.getWorldCenter(), bottom.getWorldCenter());
            joints.add(world.createJoint(externalJoint));

            if (i == totalNumOfUnits - 1) {
                Body rightAnchor = anchors.get(1).getObstacle().getBody();
                externalJoint.initialize(rightAnchor, top, rightAnchor.getPosition(), top.getPosition());
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(rightAnchor, bottom, rightAnchor.getPosition(), bottom.getPosition());
                joints.add(world.createJoint(externalJoint));
            } else {
                if (i == 0) {
                    Body leftAnchor = anchors.get(0).getObstacle().getBody();
                    externalJoint.initialize(leftAnchor, top, leftAnchor.getPosition(), top.getPosition());
                    joints.add(world.createJoint(externalJoint));
                    externalJoint.initialize(leftAnchor, bottom, leftAnchor.getPosition(), bottom.getPosition());
                    joints.add(world.createJoint(externalJoint));
                }

                Body topRight = topEntities.get(i + 1).getObstacle().getBody();
                Body bottomRight = bottomEntities.get(i + 1).getObstacle().getBody();

                externalJoint.initialize(top, topRight, top.getPosition(), topRight.getPosition());
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(bottom, bottomRight, bottom.getPosition(), bottomRight.getPosition());
                joints.add(world.createJoint(externalJoint));

                externalJoint.initialize(top, bottomRight, top.getPosition(), bottomRight.getPosition());
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(bottom, topRight, bottom.getPosition(), topRight.getPosition());
                joints.add(world.createJoint(externalJoint));
            }
        }
        return true;
    }

    private void fixAnchors() {
//        for (EnhancedObstacleSprite ebs : anchors) {
//            ebs.getObstacle().setBodyType(BodyType.StaticBody);
//        }
//        anchors.get(0).getObstacle().setBodyType(BodyType.DynamicBody);
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
