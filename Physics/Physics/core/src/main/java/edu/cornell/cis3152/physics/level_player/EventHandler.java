package edu.cornell.cis3152.physics.level_player;

import edu.cornell.cis3152.physics.level_player.enviromentals.Button;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EventHandler {


    private ArrayList<RegisteredEvent> registeredEvents;
    private Stack<Object[]> eventFlags;
    public Stack<Object[]> getEventFlags() {return eventFlags;}

    public EventHandler() {
        eventFlags = new Stack<>();
        registeredEvents = new ArrayList<>();
    }

    public <T> void registerEvent(RegisteredEvent<T> registeredEvent) {
        registeredEvents.add(registeredEvent);
    }

    public void update() {
        for (Iterator<RegisteredEvent> iter = registeredEvents.iterator(); iter.hasNext(); ) {
            RegisteredEvent registeredEvent = iter.next();
            if (registeredEvent.conditional.test(registeredEvent.getter.get())) {
                eventFlags.add(new Object[]{registeredEvent.caller, registeredEvent.source, registeredEvent.action});
                iter.remove();
            }
        }
    }

    public void dispose() {
        registeredEvents.clear();
    }
}
