package com.tonyodev.fetchapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2fileserver.FetchFileServer;
import com.tonyodev.fetch2rx.RxFetch;

import java.io.File;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    static final int REQ_PERMISSION_CODE = 50;
    static final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
    View mainView;
    boolean permissionGranted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = findViewById(R.id.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        permissionGranted = false;
        //Data.init(Data.ModeServer.MAXST, Data.ModeBitStream.BS_10M);
        Data.init(Data.ModeServer.MAXST, Data.ModeBitStream.BS_90mbps);
        //Data.init(Data.ModeServer.KT, Data.ModeBitStream.BS_10M);
        //Data.init(Data.ModeServer.LG, Data.ModeBitStream.BS_90mbps);
        requestPermission();
    }

    public static boolean hasAllPermissions(Context ctx) {
        for(String permission : permissions) {
            if(ctx.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
    public  void requestPermission() {
        if(MainActivity.hasAllPermissions(this)==false)
            requestPermissions(permissions, REQ_PERMISSION_CODE);
        else
            processPermissionGranted();
    }

    private void processPermissionGranted() {
        permissionGranted =  true;
    }

    public void btnClick(View v) {
        if(!permissionGranted) {
            Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show();
            requestPermission();
            return;
        }

        if(!Data.isInitialized()) {
            Toast.makeText(this, "서버데이터가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int btnId = v.getId();

        Intent intent;
        switch(btnId) {
            case R.id.btnLatencyPing:
                intent = new Intent(MainActivity.this, PingActivity.class);
                break;
            case R.id.btnLatencySocket:
                intent = new Intent(MainActivity.this, SocketActivity.class);
                break;
            case R.id.btnResult:
                intent = new Intent(MainActivity.this, ExperimentResultActivity.class);
                break;
            case R.id.btnSetup:
                intent = new Intent(MainActivity.this, SetupActivity.class);
                break;
            case R.id.btnDelete:
                deleteDownloadedFiles();
                return;
            default:
                intent = new Intent(MainActivity.this, DownloadListActivity.class);
                //intent = new Intent(MainActivity.this, SingleDownloadActivity.class);
        }

        intent.putExtra("expTag", (String)v.getTag());
        MainActivity.this.startActivity(intent);
    }

    private void deleteDownloadedFiles() {
        final String[] namespaces = new String[]{
                DownloadListActivity.FETCH_NAMESPACE,
//                FailedMultiEnqueueActivity.FETCH_NAMESPACE,
//                FileServerActivity.FETCH_NAMESPACE
        };
        for (String namespace : namespaces) {
            final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this).setNamespace(namespace).build();
            Fetch.Impl.getInstance(fetchConfiguration).deleteAll().close();
        }
        Fetch.Impl.getDefaultInstance().deleteAll().close();
        final RxFetch rxFetch = RxFetch.Impl.getDefaultRxInstance();
        rxFetch.deleteAll();
        rxFetch.close();
        new FetchFileServer.Builder(this)
//                .setFileServerDatabaseName(FileServerActivity.FETCH_NAMESPACE)
                .setClearDatabaseOnShutdown(true)
                .build()
                .shutDown(false);
        try {
            final File fetchDir = new File(Data.getSaveDir());
            Utils.deleteFileAndContents(fetchDir);
            boolean result = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE).edit().clear().commit();
            Toast.makeText(MainActivity.this, "Delete:" + (result ? "Success" : "Fail"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
        )
        {
            processPermissionGranted();
        } else {
            Log.e("Permission","grantResults[0]:" + grantResults[0]);
            Log.e("Permission","grantResults[1]:" + grantResults[1]);
            Toast.makeText(this, "모든 권한을 허용해주세요", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
