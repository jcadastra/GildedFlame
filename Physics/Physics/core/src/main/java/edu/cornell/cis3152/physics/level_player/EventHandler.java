package edu.cornell.cis3152.physics.level_player;

import edu.cornell.cis3152.physics.level_player.utils.Event;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class EventHandler {


    private ArrayList<Event<?,?>> registeredEvents;
    private Stack<Event<?,?>> eventFlags;
    public Stack<Event<?,?>> getEventFlags() {return eventFlags;}

    public EventHandler() {
        eventFlags = new Stack<>();
        registeredEvents = new ArrayList<>();
    }

    public void registerEvent(Event<?,?> eventFlag) {
        registeredEvents.add(eventFlag);
    }

    public void update() {
        for (Iterator<Event<?,?>> iter = registeredEvents.iterator(); iter.hasNext(); ) {
            Event<?,?> registeredEvent = iter.next();
            if (testEvent(registeredEvent)) {
                eventFlags.add(registeredEvent);
                iter.remove();
            }
        }
    }

    private static boolean testEvent(Event<?,?> event) {
        return testEventHelper(event);
    }
    private static <T> boolean testEventHelper(Event<T,?> event) {
        return  event.conditional.test(event.getter.get());
    }
    public void dispose() {
        registeredEvents.clear();
    }
}
