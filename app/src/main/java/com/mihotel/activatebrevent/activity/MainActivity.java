package com.mihotel.activatebrevent.activity;

import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.mihotel.activatebrevent.R;
import com.mihotel.activatebrevent.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_MAIN = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.alertdialog_background);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new MainFragment(), TAG_MAIN).commit();

    }
}