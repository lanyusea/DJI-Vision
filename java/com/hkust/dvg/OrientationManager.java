package com.hkust.dvg;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.Arrays;

/**
 * Created by mgrzechocinski on 1/8/14.
 */
public class OrientationManager implements SensorEventListener{

    private SensorManager sensorManager;

    //private float[] rotationMatrix = new float[16];

    private float[] valuesMagnet      = new float[3];
    private float[] valuesAccel       = new float[3];
    private float[] valuesGravity     = new float[3];
    private float[] valuesOrientation = new float[3];
    private float[] rotationMatrix    = new float[9];
    private float[] rotationMatrixTemp= new float[9];

    public int pitch;
    public int yaw;
    public int roll;

    public OrientationManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;

        //Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        //SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix);
        //SensorManager.getOrientation(rotationMatrix, orientation);
        //pitch = (int) Math.toDegrees(orientation[0]);
        //yaw   = (int) Math.toDegrees(orientation[1]);


        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, valuesAccel, 0, 3);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, valuesMagnet, 0, 3);
                break;

            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, valuesGravity, 0, 3);
                break;

            default:
                break;
        }
        if (SensorManager.getRotationMatrix(rotationMatrixTemp, null, valuesAccel, valuesMagnet)) {
            SensorManager.getOrientation(rotationMatrixTemp, valuesOrientation);

            if(valuesGravity[2] < 0)                                     {
                if (valuesOrientation[1] > 0) {
                    valuesOrientation[1] = (float) (Math.PI - valuesOrientation[1]);
                }
                else {
                    valuesOrientation[1] = (float) (-Math.PI - valuesOrientation[1]);
                }
            }

        /*
            https://developers.google.com/glass/develop/gdk/location-sensors
            values[0]: azimuth, rotation around the Z axis.
            values[1]: pitch, rotation around the X axis.
            values[2]: roll, rotation around the Y axis.
        */
            yaw = (int) (valuesOrientation[0] * 180/ Math.PI)+180;
            pitch   = (int) (valuesOrientation[1] * 180/ Math.PI);
            roll = (int) (valuesOrientation[2] * 180 /Math.PI) +180;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
