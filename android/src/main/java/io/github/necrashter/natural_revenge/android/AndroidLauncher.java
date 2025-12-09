package io.github.necrashter.natural_revenge.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import io.github.necrashter.natural_revenge.Main;

/**
 * Launches the Android application with Mod Menu support.
 */
public class AndroidLauncher extends AndroidApplication {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;
    private boolean modMenuStarted = false;
    private boolean hasOverlayPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(new Main(null), configuration);

        // Check overlay permission
        checkOverlayPermission();
    }

    /**
     * Check if overlay permission is granted, request if not
     */
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Request permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                Toast.makeText(this, "Please grant overlay permission for Mod Menu",
                        Toast.LENGTH_LONG).show();
            } else {
                hasOverlayPermission = true;
                startModMenuService();
            }
        } else {
            // Pre-Marshmallow, permission is granted at install time
            hasOverlayPermission = true;
            startModMenuService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    hasOverlayPermission = true;
                    startModMenuService();
                } else {
                    Toast.makeText(this,
                            "Overlay permission denied. Mod Menu will not be available.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Start the floating mod menu service
     */
    private void startModMenuService() {
        if (!modMenuStarted && hasOverlayPermission) {
            Intent serviceIntent = new Intent(this, ModMenuService.class);
            startService(serviceIntent);
            modMenuStarted = true;
            Toast.makeText(this, "Mod Menu Started! Tap the M button.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop the floating mod menu service
     */
    private void stopModMenuService() {
        if (modMenuStarted) {
            Intent serviceIntent = new Intent(this, ModMenuService.class);
            stopService(serviceIntent);
            modMenuStarted = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart service when app comes back to foreground
        if (hasOverlayPermission && !modMenuStarted) {
            startModMenuService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop service when app goes to background
        stopModMenuService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Ensure service is stopped when activity is no longer visible
        stopModMenuService();
    }

    @Override
    protected void onDestroy() {
        // Stop the mod menu service when app is closed
        stopModMenuService();
        super.onDestroy();
    }
}
