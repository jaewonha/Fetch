package com.tonyodev.fetchapp;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Utils {

    private Utils() {

    }

    @NonNull
    public static String getMimeType(@NonNull final Context context, @NonNull final Uri uri) {
        final ContentResolver cR = context.getContentResolver();
        final MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(uri));
        if (type == null) {
            type = "*/*";
        }
        return type;
    }

    public static void deleteFileAndContents(@NonNull final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] contents = file.listFiles();
                if (contents != null) {
                    for (final File content : contents) {
                        deleteFileAndContents(content);
                    }
                }
            }
            file.delete();
        }
    }

    @NonNull
    public static String getETAString(@NonNull final Context context, final long etaInMilliSeconds) {
        if (etaInMilliSeconds < 0) {
            return "";
        }
        int seconds = (int) (etaInMilliSeconds / 1000);
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        if (hours > 0) {
            return context.getString(R.string.download_eta_hrs, hours, minutes, seconds);
        } else if (minutes > 0) {
            return context.getString(R.string.download_eta_min, minutes, seconds);
        } else {
            return context.getString(R.string.download_eta_sec, seconds);
        }
    }

    @NonNull
    public static String getDownloadSpeedString(@NonNull final Context context, final long downloadedBytesPerSecond) {
        if (downloadedBytesPerSecond < 0) {
            return "";
        }
        double kb = (double) downloadedBytesPerSecond / (double) 1000;
        double mb = kb / (double) 1000;
        final DecimalFormat decimalFormat = new DecimalFormat(".##");
        if (mb >= 1) {
            return context.getString(R.string.download_speed_mb, decimalFormat.format(mb));
        } else if (kb >= 1) {
            return context.getString(R.string.download_speed_kb, decimalFormat.format(kb));
        } else {
            return context.getString(R.string.download_speed_bytes, downloadedBytesPerSecond);
        }
    }

    @NonNull
    public static File createFile(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            final File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static int getProgress(long downloaded, long total) {
        if (total < 1) {
            return -1;
        } else if (downloaded < 1) {
            return 0;
        } else if (downloaded >= total) {
            return 100;
        } else {
            return (int) (((double) downloaded / (double) total) * 100);
        }
    }

    public static String getDate() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String msToDate(long millis) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(millis));
    }


    public static String generateMD5(File file) throws Exception {
        return hashFile(file, "MD5");
    }

    public static String generateSHA1(File file) throws Exception {
        return hashFile(file, "SHA-1");
    }

    public static String generateSHA256(File file) throws Exception {
        return hashFile(file, "SHA-256");
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    private static String hashFile(File file, String algorithm)
            throws Exception {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new Exception(
                    "Could not generate hash from file", ex);
        }
    }

}
