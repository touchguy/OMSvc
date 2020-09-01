package kr.co.munjanara.omsvc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartService(View view) {
        Intent intent = new Intent(this, HttpService.class);
        intent.setAction("startForeground");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void onStopService(View view) {
        Intent intent = new Intent(this, HttpService.class);

        stopService(intent);
    }
}