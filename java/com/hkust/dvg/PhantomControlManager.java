package com.hkust.dvg;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.interfaces.DJIGroundStationExecutCallBack;
import dji.sdk.interfaces.DJIGroundStationGoHomeCallBack;

/**
 * Created by xufang on 15/2/4.
 */
public class PhantomControlManager {
    private static final String TAG = "PhantomControl";

    public void setPhantomYawSpeed(final int speed){
        Log.e(TAG, "SetYawSpeed=" + speed);

        new Thread() {
            public void run() {

                DJIDrone.getDjiGroundStation().setAircraftYawSpeed(speed, new DJIGroundStationExecutCallBack() {

                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
            }
        }.start();
    }

}
