package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;

/**
 * A  group that resents a rope, it is made out of a latice interwoven structure found at the end
 * of the slides for box2d, atm starts horrixontal but will implement beizer curves, of material rope
 *
 * rope is not interactable with surroundings other than to check for collision
 */
public class Rope extends ObstacleGroup {
    private Vector2 pin1;
    private Vector2 pin2;
    private float d;
    private float units;
    private JsonValue data;
    private float lenOfCurve;

    private float ropeThickness = 7;
    private float ropePieceLen = 14;
//        from json
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


        bottomVertices = genOneRowOnX(-ropeThickness/2);
        topVertices = genOneRowOnX(ropeThickness/2);
        lenOfCurve = lengthOfCurve(bottomVertices);
        anchors = genAnchors();
        bottomEntities = genFixtures(bottomVertices);
        topEntities = genFixtures(topVertices);
        fixAnchors();
    }

    /**
     * Helper method to create a whole rope from one point and length
     * Will hang downard from x y of length y
     *
     * provide PIN1 and PIN2 cords in terms of global cords, ie not box2d, ie * units
     * pin2.x - pin1.x >= 2
     *
     * @param pin1 point at which the rope hangs
     * @param pinBottom if the bottom is pinned or not, ie static or swinging
     * @param lenOfCurve total len of curve in pixels not phys cords
     * @param units physics units
     * @param data json values
     */
    public Rope(Vector2 pin1, boolean pinBottom, float lenOfCurve, float units, JsonValue data) {
        this.pin1 = pin1;
        this.units = units;
        this.data = data;

        this.pin2 = new Vector2(pin1.x, pin1.y - lenOfCurve);

        bottomVertices = genOneRowOnY(-ropeThickness / 2);
        topVertices = genOneRowOnY(ropeThickness / 2);
        this.lenOfCurve = lenOfCurve;

        anchors = genAnchors();
        bottomEntities = genFixtures(bottomVertices);
        topEntities = genFixtures(topVertices);

        if (!pinBottom) {
            anchors.get(1).getObstacle().setBodyType(BodyType.DynamicBody);
        }
    }

    public static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Given an x value, returns a y value at that point on the curve, non functional
     * @return
     */
    private float caternaryCurveFunc(float x_pos, float y_offset) {
        //TODO: FIX CATERNARY EQ
//        return (float) (h * ((Math.cosh((x_pos - (pin1.x + d)) / 1000) - 1) / (Math.cosh(d / 1000) - 1))) + y_offset;
        return y_offset;
    }

    /**
     * Generates a given row of points (with a given offset for up or down) and returns array of
     * points to generate the info, iterates from te x value of a cord
     * @param offset the offset from ideal line that the poitns are generated on
     * @return a float array of x y cords describing position
     */
    private float[] genOneRowOnX(float offset) {
        FloatArray vertexSet = new FloatArray();
        for (float i = pin1.x; i < pin2.x; i += ropePieceLen/2) {
            vertexSet.add(i);
            vertexSet.add(pin1.y + caternaryCurveFunc(i, offset));
        }
        return vertexSet.toArray();
    }
    /**
     *
     * Generates a given row of points (with a given offset for up or down) and returns array of
     * points to generate the info, iterates from te x value of a cord
     * @param offset the offset from ideal line that the poitns are generated on
     * @return a float array of x y cords describing position
     */
    private float[] genOneRowOnY(float offset) {
        FloatArray vertexSet = new FloatArray();
        for (float y = pin1.y; y > pin2.y; y -= ropePieceLen / 2) {
            float x = pin1.x + caternaryCurveFunc(y, offset);
            vertexSet.add(x);
            vertexSet.add(y);
        }
        return vertexSet.toArray();
    }

    /**
     * Given the vertices of a generated set, what is the length of the curve
     * @param vertexSet
     * @return
     */
    private float lengthOfCurve(float[] vertexSet) {
        Polyline temp = new Polyline(vertexSet);
        return temp.getLength();
    }

    /**
     * Generates the two anchors of rope pin and stores them in an ArrayList
     * @return
     */
    private ArrayList<EnhancedObstacleSprite> genAnchors() {
        ArrayList<EnhancedObstacleSprite> returnSet = new ArrayList<>();

        WheelObstacle wheel = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness/(2 * units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setPhysicsUnits(units);
        wheel.setSensor(true);
        wheel.setName("leftRopeAnchor");
        EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
        s.setMaterial(new ObstacleMaterial("rope", data.get(1)));
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);

        wheel = new WheelObstacle(pin2.x / units, pin2.y / units, ropeThickness/(2 * units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setPhysicsUnits(units);
        wheel.setSensor(true);
        wheel.setName("rightRopeAnchor");
        s = new EnhancedObstacleSprite(wheel);
        s.setMaterial(new ObstacleMaterial("rope", data.get(1)));
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);
        return returnSet;
    }

    /**
     * Generates the fixtures alone on set of vertices for a rope lattice
     * @param vertices x y format of float for vertices
     * @return
     */
    private ArrayList<EnhancedObstacleSprite> genFixtures(float[] vertices) {
        int tempLen = vertices.length;
        ArrayList<EnhancedObstacleSprite> returnSet = new ArrayList<>();
        for (int i = 0; i < tempLen; i += 2) {
            WheelObstacle wheel = new WheelObstacle(vertices[i] / units, vertices[i+1] / units, ropeThickness/(2 * units));
            wheel.setBodyType(BodyType.DynamicBody);
            wheel.setMass(0.3f);
            wheel.setSensor(true);
            wheel.setPhysicsUnits(units);
            wheel.setName("ropeSegment");
            EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
            s.setMaterial(new ObstacleMaterial("rope", data.get(1)));
            returnSet.add(s);
            s.setDebugColor( Color.PURPLE );
            sprites.add(s);
        }
        return returnSet;
    }

    /**
     * Actually generates the joints that merges the lattice together
     * @param world the box2d world referencing the obstacles
     *
     * @return
     */
    @Override
    protected boolean createJoints(World world) {
        DistanceJointDef externalJoint = new DistanceJointDef();
        externalJoint.frequencyHz = 32f;
        externalJoint.dampingRatio = .5f;
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
