package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
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
    private JsonValue data;

    private float ropeThickness;
    private float ropePieceLen;
    private Vector2 step;

    // Single row of rope nodes.
    private ArrayList<EnhancedObstacleSprite> nodes;
    // Anchor objects at the endpoints.
    private ArrayList<EnhancedObstacleSprite> anchors;
    public float[] nodeVertices;

    /**
     * Constructs a rope as a single chain of nodes with end anchors.
     *
     * @param pin1          starting point (in physics units)
     * @param pin2          ending point (in physics units)
     * @param depthOfCurve  depth of the curve, atm not used
     * @param units         physics unit scale factor
     * @param data          JSON data containing rope properties ("thickness", "piecelen", etc.)
     */
    public Rope(Vector2 pin1, Vector2 pin2, float depthOfCurve, float units, JsonValue data) {
        this.pin1 = pin1.scl(units);
        this.pin2 = pin2.scl(units);
        this.units = units;
        this.data = data;

        // Calculate the rope’s direction.
        this.internalAngle = (pin2.cpy().sub(pin1)).angleRad();
        ropeThickness = data.getFloat("thickness");
        ropePieceLen = data.getFloat("piecelen");
        // Step along the rope direction.
        this.step = ((pin2.cpy().sub(pin1)).nor()).scl(ropePieceLen);

        // Generate one row of vertices; offset = 0 gives a centered line.
        nodeVertices = genOneRow(0);
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
        // TODO: implement a proper catenary curve if desired.
        return 0;
    }

    /**
     * Generates a row of vertices along the rope's path.
     * The provided offset is applied perpendicular to the rope's direction.
     *
     * @param offset the perpendicular offset (0 for the centered line)
     * @return an array of float values (x, y, x, y, ...)
     */
    private float[] genOneRow(float offset) {
        // For a tangent (cos(angle), sin(angle)), the perpendicular normal is (-sin(angle), cos(angle)).
        float x_internalOffset = -offset * (float)Math.sin(internalAngle);
        float y_internalOffset = offset * (float)Math.cos(internalAngle);
        FloatArray vertexSet = new FloatArray();

        // Start a half-step from the first pin.
        Vector2 i = new Vector2(pin1).add(step.cpy().scl(0.5f));
        vertexSet.add(i.x + x_internalOffset);
        vertexSet.add(i.y + y_internalOffset + catenaryCurveFunc(i.x));
        // Continue adding vertices until we reach near pin2.
        while (pin2.dst(i) > ropePieceLen) {
            i.add(step);
            vertexSet.add(i.x + x_internalOffset);
            vertexSet.add(i.y + y_internalOffset + catenaryCurveFunc(i.x));
        }
        return vertexSet.toArray();
    }

    /**
     * Given an array of vertices, create rope node fixtures.
     *
     * @param vertices x, y coordinates of the rope nodes
     * @return an ArrayList of EnhancedObstacleSprite nodes.
     */
    private ArrayList<EnhancedObstacleSprite> genFixtures(float[] vertices) {
        ArrayList<EnhancedObstacleSprite> nodes = new ArrayList<>();
        for (int i = 0; i < vertices.length; i += 2) {
            BoxObstacle ropeNode = new BoxObstacle(vertices[i] / units, vertices[i + 1] / units,
                (ropePieceLen * 1.3f) / units, (ropeThickness) / (units));
            ropeNode.setPhysicsUnits(units);
            ropeNode.setBodyType(BodyType.DynamicBody);
            ropeNode.setAngle(internalAngle);
            ropeNode.setDensity(0.01f);
            ropeNode.setAngularDamping(10000f);
            ropeNode.setSensor(true);
            ropeNode.setName("ropeNode");
            EnhancedObstacleSprite sprite = new EnhancedObstacleSprite(ropeNode);
            sprite.setMaterial(new ObstacleMaterial("rope", data.get(1)));
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
        WheelObstacle anchorDef = new WheelObstacle(pin1.x / units, pin1.y / units, ropeThickness / units);
        anchorDef.setBodyType(BodyType.StaticBody);
        anchorDef.setMass(0.1f);
        anchorDef.setDensity(1f);
        anchorDef.setFixedRotation(false);
        anchorDef.setPhysicsUnits(units);
        anchorDef.setSensor(true);
        anchorDef.setName("ropeAnchorLeft");
        EnhancedObstacleSprite leftAnchor = new EnhancedObstacleSprite(anchorDef);
        leftAnchor.setMaterial(new ObstacleMaterial("rope", data.get(1)));
        leftAnchor.setDebugColor(Color.GREEN);
        sprites.add(leftAnchor);
        anchorList.add(leftAnchor);

        // Create right (end) anchor.
        anchorDef.setPosition(pin2.x / units, pin2.y / units);
        anchorDef.setName("ropeAnchorRight");
        EnhancedObstacleSprite rightAnchor = new EnhancedObstacleSprite(anchorDef);
        rightAnchor.setMaterial(new ObstacleMaterial("rope", data.get(1)));
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
            jointDef.initialize(leftAnchorBody, firstNode, leftAnchorBody.getPosition());
            joints.add(world.createJoint(jointDef));

            // Attach right anchor to the last node.
            Body rightAnchorBody = anchors.get(1).getObstacle().getBody();
            Body lastNode = nodes.get(nodes.size() - 1).getObstacle().getBody();
            jointDef.initialize(rightAnchorBody, lastNode, rightAnchorBody.getPosition());
            joints.add(world.createJoint(jointDef));
        }
        return true;
    }

    /**
     * Sets the texture for all rope nodes and anchors.
     *
     * @param ropeTexture the texture to apply.
     */
    public void setTextures(Texture endTexture, Texture ropeTexture) {
        for (EnhancedObstacleSprite sprite : nodes) {
            sprite.setTexture(ropeTexture);
        }
        for (EnhancedObstacleSprite anchor : anchors) {
            anchor.setTexture(endTexture);
        }
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
}
