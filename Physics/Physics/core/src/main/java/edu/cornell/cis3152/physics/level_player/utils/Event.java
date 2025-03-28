package edu.cornell.cis3152.physics.level_player.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class Event<T,U> {

    public Object source;
    public Supplier<T> getter;
    public Predicate<T> conditional;
    public String caller;
    public EventAction action;

    public Event(Object source, Supplier<T> getter, Predicate<T> conditional, String caller, EventAction<U> action) {
        this.source = source;
        this.getter = getter;
        this.conditional = conditional;
        this.caller = caller;
        this.action = action;
    }

}
