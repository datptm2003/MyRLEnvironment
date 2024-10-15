package com.example.myrlenvironment;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.ArSceneView;

public class MainActivity extends AppCompatActivity {

    private ArSceneView arSceneView;
    private Session arSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arSceneView = findViewById(R.id.ar_scene_view);

        // Check ARCore availability
        try {
            switch (ArCoreApk.getInstance().checkAvailability(this)) {
                case SUPPORTED_INSTALLED:
                    // Create AR session if supported and installed
                    arSession = new Session(this);
                    break;
                case SUPPORTED_APK_TOO_OLD:
                case SUPPORTED_NOT_INSTALLED:
                    // Request installation of ARCore
                    ArCoreApk.getInstance().requestInstall(this, true);
                    break;
                case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                    // Handle unsupported device case
                    finish();
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the AR session
        if (arSession != null) {
            arSceneView.setupSession(arSession);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSession != null) {
            try {
                arSceneView.resume();
            } catch (CameraNotAvailableException e) {
                Log.d("ARCamera", "Camera not available");
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
