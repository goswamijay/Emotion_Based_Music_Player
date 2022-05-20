package com.jay.mymusicplayer;

import static com.jay.mymusicplayer.MainActivity.MY_PREF;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistDialog extends BottomSheetDialogFragment {

    ArrayList<song> allSongs = new ArrayList<>();
    SongAdapter SongAdapter;
    SongAdapter  Song1Adapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView RecyclerView ;
    TextView Folder;



    public PlaylistDialog (ArrayList<song> arrayList, com.jay.mymusicplayer.SongAdapter song1Adapter) {
        this.allSongs = arrayList;
        this.Song1Adapter =  SongAdapter;


    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_layout, null);
        bottomSheetDialog.setContentView(view);

        RecyclerView = view.findViewById(R.id.playlist_rv);
        Folder = view.findViewById(R.id.playlist_name);
        SharedPreferences preferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String folerName = preferences.getString("PlaylistFolderName", "abc");
        Folder.setText(folerName);

        allSongs = fetchSong(folerName);
        Song1Adapter = new SongAdapter(getContext(),allSongs,null,null,1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
        RecyclerView.setLayoutManager(layoutManager);
        RecyclerView.setAdapter(Song1Adapter);
        Song1Adapter.notifyDataSetChanged();
        return bottomSheetDialog;


    }

    private ArrayList<song> fetchSong(String Folder) {

        ArrayList<song> songs = new ArrayList<>();
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

        try (Cursor cursor = getContext().getContentResolver().query(mediaStoreUri, projection, null, null, sortOrder)) {
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
            return songs;

        }

    }
}
