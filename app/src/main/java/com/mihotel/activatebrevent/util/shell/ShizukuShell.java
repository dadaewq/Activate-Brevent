package com.mihotel.activatebrevent.util.shell;

import android.os.Build;
import android.util.Log;

import com.mihotel.activatebrevent.util.IOUtils;

import java.util.Arrays;

import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuService;

public class ShizukuShell implements Shell {
    private static final String TAG = "ShizukuShell2";

    private static ShizukuShell sInstance;

    private ShizukuShell() {
        sInstance = this;
    }

    public static ShizukuShell getInstance() {
        synchronized (ShizukuShell.class) {
            return sInstance != null ? sInstance : new ShizukuShell();
        }
    }

    @Override
    public boolean isAvailable() {
        if (!ShizukuService.pingBinder()) {
            return false;
        }
        try {
            return exec("echo", "test").isSuccessful();
        } catch (Exception e) {
            Log.w(TAG, "Unable to access shizuku: ");
            Log.w(TAG, e);
            return false;
        }
    }

    @Override
    public Result exec(String... command) {
        return execInternal(command);
    }

    private Result execInternal(String... command) {

        StringBuilder stdOutSb = new StringBuilder();
        StringBuilder stdErrSb = new StringBuilder();

        try {
            RemoteProcess process = ShizukuService.newProcess(command, null, null);
            Thread stdOutD = IOUtils.writeStreamToStringBuilder(stdOutSb, process.getInputStream());
            Thread stdErrD = IOUtils.writeStreamToStringBuilder(stdErrSb, process.getErrorStream());

            process.waitFor();
            stdOutD.join();
            stdErrD.join();

            int exitValue = process.exitValue();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                process.destroyForcibly();
            } else {
                process.destroy();
            }

            return new Result(Arrays.toString(command), exitValue, stdOutSb.toString().trim(), stdErrSb.toString().trim());
        } catch (Exception e) {
            Log.w(TAG, "Unable execute command: ");
            Log.w(TAG, e);
            return new Result(Arrays.toString(command), -1, "", "\nShizukuShell exception: " + IOUtils.throwableToString(e));
        }
    }
}
