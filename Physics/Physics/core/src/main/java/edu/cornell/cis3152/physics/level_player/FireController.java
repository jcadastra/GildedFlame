package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.FloatArray;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.HashMap;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FireController {

    /** Map that stores the fire point diagrams of calculated flammable bodies */
    private HashMap<Obstacle, Float[]> nFireDiagrams;

    /**
     * Controls the fires...
     */
    public FireController() {
        nFireDiagrams = new HashMap<>();
    }

    public void update() {

    }

    private void genFirePoints(ObstacleSprite b, int num_of_points) {
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

        Float[] firePoints = firePointsList.toArray(new Float[0]);
        nFireDiagrams.put(b.getObstacle(), firePoints);
    }
}
