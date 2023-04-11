package com.example.newsleep;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private MediaRecorder recorder;
    private boolean isRecording = false;
    private String fileName;

    Button buttonStart;
    Button buttonStop;
    TextView textView;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查录音权限
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        buttonStart = findViewById(R.id.btn_start_sleep);
        buttonStop = findViewById(R.id.btn_end_sleep);
        textView = findViewById(R.id.tv_sleep_time);
        textView2 = findViewById(R.id.tv_sleep_status);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 初始化录音机
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                File dir = getExternalFilesDir(null);
                fileName = dir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".3gp";
                recorder.setOutputFile(fileName);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    recorder.prepare();
                } catch (IOException e) {
                    Log.e("MainActivity", "prepare() failed");
                }
                recorder.start();
                isRecording = true;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis(); // 添加开始时间
                        while (isRecording) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 获取当前时间
                                    long currentTime = System.currentTimeMillis();
                                    // 计算已经录音的时间
                                    long timePassed = currentTime - startTime; // 计算已经录音的时间
                                    // 将时间转换为秒
                                    int secondsPassed = (int) (timePassed / 1000);
                                    // 计算小时数
                                    int hours = secondsPassed / 3600;
                                    // 计算分钟数
                                    int minutes = (secondsPassed % 3600) / 60;
                                    // 计算秒数
                                    int seconds = secondsPassed % 60;
                                    // 将时间格式化为字符串
                                    String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                                    // 将时间显示在界面上
                                    textView.setText(timeString);
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(recorder!=null) {
                                // 获取分贝值
                                int amplitude = recorder.getMaxAmplitude();
                                // 判断分贝值是否小于某个阈值
                                if (amplitude < 1000) {
                                    // 用户正在睡觉
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView2.setText("用户正在睡觉");
                                        }
                                    });
                                } else {
                                    // 用户醒着
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView2.setText("用户醒着");
                                        }
                                    });
                                }
                            }
                        }
                    }
                }).start();
            }
        });





        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) { // add this check to prevent issues if the button is clicked twice
                    // 停止录音
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    isRecording = false;
                    //点击停止睡眠后睡眠时间也会停止
                    textView2.setText("睡眠结束");
                }
            }
        });













    }
}