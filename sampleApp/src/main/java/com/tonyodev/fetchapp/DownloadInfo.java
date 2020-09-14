package com.tonyodev.fetchapp;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DownloadInfo {
    public final static String PREF_NAME = "DownloadInfo";

    String name;
    String url, hash, correctHash;
    long startMs, endMs, durMs, size;
    float bytePerSec, _bytePerSec;
    boolean hashMatched, success;
    ArrayList<GPSData> gpsDataList = new ArrayList<>();
    
    @Override
    public String toString() {
        return
            "URL:" + url + "\n" +
            "시작시간:" + startMs + "(" + Utils.msToDate(startMs) + ")\n" +
            "종료시간:" + endMs + "(" + Utils.msToDate(endMs) + ")\n" +
            "걸린시간:" + durMs + "ms\n" +
            "속도:" + (Math.round(bytePerSec*100.0f/1000.0f)/100.0f) + "MB/s\n" +
            "다운로드:" + size + "bytes\n" +
            //"속도(추정, BytesPerSec): " + _bytePerSec + "\n" +
            "해시값:" + hash + "\n" +
            "원본해시:" + correctHash+ "\n" +
            "\n" +
            "GPS:" + new Gson().toJson(gpsDataList)+ "\n" +
            "\n" +
            "[판정]\n" +
            "속도(걸린시간<180sec):" + (passLatencyTest() && success ? "PASS" : "FAIL") + "\n" +
            "데이터무결성(해시값일치):" + (passIntegrityTest() ? "PASS" : "FAIL") + "\n";
    }

    public boolean passLatencyTest() {
        return durMs < Data.DOWNLOAD_GOAL_MS;
    }

    public boolean passIntegrityTest() {
        return hash.compareTo(correctHash)==0;
    }
}

