package com.monitor;



import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;


public class PopupService extends Service implements View.OnClickListener {
    public String TAG = "yooha";
    public String MONITOR = "yooha-monitor";
    /**
     * 文件监控对象
     */
    RecursiveFileObserver fileserverdata;
    RecursiveFileObserver fileserverstorage;

    public Thread mThread = null;

    /**
     * 控件window
     */
    private FloatingView mFloatingWindow;

    /**
     * 两个状态的View
     */
    private View mFloatView;
    private View mPopupView;

    /**
     * 显示结果的View
     */
    private View mShowPkgView;

    /**
     * popup功能图片
     */
    private ImageView mIvpkgname;
    private ImageView mIvAllPkgname;
    private ImageView mIvScreenShot;
    private ImageView mIvFunc4;


    /**
     * 截图相关
     */
    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String nameImage = null;
    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;
    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;


    /**
     * 显示相关控件
     */
    private ImageView mFloatImage;
    private TextView mTexyPkg1;
    private TextView mTexyPkg2;
    private TextView mTexyPkg3;
    private TextView mTexyPkg4;
    private TextView mTexyPkg5;


    @Override
    public IBinder onBind(Intent intent) {
        return new PopupBinder();
    }

    public class PopupBinder extends Binder {
        public PopupService getService() {
            return PopupService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initFloatingWindow();
    }


    private void initFloatingWindow() {
        mFloatView = LayoutInflater.from(this).inflate(R.layout.float_ball, null);

        mPopupView = LayoutInflater.from(this).inflate(R.layout.popup, null);

        mFloatImage = (ImageView) mFloatView.findViewById(R.id.id_iv);

        mIvpkgname = (ImageView) mPopupView.findViewById(R.id.id_pop_show_pkgname);
        mIvAllPkgname = (ImageView) mPopupView.findViewById(R.id.id_pop_show_all_pkgname);
        mIvScreenShot = (ImageView) mPopupView.findViewById(R.id.id_pop_screenshot);
        mIvFunc4 = (ImageView) mPopupView.findViewById(R.id.id_pop_function4);


        mIvpkgname.setOnClickListener(this);
        mIvAllPkgname.setOnClickListener(this);
        mIvScreenShot.setOnClickListener(this);
        mIvFunc4.setOnClickListener(this);

        mFloatingWindow = FloatingView.getInstance(this);//单例模式构造
        mFloatingWindow.setFloatingView(mFloatView);
        mFloatingWindow.setPopupView(mPopupView);
    }


    public void show() {
        if(null != mFloatingWindow)
            regObserver();
        mFloatingWindow.show();
    }

    public void dimiss() {
        if(null != mFloatingWindow){
            mFloatingWindow.dismiss();
        }
    }

    /**
     * 注册文件监控
     */
    public void regObserver() {
        String mStoragePath = Environment.getExternalStorageDirectory().toString();
        Log.d(MONITOR, "监控路径 -> " + mStoragePath);
        fileserverstorage = new  RecursiveFileObserver(mStoragePath) ;
        fileserverstorage.startWatching();

        String mDataPath = Environment.getDataDirectory().toString();
        Log.d(MONITOR, "监控路径 -> " + mDataPath);
        fileserverdata = new  RecursiveFileObserver(mDataPath) ;
        fileserverdata.startWatching();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.id_pop_show_pkgname: //功能按钮一
                Log.d("yooha", "id_pop_show_pkgname" );
                Toast.makeText(this, "预留功能接口", Toast.LENGTH_LONG).show();
                break;
            case R.id.id_pop_show_all_pkgname:  //功能按钮二
                Log.d("yooha", "id_pop_show_all_pkgname");
                Toast.makeText(this, "预留功能接口", Toast.LENGTH_LONG).show();
                break;
            case R.id.id_pop_screenshot:  //功能按钮三
                Log.d("yooha", "id_pop_screenshot");
                Toast.makeText(this, "预留功能接口", Toast.LENGTH_LONG).show();
                break;
            case R.id.id_pop_function4:   //功能按钮四
                Log.d("yooha", "id_pop_function4 " );
                Toast.makeText(this, "预留功能接口", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}


