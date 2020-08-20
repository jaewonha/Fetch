package com.tonyodev.fetchapp;

import java.util.Date;

public class DownloadInfo {
    public final static String PREF_NAME = "DownloadInfo";

    String url, hash, correctHash;
    long startMs, endMs, durMs, size;
    float bytePerSec, _bytePerSec;
    boolean hashMatched;

    @Override
    public String toString() {
        return
            "URL:" + this.url + "\n" +
            "시작시간:" + this.startMs + "ms\n" +
            "종료시간: " + this.endMs + "ms\n" +
            "걸린시간: " + this.durMs + "ms\n" +
            "속도: " + (Math.round(this.bytePerSec*100.0f/1000.0f)/100.0f) + "MB/Sec\n" +
            //"속도(추정, BytesPerSec): " + this._bytePerSec + "\n" +
            "해시 값: " + this.hash + "\n" +
            "원본 해시: " + this.correctHash+ "\n" +
            "해시 매칭 여부:" + this.hashMatched + "\n";
    }
}
