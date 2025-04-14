package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.utils.FireFlag;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteMesh;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.HashMap;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;


public class FireController {

    /**
     * Map that stores the fire point diagrams of calculated flammable bodies
     * ie a bunch of random points on a shape that are used to determine how on fire someting is
     */
    private HashMap<EnhancedObstacleSprite, Vector2[]> nFireDiagrams;
    private HashMap<EnhancedObstacleSprite, ArrayList<Fire>> firesOnShape;
    private Set<Fire> allFires;
    private Stack<FireFlag> fireFlags;
    private Set<Smoke> allSmoke;
    private AssetDirectory assetDirectory;

    public Stack<FireFlag> getFireFlags() {
        return fireFlags;
    }

    private EarClippingTriangulator cutter;
    private Random rand;
    private int fireID = 1;

    public boolean isBodyOnFire(EnhancedObstacleSprite s) {
        return nFireDiagrams.getOrDefault(s, null) != null;
    }

    public Set<Fire> getAllFires() {
        return allFires;
    }

    public void forceAddMiscFire(Fire f) {
        allFires.add(f);
    }
    public void forceAddTorchFire(Fire f, Torch s, Vector2 point) {
        if (!nFireDiagrams.containsKey(s)) {
            genFirePinPoints(s, point);
        }

        firesOnShape.computeIfAbsent(s, k -> new ArrayList<>()).add(f);
    }

    public Set<Fire> getLitFires() {
        Set<Fire> returnSet = new HashSet<>();
        for (EnhancedObstacleSprite obj : nFireDiagrams.keySet()) {
            returnSet.addAll(firesOnShape.get(obj));
        }
        return returnSet;
    }

    public void resetStorage() {
        nFireDiagrams = new HashMap<>();
        firesOnShape = new HashMap<>();
        fireFlags = new Stack<>();
        allFires = new HashSet<>();
        allSmoke = new HashSet<>();
        cutter = new EarClippingTriangulator();
        rand = new Random();
    }

    /**
     * Controls the fires...
     * <p>
     * but you can't control fire 🤔
     */
    public FireController(AssetDirectory directory) {
        resetStorage(); this.assetDirectory = directory;
    }

    /**
     * runs comparisons on all the values and dspreads fire/marks objects for destruction as needed/
     * creates Smoke
     */
    public void update() {
        for (EnhancedObstacleSprite object : firesOnShape.keySet()) {
            ObstacleMaterial obstacleMaterial = object.getMaterial();
                for (Fire fire : firesOnShape.get(object)) {
                    if (!fire.getObstacle().isRemoved()) {
                        if (obstacleMaterial.makesSmoke()) {
                            if (obstacleMaterial.checkSmokeTime(fire.getTimeToSmoke())) {
                                spawnSmoke(fire);
                                fire.resetTimeToSmoke();
                            } else {
                                fire.incrementTimeToSmoke(rand.nextInt(-1, 3));
                            }
                        }

                        if (fire.getInRain()) {
                            fire.modifStrength(-.01f);
                        } else if (fire.getStrength() < 1) {
                            fire.modifStrength(.02f);
                        }
                        System.out.println(
                            fire.getStrength() + ", " + fire.fireID + ", " + object.getName());

                        if (fire.getStrength() <= .01) {
                            fireFlags.add(new FireFlag("killFire", fire));
                        }
                    }
            }
        }

        updateSmoke();

        // for each obnject that exists and has been given an assoicated diagram
        for (EnhancedObstacleSprite s : nFireDiagrams.keySet()) {
            if (s.getObstacle().isRemoved()) {
                continue;
            }
            if (testIfFullyBurning(s)) {
                if (s.getMaterial().isExpiredBurnTimer()) {
                    fireFlags.push(new FireFlag("expireObj", s, firesOnShape.get(s)));
                } else {
                    s.getMaterial().incrementBurnTimer();
                }
            } else {
                // calculate new points to be lit and place there
                for (Vector2 p : findSuitableFirePoint(s)) {
                    if (rand.nextFloat() < s.getMaterial().getFlammability()) {
                        Fire f = new Fire(s.getObstacle().getPhysicsUnits(), p.cpy());
                        firesOnShape.get(s).add(f);
                        fireFlags.push(new FireFlag("attachFire", s, f));
                    }
                }
            }
        }
    }

    private void updateSmoke() {
        for (Smoke smoke : allSmoke) {
            Obstacle smokeObstacle = smoke.getObstacle();
            float currentVX = smokeObstacle.getVX();
            float newVX = (float) (currentVX + Math.signum(currentVX) * -.01);
            if (Math.signum(newVX) != Math.signum(currentVX)) {
                smokeObstacle.setVX(0);
            } else {
                smokeObstacle.setVX(newVX);
            }

            smoke.updateLifeSpan();
        }
    }

    /**
     * rotates and transforms sthe associated fire diagram of s to s's current location/rotation
      * @param s
     * @return
     */
    private ArrayList<Vector2> rotateAndTransformDiagram(EnhancedObstacleSprite s) {
        Vector2[] temp = nFireDiagrams.get(s);
        ArrayList<Vector2> reference = new ArrayList<Vector2>();
        float theta = s.getObstacle().getBody().getAngle();
        for (Vector2 v : temp) {
            Vector2 new_vector = v.cpy();
            new_vector.rotateRad(theta);
            reference.add(new_vector.add(s.getObstacle().getPosition()));
        }
        return reference;
    }

    /**
     * for a given EOS s, takes the nFireDiagram and transofrms it to the current rotation and position
     * them it gets all the points within the diagram that are less than the spread threshold and returns
     * all valid candidates
     * @param s
     * @return
     */
    private Set<Vector2> findSuitableFirePoint(EnhancedObstacleSprite s) {
        Set<Vector2> preburnt = getPointsOnFire(s);

        ArrayList<Vector2> openSpots = new ArrayList<>();
        ArrayList<Vector2> closedSpots = new ArrayList<>();

        ArrayList<Vector2> reference = rotateAndTransformDiagram(s);

        for (Vector2 v : reference) {
            if (!preburnt.contains(v)) {
                openSpots.add(v);
            } else {
                closedSpots.add(v);
            }
        }
        float spreadThreshold = s.getObstacle().getPhysicsUnits() / 2f;
        Set<Vector2> toIgnite = new HashSet<>();
        for (Vector2 closedSpot : closedSpots) {
            Vector2 candidate = null;
            float minDistance = spreadThreshold;
            for (Vector2 openSpot : openSpots) {
                float distance =
                    Math.abs(openSpot.x - closedSpot.x) + Math.abs(openSpot.y - closedSpot.y);
                if (distance < minDistance) {
                    candidate = openSpot;
                    minDistance = distance;
                }
            }
            if (candidate != null) {
                toIgnite.add(candidate);
            }
        }
        return toIgnite;
    }

    /**
     * gets the points within an nFireDiagram and returns all points that are overlapping a fire
     * @param s
     * @return
     */
    private Set<Vector2> getPointsOnFire(EnhancedObstacleSprite s) {
        Set<Vector2> returnArray = new HashSet<>();
        ArrayList<Vector2> reference = rotateAndTransformDiagram(s);

        int totalVerticesToCount = reference.size();
        for (int i = 0; i < totalVerticesToCount; i++) {
            for (Fire f : firesOnShape.get(s)) {
                if (f.queryPointInside(reference.get(i))) {
                    returnArray.add(reference.get(i));
                }
            }
        }
        return returnArray;
    }

    /**
     * returns true iff all points within an nFireDiagram are inside of a fire, ie the shape is
     * fully on fire
     * @param s
     * @return
     */
    public boolean testIfFullyBurning(EnhancedObstacleSprite s) {
        if (!nFireDiagrams.containsKey(s)) {
            return false;
        }
        return getPointsOnFire(s).size() == nFireDiagrams.get(s).length;
    }

    /**
     * Generates a fire at the passed in point p
     *
     * @param s     the body, not on fire, contacted by the fire
     * @param point the location to start the fire
     */
    public void lightAnew(EnhancedObstacleSprite s, Vector2 point) {
        if (s.getObstacle().isRemoved()) {
            return;
        }
        if (s.getClass().isInstance(Torch.class)) {
            return;
        }

        Fire f = new Fire(s.getObstacle().getPhysicsUnits(), point.cpy());
        f.setID(fireID);
        fireID++;

        ArrayList<Fire> currentFires = firesOnShape.get(s);
        if (currentFires != null) {
            for (Fire fire : currentFires) {
                if (fire.queryPointInside(point)) {
                    return;
                }
            }
        }

        if (!nFireDiagrams.containsKey(s)) {
            genFirePinPoints(s, point);
        }

        firesOnShape.computeIfAbsent(s, k -> new ArrayList<>()).add(f);
        fireFlags.push(new FireFlag("attachFire", s, f));
        allFires.add(f);
    }

    /**
     * Aglorithim for divying up a given polygon body and assigning points to it These points will
     * be used to ensure that the body s fully covered in fire and will burn
     *
     * @param b             the ObstactleSprite that will be partiionted into
     * @param ignitionPoint the Vertex of contact to ensure that it keeps burning
     */
    private void genFirePinPoints(EnhancedObstacleSprite b, Vector2 ignitionPoint) {
        SpriteMesh mesh = b.getMesh();
        FloatArray releventVertecies = new FloatArray();
        for (int i = 0; i < mesh.vertexCount(); i++) {
            releventVertecies.add(mesh.getPositionX(i));
            releventVertecies.add(mesh.getPositionY(i));
        }
        ShortArray triangleIndices = cutter.computeTriangles(releventVertecies);

        int numTriangles = triangleIndices.size / 3;
        List<float[]> triangles = new ArrayList<>();
        float[] triangleAreas = new float[numTriangles];
        float totalArea = 0;

        for (int i = 0; i < triangleIndices.size; i += 3) {
            int idx0 = triangleIndices.get(i) * 2;
            int idx1 = triangleIndices.get(i + 1) * 2;
            int idx2 = triangleIndices.get(i + 2) * 2;
            float x1 = releventVertecies.get(idx0);
            float y1 = releventVertecies.get(idx0 + 1);
            float x2 = releventVertecies.get(idx1);
            float y2 = releventVertecies.get(idx1 + 1);
            float x3 = releventVertecies.get(idx2);
            float y3 = releventVertecies.get(idx2 + 1);

            triangles.add(new float[]{x1, y1, x2, y2, x3, y3});
            float area = Math.abs(x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2f;
            triangleAreas[i / 3] = area;
            totalArea += area;
        }

        float[] cumulativeAreas = new float[numTriangles];
        float cumulative = 0;
        for (int i = 0; i < numTriangles; i++) {
            cumulative += triangleAreas[i];
            cumulativeAreas[i] = cumulative;
        }

        List<Float> firePointsList = new ArrayList<>();

        int num_of_points = (int) Math.round(
            totalArea / (10 * Math.pow(1, 2) * Math.PI * b.getObstacle().getPhysicsUnits()));
        for (int i = 0; i < num_of_points; i++) {
            float rArea = rand.nextFloat() * totalArea;
            int selectedTriangleIndex = 0;
            for (int j = 0; j < numTriangles; j++) {
                if (rArea <= cumulativeAreas[j]) {
                    selectedTriangleIndex = j;
                    break;
                }
            }
            float[] tri = triangles.get(selectedTriangleIndex);
            float r1 = rand.nextFloat();
            float r2 = rand.nextFloat();
            if (r1 + r2 > 1) {
                r1 = 1 - r1;
                r2 = 1 - r2;
            }
            float fireX = tri[0] + r1 * (tri[2] - tri[0]) + r2 * (tri[4] - tri[0]);
            float fireY = tri[1] + r1 * (tri[3] - tri[1]) + r2 * (tri[5] - tri[1]);

            firePointsList.add(fireX);
            firePointsList.add(fireY);
        }

        for (Float v : releventVertecies.toArray()) {
            firePointsList.add(v);
        }

        Array<Vector2> returnArray = new Array<Vector2>();
        int totalVerticesToCount = firePointsList.size();
        for (int i = 0; i < totalVerticesToCount; i += 2) {
            returnArray.add((new Vector2(firePointsList.get(i), firePointsList.get(i + 1))));
        }

        returnArray.add(ignitionPoint.cpy());
        for (Vector2 v : returnArray) {
            v.scl((float) Math.pow(b.getObstacle().getPhysicsUnits(), -1));
        }
        nFireDiagrams.put(b, (returnArray).toArray(Vector2.class));

        for (Vector2 f : returnArray.toArray(Vector2.class)) {
        }
    }

    public void spawnSmoke(Fire fire) {
        Smoke smoke = new Smoke(fire.getObstacle().getPosition().x, fire.getObstacle().getPosition().y + fire.getRadius()/1.5f,
            fire.getObstacle().getPhysicsUnits(), new Vector2((rand.nextFloat()-.5f) * 2,1f));
        smoke.getObstacle().setName("smoke");
        smoke.setSource(fire);
        ObstacleSprite smokeObj = new ObstacleSprite(smoke.getObstacle());
        smokeObj.setTexture(assetDirectory.getEntry("platform-flame-smoke", Texture.class));
        fireFlags.push(new FireFlag("spawnSmoke", fire, smokeObj));
        allSmoke.add(smoke);
    }

    /**
     * removes the object from any data structures that stores it to prevent double counting
     * @param s
     */
    public void cleanObj (EnhancedObstacleSprite s) {
        nFireDiagrams.remove(s);
        firesOnShape.remove(s);
    }
     public void cleanFire (Fire fire) {
        firesOnShape.forEach((enhancedObstacleSprite, fires) -> fires.remove(fire));
     }
}
