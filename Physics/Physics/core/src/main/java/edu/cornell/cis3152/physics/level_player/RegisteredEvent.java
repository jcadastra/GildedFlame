package edu.cornell.cis3152.physics.level_player;

import edu.cornell.cis3152.physics.level_player.enviromentals.Button;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RegisteredEvent<T> {

    public Object source;
    public Supplier<T> getter;
    public Predicate<T> conditional;
    public String caller;
    public Object target;

    public RegisteredEvent(Object source, Supplier<T> getter, Predicate<T> conditional, String caller, Object target) {
        this.source = source;
        this.getter = getter;
        this.conditional = conditional;
        this.caller = caller;
        this.target = target;
    }

}
