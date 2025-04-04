package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
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
    private float internalAngle;
    private float d;
    private float units;
    private JsonValue data;
    private float lenOfCurve;

    private float ropeThickness;
    private float ropePieceLen;
    private Vector2 step;
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
        this.internalAngle = (pin2.cpy().sub(pin1)).angleRad();
        this.d = (this.pin2.x - this.pin1.x)/2;
        this.units = units;
        this.data = data;

        ropeThickness = data.getFloat("thickness");
        ropePieceLen = data.getFloat("piecelen");
        this.step = ((pin2.cpy().sub(pin1)).nor()).scl(ropePieceLen);
        bottomVertices = genOneRow(-ropeThickness/4);
        topVertices = genOneRow(ropeThickness/4);
        lenOfCurve = lengthOfCurve(bottomVertices);
        anchors = genAnchors();
        bottomEntities = genFixtures(bottomVertices);
        topEntities = genFixtures(topVertices);
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
        if (d == 0) {return 0;}
//        float top = (float) ((h * ((Math.cosh((pin1.x - (pin1.x + d)) / 1000) - 1) / (Math.cosh(d / 1000) - 1))) + y_offset);
        float aa = (float) (h * ((Math.cosh((x_pos + d) / 1000) - 1) / (Math.cosh(d / 1000) - 1))) + y_offset;
//        System.out.println("x,yoffset,y (" + x_pos+ ", "+y_offset+", " +aa + " )");
//        return aa;
        return y_offset;
    }

    /**
     * Generates a given row of points (with a given offset for up or down) and returns array of
     * points to generate the info, iterates from te x value of a cord
     * @param offset the offset from ideal line that the poitns are generated on
     * @return a float array of x y cords describing position
     */
    private float[] genOneRow(float offset) {
        float y_internalOffset = (float) (-offset * Math.cos(internalAngle));
        float x_internalOffset = (float) (offset * Math.sin(internalAngle));
        FloatArray vertexSet = new FloatArray();
        Vector2 i = new Vector2(pin1).add((step.cpy()).scl(.5f));
        vertexSet.add(i.x + x_internalOffset);
        vertexSet.add(i.y + caternaryCurveFunc(i.x, y_internalOffset));
        do {
            i.add(step);
            vertexSet.add(i.x + x_internalOffset);
            vertexSet.add(i.y + caternaryCurveFunc(i.x, y_internalOffset));
        } while (pin2.dst(i) > ropePieceLen);
        System.out.println("x internal: " +x_internalOffset);
//        System.out.println(internalAngle);
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

        WheelObstacle wheel = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness/(units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setDensity(1f);
        wheel.setFixedRotation(false);
        wheel.setPhysicsUnits(units);
        wheel.setSensor(true);
        wheel.setName("ropeAnchorLeft");
        EnhancedObstacleSprite s = new EnhancedObstacleSprite(wheel);
        s.setMaterial(new ObstacleMaterial("rope", data.get(1)));
        returnSet.add(s);
        s.setDebugColor( Color.GREEN );
        sprites.add(s);

        wheel = new WheelObstacle(pin2.x / units, pin2.y / units, ropeThickness/(units));
        wheel.setBodyType(BodyType.StaticBody);
        wheel.setMass(0.1f);
        wheel.setDensity(1f);
        wheel.setFixedRotation(false);
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
            BoxObstacle ropeSeg = new BoxObstacle(vertices[i] / units, vertices[i+1] / units, (ropePieceLen * 1.3f) / (units), (ropeThickness) / (2*units));
            ropeSeg.setPhysicsUnits(units);
            ropeSeg.setBodyType(BodyType.DynamicBody);
//            System.out.println(internalAngle);
            ropeSeg.setAngle(internalAngle);
            ropeSeg.setDensity(.01f);
            ropeSeg.setAngularDamping(10000f);
//            ropeSeg.setMass(0.00001f);
            ropeSeg.setSensor(true);
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
        WeldJointDef externalJoint = new WeldJointDef();
        externalJoint.frequencyHz = 5f;
        externalJoint.dampingRatio = 0.5f;
//        externalJoint.frequencyHz = 10f;
//        externalJoint.dampingRatio = .1f;
        externalJoint.collideConnected = false;
//        externalJoint.length = 0.0f;

//        DistanceJointDef internalJoint = new DistanceJointDef();
//        internalJoint.collideConnected = false;
//        internalJoint.length = ropeThickness/2;

//        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
//        revoluteJoint.collideConnected = false;



//        float halfHeight = ropeThickness / (2*units);
//        float halfWidth = ropePieceLen / (2*units);
//        float halfHeight = 0;
        float halfWidth = (float) ((ropePieceLen/(units)) * Math.cos(internalAngle));
        float halfHeight = (float) ((ropeThickness/(units)) * Math.sin(internalAngle));
//        float halfWidth = (float) 0;
        RevoluteJointDef anchorJoint = new RevoluteJointDef();
        anchorJoint.collideConnected = false;




//        DistanceJointDef internalJoint = new DistanceJointDef();
//        internalJoint.frequencyHz = 15f;  // Stiffer than external
//        internalJoint.dampingRatio = 5f;  // Less oscillation
//        internalJoint.length = (ropePieceLen / 2) / units; // Shorter constraint

        int totalNumOfUnits = topEntities.size();

        for (int i = 0; i < totalNumOfUnits; i++) {
            Body top = topEntities.get(i).getObstacle().getBody();
            Body bottom = bottomEntities.get(i).getObstacle().getBody();

            externalJoint.initialize(top, bottom, (top.getPosition().cpy().add(bottom.getPosition())).scl(.5f));
            joints.add(world.createJoint(externalJoint));

            if (i == totalNumOfUnits - 1) {
                Body rightAnchor = anchors.get(1).getObstacle().getBody();
                anchorJoint.initialize(rightAnchor, top, rightAnchor.getPosition());
                joints.add(world.createJoint(anchorJoint));
                anchorJoint.initialize(rightAnchor, bottom, rightAnchor.getPosition());
                joints.add(world.createJoint(anchorJoint));
            } else {
                if (i == 0) {
                    Body leftAnchor = anchors.get(0).getObstacle().getBody();
                    anchorJoint.initialize(leftAnchor, top, leftAnchor.getPosition());
                    joints.add(world.createJoint(anchorJoint));
                    anchorJoint.initialize(leftAnchor, bottom, leftAnchor.getPosition().cpy());
                    joints.add(world.createJoint(anchorJoint));
                }

                Body topRight = topEntities.get(i + 1).getObstacle().getBody();
                Body bottomRight = bottomEntities.get(i + 1).getObstacle().getBody();

                externalJoint.initialize(top, topRight, top.getPosition().cpy().add(halfWidth,-2*halfHeight));
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(bottom, bottomRight, bottom.getPosition().cpy().add(halfWidth,halfHeight));
                joints.add(world.createJoint(externalJoint));

//                revoluteJoint.initialize(top, topRight, top.getPosition().cpy().add(halfWidth,0));
//                joints.add(world.createJoint(revoluteJoint));
//                revoluteJoint.initialize(bottom, bottomRight, bottom.getPosition().cpy().add(halfWidth,0));
//                joints.add(world.createJoint(revoluteJoint));

                externalJoint.initialize(top, bottomRight, top.getPosition().cpy().add(-halfWidth,-halfHeight));
                joints.add(world.createJoint(externalJoint));
                externalJoint.initialize(bottom, topRight, bottom.getPosition().cpy().add(halfWidth,halfHeight));
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
