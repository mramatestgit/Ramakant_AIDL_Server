package com.anjan.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;


public class ServerService extends Service implements SensorEventListener {

    private static final String TAG = "ShowOrientationService";
    public Context mContext;
    public SensorManager mSensorManager;
    public Sensor mSensor;
    public float mPitch = 0.0f;
    public float mRoll = 0.0f;
    public static final int SENSOR_DELAY = 8 * 1000; //8ms
    public static final int FROM_RADS_TO_DEGS = -57;

    public ServerService() {
        Log.d(TAG, "ServerService constructor");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        mSensorManager = (SensorManager)  getApplicationContext().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mSensor, SENSOR_DELAY);
        Log.d(TAG, "ShowOrientationService onCreate");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SensorService onDestroy");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mSensor) {
            if (event.values.length > 4) {
                float [] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector,
                        0, 4);
                update(truncatedRotationVector);
            } else {
                update(event.values);
            }
        }
    }

    public void update(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX,
                worldAxisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        mPitch = orientation[1] * FROM_RADS_TO_DEGS;
        mRoll = orientation[2] * FROM_RADS_TO_DEGS;
        Log.d(TAG, "roll and pitch" +mPitch + mRoll);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  displayOrientation.asBinder();
    }

    public IDisplayOrientation displayOrientation = new IDisplayOrientation.Stub() {

        @Override
        public int multiply(int a, int b) throws RemoteException {
            return 0;
        }

        @Override
        public float getOrientationPitch() throws RemoteException {
            return mPitch;
        }

        @Override
        public float getOrientationRoll() throws RemoteException {
            return mRoll;
        }
    };
}
