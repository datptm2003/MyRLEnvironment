package com.example.myrlenvironment.view.utils;

import com.google.android.filament.VertexBuffer;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Vertex;

import java.nio.FloatBuffer;

public class Util {
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
}
