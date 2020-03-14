package com.example.transfer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MyService downloader = new MyService();
    private String downloadUrl = "https://s9.pstatp.com/package/apk/aweme/aweme_aweGW_v9.6.0_7694899.apk?v=1579090067";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Button start_download = findViewById(R.id.activity_main_button_start_download);
        start_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloader.startDownload(downloadUrl);
            }
        });
        Button pause_download = findViewById(R.id.activity_main_button_pause_download);
        pause_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloader.pauseDownload();
            }
        });
        Button cancel_download = findViewById(R.id.activity_main_button_cancel_download);
        cancel_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloader.deleteFile(downloadUrl)) {
                    Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
        final ProgressBar progressBar = findViewById(R.id.activity_main_progressbar_download);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    progressBar.setProgress(downloader.getProgress());
                }
            }
        };
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }

    }

}
