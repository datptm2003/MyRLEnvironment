package com.example.myrlenvironment.view.renderer;

import android.util.Log;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.Vertex;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to render points from point cloud in the AR scene.
 */
public class Renderer {

    public static RenderableDefinition createRenderableFromPointCloud(FloatBuffer pointCloudBuffer) {
        List<Vertex> vertices = new ArrayList<>();

        // Parse the point cloud buffer
        while (pointCloudBuffer.hasRemaining()) {
            float x = pointCloudBuffer.get();
            float y = pointCloudBuffer.get();
            float z = pointCloudBuffer.get();
            float confidence = pointCloudBuffer.get();  // Ignored for now

            // Add each point as a vertex (x, y, z)
            vertices.add(Vertex.builder()
                    .setPosition(new Vector3(x, y, z))
                    .build());
        }

//        Log.d("createRenderableFromPointCloud", "Vertices: " + vertices.size());

        return RenderableDefinition.builder()
                .setVertices(vertices)
                .build();
    }
}
