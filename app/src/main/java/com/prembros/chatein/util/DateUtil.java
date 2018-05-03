package com.prembros.chatein.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressLint("Registered")
public class DateUtil extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final String MOMENTS_AGO = "moments ago";
    private static final String A_MINUTE_AGO = "a minute ago";
    private static final String MINUTES_AGO = " minutes ago";
    private static final String AN_HOUR_AGO = "an hour ago";
    private static final String HOURS_AGO = " hours ago";
    private static final String YESTERDAY = "yesterday";
    private static final String DAYS_AGO = " days ago";

    @Nullable public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return MOMENTS_AGO;
        } else if (diff < 2 * MINUTE_MILLIS) {
            return A_MINUTE_AGO;
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + MINUTES_AGO;
        } else if (diff < 90 * MINUTE_MILLIS) {
            return AN_HOUR_AGO;
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + HOURS_AGO;
        } else if (diff < 48 * HOUR_MILLIS) {
            return YESTERDAY;
        } else {
            return diff / DAY_MILLIS + DAYS_AGO;
        }
    }

    private static int getTimeType(@NotNull String timeAgo) {
        if (timeAgo.contains(MOMENTS_AGO)) return 0;
        else if (timeAgo.contains(MOMENTS_AGO) || timeAgo.contains(A_MINUTE_AGO) ||
                timeAgo.contains(MINUTES_AGO) || timeAgo.contains(AN_HOUR_AGO) || timeAgo.contains(HOURS_AGO))
            return 1;
        else if (timeAgo.contains(YESTERDAY)) return 2;
        else return 3;
    }

    @Nullable public static String getTime(long timeStamp) {
        try {
            switch (getTimeType(Objects.requireNonNull(getTimeAgo(timeStamp)))) {
                case 0:
                    return "Just now";
                case 1:
//                    Show in "hh:mm" style
                    return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timeStamp));
                case 2:
//                    Show "yesterday"
                    return "Yesterday";
                case 3:
//                    Show in "hh:mm" style
                    return new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date(timeStamp));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}