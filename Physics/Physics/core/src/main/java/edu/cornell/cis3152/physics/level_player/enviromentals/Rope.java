package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
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
     * provide PIN1 and PIN2 cords in terms of phys units
     * pin2.x - pin1.x >= 2
     *
     * @param pin1
     * @param pin2
     * @param depthOfCurve
     * @param units
     */
    public Rope(Vector2 pin1, Vector2 pin2, float depthOfCurve, float units, JsonValue data) {
        this.h = depthOfCurve * units;
        this.pin1 = pin1.scl(units);
        this.pin2 = pin2.scl(units);
        this.d = (this.pin2.x - this.pin1.x)/2;
        this.units = units;
        this.data = data;

        ropeThickness = data.getFloat("thickness");
        ropePieceLen = data.getFloat("piecelen");
        bottomVertices = genOneRowOnX(-ropeThickness/4);
        topVertices = genOneRowOnX(ropeThickness/4);
        lenOfCurve = lengthOfCurve(bottomVertices);
        anchors = genAnchors();
        bottomEntities = genFixtures(bottomVertices);
        topEntities = genFixtures(topVertices);
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
     * @param lenOfCurve total len of curve in  phys cords
     * @param units physics units
     * @param data json values
     */
    public Rope(Vector2 pin1, boolean pinBottom, float lenOfCurve, float units, JsonValue data) {
        this.pin1 = pin1.scl(units);
        this.units = units;
        this.data = data;

        this.pin2 = new Vector2(this.pin1.x, this.pin1.y - lenOfCurve * units);

        ropeThickness = data.getFloat("thickness");
        ropePieceLen = data.getFloat("piecelen");

        bottomVertices = genOneRowOnY(-ropeThickness / 4);
        topVertices = genOneRowOnY(ropeThickness / 4);
        this.lenOfCurve = lenOfCurve * units;

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
//        float top = (float) ((h * ((Math.cosh((pin1.x - (pin1.x + d)) / 1000) - 1) / (Math.cosh(d / 1000) - 1))) + y_offset);
//        return (float) (h * ((Math.cosh((x_pos - (pin1.x + d)) / 1000) - 1) / (Math.cosh(d / 1000) - 1))) + y_offset - top;
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
        float start = pin1.x + ropeThickness;
        for (float i = start; i <= pin2.x - 0; i += ropePieceLen) {
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

        WheelObstacle wheel = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness/(2f*units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setDensity(1f);
        wheel.setFixedRotation(true);
        wheel.setPhysicsUnits(units);
        wheel.setSensor(true);
        wheel.setName("ropeAnchorLeft");
        EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
        s.setMaterial(new ObstacleMaterial("rope", data.get(1)));
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);

        wheel = new WheelObstacle(pin2.x / units, pin2.y / units, ropeThickness/(2f*units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setDensity(1f);
        wheel.setFixedRotation(true);
        wheel.setPhysicsUnits(units);
        wheel.setSensor(true);
        wheel.setName("ropeAnchorRight");
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
            BoxObstacle ropeSeg = new BoxObstacle(vertices[i] / units, vertices[i+1] / units, (ropePieceLen * 1.3f) / (units), ropeThickness / (2*units));
            ropeSeg.setBodyType(BodyType.DynamicBody);
            ropeSeg.setDensity(.01f);
            ropeSeg.setAngularDamping(10000f);
//            ropeSeg.setMass(0.00001f);
            ropeSeg.setSensor(true);
            ropeSeg.setPhysicsUnits(units);
            ropeSeg.setName("ropeSegment");
            EnhancedObstacleSprite s = new EnhancedObstacleSprite(ropeSeg);
            s.setMaterial(new ObstacleMaterial("rope", data.get(1)));
            returnSet.add(s);
            s.setDebugColor( Color.PURPLE );
            s.setClimbable(true);
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
//        externalJoint.frequencyHz = 10f;
//        externalJoint.dampingRatio = .1f;
        externalJoint.collideConnected = false;
        externalJoint.length = 0.0f;

//        DistanceJointDef internalJoint = new DistanceJointDef();
//        internalJoint.collideConnected = false;
//        internalJoint.length = ropeThickness/2;

//        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
//        revoluteJoint.collideConnected = false;



        float halfHeight = ropeThickness / (2*units);
//        float halfWidth = ropePieceLen / (2*units);
//        float halfHeight = 0;
        float halfWidth = .15f;



//        DistanceJointDef internalJoint = new DistanceJointDef();
//        internalJoint.frequencyHz = 15f;  // Stiffer than external
//        internalJoint.dampingRatio = 5f;  // Less oscillation
//        internalJoint.length = (ropePieceLen / 2) / units; // Shorter constraint

        int totalNumOfUnits = topEntities.size();

        for (int i = 0; i < totalNumOfUnits; i++) {
            Body top = topEntities.get(i).getObstacle().getBody();
            Body bottom = bottomEntities.get(i).getObstacle().getBody();

            externalJoint.initialize(top, bottom, top.getPosition().cpy().add(0, -.1f), bottom.getPosition().cpy().add(0,.1f));
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

                externalJoint.initialize(top, topRight, top.getPosition().cpy().add(halfWidth,0), topRight.getPosition().cpy().add(-halfWidth,0));
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(bottom, bottomRight, bottom.getPosition().cpy().add(halfWidth,0), bottomRight.getPosition().cpy().add(-halfWidth,0));
                joints.add(world.createJoint(externalJoint));

//                revoluteJoint.initialize(top, topRight, top.getPosition().cpy().add(halfWidth,0));
//                joints.add(world.createJoint(revoluteJoint));
//                revoluteJoint.initialize(bottom, bottomRight, bottom.getPosition().cpy().add(halfWidth,0));
//                joints.add(world.createJoint(revoluteJoint));

                externalJoint.initialize(top, bottomRight, top.getPosition().cpy().add(halfWidth,0), bottomRight.getPosition().cpy().add(-halfWidth,0));
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(bottom, topRight, bottom.getPosition().cpy().add(halfWidth,0), topRight.getPosition().cpy().add(-halfWidth,0));
                joints.add(world.createJoint(externalJoint));
            }
        }
        return true;
    }

    public void setTextures(Texture endRopeTexture, Texture midRopeTexture) {
        for (EnhancedObstacleSprite eos : anchors) {
            eos.setTexture(endRopeTexture);
        }
        for (EnhancedObstacleSprite eos : bottomEntities) {
            eos.setTexture(midRopeTexture);
        }
        for (EnhancedObstacleSprite eos : topEntities) {
            eos.setTexture(midRopeTexture);
        }
    }

    public ArrayList<EnhancedObstacleSprite> getTopEntities() {
//        for (EnhancedObstacleSprite eos : bottomEntities) {
//            System.out.println(eos.getObstacle().isFixedRotation());
//            eos.getObstacle().getBody().applyAngularImpulse(10f, true);
//        }
        return bottomEntities;
    }

    public Joint attachAnchorToObj (ObstacleSprite obj, int anchorNum, World world) {
        EnhancedObstacleSprite anchor =  anchors.get(anchorNum);
        anchor.getObstacle().setBodyType(BodyType.DynamicBody);

        WeldJointDef weldJointDef = new WeldJointDef();
        weldJointDef.initialize(anchor.getObstacle().getBody(), obj.getObstacle().getBody(), anchor.getObstacle().getPosition());
        Joint joint = world.createJoint(weldJointDef);
        joints.add(joint);
        return joint;
    }
}
