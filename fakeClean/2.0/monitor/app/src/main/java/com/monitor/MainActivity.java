package com.monitor;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;



public class MainActivity extends Activity {
    public String TAG = "yooha";
    PopupService mPopupService = null;
    private ServiceConnection mServiceConnection;
    private Intent mServiceIntent;
    Switch mSwitch;
    private int result = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission(this);

        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onMyCheckedChanged(isChecked);
            }
        });

        myBindService();
    }



    public void onMyCheckedChanged(boolean isChecked) {
        if(isChecked) {
            mPopupService.show();
        } else {
            mPopupService.dimiss();
        }
    }


    public void requestPermission(Context mContext) {
        //读写权限
        checkReadWrite();

        //悬浮窗权限
        checkOVERLAYPermission();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1){
            for(int i=0;i<permissions.length;i++){//可能有多个权限，需要观测是否为PERMISSION_GRANTED状态
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //读写权限
    public void checkReadWrite() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                //第一次上面这个方法返回的是false，之后就一直返回true
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);//核心
                //此处会弹出一个框框询问你是否给予权限
            }
        }
    }

    //悬浮窗权限
    public void checkOVERLAYPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 10);
            }
        }
    }


    private void myBindService() {
        mServiceIntent = new Intent(MainActivity.this, PopupService.class);

        if(mServiceConnection == null) {
            Log.d("yooha", "myBindService  in  mServiceConnection");
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d("yooha", "myBindService  onServiceConnected");
                    mPopupService = ((PopupService.PopupBinder) service).getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("yooha", "onService   Disconnected");
                }
            };
            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private void myUnBindService() {
        if(null != mServiceConnection) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }


    @Override
    protected void onPause() {
        myUnBindService();
        super.onPause();
    }


    @Override
    protected void onStop() {
        myUnBindService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        myUnBindService();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        myBindService();
        super.onRestart();
    }


    @Override
    protected void onResume() {
        myBindService();
        super.onResume();
    }

}