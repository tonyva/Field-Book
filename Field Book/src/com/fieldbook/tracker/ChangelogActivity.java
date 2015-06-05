package com.fieldbook.tracker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ChangelogActivity extends Activity {

    private static String TAG = "Field Book";

    WindowManager.LayoutParams params;
    private LinearLayout parent;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences ep = getSharedPreferences("Settings", 0);

        // Enforce language
        String local = ep.getString("language", "en");
        String region = ep.getString("region", "");
        Locale locale2 = new Locale(local, region);
        Locale.setDefault(locale2);
        Configuration config2 = new Configuration();
        config2.locale = locale2;
        getBaseContext().getResources().updateConfiguration(config2, getBaseContext().getResources()
                .getDisplayMetrics());

        SharedPreferences.Editor ed = ep.edit();
        ed.putInt("UpdateVersion", getVersion());
        ed.apply();


        setContentView(R.layout.changelog);
        setTitle(R.string.updatemsg);
        params = getWindow().getAttributes();

        this.getWindow().setAttributes(params);
        parent = (LinearLayout) findViewById(R.id.data);

        Button close = (Button) findViewById(R.id.closeBtn);

        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });

        parseLog(R.raw.changelog_releases);
    }

    public int getVersion() {
        int v = 0;
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return v;
    }

    // Helper function to add row
    public void parseLog(int resId) {
        try {
            InputStream is = getResources().openRawResource(resId);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr, 8192);

            String curVersionName = null;

            String line;
            while ((line = br.readLine()) != null) {
                TextView header = new TextView(this);
                TextView content = new TextView(this);
                TextView spacer = new TextView(this);
                spacer.setTextSize(5);
                View ruler = new View(this);

                ruler.setBackgroundColor(0xff33b5e5);
                header.setTextAppearance(getApplicationContext(), R.style.Dialog_SectionTitles);
                content.setTextAppearance(getApplicationContext(), R.style.Dialog_SectionSubtitles);

                if (line.length() == 0) {
                    curVersionName = null;
                    spacer.setText("\n");
                    parent.addView(spacer);
                } else if (curVersionName == null) {
                    final String[] lineSplit = line.split("/");
                    curVersionName = lineSplit[1];
                    header.setText(curVersionName);
                    parent.addView(header);
                    parent.addView(ruler,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3));

                } else {
                    content.setText("•  " + line);
                    parent.addView(content);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}