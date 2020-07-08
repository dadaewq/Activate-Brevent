package com.mihotel.activatebrevent.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mihotel.activatebrevent.R;
import com.mihotel.activatebrevent.fragment.MainFragment;
import com.mihotel.activatebrevent.util.OpUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_MAIN = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.alertdialog_background);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new MainFragment(), TAG_MAIN).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.get_shizuku) {
            OpUtil.launchCustomTabsUrl(this, "https://www.coolapk.com/apk/moe.shizuku.privileged.api");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}