package com.prembros.chatein.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;

import com.prembros.chatein.R;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import static com.prembros.chatein.util.CommonUtils.showToast;

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
            String subString = uri.substring(dot);
            if (subString.contains("?")) {
                int mark = subString.lastIndexOf("?");
                if (mark >= 0) return subString.substring(0, mark).toUpperCase();
                else return "";
            }
            else return subString.toUpperCase();
        } else {
            // No extension.
            return "";
        }
    }

    public static boolean isFileAnImage(String fileName) {
        try {
            return Objects.requireNonNull(getExtension(fileName)).contains("JPG") ||
                    Objects.requireNonNull(getExtension(fileName)).contains("PNG");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isImageSizeLegal(@NotNull Context context, @NotNull Uri uri) {
        if (uri.toString().startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri,
                        null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                    return isFileSizeLegal(context, size);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
        } else if (uri.toString().startsWith("file://")) {
            try {
                return isFileSizeLegal(context, new File(uri.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isFileSizeLegal(@NotNull Context context, @NotNull File file) {
        return isFileSizeLegal(context, file.length());
    }

    public static boolean isFileSizeLegal(@NotNull Context context, long size) {
        boolean isLegal = size < 5000000;
        if (!isLegal) showToast(context, R.string.file_size_exceed_message);
        return isLegal;
    }

    public static boolean isFileSizeLegal(@NotNull Context context, Uri filePath) {
        Cursor returnCursor = context.getContentResolver().query(filePath, null, null, null, null);
        if (returnCursor != null) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            boolean isFileLegal = returnCursor.getLong(sizeIndex) < 5000000;
            if (!isFileLegal) showToast(context, R.string.file_size_exceed_message);
            returnCursor.close();
            return isFileLegal;
        }
        return false;
    }

//    public static String getNameFromUri(@NotNull Context context, Uri uri) {
//        String fileName = null;
//        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
//        Cursor metaCursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (metaCursor != null) {
//            try {
//                if (metaCursor.moveToFirst()) {
//                    fileName = metaCursor.getString(0);
//                }
//            } finally {
//                metaCursor.close();
//            }
//        }
//        return fileName;
//    }
}