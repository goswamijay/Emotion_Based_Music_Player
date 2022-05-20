package com.jay.mymusicplayer;

import android.net.Uri;

public class song {
    String title;
    Uri uri;
    Uri artworkUri;
    int size;
    int duration;

    //Constuctor


    public song(String title, Uri uri, Uri artworkUri, int size, int duration) {
        this.title = title;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.size = size;
        this.duration = duration;
    }

    //getters


    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public int getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }
}
