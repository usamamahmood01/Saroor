package com.example.saroor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {

    Button btnplay, btnforward, btnnext, btnprevious, btnrewind, pause;
    TextView txtsname, txtsstart, txtsstop;
    SeekBar seekBar, VolumeSeekBar;
    BarVisualizer visualizer;
    ImageView imageview;

    String sname;
    AudioManager audioManager;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateseekbar;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {

        if(visualizer != null)
        {
            visualizer.release();
        }

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);


        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnplay = findViewById(R.id.playbtn);
        btnforward = findViewById(R.id.forwardbtn);
        btnnext = findViewById(R.id.nextbtn);
        btnprevious = findViewById(R.id.previousbtn);
        btnrewind = findViewById(R.id.rewindbtn);
        txtsname = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekBar = findViewById(R.id.seekbar);
        VolumeSeekBar = findViewById(R.id.VolumeSeekBar);
        visualizer = findViewById(R.id.blast);
        imageview = findViewById(R.id.imageview);

        if(mediaPlayer != null)
            {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            Intent i = getIntent();
            Bundle bundle = i.getExtras();

            mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
            String songName = i.getStringExtra("songname");
            position = bundle.getInt("position", 0);
            txtsname.setSelected(true);
            Uri uri = Uri.parse(mySongs.get(position).toString());
            sname = mySongs.get(position).getName();
            txtsname.setText(sname);


            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();

            updateseekbar = new Thread()
            {
                @Override
                public void run() {
                    int tottalDuration = mediaPlayer.getDuration();
                    int currentPosition = 0;

                    while (currentPosition<tottalDuration)
                    {
                        try {
                            sleep(500);
                            currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                        }
                        catch (InterruptedException | IllegalStateException e)
                        {
                         e.printStackTrace();
                        }
                    }
                }
            };
            seekBar.setMax(mediaPlayer.getDuration());
            updateseekbar.start();
            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
            seekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            });
            String endTime = createTime(mediaPlayer.getDuration());
            txtsstop.setText(endTime);

            final Handler handler = new Handler();
            final int delay =1000;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String currentTime = createTime(mediaPlayer.getCurrentPosition());
                    txtsstart.setText(currentTime);
                    handler.postDelayed(this, delay);

                }
            }, delay);


        VolumeSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        VolumeSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);


        VolumeSeekBar.setMax(maxVolume);
        VolumeSeekBar.setProgress(currentVolume);

        VolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




            btnplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mediaPlayer.isPlaying())
                    {
                        btnplay.setBackgroundResource(R.drawable.play);
                        mediaPlayer.pause();
                    }else
                        {
                            btnplay.setBackgroundResource(R.drawable.pause);
                            mediaPlayer.start();
                        }
                }
            });



            //plays the next song as the song finishes.
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnnext.performClick();
                    seekBar.setProgress(0);
                }
            });


            int aduioSessionId = mediaPlayer.getAudioSessionId();
            if(aduioSessionId != -1)
            {
                visualizer.setAudioSessionId(aduioSessionId);
            }

            btnnext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    position = ((position+1)%mySongs.size());
                    Uri u = Uri.parse(mySongs.get(position).toString());
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                    sname = mySongs.get(position).getName();
                    txtsname.setText(sname);
                    mediaPlayer.start();
                    seekBar.setProgress(0);
                    btnplay.setBackgroundResource(R.drawable.pause);
                    int aduioSessionId = mediaPlayer.getAudioSessionId();
                    if(aduioSessionId != -1)
                    {
                        visualizer.setAudioSessionId(aduioSessionId);
                    }

                }
            });

            btnprevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mediaPlayer.stop();
                    mediaPlayer.release();
                    position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                    Uri u = Uri.parse(mySongs.get(position).toString());
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                    sname = mySongs.get(position).getName();
                    txtsname.setText(sname);
                    mediaPlayer.start();
                    seekBar.setProgress(0);
                    btnplay.setBackgroundResource(R.drawable.pause);
                    int aduioSessionId = mediaPlayer.getAudioSessionId();
                    if(aduioSessionId != -1)
                    {
                        visualizer.setAudioSessionId(aduioSessionId);
                    }


                }
            });

            btnforward.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    if(mediaPlayer.isPlaying())
                    {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                    }
                }
            });

            btnrewind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mediaPlayer.isPlaying())
                    {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                    }
                }
            });
    }

    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";
        if(sec<10)
        {
            time += "0";
        }

        time+=sec;

        return time;

    }



}
