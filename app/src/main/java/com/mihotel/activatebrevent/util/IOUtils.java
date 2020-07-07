package com.mihotel.activatebrevent.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class IOUtils {
    private static final String TAG = "IOUtils";

    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = from.read(buf)) > 0) {
            to.write(buf, 0, len);
        }
    }

    public static Thread writeStreamToStringBuilder(StringBuilder builder, InputStream inputStream) {
        Thread t = new Thread(() -> {
            try {
                char[] buf = new char[1024];
                int len;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((len = reader.read(buf)) > 0) {
                    builder.append(buf, 0, len);
                }

                reader.close();
            } catch (Exception e) {
                Log.wtf(TAG, e);
            }
        });
        t.start();
        return t;
    }

    public static String throwableToString(Throwable throwable) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.close();

        return sw.toString();
    }
}
