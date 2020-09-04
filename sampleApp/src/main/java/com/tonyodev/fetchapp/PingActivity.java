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
import android.widget.Toast;

import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;

import java.util.Scanner;


public class PingActivity extends AppCompatActivity  {

    private final int TEST_CNT = 50;

    SharedPreferences sharedpreferences;
    EditText etSummary, etFullLog;
    Handler handler;

    PingStats pingStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        etSummary = findViewById(R.id.etSummary);
        etFullLog = findViewById(R.id.etFullLog);

        handler = new Handler();

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

        String lastLine = null;
        Scanner scanner = new Scanner(resultRaw);
        while (scanner.hasNextLine()) {
            lastLine = scanner.nextLine();
        }

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("pingExpResultRaw", resultRaw);
        editor.putString("pingExpResultSummary", this.pingStats.toString() +
                "\n판정:" + ( pingStats.getAverageTimeTaken()/1000.0f < Data.LATENCY_GOAL_MS ? "성공" : "실패"));
        editor.commit();
        
        checkExpDone();
    }

    private void checkExpDone() {
        String expData = sharedpreferences.getString("pingExpResultSummary", null);
        if(expData!=null) {
            etSummary.setText("*실험결과데이터\n" + expData);
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
                    PingActivity.this.pingStats = pingStats;
                    logAll("\n*PingTestResult\n" + PingActivity.this.pingStats.toString());
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
