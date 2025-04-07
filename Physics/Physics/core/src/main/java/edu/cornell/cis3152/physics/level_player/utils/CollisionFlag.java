package edu.cornell.cis3152.physics.level_player.utils;

import com.badlogic.gdx.physics.box2d.Fixture;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class CollisionFlag {

    /**
     * name of the event that collision is about
     */
    private String name;

    public String getName() {
        return name;
    }

    /**
     * main subject of the flag (if applicable)
     */
    private ObstacleSprite subject;

    public ObstacleSprite getSubject() {
        return subject;
    }

    /**
     * main fixture of the flag (if applicable)
     */
    private Fixture fixture;

    public Fixture getFixture() {
        return fixture;
    }

    /**
     * base collision flag with not associated subject or fixture
     * @param name the type of flag called
     */
    public CollisionFlag(String name) {
        this.name = name;
    }

    /**
     * collision flag with associated subject
     * @param name type of the flag that occurs
     * @param subject subject of the flag
     */
    public CollisionFlag(String name, ObstacleSprite subject) {
        this.name = name;
        this.subject = subject;
    }

    /**
     * collision flag with associated fixture
     * @param name type of the flag that occurs
     * @param fixture fixture of the flag
     */
    public CollisionFlag(String name, Fixture fixture) {
        this.name = name;
        this.fixture = fixture;
    }
}
