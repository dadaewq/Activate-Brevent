package com.mihotel.activatebrevent.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.mihotel.activatebrevent.R;
import com.mihotel.activatebrevent.util.OpUtil;
import com.mihotel.activatebrevent.util.shell.Shell;
import com.mihotel.activatebrevent.util.shell.ShizukuShell;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;


/**
 * @author dadaewq`
 */
public class MainFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE = 2333;
    private final String[] prefStr = new String[]{"activate_brevent", "activate_icebox", "activate_stopapp", "activate_permissiondog"};
    private Preference[] Preferences;
    private Preference shizuku_service,shizuku_permission,avShizukuPreference;
    private Context context;
    private MyHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler(this);
        init();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_main, rootKey);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove all Runnable and Message.
        mHandler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onResume() {
        super.onResume();
        context = getActivity();
        Executors.newSingleThreadExecutor().execute(() -> {
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 9;
            mHandler.sendMessage(msg);
        });
    }


    private String getSh(int index) {
        String sh = null;
        switch (index) {
            case 0:
                sh = OpUtil.BREVENT_SH;
                break;
            case 1:
                sh = OpUtil.ICEBOX_SH;
                break;
            case 2:
                sh = OpUtil.STOPAPP_SH;
                break;
            case 3:
                sh = OpUtil.PERMISSIONDOG_SH;
                break;
            default:
        }
        return sh;
    }

    private MyCallBack getMyCallBack(int index) {

        return (exitCode, err) -> {
            if (0 == exitCode) {
                int stringId = R.string.unknown;
                switch (index) {
                    case 0:
                        stringId = R.string.activate_brevent_success;
                        break;
                    case 1:
                        stringId = R.string.activate_icebox_success;
                        break;
                    case 2:
                        stringId = R.string.activate_stopapp_success;
                        break;
                    case 3:
                        stringId = R.string.activate_permissiondog_success;
                        break;
                    default:
                }
                OpUtil.showToast0(context, stringId);
            } else {
                switch (index) {
                    case 0:
                        if ("sh: /data/data/me.piebridge.brevent/brevent.sh: No such file or directory".equals(err) ||
                                "sh: /data/data/me.piebridge.brevent/brevent.sh: Permission denied".equals(err) ||
                                "ERROR: please open Brevent to make a new brevent.sh".equals(err)) {
                            OpUtil.showToast1(context, "请在黑阈内打开提示启动服务的界面后再尝试激活");
                            return;
                        }
                        break;

                    case 1:
                        if ("sh: /sdcard/Android/data/com.catchingnow.icebox/files/start.sh: No such file or directory".equals(err) ||
                                "sh: /sdcard/Android/data/com.catchingnow.icebox/files/start.sh: Permission denied".equals(err)) {
                            OpUtil.showToast1(context, "请在冰箱内打开选择工作模式的界面后再尝试激活");
                            return;
                        }
                        break;
                    case 2:
                        if ("sh: /storage/emulated/0/Android/data/web1n.stopapp/files/starter.sh: No such file or directory".equals(err) ||
                                "sh: /storage/emulated/0/Android/data/web1n.stopapp/files/starter.sh: Permission denied".equals(err)) {
                            OpUtil.showToast1(context, "请在小黑屋内打开提示启动服务的界面后再尝试激活");
                            return;
                        }
                        break;
                    case 3:
                        if ("sh: /storage/emulated/0/Android/data/com.web1n.permissiondog/files/starter.sh: No such file or directory".equals(err) ||
                                "sh: /storage/emulated/0/Android/data/com.web1n.permissiondog/files/starter.sh: Permission denied".equals(err)) {
                            OpUtil.showToast1(context, "请在权限狗内打开提示启动服务的界面后再尝试激活");
                            return;
                        }
                        break;
                    default:
                }
                OpUtil.showToast1(context, String.format(MainFragment.this.getString(R.string.activate_fail), err));
            }

        };
    }


    private void init() {

        Preferences = new Preference[prefStr.length];
        PreferenceManager preferenceManager = getPreferenceManager();
        for (int i = 0; i < prefStr.length; i++) {
            Preferences[i] = preferenceManager.findPreference(prefStr[i]);
            assert Preferences[i] != null;
            int finalI = i;
            Preferences[i].setOnPreferenceClickListener(preference -> {
                execShizukuShell(finalI);
                return true;
            });
        }


        avShizukuPreference = getPreferenceManager().findPreference("av_shizuku");
        shizuku_service = getPreferenceScreen().findPreference("shizuku_service");
        shizuku_permission = getPreferenceScreen().findPreference("shizuku_permission");

        assert shizuku_permission != null;
        shizuku_permission.setOnPreferenceClickListener(preference -> {
            try {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{ShizukuApiConstants.PERMISSION},
                        REQUEST_CODE);
            } catch (Exception e) {
                OpUtil.showToast0(context, e + "");
            }
            return true;
        });

    }


    private void execShizukuShell(int index) {
        execShizukuShell(getSh(index), getMyCallBack(index));
    }

    private void execShizukuShell(String shPath, MyCallBack myCallBack) {
        try {
            Shell.Result shizukuShellResult;

            shizukuShellResult = ShizukuShell.getInstance().exec("sh", shPath);

            Log.e("Result", shizukuShellResult.toString());

            myCallBack.showResult(shizukuShellResult.exitCode, shizukuShellResult.err);

        } catch (Exception e) {
            OpUtil.showToast0(context, e + "");
        }
    }


    private void refreshStatus() {

        Intent launchShizujuIntent = context.getPackageManager().getLaunchIntentForPackage(OpUtil.SHIZUKU_PACKAGENAME);

        boolean isShizukuExist = (launchShizujuIntent != null);
        boolean isShizukuRunningService = true;

        try {
            ShizukuService.getVersion();
        } catch (Exception e) {
            // Shizuku的Exception如果为IllegalStateException则说明服务没有在运行
            // Log.e("Exception", e.getClass() + "");
            if (e.getClass() == IllegalStateException.class) {
                isShizukuRunningService = false;
            }
        }


        if (isShizukuRunningService) {
            shizuku_service.setSummary(R.string.summary_av_yes);
        } else {
            shizuku_service.setSummary(R.string.summary_av_no);
        }

        boolean hasPermission = ContextCompat.checkSelfPermission(context, ShizukuApiConstants.PERMISSION) == 0;

        if (hasPermission) {
            shizuku_permission.setSummary(R.string.summary_permission_yes);
        } else {
            if (isShizukuExist) {
                shizuku_permission.setSummary(getString(R.string.summary_permission_no) + getString(R.string.click2requestpermission));
            } else {
                shizuku_permission.setSummary(R.string.summary_permission_no);
            }
        }

        boolean avShizuku = ShizukuShell.getInstance().isAvailable();
        for (Preference preference : Preferences) {
            preference.setEnabled(avShizuku);
        }

        if (avShizuku) {
            avShizukuPreference.setSummary(R.string.summary_av_ok_installer);
        } else {
            if (isShizukuRunningService) {
                if (hasPermission) {
                    avShizukuPreference.setSummary(getString(R.string.summary_av_no) + getString(R.string.unknown));
                    for (Preference preference : Preferences) {
                        preference.setEnabled(true);
                    }
                } else {
                    if (isShizukuExist) {
                        avShizukuPreference.setOnPreferenceClickListener(preference -> {
                            try {
                                startActivity(launchShizujuIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        });
                        avShizukuPreference.setSummary(getString(R.string.summary_av_no) + getString(R.string.summary_go_shizuku));
                    } else {
                        avShizukuPreference.setSummary(getString(R.string.summary_av_no) + getString(R.string.summary_permission_no));
                    }
                }
            } else {
                if (isShizukuExist) {
                    avShizukuPreference.setOnPreferenceClickListener(preference -> {
                        try {
                            startActivity(launchShizujuIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    });
                    avShizukuPreference.setSummary(getString(R.string.summary_av_no) + getString(R.string.summary_go_shizuku));
                } else {
                    avShizukuPreference.setSummary(getString(R.string.summary_av_no) + getString(R.string.summary_check_shizuku_service));
                }
            }
        }
    }

//    private void refreshRawStatus() {
//        if (!isShExist()) {
//            try {
//                InputStream inputStream = context.getResources().openRawResource(R.raw.brevent);
//                File file = new File(getShPath());
//                FileOutputStream fos = new FileOutputStream(file);
//                byte[] buffer = new byte[inputStream.available()];
//                int lenght = 0;
//                while ((lenght = inputStream.read(buffer)) != -1) {
//                    fos.write(buffer, 0, lenght);
//                    fos.flush();
//                    fos.close();
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

//    private String getShPath() {
//        return context.getFilesDir().getAbsolutePath() + File.separator + "brevent.sh";
//    }
//
//    private boolean isShExist() {
//        return new File(getShPath()).exists();
//    }

    public interface MyCallBack {
        /**
         * 设置好时间后开始设置定时任务
         */
        void showResult(int exitCode, String err);
    }

    private static class MyHandler extends Handler {

        private final WeakReference<MainFragment> wrFragment;

        MyHandler(MainFragment fragment) {
            this.wrFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (wrFragment.get() == null) {
                return;
            }
            MainFragment mainFragment = wrFragment.get();

            if (msg.arg1 == 9) {
                mainFragment.refreshStatus();
            }
        }
    }
}
