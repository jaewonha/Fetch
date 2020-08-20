package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

/*
    · (고정측정) 코엑스 실내외 특정 구간(예시: 파르나스 몰 입구 진입구간, 별마당 도서관, 메가박스 영화관)에서 고정상태 측정

    · (이동측정) 이동상태(파르나스 ~ 별마당, 별마당 ~ 메가박스, 메가박스 ~ 파르나스)에서 이동상태 측정 (기지국이오버랩되지 않은 위치 확인 필요)

   ① (속도측정) 이통3사 MEC 서버에 저장된 속도 측정용 콘텐츠*를 이통사별 단말기 10대**가 동시 접속하여 콘텐츠를 스트리밍 받으면서 5G 네트워크 속도 측정. 영상을 고화질로 재생하기 위한 속도가 일정하게 유지되어야 함
 */
public class DownloadListActivity extends AppCompatActivity implements ActionListener {

    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final long UNKNOWN_REMAINING_TIME = -1;
    private static final long UNKNOWN_DOWNLOADED_BYTES_PER_SECOND = 0;
    private static final int GROUP_ID = "listGroup".hashCode();
    static final String FETCH_NAMESPACE = "DownloadListActivity";
    static final String TAG = "DownloadListActivity";

    private View mainView;
    private FileAdapter fileAdapter;
    private Fetch fetch;

    SharedPreferences sharedpreferences;
    private String expTag;
    private DownloadInfo downloadInfo;
    private EditText etLog;


    ACProgressFlower dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        setUpViews();
        final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(1)
                .setHttpDownloader(new OkHttpDownloader(Downloader.FileDownloaderType.SEQUENTIAL))
                .setNamespace(FETCH_NAMESPACE)
//                .setNotificationManager(new DefaultFetchNotificationManager(this) {
//                    @NotNull
//                    @Override
//                    public Fetch getFetchInstanceForNamespace(@NotNull String namespace) {
//                        return fetch;
//                    }
//                })
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);


        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);

        expTag = getIntent().getStringExtra("expTag");
        Log.e(TAG,"expTag:" + expTag);

        downloadInfo = new DownloadInfo();
        etLog = findViewById(R.id.etLog);

        checkStoragePermissions();
    }

    private void checkExpDone() {
        String expData = sharedpreferences.getString(expTag, null);
        if(expData!=null) {
            showExpSummary(expData);
        } else {
            enqueueDownloads();
        }
    }

    private void showExpSummary(String expData){
        DownloadInfo downloadInfo = new Gson().fromJson(expData, DownloadInfo.class);

        etLog.setText("*실험결과데이터\n" + downloadInfo.toString());

        findViewById(R.id.recyclerView).setVisibility(View.GONE);
    }

    private void setUpViews() {
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mainView = findViewById(R.id.activity_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fileAdapter = new FileAdapter(this);
        recyclerView.setAdapter(fileAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onPause() {
        super.onPause();
        fetch.removeListener(fetchListener);
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
        // code here to show dialog
        this.finish();
        super.onBackPressed();  // optional depending on your needs
    }

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            checkExpDone();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkExpDone();
        } else {
            Snackbar.make(mainView, R.string.permission_not_enabled, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    public void btnClickStart(View v) {
        enqueueDownloads();
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

    @Override
    public void onPauseDownload(int id) {
        fetch.pause(id);
    }

    @Override
    public void onResumeDownload(int id) {
        fetch.resume(id);
    }

    @Override
    public void onRemoveDownload(int id) {
        fetch.remove(id);
    }

    @Override
    public void onRetryDownload(int id) {
        fetch.retry(id);
    }


    @Override
    public void onRecord() {
        if(downloadInfo.startMs==0) {
            Toast.makeText(this, "데이터 에러로 저장할 수 없습니다. 다시 다운로드 해 주세요",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String json = new Gson().toJson(downloadInfo);

        SharedPreferences.Editor editor = sharedpreferences.edit();

        Log.e(TAG,"recordResult:" + expTag + "/" + json);
        editor.putString(expTag, json);
        editor.commit();

        checkExpDone();
    }


    private final FetchListener fetchListener = new AbstractFetchListener() {
        @Override
        public void onAdded(@NotNull Download download) {
            fileAdapter.addDownload(download);
        }

        @Override
        public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        //fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int)
        @Override
        public void onStarted(@NotNull Download download, List<? extends DownloadBlock> downloadBlocks, int totalBlocks) {
            downloadInfo.startMs = System.currentTimeMillis();

            Log.d(TAG,"started:" + downloadInfo.startMs);
            etLog.append("started:" + downloadInfo.startMs + "\n");

            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            downloadInfo.endMs = System.currentTimeMillis();
            downloadInfo.size = download.getTotal();

            downloadInfo.durMs = downloadInfo.endMs - downloadInfo.startMs;
            downloadInfo.bytePerSec = downloadInfo.size / (float) downloadInfo.durMs;
            downloadInfo._bytePerSec = download.getDownloadedBytesPerSecond();

            Log.d(TAG,"onCompleted:" + downloadInfo.endMs);
            etLog.append("completed:    " + downloadInfo.endMs + "\n");

            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);

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
                        File file = new File(download.getFile());
                        downloadInfo.hash = generateSHA256(file);
                        downloadInfo.hashMatched = downloadInfo.hash.compareTo(downloadInfo.correctHash)==0;
                        Log.d(TAG,"onCompleted:" + downloadInfo.endMs + "/" + downloadInfo.hash);
                        etLog.append("SHA256 Hash:" + downloadInfo.hash + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {

                    super.onPostExecute(aVoid);
                    dialog.hide();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        @Override
        public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
            super.onError(download, error, throwable);
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onProgress(@NotNull Download download, long etaInMilliseconds, long downloadedBytesPerSecond) {
            Log.d(TAG,"progress:" + etaInMilliseconds + "/" + downloadedBytesPerSecond);
            etLog.append("progress:" + etaInMilliseconds + "/" + downloadedBytesPerSecond + "\n");
            fileAdapter.update(download, etaInMilliseconds, downloadedBytesPerSecond);
        }

        @Override
        public void onPaused(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onResumed(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCancelled(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onRemoved(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onDeleted(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }
    };


    public static String generateMD5(File file) throws Exception {
        return hashFile(file, "MD5");
    }

    public static String generateSHA1(File file) throws Exception {
        return hashFile(file, "SHA-1");
    }

    public static String generateSHA256(File file) throws Exception {
        return hashFile(file, "SHA-256");
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    private static String hashFile(File file, String algorithm)
            throws Exception {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new Exception(
                    "Could not generate hash from file", ex);
        }
    }
}