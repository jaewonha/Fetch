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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;


public class PingActivity extends AppCompatActivity  {

    private final String URL = "5gmec-test.maxstlab.com";
    private final int TEST_CNT = 50;

    SharedPreferences sharedpreferences;
    EditText etSummary, etFullLog;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        etSummary = findViewById(R.id.etSummary);
        etFullLog = findViewById(R.id.etFullLog);

        handler = new Handler();

        ((TextView)findViewById(R.id.tvTitle)).setText("Ping Test");
    }


    public void btnClickStart(View v) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    runPingTest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void runPingTest() throws Exception {
        Ping.onAddress(URL)
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
                    logAll("\n*PingTestResult\n" + pingStats.toString());
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
