package com.wenx.simple.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;

public class OrientationSensorListener implements SensorEventListener {

    private static final int _DATA_X = 0;
    private static final int _DATA_Y = 1;
    private static final int _DATA_Z = 2;
    public static final int ORIENTATION_UNKNOWN = -1;
    private Handler rotateHandler;

    public void removeListener(){
        rotateHandler = null;
    }

    public OrientationSensorListener(Handler handler) {
        rotateHandler = handler;
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        int orientation = ORIENTATION_UNKNOWN;
        float X = -values[_DATA_X];
        float Y = -values[_DATA_Y];
        float Z = -values[_DATA_Z];
        float magnitude = X * X + Y * Y;

        if (magnitude * 4 >= Z * Z) {
            float OneEightyOverPi = 57.29577957855f;
            float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
            orientation = 90 - (int) Math.round(angle);
            while (orientation >= 360) {
                orientation -= 360;
            }
            while (orientation < 0) {
                orientation += 360;
            }
        }
        if (rotateHandler != null) {
            rotateHandler.obtainMessage(888, orientation, 0).sendToTarget();
        }
    }
}