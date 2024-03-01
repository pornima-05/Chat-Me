package com.example.testapplication2.Model;

import android.media.MediaPlayer;

public class MediaPlayerClass {

    public static MediaPlayer mediaPlayer;

    public  static  MediaPlayer getMediaPlayer()
    {
        if (mediaPlayer==null)
        {
            mediaPlayer=new MediaPlayer();
        }
        return mediaPlayer;
    }
}
