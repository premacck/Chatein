package com.prembros.chatein.data.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.prembros.chatein.util.Annotations.UploadCallback;

/**
 *
 * Created by Prem $ on 12/7/2017.
 */

public class VideoUploadReceiver extends ResultReceiver {

    private Receiver receiver;

    public VideoUploadReceiver(Handler handler) {
        super(handler);
    }

    public VideoUploadReceiver setReceiver(Receiver receiver) {
        this.receiver = receiver;
        return this;
    }

    @Override protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiverResult(resultCode, resultData);
        }
    }

    public interface Receiver {
        void onReceiverResult(@UploadCallback int resultCode, Bundle resultData);
    }
}
