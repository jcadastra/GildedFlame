package edu.cornell.cis3152.physics.platform;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.List;
public class AIController {


    public static Vector2 playerPosition;

    public Traci traci;
    public AIController(Torch_playground playground){
        this.traci = playground.getAvatar();
    }

    public void update(){
        playerPosition = traci.getLocation();
    }
}
