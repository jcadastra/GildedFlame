package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.particles.values.WeightMeshSpawnShapeValue;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.CapsuleObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class Button extends ObstacleGroup {

    private Vector2 pos;
    private float rotatationRads;
    private float len;
    private float height;
    private float internal_button_offset;
    private boolean double_sided;
    private boolean latch;
    private float units;
    private ObstacleSprite base;
    private ObstacleSprite button;
    private ObstacleSprite underside;

    /**
     * THe state the button is in
     * # = which side is popped up ie
     * 1 = default side elevated, 0 = depressed (nothing sticking out); 2 = alt side elevated
     *
     * note that a button will never reach state two unless it is double sided.
     */
    private int state;

    public Button(Vector2 pos, float rotatationRads, boolean double_sided, boolean latch, float units) {
        super();

        this.pos = pos;
        this.rotatationRads = rotatationRads;
        this.len = 2;
        this.height = .5f;
        this.internal_button_offset = .2f;
        this.double_sided = double_sided;
        this.latch = latch;
        this.units = units;

        this.base = genBase();
        this.button = genButton(double_sided ? 2 : 1);
    }

    private ObstacleSprite genBase() {
        BoxObstacle baseOb = new BoxObstacle(pos.x, pos.y, len,height);
        //set texture
        baseOb.setBodyType(BodyType.StaticBody);
        baseOb.setAngle(rotatationRads);
        baseOb.setName("button-base");
        baseOb.setPhysicsUnits(units);
        base = new ObstacleSprite(baseOb);
        sprites.add(base);
        base.setDebugColor( Color.GREEN );
        return base;
    }

    private ObstacleSprite genButton(float doubled) {
        CapsuleObstacle internalButton = new CapsuleObstacle(len - internal_button_offset, height * doubled);

        float offsetX = (float)((height) / 2 * Math.sin(rotatationRads));
        float offsetY = (float)((height) / 2 * Math.cos(rotatationRads));

        internalButton.setPosition(pos.x + offsetX, pos.y - offsetY);
        internalButton.setAngle(rotatationRads);
        internalButton.setName("button-core");
        internalButton.setPhysicsUnits(units);
        internalButton.setDensity(0.01f);
        ObstacleSprite buttonObj = new ObstacleSprite(internalButton);
        sprites.add(buttonObj);
        buttonObj.setDebugColor(Color.PURPLE);
        return buttonObj;
    }

    @Override
    protected boolean createJoints(World world) {
        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
        Vector2 axis = new Vector2((float)Math.sin(rotatationRads),(float)-Math.cos(rotatationRads));
        prismaticJointDef.initialize(base.getObstacle().getBody(),button.getObstacle().getBody(),base.getObstacle().getBody().getWorldCenter(),
            axis
        );
        prismaticJointDef.enableLimit = true;
        prismaticJointDef.lowerTranslation = -height / ( double_sided ? 1 : 2);
        prismaticJointDef.upperTranslation = 0f;

        prismaticJointDef.enableMotor = true;
        prismaticJointDef.motorSpeed = 10f;
        prismaticJointDef.maxMotorForce = 0.5f;

        joints.add(world.createJoint(prismaticJointDef));

        if (underside != null) {
            WeldJointDef weldJointDef = new WeldJointDef();
            weldJointDef.initialize(button.getObstacle().getBody(), underside.getObstacle().getBody(), button.getObstacle().getPosition());
            joints.add(world.createJoint(weldJointDef));
        }

        return true;
    }
}
