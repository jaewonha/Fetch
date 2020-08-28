package com.tonyodev.fetchapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;


public class SetupActivity extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        ((TextView)findViewById(R.id.tvIPAddr)).setText("IPAddr: " + Data.URL);

        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        ((TextView)findViewById(R.id.tv5G)).setText("is5G:" + (SetupActivity.isNRConnected(tm) ? "5G" : "not 5G"));
    }

    static boolean isNRConnected(TelephonyManager telephonyManager) {
        try {
            Object obj = Class.forName(telephonyManager.getClass().getName())
                    .getDeclaredMethod("getServiceState", new Class[0]).invoke(telephonyManager, new Object[0]);

            Method[] methods = Class.forName(obj.getClass().getName()).getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().equals("getNrStatus") || method.getName().equals("getNrState")) {
                    method.setAccessible(true);
                    return ((Integer) method.invoke(obj, new Object[0])).intValue() == 3;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loadRealample() {

    }


}
