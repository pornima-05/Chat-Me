package com.example.testapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class VideoPlayingActivity extends AppCompatActivity {

    ExoPlayer exoPlayer;
    StyledPlayerView styledPlayerView;
    MediaItem mediaItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playing);

        Intent intent = getIntent();
        String uri = intent.getStringExtra("videouri");


        styledPlayerView = findViewById(R.id.myvideoplayer);
        exoPlayer = new ExoPlayer.Builder(VideoPlayingActivity.this).build();

        styledPlayerView.setPlayer(exoPlayer);

        mediaItem = MediaItem.fromUri(Uri.parse(uri));

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exoPlayer.stop();
        exoPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayer.stop();
        exoPlayer.release();
    }
}