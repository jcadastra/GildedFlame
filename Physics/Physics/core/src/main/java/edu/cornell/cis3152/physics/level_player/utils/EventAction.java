package edu.cornell.cis3152.physics.level_player.utils;

import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.function.Function;

public class EventAction<T> {
    private ObstacleSprite target;

    public ObstacleSprite getTarget() {
        return target;
    }

    private String name;

    public String getName() {
        return name;
    }

    private T initialPoint;

    public T getInitialPoint() {
        return initialPoint;
    }

    public void setInitialPoint (T value) {
        initialPoint = value;
    }

    private T finalPoint;

    public T getFinalPoint() {
        return finalPoint;
    }
    public void setFinalPoint (T value) {
        finalPoint = value;
    }

    private float time;

    public float getTime() {
        return time;
    }

    public Function<Float,Float> interpolator;

    public Function<Float, Float> getInterpolator() {
        return interpolator;
    }

    public EventAction (ObstacleSprite target, String name, T initialPoint, T finalPoint, float time, Function<Float,Float> interpolator) {
        this.target = target;
        this.name = name;
        this.initialPoint = initialPoint;
        this.finalPoint = finalPoint;
        this.time = time;
        this.interpolator = interpolator;
    }

    public EventAction (String name) {
        this.name = name;
    }

    public EventAction<T> clone() {
        return new EventAction<T>(target, name, initialPoint, finalPoint, time, interpolator);
    }
}
