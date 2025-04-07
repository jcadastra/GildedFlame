package edu.cornell.cis3152.physics.level_player.utils;

import com.badlogic.gdx.physics.box2d.Fixture;

public class ContactKey {

    /**
     * the fixtures stored within that define the key and the collision
     */
    public final Fixture fix1;
    public final Fixture fix2;

    /**
     * Contact key is used here to create a key for a sustained contact memroy system
     * the key stores the speciic fixtures that collided, order doesnt matter so
     * a b == b a
     * @param a first fixture of collision
     * @param b second fixture of collision
     */
    public ContactKey(Fixture a, Fixture b) {
        if (a.hashCode() < b.hashCode()) {
            this.fix1 = a;
            this.fix2 = b;
        } else {
            this.fix1 = b;
            this.fix2 = a;
        }
    }

    /**
     * modify collision equality so that the ensure each item is equal and not superficially equal
     * @param o the object of comparison
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactKey)) return false;
        ContactKey that = (ContactKey) o;
        return fix1 == that.fix1 && fix2 == that.fix2;
    }

    /**
     * gen hashcode to (hopefully) avoid collisions
     */
    @Override
    public int hashCode() {
        return fix1.hashCode() * 42 + fix2.hashCode();
    }
}
