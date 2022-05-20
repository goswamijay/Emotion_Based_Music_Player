package com.jay.mymusicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //members
    Context context;
    List<song> songs;
    ExoPlayer player;
    ConstraintLayout playerView;
    BottomSheetDialog bottomSheetDialog;
    private int viewType;

    //constructor


    public SongAdapter(Context context, List<song> songs , ExoPlayer player,ConstraintLayout playerView,int viewType) {
        this.context = context;
        this.songs = songs;
        this.player=player;
        this.playerView = playerView;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate song row item layout

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //Current song and View Holder

        song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;

        //set Values to views

        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.sizeHolder.setText(getSize(song.getSize()));
        viewHolder.addfav.setImageResource(R.drawable.ic_playlist);

        // artWork
        Uri artworkUri = song.getArtworkUri();

        if(artworkUri != null){
            //set the uri to imageview

            viewHolder.artworkHolder.setImageURI(artworkUri);

            //make sure that the uri has an artwork
            if(viewHolder.artworkHolder.getDrawable() == null){
                viewHolder.artworkHolder.setImageResource(R.drawable.ic_baseline_music_note_24);
            }
        }
        viewHolder.itemView.setOnClickListener(view -> {
            //start the player service
            // context.startService(new Intent(context.getApplicationContext(),PlayerService.class));
            //play the song
            if(!player.isPlaying()){
                player.setMediaItems(getMediaItems(),position,0);
            }else {
                player.pause();
                player.seekTo(position,0);
            }
            //prepare and play
            player.prepare();
            player.play();
            Toast.makeText(context,song.getTitle(),Toast.LENGTH_LONG).show();

            //show the player view
            playerView.setVisibility(View.VISIBLE);
        });



        if(viewType==0)
        {
            //play song on Item Click

        }else {
            viewHolder.itemView.setVisibility(View.GONE);
            viewHolder.titleHolder.setTextColor(Color.WHITE);
            viewHolder.durationHolder.setTextColor(Color.WHITE);
            viewHolder.addfav.setVisibility(View.GONE);
        }





    }

    private List<MediaItem> getMediaItems() {
        //define a list program media items
        List<MediaItem> mediaItems = new ArrayList<>();

        for(song song:songs){
            MediaItem mediaItem =new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();

            //add the media item to media items list
            mediaItems.add(mediaItem);
        }
        return  mediaItems;
    }

    private MediaMetadata getMetadata(song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getArtworkUri())
                .build();
    }

    //View Holder
    public static class SongViewHolder extends RecyclerView.ViewHolder{
        //member
        ImageView artworkHolder,addfav;
        TextView titleHolder,durationHolder,sizeHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.imgsong);
            titleHolder = itemView.findViewById(R.id.txtsongname);
            durationHolder = itemView.findViewById(R.id.durationsong);
            sizeHolder = itemView.findViewById(R.id.sizesong);
            addfav = itemView.findViewById(R.id.addFav);


        }
    }

    @Override
    public int getItemCount() {

        return songs.size();
    }

    //filter songs /search results

    @SuppressLint("NotifyDataSetChanged")
    public void filterSong(List<song> filterList){

        songs = filterList;
        notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    private String getDuration(int totalDuration){
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = (((totalDuration%(1000*60*60))%(1000*60*60))%(1000*600))/1000;

        if(hrs<1){
            totalDurationText = String.format("%02d:%02d",min,secs);

        }
        else {
            totalDurationText = String.format("%1d:%02d:%02d",hrs,min,secs);
        }
        return totalDurationText;
    }

    //size
    private String getSize (long bytes){
        String hrSize;

        double k = bytes/1024.0;
        double m = ((bytes/1024.0)/1024.0);
        double g = (((bytes/1024.0)/1024.0)/1024.0);
        double t = ((((bytes/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if(t>1){
            hrSize = dec.format(t).concat("TB");
        }else if (g>1){
            hrSize = dec.format(g).concat("GB");
        }else if (m>1){
            hrSize = dec.format(m).concat("MB");
        }
        else if (k>1){
            hrSize = dec.format(k).concat("KB");
        }
        else{
            hrSize = dec.format(g).concat("KB");
        }

        return hrSize;

    }
}
