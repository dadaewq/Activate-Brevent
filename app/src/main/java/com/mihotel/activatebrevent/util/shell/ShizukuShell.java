package com.mihotel.activatebrevent.util.shell;

import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mihotel.activatebrevent.util.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;

import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuService;

public class ShizukuShell implements Shell {
    private static final String TAG = "ShizukuShell";

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
            return exec(new Command("echo", "test")).isSuccessful();
        } catch (Exception e) {
            Log.w(TAG, "Unable to access shizuku: ");
            Log.w(TAG, e);
            return false;
        }
    }

    @Override
    public Result exec(Command command) {
        return execInternal(command, null);
    }

    @Override
    public Result exec(Command command, InputStream inputPipe) {
        return execInternal(command, inputPipe);
    }

    @Override
    public String makeLiteral(String arg) {
        return "'" + arg.replace("'", "'\\''") + "'";
    }

    private Result execInternal(Command command, @Nullable InputStream inputPipe) {

        StringBuilder stdOutSb = new StringBuilder();
        StringBuilder stdErrSb = new StringBuilder();

        try {
            RemoteProcess process = ShizukuService.newProcess(new String[]{"sh"}, null, null);
            Thread stdOutD = IOUtils.writeStreamToStringBuilder(stdOutSb, process.getInputStream());
            Thread stdErrD = IOUtils.writeStreamToStringBuilder(stdErrSb, process.getErrorStream());
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(command.toString().getBytes());
            outputStream.flush();

            if (inputPipe != null && process.alive()) {
                try (InputStream inputStream = inputPipe) {
                    IOUtils.copyStream(inputStream, outputStream);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            outputStream.close();

            process.waitFor();
            stdOutD.join();
            stdErrD.join();
            int exitValue = process.exitValue();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                process.destroyForcibly();
            } else {
                process.destroy();
            }

            return new Result(command, exitValue, stdOutSb.toString().trim(), stdErrSb.toString().trim());
        } catch (Exception e) {
            Log.w(TAG, "Unable execute command: ");
            Log.w(TAG, e);
            return new Result(command, -1, "", "\n\n<!> SAI ShizukuShell Java exception: " + IOUtils.throwableToString(e));
        }
    }
}
