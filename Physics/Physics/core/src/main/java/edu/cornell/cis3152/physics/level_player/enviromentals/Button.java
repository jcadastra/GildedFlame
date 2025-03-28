package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.CapsuleObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class Button extends ObstacleGroup {

    private Vector2 pos;
    private float rotatationRads;
    private float adjustedRotationRads;
    private float len;
    private float height;
    private float internal_button_offset;
    private boolean doubleSided;
    public boolean getDoubleSided() { return doubleSided; }
    private boolean latch;

    public boolean getLatch() { return latch; }
    private float units;
    private ObstacleSprite base;
    private ObstacleSprite button;
    private Joint buttonJoint;
    private int direction;

    /**
     * THe state the button is in
     * # = which side is popped up ie
     * states go in order of starting to pushing in IE
     * 0 is elevated start
     * 1 is pressed in
     * 2 is second half is sticking out (only reachable if button is double sided)
     */

    public int getState() {
        int state;
        if (Math.abs(button.getObstacle().getPosition().sub(base.getObstacle().getPosition()).len()) < 0.1) {
            state = 1;
        } else {
            state = direction > 0 ? 0 : 2;
        }
        return state;
    }

    public Button(Vector2 pos, float rotatationRads, boolean doubleSided, boolean latch, float units) {
        super();

        this.pos = pos;
        this.rotatationRads = rotatationRads;
        adjustedRotationRads = -rotatationRads;
        this.len = 2;
        this.height = .5f;
        this.internal_button_offset = .2f;
        this.doubleSided = doubleSided;
        this.latch = latch;
        this.units = units;
        this.direction = 1;

        genBase();
        genButton(doubleSided ? 2 : 1);
    }

    private void genBase() {
        BoxObstacle baseOb = new BoxObstacle(pos.x, pos.y, len,height);
        //set texture
        baseOb.setBodyType(BodyType.StaticBody);
        baseOb.setAngle(rotatationRads);
        baseOb.setName("button-base");
        baseOb.setPhysicsUnits(units);
        base = new ObstacleSprite(baseOb);
        sprites.add(base);
        base.setDebugColor( Color.GREEN );
    }

    private void genButton(float doubled) {
        CapsuleObstacle internalButton = new CapsuleObstacle(len - internal_button_offset, height * doubled);

        internalButton.setAngle(rotatationRads);
        internalButton.setName("button-core");
        internalButton.setPhysicsUnits(units);
        internalButton.setDensity(0.01f);
        ObstacleSprite buttonObj = new ObstacleSprite(internalButton);
        sprites.add(buttonObj);
        buttonObj.setDebugColor(Color.PURPLE);
        button = buttonObj;
        setRelativeButtonPos();
    }

    @Override
    protected boolean createJoints(World world) {
        if (buttonJoint != null) {
            joints.removeValue(buttonJoint, true);
            world.destroyJoint(buttonJoint);
            buttonJoint = null;
        }
        setRelativeButtonPos();

        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
        Vector2 axis = new Vector2((float) Math.sin(adjustedRotationRads),(float) Math.cos(adjustedRotationRads));
        System.out.println(axis);
        System.out.println(rotatationRads);
        prismaticJointDef.initialize(base.getObstacle().getBody(),button.getObstacle().getBody(),base.getObstacle().getBody().getPosition(),
            axis
        );

        prismaticJointDef.enableLimit = true;
        float adjustedButtonDepth = (float) (height);

        prismaticJointDef.upperTranslation = 0;
        prismaticJointDef.lowerTranslation = -adjustedButtonDepth / (doubleSided ? 1 : 2);
        if (direction < 0) {
            prismaticJointDef.upperTranslation += adjustedButtonDepth;
            prismaticJointDef.lowerTranslation += adjustedButtonDepth;
        }

        prismaticJointDef.enableMotor = true;
        prismaticJointDef.motorSpeed = 10f * direction;
        prismaticJointDef.maxMotorForce = 0.5f;

        buttonJoint = world.createJoint(prismaticJointDef);
        joints.add(buttonJoint);
        return true;
    }

    public void toggleButton(World world) {
        if (doubleSided) {
            direction = direction > 0 ? -1 : 1;
            createJoints(world);
        } else {
            if (latch) {
                button.getObstacle().setPosition(base.getObstacle().getPosition());
                button.getObstacle().setBodyType(BodyType.StaticBody);
            }
        }
    }

    private void setRelativeButtonPos() {
        float offsetX = direction * (float)((height) / 2 * Math.sin(adjustedRotationRads));
        float offsetY = direction * (float)((height) / 2 * Math.cos(adjustedRotationRads));
        button.getObstacle().setPosition(pos.x + offsetX, pos.y + offsetY);
    }

}
