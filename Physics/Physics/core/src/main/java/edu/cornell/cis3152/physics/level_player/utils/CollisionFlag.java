package edu.cornell.cis3152.physics.level_player.utils;

import com.badlogic.gdx.physics.box2d.Fixture;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class CollisionFlag {
    private String name;

    public String getName() {
        return name;
    }

    private ObstacleSprite subject;

    public ObstacleSprite getSubject() {
        return subject;
    }
    private Fixture fixture;

    public Fixture getFixture() {
        return fixture;
    }

    public CollisionFlag(String name) {
        this.name = name;
    }
    public CollisionFlag(String name, ObstacleSprite subject) {
        this.name = name;
        this.subject = subject;
    }
    public CollisionFlag(String name, Fixture fixture) {
        this.name = name;
        this.fixture = fixture;
    }
}
