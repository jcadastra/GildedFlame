package edu.cornell.cis3152.physics.level_player.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A tween comes from inbetween frames used when animating, leads to interpolation
 * this framework is kidn of inspired by Godot's implementaiton in that you register a tween to
 * do something it will act in such a way using whatever method of interpolation passed in
 *
 * at the moment there is no limit to the amount of tweens that can be associated to an object
 * just that if you add tweens that modify the same attribute to the same object, unexpected things
 * will occur
 *
 * @param <T> class of attribute being modified
 */
public class TweenElement <T> {

    /**
     * The element that will be effected by the tween movement
     */
    public ObstacleSprite target;

    /**
     * the name of the action that the tween repersents
     */
    public String name;

    /**
     * the value of the state in which the object's attribute first started in
     */
    public T initalState;

    /**
     * the final value of the state that the object wants the attribute to go to
     */
    public T finalState;

    /**
     * A vector repersenting the total time the tween has been acting for (x) and the total time the
     * tween is expected to take (y), in seconds
     */
    public Vector2 timerVector;

    /**
     * The function used to get the float value for the inporlation used, typically obtained via
     * Interpolation.______
     */
    public Function<Float, Float> interpolator;

    /**
     * the getter function used to obtain the attribute desired to change in the object, could be
     * different attribute than supplier, for example could query position and set velocity
     */
    public Supplier<T> supplier;

    /**
     * the setter function of the object used to update the value, could be different attribute than
     * supplier, for example could query position and set velocity
     */
    public Consumer<T> updater;

    /**
     * Constructor for tween, the whole point is to provide a way to smoothly update a value from
     * initialState to finalState of target for a duration of maxTimeSec
     *
     * @param target       the eleemnt that will be effected by the tween
     * @param name         the name of the action that the tween repersents
     * @param initialState the value of the state in which the object's attribute first started in
     * @param finalState   the final value of the state that the object wants the attribute to go
     *                     to
     * @param maxTimeSec   max time the tween is supposed to take in secodns
     * @param interpolator The function used to get the float value for the inporlation used,
     *                     typically see Interpolation.______
     * @param supplier     the getter function used to obtain the attribute desired to change in the
     *                     object, could be  different attribute than supplier, for example could
     *                     query position and set velocity
     * @param updater      the setter function of the object used to update the value, could be
     *                     different attribute than supplier, for example could query position and
     *                     set velocity
     */
    public TweenElement(ObstacleSprite target, String name, T initialState, T finalState,
        float maxTimeSec, Function<Float, Float> interpolator, Supplier<T> supplier,
        Consumer<T> updater) {
        this.target = target;
        this.name = name;
        this.initalState = initialState;
        this.finalState = finalState;
        this.timerVector = new Vector2(0, maxTimeSec);
        this.interpolator = interpolator;
        this.supplier = supplier;
        this.updater = updater;
    }

    public OrthographicCamera cam;
    public TweenElement(OrthographicCamera cam, String name, T initialState, T finalState,
        float maxTimeSec, Function<Float, Float> interpolator, Supplier<T> supplier,
        Consumer<T> updater) {
        this.cam = cam;
        this.name = name;
        this.initalState = initialState;
        this.finalState = finalState;
        this.timerVector = new Vector2(0, maxTimeSec);
        this.interpolator = interpolator;
        this.supplier = supplier;
        this.updater = updater;
    }
}
