package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*
    · (고정측정) 코엑스 실내외 특정 구간(예시: 파르나스 몰 입구 진입구간, 별마당 도서관, 메가박스 영화관)에서 고정상태 측정

    · (이동측정) 이동상태(파르나스 ~ 별마당, 별마당 ~ 메가박스, 메가박스 ~ 파르나스)에서 이동상태 측정 (기지국이오버랩되지 않은 위치 확인 필요)

   ① (속도측정) 이통3사 MEC 서버에 저장된 속도 측정용 콘텐츠*를 이통사별 단말기 10대**가 동시 접속하여 콘텐츠를 스트리밍 받으면서 5G 네트워크 속도 측정. 영상을 고화질로 재생하기 위한 속도가 일정하게 유지되어야 함
 */
public class ExperimentResultActivity extends AppCompatActivity  {

    private static final int STORAGE_PERMISSION_CODE = 100;

    SharedPreferences sharedpreferences;
    TextView tvExpResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_result);

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);

        checkStoragePermissions();

        tvExpResult = findViewById(R.id.tvExpResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            loadExpData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadExpData();
        } else {
            Snackbar.make(findViewById(R.id.activity_main), R.string.permission_not_enabled, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    public void btnClickSave(View v) {
        String expResult = tvExpResult.getText().toString();

        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

        String fileName = "exp_" + dateFormat.format(date) + ".txt";

        try {
            File writtenFile = writeToFile(fileName, expResult);

            Toast.makeText(this, "파일 저장 성공:" + writtenFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            openFile(writtenFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "파일 저장 실패:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

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

    private void loadExpData() {
        String[] expNameList = {
                "expFix1",
                "expFix2",
                "expFix3",
                "expMove1",
                "expMove2",
                "expMove3",
                "socketExpResultSummary",
                "pingExpResultSummary"
        };

        for(String expName : expNameList) {
            String expData = sharedpreferences.getString(expName, null);
            System.err.println(expName + ":" + expData);

            tvExpResult.append(expName + "\n\n");

            if(expData!=null) {
                if( expName.compareTo("socketExpResultSummary")==0 ||
                    expName.compareTo("pingExpResultSummary")==0 )
                {
                    tvExpResult.append(expData + "\n\n");
                } else {
                    DownloadInfo downloadInfo = new Gson().fromJson(expData, DownloadInfo.class);
                    tvExpResult.append(downloadInfo.toString() + "\n");
                }

            } else {
                tvExpResult.append("None\n\n");
            }

        }
    }
}
