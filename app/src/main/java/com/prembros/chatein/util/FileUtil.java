package com.prembros.chatein.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileUtil {

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri the file's URI.
     * @return Extension including the dot("."); "" if there is no extension;
     *         null if uri was null.
     */

    @Nullable @Contract("null -> null") public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }
        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    public static boolean isFileSizeLegal(@NotNull Context context, Uri returnUri) {
//        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
//        if (returnCursor != null) {
//            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
//            returnCursor.moveToFirst();
//            boolean isFileLegal = returnCursor.getLong(sizeIndex) < 5000000;
//            returnCursor.close();
//            return isFileLegal;
//        }
//        return false;
        try {
            File file = new File(new java.net.URI(returnUri.toString()));
            return file.length() < 5000000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}