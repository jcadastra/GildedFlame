package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.SpriteSheet;

public class Totem extends Enemy {


    public SpriteSheet enemySprite;

    private int cdFrameCount = 0;
    private int frameIndex = 0;
    public static final int FRAME_SIZE = 1080;
    public static final int TOTAL_FRAMES = 9;
    private static final int FRAME_DURATION = 12; // how many updates each frame lasts


    private void resetFrameCount() {
        cdFrameCount = 0;
    }

    private void resetFrameIndex() { frameIndex = 0; }

    public Totem(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        setJustCollided(false);
    }

    // reacting to light
    @Override
    public void in_light(){
//        System.out.println("INLIGHT");
        Texture texture;
        if (isFacingRight()) {
            texture = directory.getEntry("platform-totemLIGHTRIGHT", Texture.class);
        } else {
            texture = directory.getEntry("platform-totemLIGHTLEFT", Texture.class);
        }
        setTexture(texture);
        stop();
        resetFrameCount();
        resetFrameIndex();
    }

    @Override
    public void out_of_light(){
//        System.out.println("OUTOFLIGHT");
        Texture texture;
        if (isFacingRight()) {
            texture = directory.getEntry("platform-totemDARKRIGHT", Texture.class);
        } else {
            texture = directory.getEntry("platform-totemDARKLEFT", Texture.class);
        }
        setTexture(texture);
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        move();
        resetFrameCount();
        resetFrameIndex();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (getState() == EnemyState.CD) {
            float drawX = obstacle.getX() - getWidth() / 2f;
            float drawY = obstacle.getY() - getHeight() / 2f;
            Texture cdTexture = directory.getEntry("platform-totemLIGHTANIMATION", Texture.class);
            int srcIndex = frameIndex * FRAME_SIZE;
            System.out.println(srcIndex);
            batch.draw(cdTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits(), srcIndex, 0, FRAME_SIZE, FRAME_SIZE, isFacingRight(), false);
        } else {
            super.draw(batch);
        }
    }

    @Override
    public void cd() {
        cdFrameCount++;
        frameIndex = (cdFrameCount / FRAME_DURATION) % TOTAL_FRAMES;
        if (cdFrameCount >= FRAME_DURATION * TOTAL_FRAMES) {
            cdFrameCount = 0;
        }
        decrementFreezeTimer();
        if (getFreezeTimer() <= 0) {
            setState(EnemyState.OUT_OF_LIGHT);
            resetFrameCount();
            resetFrameIndex();
        }
    }


}
