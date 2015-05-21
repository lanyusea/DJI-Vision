package com.hkust.dvg;

import java.util.Timer;
import java.util.TimerTask;


import com.google.android.glass.widget.CardScrollView;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.Battery.DJIBatteryProperty;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJIGroundStationFlyingInfoCallBack;
import dji.sdk.interfaces.DJIBattryUpdateInfoCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGroundStationExecutCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import dji.sdk.api.DJIError;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.interfaces.DJIGroundStationGoHomeCallBack;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationGoHomeResult;
import dji.sdk.api.GroundStation.DJIGroundStationFlyingInfo;
import dji.sdk.api.GroundStation.DJIGroundStationTask;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationControlMode;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationGoHomeResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHoverResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationOneKeyFlyResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResumeResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationTakeOffResult;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;


import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout.LayoutParams;
import android.hardware.SensorManager;
import android.widget.Toast;


public class MainActivity extends Activity{

    private MyoGlassService mService;

    private static final String TAG = "MainActivity";

    private RelativeLayout mStateRelativeLayout;
    private RelativeLayout mVideoRelativeLayout;
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private TextView mBatteryInfoTextView;
    private TextView mMainControllerStateTextView;
    private TextView mIMUStateTextView;

    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack  = null;
    private DJIBattryUpdateInfoCallBack mBattryUpdateInfoCallBack = null;
    private DJIMcuUpdateStateCallBack mMcuUpdateStateCallBack = null;
    private DJIGroundStationFlyingInfoCallBack mGroundStationFlyingInfoCallBack = null;

    private String BatteryInfoString = "";
    private String McStateString = "";
    private String IMUStateString = "";

    private boolean isConnected = false;
    private boolean isHeadControl = false;
    private boolean isFullScreen  = false;
    private boolean isLongPress=false;
    private boolean isRecording=false;
    private boolean getHomePiontFlag = false;
    public static boolean isMYOReady = false;

    private double homeLocationLatitude = -1;
    private double homeLocationLongitude = -1;

    private Timer mTimer;
    private OrientationManager orientationManager;

    private int glassPreviousYaw = 0;
    private int glassCurrentYaw  = 0;
    private int phantomBaseYaw   = 0;
    private int phantomCurrentYaw= 0;
    private int phantomTargetYaw = 0;
    private int phantomCurrentYawSpeed =0;

    private int pitch = 0;
    private int roll = 0;
    private int yaw = 0;
    private int throttle = 0;
    private int myo_pitch_current = 0;
    private int myo_pitch_previous = 0;
    private int myo_roll_current = 0;
    private int myo_roll_previous = 0;
    private int myo_yaw_current = 0;
    private int myo_yaw_previous = 0;
    private int glass_roll_current =0;
    private int glass_roll_initial = 0;

    public static boolean isFlying = false;

    private int timerCycle = 100;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        initGlass();
        checkPermission();
        onInitSDK();
        checkConnectState();

        // Bind to the ConnectionService so that we can communicate with it directly.
        Intent intent = new Intent(this, MyoGlassService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Start the ConnectionService normally so it outlives the activity. This allows it to
        // listen for Myo pose events when the activity isn't running.
        startService(new Intent(this, MyoGlassService.class));
        //isMYOReady=true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_CAMERA)
        {
            if(isRecording&&!isLongPress)
            {
                DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback(){

                    @Override
                    public void onResult(DJIError mErr)
                    {
                        Log.d(TAG, "Stop Recording errorCode = "+ mErr.errorCode);
                        Log.d(TAG, "Stop Recording errorDescription = "+ mErr.errorDescription);

                    }

                });
                isRecording=false;
            }else
            if(!isLongPress){

                DJIDrone.getDjiCamera().startTakePhoto(new DJIExecuteResultCallback(){

                    @Override
                    public void onResult(DJIError mErr)
                    {
                        Log.d(TAG, "Start Takephoto errorCode = "+ mErr.errorCode);
                        Log.d(TAG, "Start Takephoto errorDescription = "+ mErr.errorDescription);

                    }

                });
            }else
            {
                isLongPress=false;
            }
            return true;
        }else
            return super.onKeyUp(keyCode,event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_CAMERA)
        {
            if(!isRecording)
            {
                DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback() {

                    @Override
                    public void onResult(DJIError mErr) {
                        Log.d(TAG, "Start Recording errorCode = " + mErr.errorCode);
                        Log.d(TAG, "Start Recording errorDescription = " + mErr.errorDescription);
                    }

                });
                isRecording=true;
            }
            isLongPress=true;
            return true;
        }else
            return super.onKeyLongPress(keyCode,event);
    }

    @Override
    protected void onDestroy() {

        mDjiGLSurfaceView.destroy();
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        onUnInitSDK();
        stopService(new Intent(this, MyoGlassService.class));
        //unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        if (mService != null) {
            mService.setActivityActive(true);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mService != null) {
            mService.setActivityActive(false);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, timerCycle);

        //DJIDrone.getDjiMC().startUpdateTimer(1000);
        //DJIDrone.getDjiGroundStation().startUpdateTimer(1000);

        super.onResume();

    }

    @Override
    protected void onPause() {

        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        DJIDrone.getDjiBattery().stopUpdateTimer();
        DJIDrone.getDjiMC().stopUpdateTimer();


        //DJIDrone.getDjiGroundStation().stopUpdateTimer();

        isConnected=false;

        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*if(!isConnected)
        {
            setResultToToast("Connection Down");
            return super.onOptionsItemSelected(item);
        }
        else*/
        {
            switch (item.getItemId()) {
                case R.id.album:
                    viewAlbum();
                    return true;
                case R.id.head_control:
                    toggleHeadControl();
                    return true;
                case R.id.MYO_control:
                    toggleMYOControl();
                    return true;
                case R.id.full_screen:
                    toggleFullScreen();
                    return true;
                case R.id.return_home:
                    goHome();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    private void onInitSDK(){
        DJIDrone.initWithType(DJIDroneType.DJIDrone_Vision);
        DJIDrone.connectToDrone();
    }

    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }

    private void initGlass()
    {
        setContentView(R.layout.camera_card);//this line must be put at the first
        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView);


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientationManager = new OrientationManager(sensorManager);

        mBatteryInfoTextView = (TextView)findViewById(R.id.BatteryInfoTextView);
        mMainControllerStateTextView = (TextView)findViewById(R.id.MainControllerStateTextView);
        mIMUStateTextView = (TextView)findViewById(R.id.IMUStateTextView);
        mStateRelativeLayout = (RelativeLayout)findViewById(R.id.left_column);
        mVideoRelativeLayout = (RelativeLayout)findViewById(R.id.right_column);
    }

    private void toggleMYOControl()
    {
        if(mService.isMYOConnected())
        {
            mService.unpairWithMYO();
            //stopService(new Intent(this, MyoGlassService.class));
            //finish();
            setResultToToast("MYO Stop");
            //if (isHeadControl == true) toggleHeadControl();


            //if(!checkGetHomePoint()) return;
            DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecutCallBack(){

                @Override
                public void onResult(GroundStationResult result) {
                    // TODO Auto-generated method stub
                    String ResultsString = "return code =" + result.value();
                }

            });
        }
        else
        {
            if (mService != null) {

                MainActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run()
                    {
                      mService.attachToNewMyo();
                    }
                });

            }
            //if(!checkGetHomePoint()) return;
            //if (isHeadControl == false) toggleHeadControl();
            DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecutCallBack(){

                @Override
                public void onResult(GroundStationResult result) {
                    if(result.equals(GroundStationResult.GS_Result_Successed)) {
                           setResultToToast("GS Open");
                    }
                    else{
                           setResultToToast("GS Failed");
                       }

                }

            });

            DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack() {
                @Override
                public void onResult(GroundStationHoverResult groundStationHoverResult) {
                    if(groundStationHoverResult.equals(GroundStationHoverResult.GS_Hover_Successed))
                        setResultToToast("Paused!");
                }
            });
        }

    }

    private void toggleHeadControl()
    {
        if(!isHeadControl) {
            isHeadControl = true;
            phantomBaseYaw   = phantomCurrentYaw;
            phantomTargetYaw = phantomCurrentYaw;
            glassPreviousYaw = orientationManager.yaw;
            setResultToToast("Head Control Start");
        }else
        {
            //setPhantomYawSpeed(0);
            setResultToToast("Head Control Stop");
            isHeadControl = false;
        }
        Log.e(TAG,"HeadControl = "+isHeadControl);
    }

    private void toggleFullScreen()
    {
        if(!isFullScreen) {
            mStateRelativeLayout.getLayoutParams().width = 0;
            mStateRelativeLayout.requestLayout();

            RelativeLayout.LayoutParams videoLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            videoLayoutParams.setMargins(0, -60, 0, -60);
            mVideoRelativeLayout.setLayoutParams(videoLayoutParams);

            isFullScreen = true;
        }else
        {
            mStateRelativeLayout.getLayoutParams().width = 160;
            mStateRelativeLayout.requestLayout();

            RelativeLayout.LayoutParams videoLayoutParams = new RelativeLayout.LayoutParams(480, LayoutParams.MATCH_PARENT);
            videoLayoutParams.setMargins(160, 0, 0, 0);

            mVideoRelativeLayout.setLayoutParams(videoLayoutParams);

            isFullScreen = false;
        }

    }

    private void viewAlbum()
    {
        Intent ia = new Intent(MainActivity.this, AlbumActivity.class);
        startActivity(ia);

        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        DJIDrone.getDjiBattery().stopUpdateTimer();
        DJIDrone.getDjiMC().stopUpdateTimer();

        isConnected=false;
    }

    private void showVideo()
    {
        Log.e(TAG, "video Running");

        android.os.SystemClock.sleep(2000);

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);
        mDjiGLSurfaceView.start();
    }

    //MYO service
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyoGlassService.MBinder binder = ((MyoGlassService.MBinder)service);
            mService = binder.getService();

            // Let the service know that the activity is showing. Used by the service to trigger
            // the appropriate foreground or background events.
            mService.setActivityActive(true);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    class Task extends TimerTask {
        int times = 1;

        @Override
        public void run() {
            //Log.d(TAG ,"==========>Task Run In!");
            if (times++ % 10 == 0) {
                checkConnectState();

                if (isMYOReady) {
                    StringBuffer sb = new StringBuffer();

                    getIMUState(sb);
                    //HeadControlPhantomYaw(sb);

                    IMUStateString = sb.toString();
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mIMUStateTextView.setText(IMUStateString);

                        }
                    });
                }
                times = 1;
                Log.e(TAG, "time:" + times);
                HeadControlGimbalPitch();

            }
        }
    };


    private void checkConnectState(){

        MainActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                if(bConnectState){

                    if(!isConnected)
                    {
                        Log.e(TAG, "Connection Successful");
                        //setResultToToast("Connection Successful");
                        showVideo();
                    }
                    getBatteryInfo();
                    getPhantomStates();
                    isConnected=true;

                }
                else{
                    if(isConnected){
                        Log.e(TAG, "Connection Down");
                        //setResultToToast("Connection Down");
                    }
                    isConnected=false;
                }
            }
        });

    }

    private void checkPermission()
    {
        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGerneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getIMUState(StringBuffer sb)
    {

        if(mService.isMYOConnected()) {
            //StringBuffer sb = new StringBuffer();
            sb.append("IMU State:").append("\n");
            //sb.append("X= ").append(orientationManager.yaw).append("\n");
            //sb.append("Y= ").append(orientationManager.pitch).append("\n");

            if(mService.accelerometer!=null){
                myo_roll_current = (int)(mService.accelerometer.x()*100);
                myo_pitch_current = (int)(mService.accelerometer.y()*100);
                myo_yaw_current = (int)(mService.accelerometer.z()*100);
                //sb.append("roll= ").append(roll_current).append("\n");
                //sb.append("pitch= ").append(pitch_current).append("\n");
                //sb.append("Z= ").append((int)(mService.accelerometer.z()*100)).append("\n");

                Log.e(TAG,"roll:"+myo_roll_previous+","+myo_roll_current);
            }

            //Log.e(TAG, IMUStateString);
            if (myo_roll_current > myo_roll_previous+15) {
                pitch = -500;
                sb.append("go in\n");
            }
            else if(myo_roll_current < myo_roll_previous-15){
                pitch = 500;
                sb.append("go out\n");
            }
            else{
                pitch = 0;
                sb.append("pitch standby\n");
            }
            myo_roll_previous = myo_roll_current;
            new Thread()
            {
                public void run()
                {

                    DJIDrone.getDjiGroundStation().setAircraftPitchSpeed(pitch, new DJIGroundStationExecutCallBack(){

                        @Override
                        public void onResult(GroundStationResult result) {
                            // TODO Auto-generated method stub

                        }

                    });
                }
            }.start();

            if (myo_pitch_current > myo_pitch_previous+10) {
                throttle = 2;

                sb.append("go down\n");

            }
            else if(myo_pitch_current < myo_pitch_previous - 10){
                throttle = 1;
                sb.append("go up\n");
            }
            else {
                throttle = 0;;
                sb.append("throt standby\n");
            }
            myo_pitch_previous = myo_pitch_current;

            new Thread()
            {
                public void run()
                {

                    DJIDrone.getDjiGroundStation().setAircraftThrottle(throttle, new DJIGroundStationExecutCallBack(){

                        @Override
                        public void onResult(GroundStationResult result) {
                            // TODO Auto-generated method stub

                        }

                    });
                }
            }.start();


            if (myo_yaw_current > 70) {
                roll = -500;
                sb.append("go right\n");
            }
            else if(myo_yaw_current < -70){
                roll = 500;
                sb.append("go left\n");
            }
            else{
                roll = 0;
                sb.append("roll standby\n");
            }
            myo_yaw_previous = myo_yaw_current;
            new Thread()
            {
                public void run()
                {

                    DJIDrone.getDjiGroundStation().setAircraftRollSpeed(roll, new DJIGroundStationExecutCallBack(){

                        @Override
                        public void onResult(GroundStationResult result) {
                            // TODO Auto-generated method stub

                        }

                    });
                }
            }.start();
            /*new Thread()
            {
                public void run()
                {

                    DJIDrone.getDjiGroundStation().setAircraftJoystick(0, pitch, roll, throttle,new DJIGroundStationExecutCallBack(){

                        @Override
                        public void onResult(GroundStationResult result) {
                            // TODO Auto-generated method stub

                        }

                    });
                }
            }.start();*/
            //mGroundStationFlyingInfoCallBack = new DJIGroundStationFlyingInfoCallBack() {
            //   @Override
            //    public void onResult(DJIGroundStationFlyingInfo djiGroundStationFlyingInfo) {

            //    }
            //};

        }
    }

    private void getBatteryInfo()
    {
        mBattryUpdateInfoCallBack = new DJIBattryUpdateInfoCallBack(){

            @Override
            public void onResult(DJIBatteryProperty state) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();
                sb.append(getString(R.string.battery_info)).append("\n");
                sb.append("   ").append(state.remainPowerPercent).append(" %");
                sb.append("   ").append(state.batteryTemperature).append("°C\n");
                BatteryInfoString = sb.toString();
                MainActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run()
                    {
                        mBatteryInfoTextView.setText(BatteryInfoString);
                    }
                });


            }

        };

        DJIDrone.getDjiBattery().setBattryUpdateInfoCallBack(mBattryUpdateInfoCallBack);
        DJIDrone.getDjiBattery().startUpdateTimer(1000);
    }

    private void getPhantomStates()
    {
        mMcuUpdateStateCallBack = new DJIMcuUpdateStateCallBack(){

            @Override
            public void onResult(DJIMainControllerSystemState state) {
                // TODO Auto-generated method stub

                StringBuffer sb = new StringBuffer();
                sb.append(getString(R.string.main_controller_state)).append("\n");
                sb.append("satellite=").append((int) state.satelliteCount).append("\n");
                sb.append("speed=").append((int) state.speed).append(" m/s\n");
                sb.append("altitude=").append((int)state.altitude).append("m\n");
                sb.append("yaw=").append((int)state.yaw).append("");

                isFlying = state.isFlying;
                phantomCurrentYaw     = (int)state.yaw;
                homeLocationLatitude  = state.homeLocationLatitude;
                homeLocationLongitude = state.homeLocationLongitude;

                if(homeLocationLatitude != -1 && homeLocationLongitude != -1 && homeLocationLatitude != 0 && homeLocationLongitude != 0){
                    getHomePiontFlag = true;
                }
                else{
                    getHomePiontFlag = false;
                }

                McStateString = sb.toString();
                //Log.e(TAG, McStateString);
                MainActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run()
                    {
                        mMainControllerStateTextView.setText(McStateString);
                    }
                });
            }

        };
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mMcuUpdateStateCallBack);
        DJIDrone.getDjiMC().startUpdateTimer(1000);
    }

    private void setResultToToast(String result){
        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    private void HeadControlGimbalPitch()
    {
        DJIGimbalRotation mPitch;
        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);

        if(isHeadControl)
        {
            if(orientationManager.pitch>=-125&&orientationManager.pitch<=-70)
            {
                mPitch = new DJIGimbalRotation(true, false,true, (int)((orientationManager.pitch+125)*1000/55));
                DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);
            }
        }else
        {
            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
        }

    }

    private void GlassControlPhantom(StringBuffer sb)
    {

        glass_roll_current = orientationManager.roll;

        if((glass_roll_current > 150) && (glass_roll_current<180))
        {
            //sb.append("go left\n");
            roll = 700;

        }
        else if(glass_roll_current > 180 && (glass_roll_current<230))
        {
            //sb.append("go right\n");
            roll = -700;

        }
        else
        {
            //sb.append("roll standby\n");
            roll = 0;
        }

        sb.append("roll:"+glass_roll_current+"\n");



        new Thread()
        {
            public void run()
            {

                DJIDrone.getDjiGroundStation().setAircraftJoystick(0, 0, roll, 0,new DJIGroundStationExecutCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
            }
        }.start();

    }

    private void HeadControlPhantomYaw(StringBuffer sb)
    {
        int deltaYaw;
        int thresh = 10;

        glassCurrentYaw = orientationManager.yaw;


        deltaYaw = glassCurrentYaw - glassPreviousYaw;
        deltaYaw += 180;
        deltaYaw %= 360;
        deltaYaw -= 180;

        phantomTargetYaw += deltaYaw;
        phantomTargetYaw %= 360;

        if(phantomTargetYaw-phantomCurrentYaw>thresh)
        {
           yaw = 500;

            sb.append("Turn right");
        }else
        if(phantomTargetYaw-phantomCurrentYaw<-thresh)
        {
            yaw = -500;
            sb.append("Turn left");
        }else
        {
            yaw = 0;
            sb.append("Yaw standby");
        }
        new Thread()
        {
            public void run()
            {

                DJIDrone.getDjiGroundStation().setAircraftYawSpeed(yaw,new DJIGroundStationExecutCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
            }
        }.start();
        glassPreviousYaw = glassCurrentYaw;
    }

    private boolean checkGetHomePoint(){
        if(!getHomePiontFlag){
            setResultToToast(getString(R.string.gs_not_get_home_point));
        }
        return getHomePiontFlag;
    }

    private void goHome(){
        if(!checkGetHomePoint()) return;

        DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecutCallBack(){
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
            }

        });

        DJIDrone.getDjiGroundStation().goHome(new DJIGroundStationGoHomeCallBack(){

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationGoHomeResult result) {
            }

        });
    }

}

