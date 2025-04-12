package edu.cornell.cis3152.physics.level_player.player;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.cis3152.physics.level_player.utils.TrajectoryFlag;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import edu.cornell.gdiac.util.PooledList;
import java.util.Stack;

public class TorchTrajectorySystem {

    private PooledList<ObstacleSprite> trackers;
    private int internalTimer;
    private boolean active = false;
    private int index = 0;
    private int timeBetweenTracker = 10;
    private int timer = 0;
    private int maxTrackers = 15;
    private Stack<TrajectoryFlag> trajectoryFlags;

    public Stack<TrajectoryFlag> getTrajectoryFlags() {
        return trajectoryFlags;
    }

    public TorchTrajectorySystem() {
        this.trackers = new PooledList<>();
        this.trajectoryFlags = new Stack<>();
        this.internalTimer = 0;

        for (int i = 0; i < maxTrackers; i++) {
            trackers.add(null);
        }
    }

    public void toggle() {
        active = !active;
        if (!active) {
            reset();
        }
    }

    public void reset() {
        for (int i = 0; i < trackers.size(); i++) {
            ObstacleSprite sprite = trackers.get(i);
            if (sprite != null) {
                sprite.getObstacle().markRemoved(true);
            }
        }

        trackers.clear();
        for (int i = 0; i < maxTrackers; i++) {
            trackers.add(null);
        }
        index = 0;
    }

    public void update(Vector2 pos) {
        if (!active || timer > 0) {
            if (timer > 0) {
                timer--;
            }
            return;
        }

        WheelObstacle temp = new WheelObstacle(pos.x, pos.y, 0.3f);
        ObstacleSprite tracker = new ObstacleSprite(temp);
        tracker.getObstacle().setName("trackerBall");
        tracker.getObstacle().setSensor(true);

        System.out.println(index%maxTrackers);
        int activeIndex = index++ % maxTrackers;

        ObstacleSprite oldTracker = trackers.get(activeIndex);
//        if (oldTracker != null) {
//            oldTracker.getObstacle().markRemoved(true);
//        }

        trackers.set(activeIndex, tracker);
        trajectoryFlags.add(new TrajectoryFlag("addSubject", tracker));

        timer = timeBetweenTracker;
    }
}
