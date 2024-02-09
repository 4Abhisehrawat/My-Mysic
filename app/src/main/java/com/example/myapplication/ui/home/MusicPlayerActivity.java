package com.example.myapplication.ui.home;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;



public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon, repeatModeBtn, listBtn;
    static ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    int x = 0;
    private boolean shuffleMode = false;
    private int repeatMode = 0; // 0: No repeat, 1: Repeat One, 2: Repeat All, 3: Shuffle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        repeatModeBtn = findViewById(R.id.RepeatMode);
        listBtn = findViewById(R.id.list);

        titleTv.setSelected(true);

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                handleSongCompletion();
            }
        });

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentTimeTv.setText(convertToMMSS(currentPosition + ""));

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.pause);
                        musicIcon.setRotation(x++);
                    } else {
                        pausePlay.setImageResource(R.drawable.play);
                        musicIcon.setRotation(0);
                    }
                }
                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        pausePlay.setOnClickListener(v -> togglePlayPause());
        repeatModeBtn.setOnClickListener(v -> toggleRepeatMode());
        listBtn.setOnClickListener(v -> openPlaylist());
    }

    private void toggleRepeatMode() {
        repeatMode = (repeatMode + 1) % 4; // Cycle through 0, 1, 2, 3 (No repeat, Repeat One, Repeat All, Shuffle)
        toggleRepeatIcons();
    }

    private void toggleRepeatIcons() {
        int repeatIconResource;
        switch (repeatMode) {
            case 0:
                repeatIconResource = R.drawable.no_repeat;
                shuffleMode = false;
                break;
            case 1:
                repeatIconResource = R.drawable.repeat1;
                shuffleMode = false;
                break;
            case 2:
                repeatIconResource = R.drawable.repeatall;
                shuffleMode = false;
                break;
            case 3:
                repeatIconResource = R.drawable.shuffle;
                shuffleMode = true;
                break;
            default:
                repeatIconResource = R.drawable.no_repeat;
                shuffleMode = false;
                break;
        }

        repeatModeBtn.setImageResource(repeatIconResource);
    }

    private void openPlaylist() {
        // Implement logic to open the playlist screen
    }

    private void handleSongCompletion() {
        switch (repeatMode) {
            case 0:
                // No Repeat
                break;
            case 1:
                // Repeat One
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                break;
            case 2:
                // Repeat All
                if (MyMediaPlayer.currentIndex < songsList.size() - 1) {
                    MyMediaPlayer.currentIndex += 1;
                } else {
                    MyMediaPlayer.currentIndex = 0;
                }
                mediaPlayer.reset();
                setResourcesWithMusic();
                break;
            case 3:
                // Shuffle
                shuffleAndPlayNext();
                break;
        }
    }

    private void shuffleAndPlayNext() {
        Collections.shuffle(songsList);
        MyMediaPlayer.currentIndex = 0;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pausePlay.setImageResource(R.drawable.play);
            musicIcon.setRotation(0);
        } else {
            mediaPlayer.start();
            pausePlay.setImageResource(R.drawable.pause);
            musicIcon.setRotation(x++);
        }
    }

    @SuppressLint("DefaultLocale")
    public static String convertToMMSS(String duration) {
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private void setResourcesWithMusic() {
        currentSong = songsList.get(MyMediaPlayer.currentIndex);

        titleTv.setText(currentSong.getTitle());
        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        nextBtn.setOnClickListener(v -> playNextSong());
        previousBtn.setOnClickListener(v -> playPreviousSong());

        if (mediaPlayer.isPlaying()) {
            return;
        }

        playMusic();
    }

    public static ArrayList<AudioModel> getCurrentSongsList() {
        return songsList;
    }

    private void playMusic() {
        if (MyMediaPlayer.currentPlayingPath != null &&
                MyMediaPlayer.currentPlayingPath.equals(currentSong.getPath())) {
            mediaPlayer.start();
        } else {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(currentSong.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                seekBar.setProgress(0);
                seekBar.setMax(mediaPlayer.getDuration());
                MyMediaPlayer.currentPlayingPath = currentSong.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playNextSong() {
        if (shuffleMode) {
            shuffleAndPlayNext();
        } else {
            if (MyMediaPlayer.currentIndex == songsList.size() - 1)
                return;
            MyMediaPlayer.currentIndex += 1;
            mediaPlayer.reset();
            setResourcesWithMusic();
        }
    }

    private void playPreviousSong() {
        if (shuffleMode) {
            shuffleAndPlayNext();
        } else {
            if (MyMediaPlayer.currentIndex == 0)
                return;
            MyMediaPlayer.currentIndex -= 1;
            mediaPlayer.reset();
            setResourcesWithMusic();
        }
    }
}