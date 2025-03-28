package edu.cornell.cis3152.physics.level_player.utils;

import com.badlogic.gdx.physics.box2d.Fixture;

public class ContactKey {
    public final Fixture fix1;
    public final Fixture fix2;

    public ContactKey(Fixture a, Fixture b) {
        if (a.hashCode() < b.hashCode()) {
            this.fix1 = a;
            this.fix2 = b;
        } else {
            this.fix1 = b;
            this.fix2 = a;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactKey)) return false;
        ContactKey that = (ContactKey) o;
        return fix1 == that.fix1 && fix2 == that.fix2;
    }

    @Override
    public int hashCode() {
        return fix1.hashCode() * 42 + fix2.hashCode();
    }
}
