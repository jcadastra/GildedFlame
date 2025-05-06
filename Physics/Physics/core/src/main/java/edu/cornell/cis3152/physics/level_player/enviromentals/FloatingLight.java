package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;

public class FloatingLight extends ObstacleSprite {

    private enum FloatingLightState {
        CIRCULATING,
        TRAVELING,
        OFF
    }
        private static int counter = 0;
        public int ID;
        private FloatingLightState state;
        private Vector2 centerPoint;     // For circulating
        private Vector2 position;
        private Vector2 destination;
        private Vector2 start;// For traveling
        private float radius;            // Orbit radius
        private float angle;             // Current angle around center
        private float speed;             // Speed of circulation/travel
        private final float travelThreshold = 0.1f;
        //WheelObstacle obstacle;
        private ShapeRenderer shapeRenderer = new ShapeRenderer();
        private boolean debug = true;

        int circleCounter = 500;
        int offCounter = 500;

        public FloatingLight(Float units,Vector2 start, float radius, Vector2 end) {
            this.position = new Vector2(start);
            this.centerPoint = new Vector2(start); // Initially circulate around own position
            this.start = new Vector2(start);
            this.radius = radius;
            this.angle = 0f;
            this.speed = 2;
            this.state = FloatingLightState.CIRCULATING;
            destination = end;
            obstacle = new WheelObstacle(position.x,position.y,1);
            obstacle.setPosition(position.x, position.y);
            obstacle.setDensity(0.0001f); // Set appropriate physical properties
            obstacle.setMass(0.001f);
            obstacle.setInertia(0.0001f);
            obstacle.setRestitution(0f); // If necessary
            obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
            obstacle.setGravityScale(0); // If you don't want gravity to affect it
            obstacle.setName("floatingLight");
            obstacle.setUserData(this);
            obstacle.setPhysicsUnits(units);
            obstacle.setSensor(true);
            this.ID = counter;
            counter++;
        }

        public void update(float deltaTime) {
            if (debug) {

            } else {
                switch (state) {
                    case CIRCULATING:
                        circleCounter--;
                        angle += speed * deltaTime;
                        position.x = centerPoint.x + radius * (float) Math.cos(angle);
                        position.y = centerPoint.y + radius * (float) Math.sin(angle);
                        if (circleCounter < 0) {
                            state = FloatingLightState.TRAVELING;
                        }
                        break;

                    case TRAVELING:
                        circleCounter = 1000;
                        offCounter = 1000;
                        Vector2 direction = destination.cpy().sub(position);
                        float distance = direction.len();
                        if (distance < travelThreshold) {
                            position = new Vector2(obstacle.getX(), obstacle.getY());
                            destination = start.cpy();
                            start = position.cpy();
                            state = FloatingLightState.OFF;
                        } else {
                            direction.nor().scl(speed * deltaTime);
                            position.add(direction);
                        }
//                    if (position.dst2(destination) < 0.01f) {//stop moving
//                        position = obstacle.getPosition();
//                        destination = start.cpy();
//                        start = position.cpy();
//                        state = FloatingLightState.OFF;
//                    }
                        break;

                    case OFF:
                        // Do nothing or flicker/dim if desired
                        offCounter--;
                        if (offCounter < 0) {
                            state = FloatingLightState.CIRCULATING;
                        }
                        break;
                }

                obstacle.setPosition(position.x, position.y); // Update visual

            }
        }

        public void setTravelDestination(Vector2 dest) {
            this.destination = dest;
            this.state = FloatingLightState.TRAVELING;
        }

        public void stopMoving() {
            this.state = FloatingLightState.OFF;
        }

        public void startCirculating(Vector2 center, float radius) {
            this.centerPoint = center;
            this.radius = radius;
            this.state = FloatingLightState.CIRCULATING;
        }

        public void render(){
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(position.x, position.y, 1f); // small white dot at center
            shapeRenderer.end();
        }
        public boolean isCirculating() {return state == FloatingLightState.CIRCULATING;}
        public boolean isTraveling() {return state == FloatingLightState.TRAVELING;}
        public boolean isOff() {return state == FloatingLightState.OFF;}
    public void reset(){ counter = 0; }
}


