package com.abrody.brightness;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings.System;
import android.view.WindowManager.LayoutParams;

public class BrightnessToggle extends Activity {
    private static final int MINIMUM_BACKLIGHT = 5;
    private static final int MAXIMUM_BACKLIGHT = 255;
    boolean toManual;
    
    // logging constants
    final static String TAG = "BrightnessToggle";

    private Handler handler = new Handler();
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        if (checkForPermissions()) {
            toggleAutoBrightness();

            if (toManual) {
                // Stall for a bit so the brightness change will actually take effect, then exit.
                handler.postDelayed(new Runnable() {
                    public void run() { finish(); }
                }, 300);
            } else {
                // Otherwise exit immediately.
                finish();
            }
        } else {
            Log.d(TAG, "exiting onCreate()");
            finish();
        }
    }

    final private int PERMISSIONS_REQUEST_ID = 42;

    private boolean checkForPermissions() {
        Log.d(TAG, "checkForPermissions()");

        /*
         * For some reason, WRITE_SETTINGS is handled via an entirely different method than the
         * normal "dangerous" settings. We have to start the ACTION_MANAGE_WRITE_SETTINGS activity
         * to ask the user to manually give us access.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                return true;
            } else {
                Log.d(TAG, "starting action_manage_write_settings activity");

                // start up settings to ask for permission
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                Toast.makeText(this, getString(R.string.need_perms), Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            return true;
        }
    }
    
    private void toggleAutoBrightness() {
        int mode;
        int manualBrightnessLevel, brightness;
        final ContentResolver resolver = getContentResolver();

        Log.v(TAG, "toggleAutoBrightness()");

        // Get the current brightness mode.
        mode = System.getInt(resolver, System.SCREEN_BRIGHTNESS_MODE, -1);
        
        // Figure out which mode to switch to and pop up a toast.
        toManual = false;
        if (mode == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            toManual = true;
            mode = System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            Log.i(TAG, "Switching to manual");
            Toast.makeText(this, getString(R.string.toast_off), Toast.LENGTH_SHORT).show();
        } else if (mode == System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            mode = System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Log.i(TAG, "Switching to automatic");
            Toast.makeText(this, getString(R.string.toast_on), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unknown mode: " + Integer.toString(mode), Toast.LENGTH_SHORT).show();
            throw new UnsupportedOperationException("Unknown value for SCREEN_BRIGHTNESS_MODE: " + mode);
        }
        
        // Set the new SCREEN_BRIGHTNESS_MODE
        System.putInt(resolver, System.SCREEN_BRIGHTNESS_MODE, mode);
        
        // Restore preferences: saved manual brightness
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        manualBrightnessLevel = settings.getInt("manualBrightnessLevel", -1);
        
        if (toManual) {
            if (manualBrightnessLevel < MINIMUM_BACKLIGHT) {
                Log.v(TAG, "truncating to minimum backlight");
                manualBrightnessLevel = MINIMUM_BACKLIGHT;
            }

            // If we're going to manual, restore the saved brightness level.
            Log.v(TAG, "Setting SCREEN_BRIGHTNESS to " + Integer.toString(manualBrightnessLevel));
            System.putInt(resolver, System.SCREEN_BRIGHTNESS, manualBrightnessLevel);

            // And set the brightness of the window manager
            LayoutParams attrs = getWindow().getAttributes();
            attrs.screenBrightness = (float) manualBrightnessLevel / MAXIMUM_BACKLIGHT;
            Log.v(TAG, "Setting current brightness to " + Float.toString(attrs.screenBrightness));

        } else {
            // If we're going to automatic, save current brightness level as default manual level.
            SharedPreferences.Editor editor = settings.edit();
            brightness = System.getInt(resolver, System.SCREEN_BRIGHTNESS, -1);

            if (brightness < MINIMUM_BACKLIGHT) {
                Log.v(TAG, "truncating to minimum backlight before saving");
                brightness = MINIMUM_BACKLIGHT;
            }

            Log.v(TAG, String.format("Saving manual brightness level as %d", brightness));
            editor.putInt("manualBrightnessLevel", brightness);
            editor.commit();
        }
    }
}
