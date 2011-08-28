package com.abrody.brightness;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings.System;
import android.view.WindowManager.LayoutParams;

public class BrightnessToggle extends Activity {
    private static final int MINIMUM_BACKLIGHT = 18;
    private static final int MAXIMUM_BACKLIGHT = 255;
    boolean toManual;
    
    // logging constants
    final static String TAG = "BrightnessToggle";
    final static boolean LOGV = false;
    
    private Handler handler = new Handler();
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
    
    private void toggleAutoBrightness() {
        int mode;
        int manualBrightnessLevel, brightness;
        final ContentResolver resolver = getContentResolver();

        // Get the current brightness mode.
        mode = System.getInt(resolver, System.SCREEN_BRIGHTNESS_MODE, -1);
        
        // Figure out which mode to switch to and pop up a toast.
        toManual = false;
        if (mode == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            toManual = true;
            mode = System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            Log.i("BrightnessToggle", "Switching to manual");
            Toast.makeText(this, getString(R.string.toast_off), Toast.LENGTH_SHORT).show();
        } else if (mode == System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            mode = System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Log.i("BrightnessToggle", "Switching to automatic");
            Toast.makeText(this, getString(R.string.toast_on), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unknown mode: " + Integer.toString(mode), Toast.LENGTH_SHORT).show();
            Log.e("BrightnessToggle", "Unknown value for SCREEN_BRIGHTNESS_MODE: " + Integer.toString(mode));
            return;
        }
        
        // Set the new SCREEN_BRIGHTNESS_MODE
        System.putInt(resolver, System.SCREEN_BRIGHTNESS_MODE, mode);
        
        // Restore preferences: saved manual brightness
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        manualBrightnessLevel = settings.getInt("manualBrightnessLevel", -1);
        
        if (toManual && manualBrightnessLevel >= MINIMUM_BACKLIGHT) {
            // If we're going to manual, restore the saved brightness level.
            if (LOGV) Log.d("BrightnessToggle", "Setting SCREEN_BRIGHTNESS to " + Integer.toString(manualBrightnessLevel));
            System.putInt(resolver, System.SCREEN_BRIGHTNESS, manualBrightnessLevel);
            
            // And set the brightness of the window manager
            LayoutParams attrs = getWindow().getAttributes();
            attrs.screenBrightness = (float) manualBrightnessLevel / MAXIMUM_BACKLIGHT;
            if (LOGV) Log.d("BrightnessToggle", "Setting current brightness to " + Float.toString(attrs.screenBrightness));
            
        } else {
            // If we're going to automatic, save current brightness level as default manual level.
            SharedPreferences.Editor editor = settings.edit();
            brightness = System.getInt(resolver, System.SCREEN_BRIGHTNESS, -1);
            
            if (brightness >= MINIMUM_BACKLIGHT) {
                editor.putInt("manualBrightnessLevel", brightness);
                editor.commit();
            }
        }
    }
}
