package edu.cornell.cis3152.physics.level_player;

import edu.cornell.cis3152.physics.level_player.utils.Event;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class EventHandler {

    /**
     * registered events to traverse each call
     */
    private ArrayList<Event<?,?>> registeredEvents;

    /**
     * flags to process, ie their predicate was succesful in the last ieration
     */
    private Stack<Event<?,?>> eventFlags;
    public Stack<Event<?,?>> getEventFlags() {return eventFlags;}

    /**
     * The event handler is what constantly checks the events to see if their predicate was succesful
     * or not. Ie, this is the thing that gets the alue of each registered object, constantly, and then
     * if there is a succesful event it registers it to the event flags to be pulled by GameplayScene to be processed
     */
    public EventHandler() {
        eventFlags = new Stack<>();
        registeredEvents = new ArrayList<>();
    }

    /**
     * register an event to the handler to be checked each iteration
     * @param event the event to be registered
     */
    public void registerEvent(Event<?,?> event) {
        registeredEvents.add(event);
    }

    /**
     * the function that loops through all registered events and checks to see if the event
     * passes it's predicate, if so then lists it in the flags and REMOVES THE EVENT
     * after a SUCCESSFUL call
     */
    public void update() {
        for (Iterator<Event<?,?>> iter = registeredEvents.iterator(); iter.hasNext(); ) {
            Event<?,?> registeredEvent = iter.next();
            if (testEvent(registeredEvent)) {
                eventFlags.add(registeredEvent);
                iter.remove();
            }
        }
    }

    /**
     * helper function to avoid funky limitations and keep multi class events in one handler without problem
     */
    private static boolean testEvent(Event<?,?> event) {
        return testEventHelper(event);
    }
    private static <T> boolean testEventHelper(Event<T,?> event) {
        return  event.conditional.test(event.getter.get());
    }

    /**
     * clears registered events
     */
    public void dispose() {
        registeredEvents.clear();
    }
}
