package com.abrody.brightness;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System; 

public class BrightnessToggle extends Activity {
    float BackLightValue = 0.5f; // default value
    boolean turnoff;
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        Log.d("BrightnessToggle", "Clicked!");
        finish();
        toggleAutoBrightness();

    }
    
    private void toggleAutoBrightness() {
        int curLevel, newLevel;
        try {
            curLevel = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            Toast.makeText(this, "Exception!", Toast.LENGTH_SHORT).show();
            Log.e("BrightnessToggle", "Fail");
            return;
        }
        
        turnoff = false;
        if (curLevel == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            turnoff = true;
            newLevel = System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            Toast.makeText(this, "Auto brightness off!", Toast.LENGTH_SHORT).show();
            Log.i("BrightnessToggle", "Setting SCREEN_BRIGHTNESS_MODE_MANUAL");
        } else if (curLevel == System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            newLevel = System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Toast.makeText(this, "Auto brightness on!", Toast.LENGTH_SHORT).show();
            Log.i("BrightnessToggle", "Setting SCREEN_BRIGHTNESS_MODE_AUTOMATIC");
        } else {
            Toast.makeText(this, "Unknown level!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (turnoff) {
            System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, 1);
            Log.i("BrightnessToggle", "Setting SCREEN_BRIGHTNESS to 1");
        }
        
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, newLevel);
    }
}
