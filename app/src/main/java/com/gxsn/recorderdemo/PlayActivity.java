package com.gxsn.recorderdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class PlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        JCVideoPlayerStandard jcVideoPlayerStandard = (JCVideoPlayerStandard) findViewById(R.id.videoplayer);

        String path = getIntent().getStringExtra("path");

        jcVideoPlayerStandard.setUp("file://" + path,
                JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, "");

        jcVideoPlayerStandard.startVideo();

    }

    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
//            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }
}
