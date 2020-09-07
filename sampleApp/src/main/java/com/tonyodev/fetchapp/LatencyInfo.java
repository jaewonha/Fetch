package com.tonyodev.fetchapp;

import java.net.Socket;

public class LatencyInfo {
    String name;
    int cnt, ok, fail;
    float avg, min, max;

    public LatencyInfo(String name, int cnt, int ok, int fail, float avg, float min, float max) {
        this.name = name;

        this.cnt = cnt;
        this.ok = ok;
        this.fail = fail;

        this.avg = avg;
        this.min = min;
        this.max = max;
    }


    public String statsWithJudge(boolean withJudge) {
        String msg =
                "Count:" + cnt + ", OK:" + ok + ", Fail:" + fail + "\n" +
                "Average:" + avg + ", Min:" + min + ", Max:" + max + "\n";

        String judge = "\n[판정]\n" +
                        " 속도측정(avg<16.6ms):" + ( passLatencyTest() ? "PASS" : "FAIL");

        return msg + (withJudge ? judge : "");
    }

    @Override
    public String toString() {
        return statsWithJudge(false);
    }


    public boolean passLatencyTest() {
        return avg < Data.LATENCY_GOAL_MS;
    }
}
