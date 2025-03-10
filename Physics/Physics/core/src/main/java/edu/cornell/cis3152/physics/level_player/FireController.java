package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.gdiac.graphics.SpriteMesh;
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


public class FireController {

    /** Map that stores the fire point diagrams of calculated flammable bodies */
    private HashMap<ObstacleSprite, Vector2[]> nFireDiagrams;
    private HashMap<ObstacleSprite, ArrayList<Fire>> firesOnShape;
    private Stack<Object[]> fireFlags;
    public Stack<Object[]> getFireFlags() {
        return fireFlags;
    }
    private EarClippingTriangulator cutter;
    private Random rand;

    public boolean isBodyOnFire(ObstacleSprite s) {
        return nFireDiagrams.getOrDefault(s, null) != null;
    }

    /**
     * Controls the fires...
     *
     * but you can't control fire 🤔
     */
    public FireController() {
        nFireDiagrams = new HashMap<>();
        firesOnShape = new HashMap<>();
        fireFlags = new Stack<>();
        cutter = new EarClippingTriangulator();
        rand = new Random();
    }

    public void update() {
        for (ObstacleSprite s : nFireDiagrams.keySet()) {
//            System.out.println("art thou burnth?" + testIfFullyBurnt(s));
//            System.out.println(firesOnShape.get(s).size());
//            System.out.println("NOW EVALUATING " + s.getName());
            if (testIfFullyBurnt(s)) {
//                if (s.burnTimer == 0) {
//                    s.dispose();
//                } else {
//                    s.setBurnTimer(120);
//                    TODO: ??????? put in json/base off of material
//                }
            } else {
                for (Vector2 p : findSuitableFirePoint(s)) {
//                    System.out.println("suitable point here" + p);
                    Fire f = new Fire(s.getObstacle().getPhysicsUnits(), p);
                    firesOnShape.get(s).add(f);
                    fireFlags.push(new Object[]{"attachFire", s, f});
//                    System.out.println("spreading fire");
                }
            }
        }
    }

    private Set<Vector2> findSuitableFirePoint(ObstacleSprite s) {
        Array<Vector2> preburnt = getPointsOnFire(s);
        Vector2[] reference = nFireDiagrams.get(s);
        ArrayList<Vector2> openSpots = new ArrayList<>();
        ArrayList<Vector2> closedSpots = new ArrayList<>();

        for (Vector2 v : reference) {
            if (!preburnt.contains(v, true)) {
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
                float distance = Math.abs(openSpot.x - closedSpot.x) + Math.abs(openSpot.y - closedSpot.y);
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


    private Array<Vector2> getPointsOnFire(ObstacleSprite s) {
        Array<Vector2> returnArray = new Array<Vector2>();
        Vector2[] subject = nFireDiagrams.get(s);
        int totalVerticesToCount = subject.length;
        for (int i = 0; i < totalVerticesToCount; i++) {
            for (Fire f : firesOnShape.get(s)) {
//                System.out.println("centroid shit  " + f.getObstacle().getPosition());
                if (f.queryPointInside(subject[i])) {
                    returnArray.add(subject[i]);
                }
            }
        }
        return returnArray;
    }

    private boolean testIfFullyBurnt (ObstacleSprite s) {
//        System.out.println("total n points for " + s.getName() +":"+ nFireDiagrams.get(s).length);
//        System.out.println("fire points for " + s.getName() +":"+ getPointsOnFire(s).size);
        return getPointsOnFire(s).size == nFireDiagrams.get(s).length;
    }

    /**
     * Generates a fire at the passed in point p
     *
     * @param s     the body, not on fire, contacted by the fire
     * @param point the location to start the fire
     */
    public void lightAnew (ObstacleSprite s, Vector2 point) {
        genFirePinPoints(s, point);
        Fire f = new Fire(s.getObstacle().getPhysicsUnits(), point);
        firesOnShape.computeIfAbsent(s, k -> new ArrayList<>()).add(f);
        fireFlags.push(new Object[]{"attachFire", s, f});
        f.getObstacle().setBodyType( BodyType.StaticBody );
//        System.out.println("running anew");
    }

    /**
     * Aglorithim for divying up a given polygon body and assigning points to it
     * These points will be used to ensure that the body s fully covered in fire and will burn
     * TODO: double check to ensure they are points relative to the body as opposed to space
     *          Pretty sure they are global cords
     *
     * @param b the ObstactleSprite that will be partiionted into
     * @param ignitionPoint the Vertex of contact to ensure that it keeps burning
     */
    private void genFirePinPoints(ObstacleSprite b, Vector2 ignitionPoint) {
//        System.out.println("GENERATED POINTS FOR " + b.getName());
        FloatArray vertices = b.getMesh().vertices;
        SpriteMesh mesh = b.getMesh();
        // position x position y, color, texture cords x texture cords y
        FloatArray releventVertecies = new FloatArray();
        for (int i = 0; i < mesh.vertexCount(); i++) {
            releventVertecies.add(mesh.getPositionX(i));
            releventVertecies.add(mesh.getPositionY(i));
        }
        ShortArray triangleIndices = cutter.computeTriangles(releventVertecies);
//        System.out.println(b.getMesh());
//        System.out.println("elephant ->" + releventVertecies);
//        System.out.println(vertices);
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
        int num_of_points = Math.round(totalArea / (15 * b.getObstacle().getPhysicsUnits()));
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
            returnArray.add((new Vector2 ( firePointsList.get(i), firePointsList.get(i+1))));
        }

        for (Vector2 v : returnArray) {
            v.scl((float) Math.pow(b.getObstacle().getPhysicsUnits(), -1));
        }
        returnArray.add(ignitionPoint);
        nFireDiagrams.put(b, (returnArray).toArray(Vector2.class));

        for (Vector2 f : returnArray.toArray(Vector2.class)) {
//            System.out.println("a generated point" + f);
        }
    }
}
