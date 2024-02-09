package com.example.myapplication.ui.home;

import android.media.MediaPlayer;

public class MyMediaPlayer {
    static MediaPlayer instance;
    static String currentPlayingPath; // Add this line

    public static int currentIndex = -1;


    public static MediaPlayer getInstance(){
        if(instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

}