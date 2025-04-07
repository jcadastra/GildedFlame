package edu.cornell.cis3152.physics.level_player.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class Event<T,U> {

    /**
     * the object that is responsible to trigger this event
     */
    public Object source;

    /**
     * the gett value used to obtain the T value that the event is expected to check on the predicate
     */
    public Supplier<T> getter;

    /**
     * the conditional used to check whether or not the target has reached the particular value
     */
    public Predicate<T> conditional;

    /**
     * the event action that will occur once this event is triggered
     */
    public EventAction<U> action;

    /**
     * creates a new event action that will need to be added to the event handler that will check the
     * predicate and in turn will add this event to a list of events to process once the predicate is met
     * these are meant to be terminal on use, to create a follow up action with similar conditions, use
     * clone
     * @param source element that we are reading value from to trigger event
     * @param getter the getter function for the attribute to read
     * @param conditional the conditional used to compare against and determine if an event should be fired
     * @param action the EventAction that will ocurr after the event is fired and resolved
     */
    public Event(Object source, Supplier<T> getter, Predicate<T> conditional, EventAction<U> action) {
        this.source = source;
        this.getter = getter;
        this.conditional = conditional;
        this.action = action;
    }

    /**
     * secure way to clone event
     * @return a new instance of the event this was called upon
     */
    @Override
    public Event<T,U> clone() {
        return new Event<T,U>(source,getter,conditional,action.cloneTweenEvent());
    }
}
