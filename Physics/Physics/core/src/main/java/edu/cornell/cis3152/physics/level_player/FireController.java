package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.gdiac.physics2.Obstacle;
import java.util.HashMap;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;


public class FireController {

    /** Map that stores the fire point diagrams of calculated flammable bodies */
    private HashMap<Surface, Vector2[]> nFireDiagrams;

    /**
     * Controls the fires...
     */
    public FireController() {
        nFireDiagrams = new HashMap<>();
    }

    public void update() {
        for (Surface s : nFireDiagrams.keySet()) {
//            if (testIfFullyBurnt(s)) {
//                if (s.burnTimer == 0) {
//                    s.dispose();
//                } else {
//                    s.setBurnTimer(120);
////                    TODO: ??????? put in json/base off of material
//                }
//            } else {
//                Vector2 p = findSuitableFirePoint(s);
//                // TODO: because of joint turn below  into flag v
//                s.addFire(new Fire(p));
//            }
        }
    }

    private Vector2 findSuitableFirePoint(Surface s) {
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

        float lowestDelta = Float.POSITIVE_INFINITY;
        Vector2 open = new Vector2();
        for (Vector2 o : openSpots) {
            for (Vector2 c : closedSpots) {
                float manhattan = Math.abs(o.x - c.x) + Math.abs(o.y - c.y);
                if (manhattan < lowestDelta) {
                    open = o;
                    lowestDelta = manhattan;
                }
            }
        }
        return open;
    }


    private Array<Vector2> getPointsOnFire(Surface s) {
        Array<Vector2> returnArray = new Array<Vector2>();
//        Float[] subject = nFireDiagrams.get(s);
//        int totalVerticesToCount = subject.length;
//        for (int i = 0; i < totalVerticesToCount; i += 2) {
//            for (Fire f : s.getFires()) {
//                if (f.getPolygon().contains(subject[i], subject[i+1])) {
//                    returnArray.add(new Vector2 ( subject[1], subject[1+1]));
//                }
//            }
//        }
        return returnArray;
    }

    private boolean testIfFullyBurnt (Surface s) {
        return getPointsOnFire(s).size * 2 == nFireDiagrams.get(s).length;
    }

    /**
     * Generates a fire at the passed in point p
     *
     * @param b the body, not on fire, contacted by the fire
     * @param point the location to start the fire
     * @return fire generated
     */
    private Fire lightAnew (Surface b, Vector2 point) {
        genFirePinPoints(b, 20);
        Fire f = new Fire(point);
//        b.addFire(f);
        // TODO: because of joint turn above into flag^
        return f;
    }

    /**
     * Aglorithim for divying up a given polygon body and assigning points to it
     * These points will be used to ensure that the body s fully covered in fire and will burn
     * TODO: double check to ensure they are points relative to the body as opposed to space
     *
     * @param b the ObstactleSprite that will be partiionted into
     * @param num_of_points the number of points within b
     */
    private void genFirePinPoints(Surface b, int num_of_points) {
        FloatArray vertices = b.getMesh().vertices;
        EarClippingTriangulator cutter = new EarClippingTriangulator();
        ShortArray triangleIndices = cutter.computeTriangles(vertices);

        int numTriangles = triangleIndices.size / 3;
        List<float[]> triangles = new ArrayList<>();
        float[] triangleAreas = new float[numTriangles];
        float totalArea = 0;

        for (int i = 0; i < triangleIndices.size; i += 3) {
            int idx0 = triangleIndices.get(i) * 2;
            int idx1 = triangleIndices.get(i + 1) * 2;
            int idx2 = triangleIndices.get(i + 2) * 2;
            float x1 = vertices.get(idx0);
            float y1 = vertices.get(idx0 + 1);
            float x2 = vertices.get(idx1);
            float y2 = vertices.get(idx1 + 1);
            float x3 = vertices.get(idx2);
            float y3 = vertices.get(idx2 + 1);

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
        Random rand = new Random();

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

        for (Float v : vertices.toArray()) {
            firePointsList.add(v);
        }


        Array<Vector2> returnArray = new Array<Vector2>();
        int totalVerticesToCount = firePointsList.size();
        for (int i = 0; i < totalVerticesToCount; i += 2) {
            returnArray.add(new Vector2 ( firePointsList.get(i), firePointsList.get(i+1)));
        }
        nFireDiagrams.put(b, returnArray.toArray());
    }
}
