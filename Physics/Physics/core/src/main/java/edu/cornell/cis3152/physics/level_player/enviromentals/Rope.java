package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
 * A single-line rope made of nodes along a (potentially curved) path with anchors at both ends.
 * The rope is constructed as a chain of nodes connected by weld joints,
 * and two anchor obstacles at the endpoints hold the rope in place.
 */
public class Rope extends ObstacleGroup {
    private Vector2 pin1;
    private Vector2 pin2;
    private float internalAngle;
    private float units;
    private float depthOfCurve;
    private float x_c;
    private JsonValue data;

    private float ropeThickness;
    private float ropePieceLen;
    private Vector2 step;

    // Single row of rope nodes.
    private ArrayList<EnhancedObstacleSprite> nodes;
    // Anchor objects at the endpoints.
    private ArrayList<EnhancedObstacleSprite> anchors;
    public FloatArray nodeVertices;

    /**
     * Constructs a rope as a single chain of nodes with end anchors.
     *
     * @param pin1          starting point (in physics units)
     * @param pin2          ending point (in physics units)
     * @param depthOfCurve  depth of the curve, atm not used
     * @param units         physics unit scale factor
     * @param data          JSON data containing rope properties ("thickness", "piecelen", etc.)
     */
    public Rope(Vector2 pin1, Vector2 pin2, float depthOfCurve, float thickness, float piecelen, float units, JsonValue data) {
        this.pin1 = pin1.scl(units);
        this.pin2 = pin2.scl(units);
        this.units = units;
        this.data = data;
        this.depthOfCurve = depthOfCurve;
        this.x_c = pin2.x-pin1.x;

        // Calculate the rope’s direction.
        this.internalAngle = (pin2.cpy().sub(pin1)).angleRad();
        ropeThickness = thickness * 1.5f * units/80;
        ropePieceLen = piecelen * 1.5f * units/80;
        //ropeThickness = data.getFloat("thickness");
        //ropePieceLen = data.getFloat("piecelen");
        // Step along the rope direction.
        this.step = ((pin2.cpy().sub(pin1)).nor()).scl(ropePieceLen);

        // Generate one row of vertices; offset = 0 gives a centered line.
        nodeVertices = genOneRow();
        adjustNodeVertices();
        nodes = genFixtures(nodeVertices);
        anchors = genAnchors();
    }

    /**
     * Placeholder for a catenary curve function.
     * Currently, returns 0 so the rope is straight.
     *
     * @param x_pos the x-position (in physics units)
     * @return a y offset (in physics units)
     */
    private float catenaryCurveFunc(float x_pos) {
        double y_0 = -depthOfCurve * Math.cosh(-x_c/depthOfCurve);
        System.out.println(y_0);
        System.out.println(x_pos - x_c);
        System.out.println( Math.cosh( (x_pos - x_c)));
        System.out.println((depthOfCurve * Math.cosh( (x_pos - x_c) / depthOfCurve ) + y_0));
        return (float) (depthOfCurve * Math.cosh( (x_pos - x_c) / depthOfCurve ) + y_0) / units;
//        return 0;
    }

    /**
     * Generates a row of vertices along the rope's path.
     * The provided offset is applied perpendicular to the rope's direction.
     *
     * @return an array of float values (x, y, x, y, ...)
     */
    private FloatArray genOneRow() {
        // For a tangent (cos(angle), sin(angle)), the perpendicular normal is (-sin(angle), cos(angle)).
        float x_internalOffset = (float)Math.sin(internalAngle);
        float y_internalOffset = (float)Math.cos(internalAngle);
        FloatArray vertexSet = new FloatArray();

        // Start a half-step from the first pin.
        Vector2 i = new Vector2(pin1).add(step.cpy().scl(0.5f));
        vertexSet.add(i.x + x_internalOffset);
        vertexSet.add(i.y + y_internalOffset);
        // Continue adding vertices until we reach near pin2.
        while (pin2.dst(i) > ropePieceLen) {
            i.add(step);
            vertexSet.add(i.x + x_internalOffset);
            vertexSet.add(i.y + y_internalOffset);
        }
        return vertexSet;
    }

    private void adjustNodeVertices() {
//        for (int i=0; i < nodeVertices.size; i += 2) {
//            System.out.println("x: " + nodeVertices.get(i) + ", y: " + nodeVertices.get(i+1) + ", catheter: " + catenaryCurveFunc(nodeVertices.get(i)));
//            nodeVertices.set(i + 1, catenaryCurveFunc(nodeVertices.get(i)));
//        }
    }

    /**
     * Given an array of vertices, create rope node fixtures.
     *
     * @param vertices x, y coordinates of the rope nodes
     * @return an ArrayList of EnhancedObstacleSprite nodes.
     */
    private ArrayList<EnhancedObstacleSprite> genFixtures(FloatArray vertices) {
        ArrayList<EnhancedObstacleSprite> nodes = new ArrayList<>();
        for (int i = 0; i < vertices.size; i += 2) {
            BoxObstacle ropeNode = new BoxObstacle(vertices.get(i) / units, vertices.get(i + 1) / units,
                (ropePieceLen * 1.3f) / units, (ropeThickness) / (units));
            ropeNode.setPhysicsUnits(units);
            ropeNode.setBodyType(BodyType.DynamicBody);
            ropeNode.setAngle(internalAngle);
            ropeNode.setDensity(0.01f);
            ropeNode.setAngularDamping(10000f);
            ropeNode.setSensor(true);
            ropeNode.setName("ropeNode");
            EnhancedObstacleSprite sprite = new EnhancedObstacleSprite(ropeNode);
            sprite.setDebugColor(Color.PURPLE);
            sprite.setClimbable(true);
            nodes.add(sprite);
            sprites.add(sprite);
        }
        return nodes;
    }

    /**
     * Creates two anchor objects at the endpoints.
     *
     * @return an ArrayList of two EnhancedObstacleSprite anchors.
     */
    private ArrayList<EnhancedObstacleSprite> genAnchors() {
        ArrayList<EnhancedObstacleSprite> anchorList = new ArrayList<>();

        // Create left (start) anchor.
        WheelObstacle leftAnchorObs = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness / units);
        leftAnchorObs.setBodyType(BodyType.StaticBody);
        leftAnchorObs.setMass(0.1f);
        leftAnchorObs.setDensity(1f);
        leftAnchorObs.setFixedRotation(false);
        leftAnchorObs.setPhysicsUnits(units);
        leftAnchorObs.setSensor(true);
        leftAnchorObs.setName("ropeAnchorLeft");
        EnhancedObstacleSprite leftAnchor = new EnhancedObstacleSprite(leftAnchorObs);
        leftAnchor.setDebugColor(Color.GREEN);
        sprites.add(leftAnchor);
        anchorList.add(leftAnchor);

        // Create right (end) anchor.
        WheelObstacle rightAnchorObs = new WheelObstacle(pin2.x / units, pin2.y / units, ropeThickness / units);
        rightAnchorObs.setBodyType(BodyType.StaticBody);
        rightAnchorObs.setMass(0.000000001f);
        rightAnchorObs.setDensity(0.0000001f);
        rightAnchorObs.setFixedRotation(false);
        rightAnchorObs.setPhysicsUnits(units);
        rightAnchorObs.setSensor(true);
        rightAnchorObs.setName("ropeAnchorRight");
        EnhancedObstacleSprite rightAnchor = new EnhancedObstacleSprite(rightAnchorObs);
        rightAnchor.setDebugColor(Color.GREEN);
        sprites.add(rightAnchor);
        anchorList.add(rightAnchor);

        return anchorList;
    }

    /**
     * Creates weld joints between consecutive rope nodes and attaches the end nodes to anchors.
     *
     * @param world the Box2D world instance.
     * @return true upon successful creation of joints.
     */
    @Override
    protected boolean createJoints(World world) {
        WeldJointDef jointDef = new WeldJointDef();
        jointDef.frequencyHz = 5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.collideConnected = false;

        RevoluteJointDef anchorJoint = new RevoluteJointDef();

        // Connect each node to its following neighbor.
        for (int i = 0; i < nodes.size() - 1; i++) {
            Body current = nodes.get(i).getObstacle().getBody();
            Body next = nodes.get(i + 1).getObstacle().getBody();
            Vector2 anchor = current.getPosition().cpy().add(next.getPosition()).scl(0.5f);
            jointDef.initialize(current, next, anchor);
            joints.add(world.createJoint(jointDef));
        }

        // Attach left anchor to the first node.
        if (!nodes.isEmpty() && !anchors.isEmpty()) {
            Body leftAnchorBody = anchors.get(0).getObstacle().getBody();
            Body firstNode = nodes.get(0).getObstacle().getBody();
            anchorJoint.initialize(leftAnchorBody,firstNode,leftAnchorBody.getPosition());
            joints.add(world.createJoint(anchorJoint));

            // Attach right anchor to the last node.
            Body rightAnchorBody = anchors.get(1).getObstacle().getBody();
            Body lastNode = nodes.get(nodes.size() - 1).getObstacle().getBody();
            anchorJoint.initialize(rightAnchorBody, lastNode, rightAnchorBody.getPosition());
            joints.add(world.createJoint(anchorJoint));
        }
        return true;
    }

    /**
     * Sets the texture for all rope nodes and anchors.
     *
     * @param ropeTexture the texture to apply.
     */
    public void blanketSetTextures(Texture endTexture, Texture ropeTexture) {
        for (EnhancedObstacleSprite sprite : nodes) {
            sprite.setTexture(ropeTexture);
            sprite.setMaterial(new ObstacleMaterial("rope"));
        }
        for (EnhancedObstacleSprite anchor : anchors) {
            anchor.setTexture(endTexture);
            anchor.setMaterial(new ObstacleMaterial("rope"));
        }
    }

    public void customRopeDesignation(Texture end1Texture, Texture ropeCoreTexture, Texture end2Texture, String end1Material, String ropeCoreMaterial, String end2Material) {
        anchors.get(0).setMaterial(new ObstacleMaterial(end1Material));
        anchors.get(1).setTexture(end1Texture);

        for (EnhancedObstacleSprite sprite : nodes) {
            sprite.setTexture(ropeCoreTexture);
            sprite.setMaterial(new ObstacleMaterial(ropeCoreMaterial));
        }

        anchors.get(1).setMaterial(new ObstacleMaterial(end2Material));
        anchors.get(1).setTexture(end2Texture);
    }

    /**
     * Returns the list of rope nodes.
     *
     * @return an ArrayList of EnhancedObstacleSprite nodes.
     */
    public ArrayList<EnhancedObstacleSprite> getNodes() {
        return nodes;
    }

    /**
     * Attaches a specified rope node to an external object.
     *
     * @param obj       the external object to attach to.
     * @param nodeIndex the index of the node in the rope.
     * @param world     the Box2D world.
     * @return the created joint.
     */
    public Joint attachNodeToObj(ObstacleSprite obj, int nodeIndex, World world) {
        EnhancedObstacleSprite node = nodes.get(nodeIndex);
        node.getObstacle().setBodyType(BodyType.DynamicBody);
        WeldJointDef weldJointDef = new WeldJointDef();
        weldJointDef.initialize(node.getObstacle().getBody(), obj.getObstacle().getBody(), node.getObstacle().getPosition());
        Joint joint = world.createJoint(weldJointDef);
        joints.add(joint);
        return joint;
    }

    public void deactivateAnchor (int anchorNum) {
        anchors.get(anchorNum).getObstacle().setBodyType(BodyType.DynamicBody);
    }
}

