package com.cemCloud.videoQuicPlayer;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cronet.CronetDataSourceFactory;
import com.google.android.exoplayer2.ext.cronet.CronetEngineWrapper;
import org.chromium.net.CronetEngine;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;


public class MainActivity extends AppCompatActivity {

    private static final String MEDIA_STREAM_URL ="https://static.cem-cloud.com/videos/travel.mp4";
    private SimpleExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        CronetEngine.Builder engineBuilder = new CronetEngine.Builder(this);
        engineBuilder.enableHttp2(true).enableQuic(true);
        engineBuilder.enableHttpCache(
                CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 100 * 1024);
        CronetEngine cronetEngine = engineBuilder.enableHttp2(true).enableQuic(true).build();
        CronetEngineWrapper wrapper = new CronetEngineWrapper(cronetEngine);

        Executor executor = Executors.newSingleThreadExecutor();
        CronetDataSourceFactory factory = new CronetDataSourceFactory(wrapper, executor, null, Util.getUserAgent(this,"VideoQUICPlayer"));

        player = new SimpleExoPlayer.Builder(this).build();
        MediaSource viedoSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(Uri.parse(MEDIA_STREAM_URL));
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        player.prepare(viedoSource);
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.release();
    }
}
