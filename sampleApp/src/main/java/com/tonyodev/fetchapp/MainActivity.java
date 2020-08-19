package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2fileserver.FetchFileServer;
import com.tonyodev.fetch2rx.RxFetch;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 50;

    private View mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = findViewById(R.id.activity_main);
    }

    public void btnClick(View v) {
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
            case R.id.btnDelete:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                } else {
                    deleteDownloadedFiles();
                }
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
                FailedMultiEnqueueActivity.FETCH_NAMESPACE,
                FileServerActivity.FETCH_NAMESPACE};
        for (String namespace : namespaces) {
            final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this).setNamespace(namespace).build();
            Fetch.Impl.getInstance(fetchConfiguration).deleteAll().close();
        }
        Fetch.Impl.getDefaultInstance().deleteAll().close();
        final RxFetch rxFetch = RxFetch.Impl.getDefaultRxInstance();
        rxFetch.deleteAll();
        rxFetch.close();
        new FetchFileServer.Builder(this)
                .setFileServerDatabaseName(FileServerActivity.FETCH_NAMESPACE)
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
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            deleteDownloadedFiles();
        } else {
            Snackbar.make(mainView, R.string.permission_not_enabled, Snackbar.LENGTH_INDEFINITE).show();
        }
    }
}
