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

    public static String IP = "3.34.147.254"; //MAXST
    //public static String IP = "10.50.109.101"; //LG U+
    //public static String IP = "211.246.70.13"; //KT

    public static int SOCKET_PORT = 8080;

    public static String URL = "http://" + Data.IP;

    public static float LATENCY_GOAL_MS = 1000f/60f;

    public static final String[] sampleIDs = new String[]{
            URL + "/data/70mbps_3min/01.rand",
            URL + "/data/70mbps_3min/02.rand",
            URL + "/data/70mbps_3min/03.rand",
            URL + "/data/70mbps_3min/04.rand",
            URL + "/data/70mbps_3min/05.rand",
            URL + "/data/70mbps_3min/06.rand",
            URL + "/data/70mbps_3min/07.rand",
            URL + "/data/70mbps_3min/08.rand",
            URL + "/data/70mbps_3min/09.rand",
            URL + "/data/70mbps_3min/10.rand"
    };

    public static final String[] sampleHashs = new String[]{
            "904788544bf0fb42afa5105f4056254cc42e259f79b37fe59a91129f280013db", //01.rand
            "6cc92fbae0973888140d4e4fc3bdb5ddf69e110dcad11386e8250487a3d99572", //  02.rand
            "06a3f8f97c26f59bd0b16b4a38d2c391646fcf320626d9f3691e2e92bb9730a1" , // 03.rand
            "7b074a231a2c9d6c05aed35ab333b9bcedc2de6626522c8db6dfca83c8357c63", //  04.rand
            "518bb19022b6965c0c1ea57183706b70e5d9d71d6755908ea52c9d977b9711f3", //  05.rand
            "988ad1d4ec4d727bd197008896a0e9216f6a80a44be2a3656150a68ea0d19bc9", //  06.rand
            "dc4f7c3e3ce7fd34b439af2344c627b556f8e420ee86218205661cbcc71afa2f", //  07.rand
            "8da8ca3d34dafa7b49fd3d6ff7c08b7699a8654f4b4e26e38fe171b7f18ebe34", //  08.rand
            "bcbcd40ded249060b29e77eeef56137d78a8bb6093b71cbda219d4eb7466cd5b", //  09.rand
            "d0540b559b8b34a437b8dead1bcf884dbc88aac34fb3202942b13d90358b91b9", //  10.rand
    };
    
//    public static final String[] sampleIDs = new String[]{
//            URL + "/data/33mbps_3min/01.rand",
//            URL + "/data/33mbps_3min/02.rand",
//            URL + "/data/33mbps_3min/03.rand",
//            URL + "/data/33mbps_3min/04.rand",
//            URL + "/data/33mbps_3min/05.rand",
//            URL + "/data/33mbps_3min/06.rand",
//            URL + "/data/33mbps_3min/07.rand",
//            URL + "/data/33mbps_3min/08.rand",
//            URL + "/data/33mbps_3min/09.rand",
//            URL + "/data/33mbps_3min/10.rand"
//    };
//
//    public static final String[] sampleHashs = new String[]{
//            "894bebe93b07f7b2a8baff6f1ba60dbc19291d62d9328fdccaa4542ddf594564", // 01.rand
//            "ead0e5019b73c9fda53ec2b7d851b4599f25fdb934dbfe7237d4e2aa00246777", // 02.rand
//            "b169286fb4f634075b033967ecdaa3f806c7ad72e39a356866bea450f04d636b", // 03.rand
//            "6a96f1066c3db740c376e7645643e4b20a0fa9971c7d28a338fd1df44867bd88", // 04.rand
//            "5a41eded44455bd674605143feffa3c2e921197f939d7f510bfbdf28796774cd", // 05.rand
//            "c92e55cfc0990396abc3bb01c104e29893c2579b2f20150603554d8a3018e87e", // 06.rand
//            "caa2133e41f655d1cdc5f4a4cebef7edbb699379571d4e449fd0c3bfeb87578e", // 07.rand
//            "a415cb203ee9249d24426a9780f6846f6902e4a9fc79c631e2f1b4072fdb76b3", // 08.rand
//            "029fc5bc9641f49033024803d3a5ae6c3dbc40072922df75ef84e664f7f3b2da", // 09.rand
//            "95c3138532b2ac80a7c29b3ac0924211e95eb1e87bd1cc951414391f1d21a6ce"  // 10.rand
//    };

//    public static final String[] sampleIDs = new String[]{
//            URL + "/data/10M/01.rand",
//            URL + "/data/10M/02.rand",
//            URL + "/data/10M/03.rand",
//            URL + "/data/10M/01.rand",
//            URL + "/data/10M/02.rand",
//            URL + "/data/10M/03.rand",
//            URL + "/data/10M/01.rand",
//            URL + "/data/10M/02.rand",
//            URL + "/data/10M/03.rand",
//            URL + "/data/10M/01.rand",
//    };
//
//    public static final String[] sampleHashs = new String[]{
//            "bb19ad91946c92b906f903aef59bb6282c347f367d8cb8f67eb97e31996714a8",
//            "dfd22a49474e4a820b6c9bbba6e433e2494bb398f94372a8ebc2fcef39ebae1c",
//            "fbefc8b2ee3885c7080e7e23e5c84c1f0dc19be422687fc7681f90e9cef33621",
//            "bb19ad91946c92b906f903aef59bb6282c347f367d8cb8f67eb97e31996714a8",
//            "dfd22a49474e4a820b6c9bbba6e433e2494bb398f94372a8ebc2fcef39ebae1c",
//            "fbefc8b2ee3885c7080e7e23e5c84c1f0dc19be422687fc7681f90e9cef33621",
//            "bb19ad91946c92b906f903aef59bb6282c347f367d8cb8f67eb97e31996714a8",
//            "dfd22a49474e4a820b6c9bbba6e433e2494bb398f94372a8ebc2fcef39ebae1c",
//            "fbefc8b2ee3885c7080e7e23e5c84c1f0dc19be422687fc7681f90e9cef33621",
//            "bb19ad91946c92b906f903aef59bb6282c347f367d8cb8f67eb97e31996714a8"
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
    public static List<Request> getFetchSampleWithUrl(String url) {
        final List<Request> requests = new ArrayList<>();

        final Request request = new Request(url, getFilePath(url));
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
