package com.example.johnchoate.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.snapchat.kit.sdk.SnapCreative;
import com.snapchat.kit.sdk.creative.api.SnapCreativeKitApi;
import com.snapchat.kit.sdk.creative.exceptions.SnapMediaSizeException;
import com.snapchat.kit.sdk.creative.exceptions.SnapVideoLengthException;
import com.snapchat.kit.sdk.creative.media.SnapMediaFactory;
import com.snapchat.kit.sdk.creative.media.SnapVideoFile;
import com.snapchat.kit.sdk.creative.models.SnapVideoContent;


import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button testButton, testButton2;
    final int REQUEST_VIDEO_CAPTURE = 1;
    Intent videoIntent = null;
    private final File inputFile = new File(Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/input.mp4");
    private final File outputFile = new File(Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/snapify.mp4");
    private final File musicFile = new File(Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/music.mp3");
    private FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testButton = findViewById(R.id.testButton);
        testButton2 = findViewById(R.id.testButton2);
        ffmpeg = FFmpeg.getInstance(MainActivity.this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            toast(e.getMessage());
        }
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                Uri videoUri = Uri.fromFile(inputFile);
                videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                if (videoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        });

        testButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Uri videoUri = Uri.fromFile(outputFile);
                /*String cmd[] = {"ffmpeg ", "-i "+ inputFile.getPath()+" ", "-i " + musicFile.getPath()+" ", "-map ", "0:0 ", "-map ", "1:0 ",
                        "-c:v ", "copy ", "-c:a ", "aac ", "-b:a ", "256k ", "-shortest ", outputFile.getPath()};*/
                /*String cmd[] = {"-i ", inputFile.getPath()+" ", "-codec ", "copy ", "-an ", outputFile.getPath()};*/
                String cmd[] = {"-i "+inputFile.getPath()+" "+outputFile.getPath()+".avi"};
                final SnapCreativeKitApi snapCreativeKitApi = SnapCreative.getApi(MainActivity.this);
                final SnapMediaFactory snapMediaFactory = SnapCreative.getMediaFactory(MainActivity.this);
                try {
                    final SnapVideoFile videoFile = snapMediaFactory.getSnapVideoFromFile(new File(videoUri.getPath()));
                    try {
                        // to execute "ffmpeg -version" command you just need to pass "-version"
                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onProgress(String message) {
                            }

                            @Override
                            public void onFailure(String message) {
                                toast(message);
                            }

                            @Override
                            public void onSuccess(String message) {
                                try {
                                    SnapVideoContent snapVideoContent = new SnapVideoContent(videoFile);
                                    snapCreativeKitApi.send(snapVideoContent);
                                } catch (Exception e) {
                                    toast(e.getMessage());
                                }
                            }

                            @Override
                            public void onFinish() {
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        // Handle if FFmpeg is already running
                    }
                } catch (Exception e){
                    toast(e.getMessage());
                }
            }
        });
    }

    public void toast(String msg){
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}
