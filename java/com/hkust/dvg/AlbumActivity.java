package com.hkust.dvg;

import com.hkust.dvg.R;
import com.hkust.dvg.adapter.MovieCardsAdapter;
import com.hkust.dvg.model.MovieCard;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.widget.CardScrollView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.tsz.afinal.FinalActivity;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraMode;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.media.DJIMedia;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIMediaFetchCallBack;
import dji.sdk.interfaces.DJIReceivedFileDataCallBack;
import android.R.anim;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AlbumActivity extends Activity {

    private List<MovieCard> mCards = new ArrayList<MovieCard>();
    private MovieCardsAdapter adapter;
    private CardScrollView mCardScrollView;
    private Context context;


    private static final String TAG = "AlbumActivity";
    private static String ALBUM_NAME = "/DJI_SDK_DCIM";
    private static final String MNT = android.os.Environment.getExternalStorageDirectory().getPath();
    private RandomAccessFile mSyncFile;

    private Timer mTimer;

    private List<DJIMedia> mDJIMediaList = null;
    private DJICameraSystemStateCallBack mCameraSystemStateCallBack = null;



    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
        }

    };

    private void checkConnectState(){

        AlbumActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                if(bConnectState){
                    Log.e(TAG, "Connection Successful");
                }
                else{
                    Log.e(TAG, "Connection Down");
                    setResultToToast("Connection Down");
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        adapter = new MovieCardsAdapter(context, mCards);

        mDJIMediaList = new ArrayList<DJIMedia>();
        getFileList();
        android.os.SystemClock.sleep(5000);
        //prepareMovieCards();
        for(int i=0;i<mDJIMediaList.size();i++) { getThumbnailByIndex(i); android.os.SystemClock.sleep(100);}

        mCardScrollView = new CardScrollView(this);

        mCardScrollView.setAdapter(adapter);
        mCardScrollView.activate();
        setContentView(mCardScrollView);
    }

    @Override
    protected void onDestroy() {
        DJIDrone.getDjiCamera().setCameraMode(CameraMode.Camera_Camera_Mode,new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr) {
                // TODO Auto-generated method stub

            }

        });

        if(mDJIMediaList != null){
            mDJIMediaList.clear();
        }

        super.onDestroy();
    }


    @Override
    protected void onResume() {
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 1000);
        DJIDrone.getDjiCamera().startUpdateTimer(1000);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        DJIDrone.getDjiCamera().stopUpdateTimer();
        super.onPause();
    }


    private void getFileList(){
        new Thread(){
            public void run() {
                DJIDrone.getDjiCamera().fetchMediaList(new DJIMediaFetchCallBack(){

                    @Override
                    public void onResult(List<DJIMedia> mList, DJIError mError) {

                        if(mError.errorCode == DJIError.RESULT_OK ){
                            if(mDJIMediaList != null){
                                mDJIMediaList.clear();
                            }
                            Log.d(TAG, "fetchMediaList success");
                            mDJIMediaList.addAll(mList);

                            //mListAdapter.notifyDataSetChanged();
                        }
                        else{
                            Log.e(TAG, "fetchMediaList failed,errorCode = "+ mError.errorCode);
                            //handler.sendMessage(handler.obtainMessage(SHOWTOAST, DJIError.getErrorDescriptionByErrcode(mError.errorCode)));
                        }
                    }

                });
            }
        }.start();
    }

    private void getThumbnailByIndex(int index){

        if(mDJIMediaList == null){
            Log.e(TAG, "Nothing");
            return;
        }

        if(index >= mDJIMediaList.size()){
            Log.e(TAG, "Out of Range");
            return;
        }

        final DJIMedia mMedia = mDJIMediaList.get(index);

        DJIDrone.getDjiCamera().fetchMediaThumbnail(mMedia,new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr) {
                // TODO Auto-generated method stub
                if(mErr.errorCode == DJIError.RESULT_OK){
                    Log.d(TAG, "getThumbnailByIndex success");

                    AlbumActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Drawable mDrawable = new BitmapDrawable(mMedia.thumbnail);

                            MovieCard mc = new MovieCard("",
                                    "DJI Phantom", ImageLayout.FULL,
                                    new Drawable[] { mDrawable });//catd_full is the nmae in drawable
                            mCards.add(mc);

                            adapter.notifyDataSetChanged();
                        }
                    });

                }
                else{
                    Log.e(TAG, "getThumbnailByIndex failed,errorCode = "+ mErr.errorCode);
                }
            }

        });

    }

    private void setResultToToast(String result){
        Toast.makeText(AlbumActivity.this, result, Toast.LENGTH_SHORT).show();
    }


}
