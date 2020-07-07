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

import com.mihotel.activatebrevent.R;
import com.mihotel.activatebrevent.util.OpUtil;
import com.mihotel.activatebrevent.util.shell.Shell;
import com.mihotel.activatebrevent.util.shell.ShizukuShell;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;


/**
 * @author dadaewq
 */
public class MainFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE = 2333;

    private Preference click2activate;
    private Preference shizuku_service;
    private Preference shizuku_permission;
    private Preference avShizukuPreference;
    private Context context;
    private MyHandler mHandler;
    private String command;

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


    private void init() {
        click2activate = getPreferenceManager().findPreference("click2activate");
        avShizukuPreference = getPreferenceManager().findPreference("av_shizuku");
        shizuku_service = getPreferenceScreen().findPreference("shizuku_service");
        shizuku_permission = getPreferenceScreen().findPreference("shizuku_permission");

        assert click2activate != null;
        click2activate.setOnPreferenceClickListener(preference -> {
            try {
                Shell.Result ShizukuShellResult;

                ShizukuShellResult = ShizukuShell.getInstance().exec(new Shell.Command("sh", OpUtil.BREVENT_SH));

                Log.e("Result", ShizukuShellResult.toString());

                if (0 == ShizukuShellResult.exitCode) {
                    OpUtil.showToast0(context, R.string.activate_success);
                } else {
                    if ("sh: /data/data/me.piebridge.brevent/brevent.sh: No such file or directory".equals(ShizukuShellResult.err)) {
                        OpUtil.showToast1(context, "请在黑阈内打开提示启动服务的界面后再尝试激活");
                    } else {
                        OpUtil.showToast1(context, String.format(getString(R.string.activate_fail), ShizukuShellResult.err));
                    }
                }


            } catch (Exception e) {
                OpUtil.showToast0(context, e + "");
            }
            return true;
        });

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


    private void refreshInstallerStatus() {

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
        click2activate.setEnabled(avShizuku);
        if (avShizuku) {
            avShizukuPreference.setSummary(R.string.summary_av_ok_installer);
        } else {
            if (isShizukuRunningService) {
                if (hasPermission) {
                    avShizukuPreference.setSummary(getString(R.string.summary_av_no) + getString(R.string.unknown));
                    avShizukuPreference.setOnPreferenceClickListener(preference -> {
                        click2activate.setEnabled(true);
                        return true;
                    });
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

    private void refreshStatus() {
        refreshInstallerStatus();
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
