package com.example.myrlenvironment.view.utils;

import android.util.Log;

import com.google.android.filament.VertexBuffer;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Vertex;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Util {
    // Define how many floats represent one point (x, y, z, confidence)
    private static final int FLOATS_PER_POINT = 4;

    // Method to convert ARCore vertices/normals to Sceneform VertexBuffer
    public static Vertex[] createVertexList(FloatBuffer verticesBuffer) {
        int vertexCount = verticesBuffer.limit() / 3;  // Each vertex has x, y, z coordinates
        Vertex[] vertices = new Vertex[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            float x = verticesBuffer.get(i * 3);
            float y = verticesBuffer.get(i * 3 + 1);
            float z = verticesBuffer.get(i * 3 + 2);

            // Create Vertex with position and normal
            vertices[i] = Vertex.builder()
                    .setPosition(new Vector3(x, y, z))
                    .build();
        }
        return vertices;
    }

    /**
     * Converts a FloatBuffer containing 3D point data (with FLOATS_PER_POINT = 4) into a list of Vector3.
     *
     * @param floatBuffer The FloatBuffer containing point data.
     * @return A list of Vector3 points.
     */
    public static List<Vector3> convertFloatBufferToVector3List(FloatBuffer floatBuffer) {
        List<Vector3> vector3List = new ArrayList<>();

        // Reset the buffer position to the beginning, if necessary
        floatBuffer.rewind();

        // Iterate through the buffer, reading every 4 floats (x, y, z, confidence)
        while (floatBuffer.hasRemaining()) {
            float x = floatBuffer.get();  // Get X coordinate
            float y = floatBuffer.get();  // Get Y coordinate
            float z = floatBuffer.get();  // Get Z coordinate
            floatBuffer.get();            // Skip the confidence/intensity value (4th float)

            // Create a new Vector3 and add it to the list
            vector3List.add(new Vector3(x, y, z));
        }
        Log.d("Util", "convertFloatBufferToVector3List: " + vector3List.size() + " points");

        return vector3List;
    }
}
