package com.tonyodev.fetchapp;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class Data {

    //vars - fixed
    public static int SOCKET_PORT = 8080;
    public static float DOWNLOAD_GOAL_MS = 180f*1000;
    public static float LATENCY_GOAL_MS  = 1000f/60f;

    //vars - variable
    public static String IP = null;
    public static String URL = null;
    public static String[] sampleIDs = null;
    public static String[] sampleHashs = null;

    //10M
    public static final String[] sampleIDs_10M = new String[]{
            "/data/10M/01.rand",
            "/data/10M/02.rand",
            "/data/10M/03.rand",
            "/data/10M/04.rand",
            "/data/10M/05.rand",
            "/data/10M/06.rand",
            "/data/10M/07.rand",
            "/data/10M/08.rand",
            "/data/10M/09.rand",
            "/data/10M/10.rand",
    };

    public static final String[] sampleHashs_10M = new String[]{
            "4833467d055f93c4a35bf397ab710d1086c0d4872fbf3aff2d83fa4cb206ff13", //01.rand
            "0ced88e837d891913ae0fe36d1624c148070f4822ec00c0847ea0a748293fd20", //  02.rand
            "150a912c2eb6ecde70454942f475cd8fb70d4758da751bf992aba059243b19c1", //  03.rand
            "84577f5f05caecb7037450638101a04b077cb49c68282b5190d8ac70ce88babd", //  04.rand
            "eb1306fbee11cb47ea1cd28664c8d87e8a5304241d2b06e98484586eaeb95c3e", //  05.rand
            "13ce4f66c07067eb41f77299add5e22e48488cd7a03333eba7af7e288e70b683", //  06.rand
            "7afff8b8e2557901452c5cd1c01b27b341d153ef2488845e1028409717fb22f3", //  07.rand
            "88319258786606b7077c80049a97bbd1b79f71298d4fca4a7c39f28d88882e4c", //  08.rand
            "9e20e82a823fdd515f577bc62c530855281c180ff3e1c47926d03c2f7ee6a634", //  09.rand
            "00d84eecef9c5827bcd355548ac9958dfe103706c1ab3754ce53de0df9e4132f", //  10.rand
    };

    //33mbps
    public static final String[] sampleIDs_33mbps = new String[]{
            "/data/33mbps_3min/01.rand",
            "/data/33mbps_3min/02.rand",
            "/data/33mbps_3min/03.rand",
            "/data/33mbps_3min/04.rand",
            "/data/33mbps_3min/05.rand",
            "/data/33mbps_3min/06.rand",
            "/data/33mbps_3min/07.rand",
            "/data/33mbps_3min/08.rand",
            "/data/33mbps_3min/09.rand",
            "/data/33mbps_3min/10.rand"
    };

    public static final String[] sampleHashs_33mbps = new String[]{
            "894bebe93b07f7b2a8baff6f1ba60dbc19291d62d9328fdccaa4542ddf594564", // 01.rand
            "ead0e5019b73c9fda53ec2b7d851b4599f25fdb934dbfe7237d4e2aa00246777", // 02.rand
            "b169286fb4f634075b033967ecdaa3f806c7ad72e39a356866bea450f04d636b", // 03.rand
            "6a96f1066c3db740c376e7645643e4b20a0fa9971c7d28a338fd1df44867bd88", // 04.rand
            "5a41eded44455bd674605143feffa3c2e921197f939d7f510bfbdf28796774cd", // 05.rand
            "c92e55cfc0990396abc3bb01c104e29893c2579b2f20150603554d8a3018e87e", // 06.rand
            "caa2133e41f655d1cdc5f4a4cebef7edbb699379571d4e449fd0c3bfeb87578e", // 07.rand
            "a415cb203ee9249d24426a9780f6846f6902e4a9fc79c631e2f1b4072fdb76b3", // 08.rand
            "029fc5bc9641f49033024803d3a5ae6c3dbc40072922df75ef84e664f7f3b2da", // 09.rand
            "95c3138532b2ac80a7c29b3ac0924211e95eb1e87bd1cc951414391f1d21a6ce"  // 10.rand
    };

    //90mbps
    public static final String[] sampleIDs_90MBps = new String[]{
            "/data/90mbps_3min/01.rand",
            "/data/90mbps_3min/02.rand",
            "/data/90mbps_3min/03.rand",
            "/data/90mbps_3min/04.rand",
            "/data/90mbps_3min/05.rand",
            "/data/90mbps_3min/06.rand",
            "/data/90mbps_3min/07.rand",
            "/data/90mbps_3min/08.rand",
            "/data/90mbps_3min/09.rand",
            "/data/90mbps_3min/10.rand"
    };

    public static final String[] sampleHashs_90MBps_SK = new String[] { //skt, lg
            "2ffc7666d0f5313aba28cdab4ae49299b1b0fb91d4ef8583e2b19383e107b0c5", //01.rand
            "f0a4e7e2bb066e44b4c354f1560ac843c74b6588c24e2881f558d67987a2e6fb", //  02.rand
            "671cbd578fd9fa42f5f61c4c99514218bca8eb658ebdfe4802c85a23e7655122", //  03.rand
            "40c768d861a38cec96d85ff638b8bc44d8ede38b34e1bfe83e369c14211673c9", //  04.rand
            "3dcd5e1541db2403810a96d5a849fb578a1ef61d63dc03ce4383aae474248210", //  05.rand
            "eb008f3e84ac02eb64b8e7d2ce5fbeb9c24686c0cadc7aaf81515b725119ca26", //  06.rand
            "81d2900b091b34c7270960af815bb162e65438e8cbce1862df145fb64f039184", //  07.rand
            "414ffdeb8338c33a9f349ae4d04de674e5293bf7445e1fa8794d72df2ad2b8ce", //  08.rand
            "0dd51e8ff4026c6d45b9957ee71fdd3fa2d2012e71e17d01bf0539f7ee03c6a0", //  09.rand
            "3e9cd3c186666846d276377bc429676cdbff3e24a4ce3f560252a7e3e716e0cf", //  10.rand
            "2c4e52d9ba988b440240c54a11a993428d673d30384fd0be3c5793f4337492d4", //  09.rand
            "59b48c97435325407a053b50d2df79122e26d6cde238af842c3f17ccfde39f36", //  10.rand
    };

    public static final String[] sampleHashs_90MBps_KT = new String[]{ //kt
            "446b4213d5e5282f9ad5a54da9e300a6404c7cf6a7ea9e266d27c53e9866e410", //01.rand
            "5e9b2f1cc94015107c67747670e6069f936a1faa9bbaa0b21f0f7ac495a5e9d4", //02.rand
            "45230df6cb179cc76dbacc402b5c11495ab2ff878676b30a567144d703dd1d95", //03.rand
            "135f974aa2223183fb88124ac13e3bc2a43060b63d1ba4aae12f3b79f9e4de5d", //04.rand
            "b77e0afb6492f0dd416959d59ce6852ebd8d47a9ab2adf8322c1f34d821bcf85", //05.rand
            "0ed2a55766df36049bfeebb517f2245fe98e705c10712625fe49020fbcd433b3", //06.rand
            "d031bb67d909dff206038fe847a8f768d916fccad6583444b4ccd10370a88f7d", //07.rand
            "c9710efcc2dbe47e5eda7c53a2c9648fac9abe865efaf62c35b283d7e60ebe8f", //08.rand
            "691877236a9763c5dc632213506e9c5486b14acc7fc1e888bf468539d568df15", //09.rand
            "0750c276d243ed7836ee3b95ed4fa14ac0c57484716c06bd52a2e5e9b916ced2", //10.rand
    };

    public static final String[] sampleHashs_90MBps_LG = new String[] { //lg
            "43b80f137a9bc1306601f68aba5c21a69aa5193d43a7bdbd60cde19ee829e444",  //01.rand
            "afe4726b0807556496d1063e41354e6c990d4e44f4ede15e1fe3a5c325c8809c",  //02.rand
            "5b48d3d29431a422dd91f0371bc8fed811f62a53fae159df760167fe9135db44",  //03.rand
            "aafe7434961fe58f0dd7a1cbb0228a0afd2467a349586f693950c6a24cf25b45",  //04.rand
            "7c83ffcdb795c10edff0c248c7706437c3cc77a9f08c822dce7223db57085da3",  //05.rand
            "841d390321f00dce660a4dc95e5b1743350ce519bf7538288be7bd33477e1bdb",  //06.rand
            "80468523d6ff5159fd0510661aa19d51f3447473a2aabc50ade1b0fe433a9621",  //07.rand
            "3baf1257c6a799c3a3256dbd3c2f821a6b64b125acfa2dd99f8580ae630a39d4",  //08.rand
            "d3ad7df9d1073a8bce1fc995dc3c3278644318baa2058c9da00504f539c0882f",  //09.rand
            "91d76b9dd626563e0ec7870eb852ffebc00c5d54ac1c9ce0f568a62259997a81",  //10.rand
    };


    private Data() { }

    enum ModeServer {
        SK,
        KT,
        LG,
        MAXST
    }


    enum ModeBitStream {
        BS_10M,
        BS_33mbps,
        BS_90mbps,
    }

    public static void init(ModeServer modeServer, ModeBitStream modeBitstream) {

        switch(modeServer) {
            case SK: IP     = "223.62.93.153";  break;
            case KT: IP     = "211.246.70.13";  break;
            case LG: IP     = "10.50.109.101";  break;
            case MAXST: IP  = "3.34.147.254";   break;
        }

        Data.URL = "http://" + Data.IP;

        switch(modeBitstream) {
            case BS_10M:
                sampleIDs   = Arrays.copyOf(sampleIDs_10M, sampleIDs_10M.length);
                sampleHashs = sampleHashs_10M;
                break;
            case BS_33mbps:
                sampleIDs =  Arrays.copyOf(sampleIDs_33mbps, sampleIDs_33mbps.length);
                sampleHashs = sampleHashs_33mbps;
                break;
            case BS_90mbps:
                sampleIDs   = Arrays.copyOf(sampleIDs_90MBps, sampleIDs_90MBps.length);
                if(modeServer== ModeServer.SK) sampleHashs = sampleHashs_90MBps_SK;
                if(modeServer== ModeServer.KT) sampleHashs = sampleHashs_90MBps_KT;
                if(modeServer== ModeServer.LG) sampleHashs = sampleHashs_90MBps_LG;
                if(modeServer== ModeServer.MAXST) sampleHashs = sampleHashs_90MBps_SK;
                break;
        }

        for(int i=0; i<sampleIDs.length; i++) {
            sampleIDs[i] = Data.URL + sampleIDs[i];
        }

    }

    public static boolean isInitialized() {
        return (
            Data.IP != null && Data.URL != null
            && Data.sampleIDs != null && Data.sampleHashs != null
        );
    }

    @NonNull
    public static List<Request> getFetchSampleWithUrl(String url) {
        final List<Request> requests = new ArrayList<>();

        final Request request = new Request(url, getFilePath(url));
        requests.add(request);

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
    public static String getSaveDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/fetch";
    }

}
