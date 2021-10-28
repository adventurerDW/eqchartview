package com.wenx.simple.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

public class ChangeOrientationHandler extends Handler {
    private Activity activity;
    private boolean needRotate;
    private int oriStatus;

    public ChangeOrientationHandler(Activity ac) {
        activity = ac;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 888) {
            int orientation = msg.arg1;
            if (orientation > 45 && orientation < 135) {
                if (oriStatus == SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    return;
                }
                oriStatus = SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else if (orientation > 135 && orientation < 225) {
                if (oriStatus == SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    return;
                }
                oriStatus = SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else if (orientation > 225 && orientation < 315) {
                if (oriStatus == SCREEN_ORIENTATION_LANDSCAPE) {
                    return;
                }
                oriStatus = SCREEN_ORIENTATION_LANDSCAPE;
                activity.setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                if (oriStatus == SCREEN_ORIENTATION_PORTRAIT) {
                    return;
                }
                oriStatus = SCREEN_ORIENTATION_PORTRAIT;
                activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        super.handleMessage(msg);
    }
}
