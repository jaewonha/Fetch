package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.DefaultFetchNotificationManager;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

/*
    · (고정측정) 코엑스 실내외 특정 구간(예시: 파르나스 몰 입구 진입구간, 별마당 도서관, 메가박스 영화관)에서 고정상태 측정

    · (이동측정) 이동상태(파르나스 ~ 별마당, 별마당 ~ 메가박스, 메가박스 ~ 파르나스)에서 이동상태 측정 (기지국이오버랩되지 않은 위치 확인 필요)

   ① (속도측정) 이통3사 MEC 서버에 저장된 속도 측정용 콘텐츠*를 이통사별 단말기 10대**가 동시 접속하여 콘텐츠를 스트리밍 받으면서 5G 네트워크 속도 측정. 영상을 고화질로 재생하기 위한 속도가 일정하게 유지되어야 함
 */
public class DownloadListActivity extends AppCompatActivity {

    //final vars
    static final long UNKNOWN_REMAINING_TIME = -1;
    static final long UNKNOWN_DOWNLOADED_BYTES_PER_SECOND = 0;
    //static final long GPS_SAVE_PERIOD_MS =  1;
    static final long GPS_SAVE_PERIOD_MS =  60*1000;
    static final boolean USE_GPS = false;

    static final String FETCH_NAMESPACE = "DownloadListActivity";
    static final String TAG = "DownloadListActivity";

    //platform vars
    SharedPreferences sharedpreferences;
    FusedLocationProviderClient fusedLocationClient;

    //core function vars
    FileAdapter fileAdapter;
    Fetch fetch;

    //temp state vars
    String expID, expName;
    DownloadInfo downloadInfo;
    GPSData lastGPSLocation;
    String downloadedFilePath;
    //ArrayList<GPSData> gpsDataList;


    //ui vars
    View mainView;
    ACProgressFlower dialog;
    EditText etLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup views
        setContentView(R.layout.activity_download_list);
        mainView = findViewById(R.id.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fileAdapter = new FileAdapter(fileActionListener);
        recyclerView.setAdapter(fileAdapter);

        etLog = findViewById(R.id.etLog);

        //setup functions
        final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(1)
                .setHttpDownloader(new OkHttpDownloader(Downloader.FileDownloaderType.SEQUENTIAL))
                .setNamespace(FETCH_NAMESPACE)
                .enableAutoStart(false)
                .enableRetryOnNetworkGain(false)
                .setAutoRetryMaxAttempts(0)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        fetch.deleteAll(); //clear prev downloading incase app closed unexpectedly

        //setup vars
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);

        expID = getIntent().getStringExtra("expID");
        expName = getIntent().getStringExtra("expName");

        downloadInfo = new DownloadInfo();
        downloadInfo.name = expName;

        Log.e(TAG,"expID:" + expID);

        //check permission
        if(!MainActivity.hasAllPermissions(this)) {
            Toast.makeText(this,"필수 권한을 확인해주세요",Toast.LENGTH_LONG).show();
            finish();
        }

        updateDownload();
    }

    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " + (location.getLatitude()) + "," + (location.getLongitude());
        Log.e("DBG", "LocChange:" + msg);   //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        lastGPSLocation = new GPSData(System.currentTimeMillis(), location.getLatitude(), location.getLongitude());
    }

    void waitLocationUpdates() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ACProgressFlower.Builder(DownloadListActivity.this)
                        //.direction(ACProgressConstant.DIRECT_CLOCKWISE)
                        .themeColor(Color.WHITE)
                        .text("GPS 초기화 중입니다..")
                        .fadeColor(Color.DKGRAY).build();
                dialog.show();

                lastGPSLocation = null;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    while(lastGPSLocation==null)
                        Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Toast.makeText(DownloadListActivity.this,
                    "GPS 수신완료:" + lastGPSLocation.latitude + "/" + lastGPSLocation.longitude,
                    Toast.LENGTH_SHORT)
                    .show();

                dialog.hide();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fetch.removeListener(fetchListener);

        fetch.cancelAll();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetch.deleteAll();
        fetch.close();
    }

    @Override
    public void onBackPressed()
    {
        //backPressed = true;
        super.onBackPressed();  // optional depending on your needs
        this.finish();
    }

//    public void btnClickStart(View v) {
//        enqueueDownloads();
//    }

    private void updateDownload() {
        String expData = sharedpreferences.getString(expID, null);
        if(expData!=null) {
            showExpSummary(expData);
        } else {
            if(USE_GPS) {
                startLocationUpdates();
                waitLocationUpdates();
            } else {
                lastGPSLocation = new GPSData(System.currentTimeMillis(), 0, 0);
            }
            enqueueDownloads();
            updateDownloadUI();
        }
    }

    private void showExpSummary(String expData){
        DownloadInfo downloadInfo = new Gson().fromJson(expData, DownloadInfo.class);

        etLog.setText("*다운로드 테스트 결과\n" + downloadInfo.toString());

        findViewById(R.id.recyclerView).setVisibility(View.GONE);
    }


    private void enqueueDownloads() {
        //final List<Request> requests = Data.getFetchRequestWithGroupId(GROUP_ID);
        int sampleId = (int)(Math.random()*9+0.5);

        downloadInfo.url = Data.sampleIDs[sampleId];
        downloadInfo.correctHash = Data.sampleHashs[sampleId];

        final List<Request> requests = Data.getFetchSampleWithUrl(downloadInfo.url);

        fetch.enqueue(requests, updatedRequests -> {

        });
        fetch.pauseAll();
    }

    private void updateDownloadUI() {
        //fetch.getDownloadsInGroup(GROUP_ID, downloads -> {
        fetch.getDownloads(downloads -> {
            final ArrayList<Download> list = new ArrayList<>(downloads);
            Collections.sort(list, (first, second) -> Long.compare(first.getCreated(), second.getCreated()));
            for (Download download : list) {
                System.err.println("#### add");
                fileAdapter.addDownload(download);
            }
        }).addListener(fetchListener);
    }

    void recordDownloadResult(Download download, boolean success) {
        downloadInfo.endMs = System.currentTimeMillis();
        downloadInfo.size = download.getTotal();

        downloadInfo.durMs = downloadInfo.endMs - downloadInfo.startMs;
        downloadInfo.bytePerSec = downloadInfo.size / (float) downloadInfo.durMs;
        downloadInfo._bytePerSec = download.getDownloadedBytesPerSecond();

        Log.d(TAG,"onCompleted:" + downloadInfo.endMs);
        etLog.append("[" + Utils.msToDate(downloadInfo.endMs) + "]:"+ (success ? "Completed" : "Error") + "\n"); //+gps
        downloadInfo.gpsDataList.add(new GPSData(downloadInfo.endMs, lastGPSLocation.latitude, lastGPSLocation.longitude));

        downloadInfo.success = success;
        downloadedFilePath = download.getFile();
    }

    //download ui listener
    final ActionListener fileActionListener = new ActionListener() {
        @Override
        public void onPauseDownload(int id) {
            Log.e("DBG","ActionListener:onPauseDownload");
            fetch.pause(id);
        }

        @Override
        public void onCancelDownload(int id) {
            Log.e("DBG","ActionListener:onCancelDownload");
            fetch.cancel(id);
            DownloadListActivity.this.finish();
        }

        @Override
        public void onResumeDownload(int id) {
            Log.e("DBG","ActionListener:onResumeDownload");
            fetch.resume(id);
        }

        @Override
        public void onRemoveDownload(int id) {
            Log.e("DBG","ActionListener:onRemoveDownload");
            fetch.remove(id);
        }

        @Override
        public void onRetryDownload(int id) {
            Log.e("DBG","ActionListener:onRetryDownload");
            fetch.retry(id);
        }

        @Override
        public void onRecord() {
            Log.e("DBG","ActionListener:onRecord");
            if(downloadInfo.startMs==0) {
                Toast.makeText(DownloadListActivity.this,
                                "데이터 에러로 저장할 수 없습니다. 다시 다운로드 해 주세요",
                                Toast.LENGTH_SHORT).show();
                return;
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //etLog.append("Computing Hash...\n");
                    dialog = new ACProgressFlower.Builder(DownloadListActivity.this)
                            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                            .themeColor(Color.WHITE)
                            .text("Computing Hash")
                            .fadeColor(Color.DKGRAY).build();
                    dialog.show();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        File file = new File(downloadedFilePath);
                        downloadInfo.hash = Utils.generateSHA256(file);
                        downloadInfo.hashMatched = downloadInfo.hash.compareTo(downloadInfo.correctHash)==0;
                        Log.d(TAG,"onCompleted:" + downloadInfo.endMs + "/" + downloadInfo.hash);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    etLog.append("SHA256 Hash:" + downloadInfo.hash + "\n");

                    //
                    String json = new Gson().toJson(downloadInfo);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    Log.e(TAG,"recordResult:" + expID + "/" + json);
                    editor.putString(expID, json);
                    editor.commit();

                    //=== save raw exp data

                    String fileName = "expRawData_" + expID + "_" + downloadInfo.endMs + ".txt";
                    try {
                        String contents = expName + "\n" + etLog.getText().toString();
                        File writtenFile  = writeToFile(fileName, contents);
                        Toast.makeText(DownloadListActivity.this, "실험 로우데이터 저장에 성공했습니다..", Toast.LENGTH_SHORT).show();
                        openFile(writtenFile);
                    } catch (IOException ioe) {
                        Toast.makeText(DownloadListActivity.this, "실험 로우데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }

                    //===

                    updateDownload();
                    //

                    dialog.hide();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };



    public void openFile(File writtenFile) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", writtenFile);
        //Uri fileUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", writtenFile);
        intent.setData(fileUri);

        startActivity(intent);
    }

    private File writeToFile(String fileName, String content) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);

        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        writer.append(content);
        writer.flush();
        writer.close();

        return file;
    }


    //fetch downloader listener
    private final FetchListener fetchListener = new AbstractFetchListener() {

        @Override
        public void onAdded(@NotNull Download download) {
            Log.e("DBG","Fetch:onAdded");
            fileAdapter.addDownload(download);
        }

        @Override
        public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
            Log.e("DBG","Fetch:onQueued");
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        //fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int)
        @Override
        public void onStarted(@NotNull Download download, List<? extends DownloadBlock> downloadBlocks, int totalBlocks) {
            Log.e("DBG","Fetch:onStarted");
            downloadInfo.startMs = System.currentTimeMillis();

            Log.d(TAG,"started:" + downloadInfo.startMs);
            etLog.append("[" + Utils.msToDate(downloadInfo.startMs) + "]:Started\n"); //+gps

            downloadInfo.gpsDataList.clear();
            downloadInfo.gpsDataList.add(new GPSData(downloadInfo.startMs, lastGPSLocation.latitude, lastGPSLocation.longitude));

            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        //* @param etaInMilliSeconds Estimated time remaining in milliseconds for the download to complete.
        //* @param downloadedBytesPerSecond Average downloaded bytes per second.
        @Override
        public void onProgress(@NotNull Download download, long etaInMilliseconds, long downloadedBytesPerSecond) {
            Log.e("DBG","Fetch:onProgress");
            etLog.append("[" + Utils.getDate() + "]:" +
                    byteToMB(download.getDownloaded()) + "/" + byteToMB(download.getTotal()) + "MB, "
                    + byteToMB(downloadedBytesPerSecond) + "MB/s\n");

            long curMs = System.currentTimeMillis();
            if( (curMs - downloadInfo.gpsDataList.get(downloadInfo.gpsDataList.size()-1).ts) >= GPS_SAVE_PERIOD_MS )
                downloadInfo.gpsDataList.add(new GPSData(curMs, lastGPSLocation.latitude, lastGPSLocation.longitude));

            fileAdapter.update(download, etaInMilliseconds, downloadedBytesPerSecond);
        }

        float byteToMB(long bytes) {
            return Math.round(bytes/(1000f*1000f)*100)/100f;
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            Log.e("DBG","Fetch:onCompleted");
            recordDownloadResult(download, true);
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
            super.onError(download, error, throwable);
            Log.e("DBG","Fetch:onError");
            recordDownloadResult(download, false);
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onPaused(@NotNull Download download) {
            Log.e("DBG","Fetch:onPaused");
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onResumed(@NotNull Download download) {
            Log.e("DBG","Fetch:onResumed");
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCancelled(@NotNull Download download) {
            Log.e("DBG","Fetch:onCancelled");
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onRemoved(@NotNull Download download) {
            Log.e("DBG","Fetch:onRemoved");
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onDeleted(@NotNull Download download) {
            Log.e("DBG","Fetch:onDeleted");
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }
    };


    //location
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }
}