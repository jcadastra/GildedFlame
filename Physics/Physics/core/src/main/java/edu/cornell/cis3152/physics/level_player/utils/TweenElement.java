package edu.cornell.cis3152.physics.level_player.utils;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.function.Consumer;
import java.util.function.Function;

public class TweenElement <T> {
    public ObstacleSprite target;
    public String name;
    public T initalState;
    public T finalState;
    public Vector2 timerVector;
    public Function<Float,Float> interpolator;
    public Consumer<T> updater;
    public TweenElement (ObstacleSprite target, String name, T initialState, T finalState, float maxTimeSec, Function<Float,Float> interpolator, Consumer<T> updater) {
        this.target = target;
        this.name = name;
        this.initalState = initialState;
        this.finalState = finalState;
        this.timerVector = new Vector2(0, maxTimeSec);
        this.interpolator = interpolator;
        this.updater = updater;
    }


}
