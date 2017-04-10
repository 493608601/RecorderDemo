package com.gxsn.recorderdemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.gxsn.recorderdemo.adapter.ListAdapter;
import com.gxsn.recorderdemo.entity.Resource;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecorderDemo/";
    private static final int REQUEST_RECORD_AUDIO = 0;
    private SimpleDateFormat dateFormat;
    private String time;
    private MediaPlayer player;
    private ArrayList<Resource> resources;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview);
        Button btAudio = (Button) findViewById(R.id.audio);
        Button btVideo = (Button) findViewById(R.id.video);
        btAudio.setOnClickListener(this);
        btVideo.setOnClickListener(this);


    }


    @Override
    protected void onResume() {
        super.onResume();
        dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        resources = new ArrayList<>();


        File file = new File(FILE_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }

        File[] files = file.listFiles();
        for (File f : files) {
            try {
                player = new MediaPlayer();
                player.setDataSource(f.getAbsolutePath());
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int duration = player.getDuration();

            Resource r = new Resource();
            String name = f.getName();
            r.setName(name);
            r.setPath(f.getAbsolutePath());
            time = dateFormat.format(Long.parseLong(name.substring(0, name.lastIndexOf("."))));

            r.setTime(time);
            r.setDuration(duration);

            if (name.substring(name.lastIndexOf(".")).equals(".mp3")) {
                r.setType(Resource.TYPE_AUDIO);
            } else {
                r.setType(Resource.TYPE_VIDEO);
            }
            player.release();

            resources.add(r);
        }


        listView.setAdapter(new ListAdapter(this, resources));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtra("path", resources.get(i).getPath());

                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.audio:
                recorder_audio();
                break;
            case R.id.video:
                startActivity(new Intent(MainActivity.this, VideoActivity.class));
                break;
        }
    }


    private void recorder_audio() {
        AndroidAudioRecorder.with(this)
                // Required
                .setFilePath(FILE_PATH + System.currentTimeMillis() + ".mp3")
                .setColor(ContextCompat.getColor(this, R.color.recorder_bg))
                .setRequestCode(REQUEST_RECORD_AUDIO)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(false)
                .setKeepDisplayOn(true)

                // Start recording
                .record();
    }
}
