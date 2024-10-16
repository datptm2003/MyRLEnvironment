package com.example.myrlenvironment.view.activity;

import com.example.myrlenvironment.view.utils.Util;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myrlenvironment.R;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.StreetscapeGeometry;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Vertex;
import com.google.ar.sceneform.ux.TransformableNode;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int INTERNET_PERMISSION_CODE = 101;
    private static final int FINE_LOCATION_PERMISSION_CODE = 102;
    private static final int COARSE_LOCATION_PERMISSION_CODE = 103;

    private ArSceneView arSceneView;
    private Session arSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission(android.Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        checkPermission(android.Manifest.permission.INTERNET, INTERNET_PERMISSION_CODE);
        checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION_CODE);
        checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_PERMISSION_CODE);

        setContentView(R.layout.activity_main);
        arSceneView = findViewById(R.id.ar_scene_view);
        try {
            arSession = new Session(this);
        } catch (UnavailableArcoreNotInstalledException | UnavailableApkTooOldException |
                 UnavailableSdkTooOldException | UnavailableDeviceNotCompatibleException e) {
            throw new RuntimeException(e);
        }

        Config arConfig = new Config(arSession);
        arConfig.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        arConfig.setGeospatialMode(Config.GeospatialMode.ENABLED);
        arConfig.setStreetscapeGeometryMode(Config.StreetscapeGeometryMode.ENABLED);
        arSession.configure(arConfig);

        // Set up the AR session
        if (arSession != null) {
            arSceneView.setupSession(arSession);
        }

        Handler handler = new Handler(Looper.getMainLooper());
        // Post the runnable again after the specified interval
        Runnable captureSceneRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO: Capture the scene and display the mesh on screen
                if (arSession != null) {
                    // Retrieve Streetscape Geometry (surface mesh data)
                    Collection<StreetscapeGeometry> geometries = arSession.getAllTrackables(StreetscapeGeometry.class);

                    // Iterate through each StreetscapeGeometry and handle the surface mesh data
                    for (StreetscapeGeometry geometry : geometries) {
                        // Get the pose of the geometry in the world
                        Pose pose = geometry.getMeshPose();

                        // Retrieve the mesh data from StreetscapeGeometry
                        FloatBuffer verticesBuffer = geometry.getMesh().getVertexList();
                        IntBuffer indicesBuffer = geometry.getMesh().getIndexList();

                        // Create a custom renderable with the ARCore mesh
                        createMeshRenderable(verticesBuffer, indicesBuffer, pose);
                    }

                    // Schedule the next frame capture
                    handler.postDelayed(this, 500);
                }
            }
        };

        // Start the logging process
        handler.post(captureSceneRunnable);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSession != null) {
            try {
                arSceneView.resume();
            } catch (CameraNotAvailableException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    // Function to check and request permission
    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
        if (arSession != null) {
            arSession.close();
            arSession = null;
        }
    }

    // Method to create and render the mesh as a Sceneform renderable
    protected void createMeshRenderable(FloatBuffer verticesBuffer, IntBuffer indicesBuffer, Pose pose) {
        // Convert vertices and normals into Sceneform's Vertex format
        Vertex[] vertices = Util.createVertexList(verticesBuffer);
//        IndexBuffer indexBuffer = new IndexBuffer(indicesBuffer);

        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
                .setVertices(Arrays.asList(vertices))
                .build();

        // Create the Renderable
        ModelRenderable.builder()
                .setSource(renderableDefinition)
                .build()
                .thenAccept(renderable -> {
                    // Position the renderable based on the ARCore geometry pose
                    addRenderableToScene(renderable, pose);
                });
    }

    // Method to add the Renderable to the AR scene at the correct location
    private void addRenderableToScene(Renderable renderable, Pose pose) {
        // Create a new Node to represent the mesh
        Node node = new Node();

        // Set the node's position and rotation from the ARCore pose
        node.setWorldPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
        node.setWorldRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));

        // Attach the renderable to the node
        node.setRenderable(renderable);

        // Add the node to the scene
        arSceneView.getScene().addChild(node);
    }

    private void testRenderable() {
        Node redSphereNode = new Node();
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            redSphereNode.setRenderable(
                                    ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.0f, -1f), material)
                            );
                        });
        arSceneView.getScene().addChild(redSphereNode);
    }
}
