package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;

import java.util.Scanner;


public class PingActivity extends AppCompatActivity  {

    //private final int TEST_CNT = 10;
    private final int TEST_CNT = 50;
    //private final int TEST_CNT = 1000;

    SharedPreferences sharedpreferences;
    EditText etSummary, etFullLog;
    Handler handler;

    //PingStats pingStats;
    LatencyInfo latencyInfo;
    String expName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        etSummary = findViewById(R.id.etSummary);
        etFullLog = findViewById(R.id.etFullLog);

        handler = new Handler();
        expName = getIntent().getStringExtra("expName");

        ((TextView)findViewById(R.id.tvTitle)).setText("Ping Test");
        ((TextView)findViewById(R.id.tvTargetAddr)).setText(Data.IP);

        checkExpDone();
    }


    public void btnClickStart(View v) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    runPingTest();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(()->
                            Toast.makeText(PingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void btnClickRecord(View v) {
        String resultRaw = etFullLog.getText().toString();

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        Log.d("DBG", "pingStats.getAverageTimeTaken():" + latencyInfo.avg); //ms
        //Log.d("DBG", "pingStats.getAverageTimeTakenMillis():" + pingStats.getAverageTimeTakenMillis()); //bug
        editor.putString("pingExpResultRaw", resultRaw);
        editor.putString("pingExpResult", new Gson().toJson(latencyInfo));
        editor.commit();
        
        checkExpDone();
    }

    private void checkExpDone() {
        String expData = sharedpreferences.getString("pingExpResult", null);
        if(expData!=null) {
            latencyInfo = new Gson().fromJson(expData, LatencyInfo.class);
            etSummary.setText("*핑 응답속도 실험결과\n" + latencyInfo.statsWithJudge(true));
            findViewById(R.id.llCmd).setVisibility(View.GONE);
            findViewById(R.id.etFullLog).setVisibility(View.GONE);
        }
    }

    private void runPingTest() {
        Ping.onAddress(Data.IP)
            .setTimes(TEST_CNT)
            .setDelayMillis(500)
            .setTimeOutMillis(1000)
            .doPing(new Ping.PingListener() {
                @Override
                public void onResult(PingResult pingResult) {
                    if(pingResult.isReachable) {
                        logSummary("ping: " + pingResult.timeTaken + "ms");
                        logFull("pingResult:" + pingResult.toString());
                    } else {
                        logAll("pingResult.error:" + pingResult.error);
                    }
                }

                @Override
                public void onFinished(PingStats pingStats) {
                    //PingActivity.this.pingStats = pingStats;
                    latencyInfo = new LatencyInfo(expName,
                            TEST_CNT, (int)pingStats.getNoPings(), (int)pingStats.getPacketsLost(),
                            pingStats.getAverageTimeTaken(), pingStats.getMinTimeTaken(), pingStats.getMaxTimeTaken() );
                    //logAll("\n*PingTestResult\n" + PingActivity.this.pingStats.toString());
                    logAll(latencyInfo.toString());
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    logAll("pingResult.error:" + e.getMessage());
                }
            });
    }


    private void logSummary(String msg) {
        handler.post(()-> etSummary.append(msg + "\n"));
    }

    private void logFull(String msg) {
        handler.post(()-> etFullLog.append(msg + "\n"));
    }

    private void logAll(String msg) {
        handler.post(()-> {
            etSummary.append(msg + "\n");
            etFullLog.append(msg + "\n");
        });
    }
}
