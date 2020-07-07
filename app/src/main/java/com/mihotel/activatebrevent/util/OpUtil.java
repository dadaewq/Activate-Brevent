package com.mihotel.activatebrevent.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

/**
 * @author dadaewq
 */
public class OpUtil {
    public static final String SHIZUKU_PACKAGENAME = "moe.shizuku.privileged.api";
    @SuppressLint("SdCardPath")
    public static final String BREVENT_SH = "/data/data/me.piebridge.brevent/brevent.sh";

    public static void showToast0(Context context, final String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast0(Context context, final int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast1(Context context, final String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast1(Context context, final int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }

}
