package com.jay.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HappyMood extends AppCompatActivity {
    List<song> allSongs = new ArrayList<>();
    Uri mediaStoreUri;
    SongAdapter songAdapter;
    RecyclerView recyclerView;
    ConstraintLayout playerView;
    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_happy_mood);

        recyclerView = findViewById(R.id.Happy_playlist1);
        fetchSongs();

    }

    private void fetchSongs() {

        List<song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //define Projection

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        //order
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED;

        //get the Songs

        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, sortOrder)) {
            //cache cursor indice
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                Long albumid = cursor.getLong(albumIdColumn);


                //Song Uri

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                //album  artwork uri

                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumid);

                //remove .mp3 extension from the song name
                name = name.substring(0, name.lastIndexOf("."));

                //song items
                song song = new song(name, uri, albumArtworkUri, size, duration);

                //add song item to song list
                songs.add(song);
            }
            //display songs
            showSongs(songs);
        }
    }


    private void showSongs(List<song> songs) {

        if (songs.size() == 0) {
            Toast.makeText(this, "No Songs", Toast.LENGTH_LONG).show();
            return;
        }


        allSongs.clear();
        allSongs.addAll(songs);

        //update the tools bar title
        String title = getResources().getString(R.string.app_name) + "-" + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        //Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //for list new song on top
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        //songs Adapter
        songAdapter = new SongAdapter(this, songs, player, playerView, 0);

        //set the adapter to reclerview
        recyclerView.setAdapter(songAdapter);
    }
}


