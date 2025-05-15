package edu.cornell.cis3152.physics.level_player.utils;

import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.function.Function;

public class EventAction<T> {

    /**
     * The target which the action will apply to
     */
    private ObstacleSprite target;
    private Object targetobj;

    public ObstacleSprite getTarget() {
        return target;
    }

    /**
     * the name of the action that will occur
     */
    private String name;

    public String getName() {
        return name;
    }

    /**
     * the inital state of the action
     */
    private T initialValue;

    public T getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(T value) {
        initialValue = value;
    }

    /**
     * the final state of the action
     */
    private T finalValue;

    public T getFinalValue() {
        return finalValue;
    }
    public void setFinalValue(T value) {
        finalValue = value;
    }

    /**
     * the expected time that the whole action will take
     */
    private float time;

    public float getTime() {
        return time;
    }

    /**
     * the type of interpolator used to update the action
     */
    public Function<Float,Float> interpolator;

    public Function<Float, Float> getInterpolator() {
        return interpolator;
    }
    public void forceUpdateTarget(ObstacleSprite target) {this.target = target;}

    /**
     * An event action with tween information contains the information to affect an object after the event is called
     * store the information as needed and when the event is completed, it will add in the event action and this
     * associated tween to be processed
     *
     * @param target the object whose attribute will be changed
     * @param name the name of the action that will occur
     * @param initialValue the starting value for the attribute that will be changed
     * @param finalValue the ending vavlue of the attribute that will be chagned
     * @param time the time (in seconds) that it will take for the attribute to go from inital to final
     * @param interpolator the type of interpolator that will be used to guide the change in the value
     */
    public EventAction (ObstacleSprite target, String name, T initialValue, T finalValue, float time, Function<Float,Float> interpolator) {
        this.target = target;
        this.name = name;
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.time = time;
        this.interpolator = interpolator;
    }
    public EventAction (Object targetobj, String name, T initialValue, T finalValue, float time, Function<Float,Float> interpolator) {
        this.targetobj = targetobj;
        this.name = name;
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.time = time;
        this.interpolator = interpolator;
    }

    public EventAction (ObstacleSprite target, String name, T initialValue, T finalValue) {
        this.target = target;
        this.name = name;
        this.initialValue = initialValue;
        this.finalValue = finalValue;
    }

    /**
     * Tween-less event, mainly used to do an instant event that doesn't need to occur over time and
     * can be self contained
     * @param name name of the event
     */
    public EventAction (String name) {
        this.name = name;
    }

    /**
     * @return returns a clone of the tween event not of the single name event (undefined behavior)
     */
    public EventAction<T> cloneTweenEvent() {
        return new EventAction<T>(target, name, initialValue, finalValue, time, interpolator);
    }
}
