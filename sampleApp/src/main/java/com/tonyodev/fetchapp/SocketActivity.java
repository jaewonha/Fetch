package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/*
    · (고정측정) 코엑스 실내외 특정 구간(예시: 파르나스 몰 입구 진입구간, 별마당 도서관, 메가박스 영화관)에서 고정상태 측정

    · (이동측정) 이동상태(파르나스 ~ 별마당, 별마당 ~ 메가박스, 메가박스 ~ 파르나스)에서 이동상태 측정 (기지국이오버랩되지 않은 위치 확인 필요)

   ① (속도측정) 이통3사 MEC 서버에 저장된 속도 측정용 콘텐츠*를 이통사별 단말기 10대**가 동시 접속하여 콘텐츠를 스트리밍 받으면서 5G 네트워크 속도 측정. 영상을 고화질로 재생하기 위한 속도가 일정하게 유지되어야 함
 */
public class SocketActivity extends AppCompatActivity  {

    //private final int PORT = 8080;
    private final int TEST_LENGTH = 128;
    private final int TEST_CNT = 100;
    //private final int TEST_CNT = 1000;

    SharedPreferences sharedpreferences;
    EditText etExpResult;
    Handler handler;

    String expName;
    float avgMs;
    LatencyInfo latencyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        etExpResult = findViewById(R.id.etExpResult);

        handler = new Handler();
        expName = getIntent().getStringExtra("expName");

        ((TextView)findViewById(R.id.tvTitle)).setText("Socket Test");
        ((TextView)findViewById(R.id.tvTargetAddr)).setText(Data.IP + ":" + Data.SOCKET_PORT);
        checkExpDone();
    }


    public void btnClickStart(View v) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    runSocketTest();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(()->
                        Toast.makeText(SocketActivity.this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void btnClickRecord(View v) {
        String resultRaw = etExpResult.getText().toString();

//        String lastLine = null;
//        Scanner scanner = new Scanner(resultRaw);
//        while (scanner.hasNextLine()) {
//            lastLine = scanner.nextLine();
//        }

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("socketExpResultRaw", resultRaw);
        editor.putString("socketExpResult", new Gson().toJson(latencyInfo));

        editor.commit();

        checkExpDone();
    }

    private void checkExpDone() {
        String expData = sharedpreferences.getString("socketExpResult", null);
        if(expData!=null) {
            latencyInfo = new Gson().fromJson(expData, LatencyInfo.class);
            etExpResult.setText("*소켓 응답속도 실험결과:\n" + latencyInfo.statsWithJudge(true));
            findViewById(R.id.llCmd).setVisibility(View.GONE);
        }
    }

    private void runSocketTest() throws IOException {

        Socket socket = new Socket(Data.IP, Data.SOCKET_PORT);

        log("초기화:시작\n");

        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        log("초기화:완료\n");


        Random random = new Random();
        byte[] send = new byte[TEST_LENGTH];
        byte[] recv = new byte[TEST_LENGTH];

        float minMs = 99999999f, maxMs=-1f;
        long startMs = System.currentTimeMillis();
        int i=0, ok = 0, fail = 0;
        for(; i<TEST_CNT; i++) {
            long _startMs = System.currentTimeMillis();
            random.nextBytes(send);

            out.write(send);

            in.read(recv, 0, recv.length);

            if(Arrays.equals(send, recv)) {
                log("수신["+(i+1)+"]:OK\n"); ++ok;
            } else {
                log("수신["+(i+1)+"]:Fail(send:" + new String(send) + "/recv:" + new String(recv) + ")\n" ); ++fail;
                break;
            }
            long _endMs = System.currentTimeMillis();

            long _durMs = _endMs - _startMs;
            if(_durMs < minMs) minMs = _durMs;
            if(_durMs > maxMs) maxMs = _durMs;
        }
        long endMs = System.currentTimeMillis();
        socket.close();

        avgMs = (endMs-startMs)/(float)i;
        latencyInfo = new LatencyInfo(expName, i, ok, fail, avgMs, minMs, maxMs);

        log( "\n" + latencyInfo.toString());
    }

    private void log(String msg) {
        handler.post(()-> etExpResult.append(msg));
    }
}
