package com.tonyodev.fetchapp;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public final class Data {

    public static final String[] sampleUrls = new String[]{
            "http://speedtest.ftp.otenet.gr/files/test100Mb.db",
//            "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v",
//            "http://media.mongodb.org/zips.json",
//            "http://www.exampletonyotest/some/unknown/123/Errorlink.txt",
//            "http://storage.googleapis.com/ix_choosemuse/uploads/2016/02/android-logo.png",
//            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    };


//    public static final String[] sampleIDs = new String[]{
//            "http://speedtest.ftp.otenet.gr/files/test100Mb.db", //0
//            "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v",
//            "http://media.mongodb.org/zips.json",
//            "http://www.exampletonyotest/some/unknown/123/Errorlink.txt",
//            "http://storage.googleapis.com/ix_choosemuse/uploads/2016/02/android-logo.png",
//            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//            "http://speedtest.ftp.otenet.gr/files/test100Mb.db",
//            "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v",
//            "http://media.mongodb.org/zips.json",
//            "http://www.exampletonyotest/some/unknown/123/Errorlink.txt" //9
//    };

      //public static final String URL = "http://192.168.1.62:8080;
      public static final String URL = "http://5gmec-test.maxstlab.com";
      
//    public static final String[] sampleIDs = new String[]{
//            URL + "/data/1000M.raw", //0
//            URL + "/data/100M.raw",
//            URL + "/data/1000M.raw",
//            URL + "/data/100M.raw",
//            URL + "/data/1000M.raw",
//            URL + "/data/100M.raw",
//            URL + "/data/1000M.raw",
//            URL + "/data/100M.raw",
//            URL + "/data/1000M.raw",
//            URL + "/data/100M.raw",
//    };

    public static final String[] sampleIDs = new String[]{
            URL + "/data/100M.raw", //0
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
            URL + "/data/100M.raw",
    };

//    public static final String[] sampleIDs = new String[]{
//            URL + "/data/1K.raw", //0
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//            URL + "/data/1K.raw",
//    };



    private Data() {

    }

    @NonNull
    private static List<Request> getFetchRequests() {
        final List<Request> requests = new ArrayList<>();
        for (String sampleUrl : sampleUrls) {
            final Request request = new Request(sampleUrl, getFilePath(sampleUrl));
            requests.add(request);
        }
        return requests;
    }

    @NonNull
    public static List<Request> getFetchSampleWithId(final int id) {
        final List<Request> requests = new ArrayList<>();

        final Request request = new Request(sampleIDs[id], getFilePath(sampleIDs[id]));
        requests.add(request);

        return requests;
    }

    @NonNull
    public static List<Request> getFetchRequestWithGroupId(final int groupId) {
        final List<Request> requests = getFetchRequests();
        for (Request request : requests) {
            request.setGroupId(groupId);
        }
        return requests;
    }



    @NonNull
    private static String getFilePath(@NonNull final String url) {
        final Uri uri = Uri.parse(url);
        final String fileName = uri.getLastPathSegment();
        final String dir = getSaveDir();
        return (dir + "/DownloadList/" + fileName);
    }

    @NonNull
    static String getNameFromUrl(final String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    @NonNull
    public static List<Request> getGameUpdates() {
        final List<Request> requests = new ArrayList<>();
        final String url = "http://speedtest.ftp.otenet.gr/files/test100k.db";
        for (int i = 0; i < 10; i++) {
            final String filePath = getSaveDir() + "/gameAssets/" + "asset_" + i + ".asset";
            final Request request = new Request(url, filePath);
            request.setPriority(Priority.HIGH);
            requests.add(request);
        }
        return requests;
    }

    @NonNull
    public static String getSaveDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/fetch";
    }

}
