package com.jay.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import com.jay.mymusicplayer.MainActivity;


public class search extends AppCompatActivity {
ArrayList<song> allSongs = new ArrayList<>();
SongAdapter songAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }
    private  void SearchSong(SearchView searchView){
        //search view listener

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter songs
                filterSongs(newText.toLowerCase());
                return true;
            }
        });

    }

    private void filterSongs(String query) {
        List<song> filterlist = new ArrayList<>();

        if(allSongs.size()>0){
            for(song song : allSongs){
                if(song.getTitle().toLowerCase().contains(query)){
                    filterlist.add(song);
                }
            }
            if(songAdapter != null){
                songAdapter.filterSong(filterlist);
            }
        }
    }
}