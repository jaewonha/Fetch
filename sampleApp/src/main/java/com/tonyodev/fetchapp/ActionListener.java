package com.tonyodev.fetchapp;

public interface ActionListener {

    void onCancelDownload(int id);

    void onPauseDownload(int id);

    void onResumeDownload(int id);

    void onRemoveDownload(int id);

    void onRetryDownload(int id);

    void onRecord();
}
