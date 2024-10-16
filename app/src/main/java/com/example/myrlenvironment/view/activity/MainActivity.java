package com.example.myrlenvironment.view.activity;

import com.example.myrlenvironment.model.geometry.DepthData;
import com.example.myrlenvironment.model.geometry.Triangle;
import com.example.myrlenvironment.view.renderer.Renderer;
import com.example.myrlenvironment.view.utils.Util;

import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myrlenvironment.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

        Config arConfig = arSession.getConfig();
        arConfig.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        arConfig.setGeospatialMode(Config.GeospatialMode.ENABLED);
        if (arSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            // Enable depth mode.
            Log.d("AR", "Depth mode is supported.");
            arConfig.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
//        arConfig.setStreetscapeGeometryMode(Config.StreetscapeGeometryMode.ENABLED);
        arSession.configure(arConfig);

        // Set up the AR session
        if (arSession != null) {
            arSceneView.setupSession(arSession);
        }
        arSceneView.getScene().addOnUpdateListener(this::onUpdateFrame);

//        testRenderable();
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

    private void testRenderable(float radius, Vector3 pos) {
        Node redSphereNode = new Node();
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                .thenAccept(
                        material -> {
                            redSphereNode.setRenderable(
                                    ShapeFactory.makeSphere(radius, pos, material)
                            );
                        });
        arSceneView.getScene().addChild(redSphereNode);
    }

    public void onUpdateFrame(FrameTime frameTime) {
        if (arSceneView == null || arSession == null) {
            return;
        }

        Frame frame;
        try {
            frame = arSession.update();
        } catch (CameraNotAvailableException e) {
            throw new RuntimeException(e);
        }

        // Process the point cloud data
        if (frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            FloatBuffer points = DepthData.create(frame, arSession.createAnchor(frame.getCamera().getPose()));
            if (points == null) {
                return;
            }
            List<Vector3> pointList = Util.convertFloatBufferToVector3List(points);
            for (Vector3 point : pointList) {
                // TODO: Do something here
//                testRenderable(0.001f, point);
            }
        }
    }

    // Function to check and request permission
    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
    }
}
