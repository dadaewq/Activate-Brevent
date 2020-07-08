package com.mihotel.activatebrevent.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;

/**
 * @author dadaewq
 */
public class OpUtil {
    public static final String SHIZUKU_PACKAGENAME = "moe.shizuku.privileged.api";
    @SuppressLint("SdCardPath")
    public static final String BREVENT_SH = "/data/data/me.piebridge.brevent/brevent.sh";
    @SuppressLint("SdCardPath")
    public static final String ICEBOX_SH = "/sdcard/Android/data/com.catchingnow.icebox/files/start.sh";
    public static final String STOPAPP_SH = "/storage/emulated/0/Android/data/web1n.stopapp/files/starter.sh";
    public static final String PERMISSIONDOG_SH = "/storage/emulated/0/Android/data/com.web1n.permissiondog/files/starter.sh";

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

    public static void launchCustomTabsUrl(Context context, String url) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build();

            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            showToast1(context, "" + e);
        }
    }
}
