package com.nokia.streamer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.asha.md360player4android.R;

/**
 * Created by hzqiujiadi on 16/1/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        final EditText rtmp = (EditText) findViewById(R.id.edit_text_url);
        final EditText sdkServer = (EditText) findViewById(R.id.edit_controller_url);

        findViewById(R.id.video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rtmpStream = rtmp.getText().toString();
                String controllerIPPort = sdkServer.getText().toString();
                if (!TextUtils.isEmpty(rtmpStream)){
                    MD360PlayerActivity.startVideo(DemoActivity.this, Uri.parse(rtmpStream));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent serviceIntent = new Intent(getApplicationContext(), BluetoothConnectionService.class);
        startService(serviceIntent);
    }
}
