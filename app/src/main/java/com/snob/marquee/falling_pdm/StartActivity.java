package com.snob.marquee.falling_pdm;

import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.support.v7.app.*;

public class StartActivity extends AppCompatActivity {

    public static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);

        Button btnJogar = (Button) findViewById(R.id.btnJogar);
        btnJogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, GameActivity.class));
            }
        });
    }
}
