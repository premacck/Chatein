package com.prembros.chatein.data.service;

public interface UploadCallbacks {

    void onProgressUpdate(int percentage);

    void onUploadError(Exception exception);

    void onUploadFinish(String downloadUrl);
}