package com.cemCloud.videoQuicPlayer;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cronet.CronetDataSourceFactory;
import com.google.android.exoplayer2.ext.cronet.CronetEngineWrapper;
import org.chromium.net.CronetEngine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;


public class MainActivity extends AppCompatActivity {

    private static final String MEDIA_STREAM_URL ="https://static.cem-cloud.com/videos/travel.mp4";
    private SimpleExoPlayer player;
    private PlayerView playerView;
    private CronetDataSourceFactory factory;
    private CronetEngine cronetEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializePlayer(this);
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
    }

    private void initializePlayer(Context context){

        if(cronetEngine==null){
            CronetEngine.Builder engineBuilder = new CronetEngine.Builder(context);
            //Enable the QUIC
            cronetEngine = engineBuilder.enableQuic(true)
                    //Enable the Http Cache
                    .enableHttpCache(
                            CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 100 * 1024)
                    //Signals to cronet that it should try to speak QUIC to this host
                    .addQuicHint("static.cem-cloud.com",443,443)
                    .build();
            CronetEngineWrapper wrapper = new CronetEngineWrapper(cronetEngine);

            Executor executor = Executors.newSingleThreadExecutor();
            factory = new CronetDataSourceFactory(wrapper, executor, null, Util.getUserAgent(context,"VideoQUICPlayer"));
        }

        if(player==null){
            player = new SimpleExoPlayer.Builder(context).build();
            //Start net log
            File outputFile;
            try {
                outputFile = File.createTempFile("cronet", "log",
                        Environment.getExternalStorageDirectory());
                cronetEngine.startNetLogToFile(outputFile.toString(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaSource viedoSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(Uri.parse(MEDIA_STREAM_URL));
        player.prepare(viedoSource);
        player.setPlayWhenReady(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer(this);
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    private void releasePlayer(){
        player.release();
        player=null;
        cronetEngine.stopNetLog();
    }
}
