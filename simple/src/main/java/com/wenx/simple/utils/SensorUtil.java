package com.wenx.simple.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;

/**
 * Created By WenXiong on 2021/10/18.
 */
public class SensorUtil {

    private static Handler handler;
    private static OrientationSensorListener listener;
    private static SensorManager sm;
    private static Sensor sensor;

    private static SensorUtil INIT;
    public static SensorUtil getInstance(Activity ac){
        if (INIT==null) {
            synchronized (SensorUtil.class) {
                if (INIT==null) {
                    INIT = new SensorUtil();
                    sm = (SensorManager) ac.getSystemService(Context.SENSOR_SERVICE);
                    sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    handler = new ChangeOrientationHandler(ac);
                    listener = new OrientationSensorListener(handler);
                }
            }
        }
        return INIT;
    }

    public static void register(){
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public static void unregister(){
        sm.unregisterListener(listener);
    }

    public void destroy(){
        if (handler!=null) {
            handler.sendEmptyMessage(0);
            handler = null;
        }

        if (listener!=null) {
            listener.removeListener();
            listener = null;
        }

        if (sm!=null) {
            sm = null;
        }

        if (sensor!=null) {
            sensor = null;
        }

    }

}
