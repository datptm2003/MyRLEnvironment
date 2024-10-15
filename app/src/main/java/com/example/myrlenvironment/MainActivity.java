package com.example.myrlenvironment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.StreetscapeGeometry;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int INTERNET_PERMISSION_CODE = 101;
    private static final int FINE_LOCATION_PERMISSION_CODE = 102;
    private static final int COARSE_LOCATION_PERMISSION_CODE = 103;

    private ArSceneView arSceneView;
    private Session arSession;

    private Handler handler;

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

        handler = new Handler(Looper.getMainLooper());
        // Post the runnable again after the specified interval
        Runnable captureStreetscapeRunnable = new Runnable() {
            @Override
            public void run() {
                Collection<StreetscapeGeometry> streetscapeGeometries = arSession.getAllTrackables(StreetscapeGeometry.class);;
                // Log the center point of the first StreetscapeGeometry mesh
                if (!streetscapeGeometries.isEmpty()) {
                    StreetscapeGeometry firstGeometry = streetscapeGeometries.iterator().next();
                    Pose firstPose = firstGeometry.getMeshPose();
                    Log.d("StreetscapeGeometry", "Center Pose: " + firstPose.toString());
                } else {
                    Log.d("StreetscapeGeometry", "No StreetscapeGeometry found");
                }
                // Post the runnable again after the specified interval
                handler.postDelayed(this, 1000);
            }
        };

        // Start the logging process
        handler.post(captureStreetscapeRunnable);

        // Set up the AR session
        if (arSession != null) {
            arSceneView.setupSession(arSession);
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
}
