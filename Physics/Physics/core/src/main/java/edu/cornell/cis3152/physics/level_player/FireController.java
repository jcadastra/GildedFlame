package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.gdiac.graphics.SpriteMesh;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.Arrays;
import java.util.HashMap;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;


public class FireController {

    /**
     * Map that stores the fire point diagrams of calculated flammable bodies
     */
    private HashMap<EnhancedObstacleSprite, Vector2[]> nFireDiagrams;
    private HashMap<EnhancedObstacleSprite, ArrayList<Fire>> firesOnShape;
    private Stack<Object[]> fireFlags;

    public Stack<Object[]> getFireFlags() {
        return fireFlags;
    }

    private EarClippingTriangulator cutter;
    private Random rand;

    public boolean isBodyOnFire(EnhancedObstacleSprite s) {
        return nFireDiagrams.getOrDefault(s, null) != null;
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
        cutter = new EarClippingTriangulator();
        rand = new Random();
    }

    /**
     * Controls the fires...
     * <p>
     * but you can't control fire 🤔
     */
    public FireController() {
        resetStorage();
    }

    public void update() {
        int sum = 0;
        for (EnhancedObstacleSprite object : firesOnShape.keySet()) {
            for (Fire fireList : firesOnShape.get(object)) {

            }
        }
        for (EnhancedObstacleSprite s : nFireDiagrams.keySet()) {
            if (s.getObstacle().isRemoved()) {
                continue;
            }
//            System.out.println("art thou burnth?" + testIfFullyBurnt(s));
//            System.out.println(firesOnShape.get(s).size());
//            System.out.println("NOW EVALUATING " + s.getName());
            if (testIfFullyBurnt(s)) {
                if (s.getMaterial().isExpiredBurnTimer()) {
                    fireFlags.push(new Object[]{"expireObj", s, firesOnShape.get(s)});
                } else {
                    s.getMaterial().incrementBurnTimer();
                }
            } else {
//                System.out.println("fires --> "+ Arrays.toString(firesOnShape.get(s).toArray()));
//                System.out.println("points --> "+ Arrays.toString(nFireDiagrams.get(s)));
                for (Vector2 p : findSuitableFirePoint(s)) {
                    if (rand.nextFloat() < s.getMaterial().getFlammability()) {
//                    System.out.println("suitable point here" + p);
                        Fire f = new Fire(s.getObstacle().getPhysicsUnits(), p.cpy());
                        firesOnShape.get(s).add(f);
                        fireFlags.push(new Object[]{"attachFire", s, f});
//                    System.out.println("spreading fire");
                    }
                }
            }
        }
    }

    private Set<Vector2> findSuitableFirePoint(EnhancedObstacleSprite s) {
        Set<Vector2> preburnt = getPointsOnFire(s);
//        System.out.println("points on fire -> " + getPointsOnFire(s));
        Vector2[] temp = nFireDiagrams.get(s);
        ArrayList<Vector2> reference = new ArrayList<Vector2>();
//        System.out.println("A"+Arrays.toString(temp));
        float th = s.getObstacle().getBody().getAngle();
        for (Vector2 v : temp) {
            Vector2 new_vector = v.cpy();
            new_vector.rotateRad(th);
            reference.add(new_vector.add(s.getObstacle().getPosition()));
        }
//        System.out.println("reference "+(reference));

        ArrayList<Vector2> openSpots = new ArrayList<>();
        ArrayList<Vector2> closedSpots = new ArrayList<>();

        for (Vector2 v : reference) {
            if (!preburnt.contains(v)) {
                openSpots.add(v);
            } else {
                closedSpots.add(v);
            }
        }
//
//        System.out.println("open" + openSpots);
//        System.out.println("closed" + closedSpots);

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
//        System.out.println("ignit"+toIgnite);
        return toIgnite;
    }


    private Set<Vector2> getPointsOnFire(EnhancedObstacleSprite s) {
        Set<Vector2> returnArray = new HashSet<>();
        Vector2[] temp = nFireDiagrams.get(s);
        ArrayList<Vector2> reference = new ArrayList<Vector2>();
//        System.out.println(Arrays.toString(nFireDiagrams.get(s)));
        float th = s.getObstacle().getBody().getAngle();
        for (Vector2 v : temp) {
            Vector2 new_vector = v.cpy();
            new_vector.rotateRad(th);
            reference.add(new_vector.add(s.getObstacle().getPosition()));
        }
        int totalVerticesToCount = reference.size();
        for (int i = 0; i < totalVerticesToCount; i++) {
            for (Fire f : firesOnShape.get(s)) {
//                System.out.println("centroid shit  " + f.getObstacle().getPosition());
                if (f.queryPointInside(reference.get(i))) {
                    returnArray.add(reference.get(i));
                }
            }
        }
        return returnArray;
    }

    public boolean testIfFullyBurnt(EnhancedObstacleSprite s) {
        if (!nFireDiagrams.containsKey(s)) {
            return false;
        }
//        System.out.println("total n points for " + s.getName() +":"+ nFireDiagrams.get(s).length);
//        System.out.println("fire points for " + s.getName() +":"+ getPointsOnFire(s).size);
//        System.out.println("temp" + Arrays.toString(nFireDiagrams.get(s)) + ", "+nFireDiagrams.get(s).length);
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
        ArrayList<Fire> currentFires = firesOnShape.get(s);
        if (currentFires != null) {
            for (Fire f : currentFires) {
                if (f.queryPointInside(point)) {
                    return;
                }
            }
        }

        if (!nFireDiagrams.containsKey(s)) {
            genFirePinPoints(s, point);
        }

        Fire f = new Fire(s.getObstacle().getPhysicsUnits(), point.cpy());
        firesOnShape.computeIfAbsent(s, k -> new ArrayList<>()).add(f);
//        System.out.println("new fire" + point);
        fireFlags.push(new Object[]{"attachFire", s, f});
//        System.out.println("running anew");
    }

    /**
     * Aglorithim for divying up a given polygon body and assigning points to it These points will
     * be used to ensure that the body s fully covered in fire and will burn
     * TODO: double check to ensure they are points relative to the body as opposed to space
     *          Pretty sure they are global cords
     *
     * @param b             the ObstactleSprite that will be partiionted into
     * @param ignitionPoint the Vertex of contact to ensure that it keeps burning
     */
    private void genFirePinPoints(EnhancedObstacleSprite b, Vector2 ignitionPoint) {
//        System.out.println("GENERATED POINTS FOR " + b.getName());
        FloatArray vertices = b.getMesh().vertices;
        SpriteMesh mesh = b.getMesh();
        // position x position y, color, texture cords x texture cords y
        FloatArray releventVertecies = new FloatArray();
        for (int i = 0; i < mesh.vertexCount(); i++) {
            releventVertecies.add(mesh.getPositionX(i));
            releventVertecies.add(mesh.getPositionY(i));
        }
//        System.out.println("ignition here ->" +ignitionPoint);
        ShortArray triangleIndices = cutter.computeTriangles(releventVertecies);
//        System.out.println(b.getMesh());
        System.out.println("elephant ->" + releventVertecies);
//        System.out.println(releventVertecies);
//        System.out.println(vertices.get(12));
//        System.out.println(vertices.getClass());

        int numTriangles = triangleIndices.size / 3;
        List<float[]> triangles = new ArrayList<>();
//        System.out.println("triis" + triangleIndices);
        float[] triangleAreas = new float[numTriangles];
        float totalArea = 0;

        for (int i = 0; i < triangleIndices.size; i += 3) {
//            System.out.println("---------------------------------  TRIANGLE NUMBER " + i/3);
            int idx0 = triangleIndices.get(i) * 2;
            int idx1 = triangleIndices.get(i + 1) * 2;
            int idx2 = triangleIndices.get(i + 2) * 2;
//            System.out.println("idx0 " + idx0);
//            System.out.println("idx1 " + idx1);
//            System.out.println("idx2 " + idx2);
            float x1 = releventVertecies.get(idx0);
            float y1 = releventVertecies.get(idx0 + 1);
            float x2 = releventVertecies.get(idx1);
            float y2 = releventVertecies.get(idx1 + 1);
            float x3 = releventVertecies.get(idx2);
            float y3 = releventVertecies.get(idx2 + 1);
//            System.out.println("idx0x " + x1);
//            System.out.println("idx0y " + y1);
//            System.out.println("idx1x " + x2);
//            System.out.println("idx1y " + y2);
//            System.out.println("idx2x " + x3);
//            System.out.println("idx2y " + y3);

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

        // TODO: MAKE BELOW FROM JSON BUT FINE ATM vvvv
        int num_of_points = (int) Math.round(
            totalArea / (10 * Math.pow(1, 2) * Math.PI * b.getObstacle().getPhysicsUnits()));
//        num_of_points = 20;
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
//            System.out.println(("tri 0" + tri[0]));
//            System.out.println(("tri 1" + tri[1]));
//            System.out.println(("tri 2" + tri[2]));
//            System.out.println(("tri 3" + tri[3]));
//            System.out.println(("tri 4" + tri[4]));
//            System.out.println(("tri 5" + tri[5]));
//            System.out.println("firex" +fireX);
        }

        for (Float v : releventVertecies.toArray()) {
            firePointsList.add(v);
        }

        Array<Vector2> returnArray = new Array<Vector2>();
        int totalVerticesToCount = firePointsList.size();
        for (int i = 0; i < totalVerticesToCount; i += 2) {
            returnArray.add((new Vector2(firePointsList.get(i), firePointsList.get(i + 1))));
        }

//        System.out.println("pre"+returnArray);
//        returnArray.add(ignitionPoint);
        returnArray.add(ignitionPoint.cpy());
        for (Vector2 v : returnArray) {
            v.scl((float) Math.pow(b.getObstacle().getPhysicsUnits(), -1));
//            v.add(b.getObstacle().getPosition());
        }
//        System.out.println("post"+returnArray);
        nFireDiagrams.put(b, (returnArray).toArray(Vector2.class));

        for (Vector2 f : returnArray.toArray(Vector2.class)) {
//            System.out.println("a generated point" + f);
        }
    }

    public void cleanObj (EnhancedObstacleSprite s) {
        nFireDiagrams.remove(s);
        firesOnShape.remove(s);
    }
}
