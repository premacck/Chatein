package com.prembros.chatein.util;

import com.prembros.chatein.util.Annotations.SocialState;

public interface SocialListener {

    void actionStarted();

    void actionCompleted();

    void updateState(@SocialState int state);

    void error(String message);
}