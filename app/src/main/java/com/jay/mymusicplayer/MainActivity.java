package com.jay.mymusicplayer;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.util.Log;
import com.google.gson.Gson;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button cameraButton;
    String[] items;

    //member
    RecyclerView recyclerView;
    ArrayList<song> songs = new ArrayList<>();
    ArrayList<song> favList = new ArrayList<song>();
    SongAdapter songAdapter;
    SongAdapter Song1Adapter;

    ArrayList<song> allSongs = new ArrayList<>();

    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String MY_PREF="my pref";

    //today
    ExoPlayer player;
    ActivityResultLauncher<String> recordAudioPermissonLauncher;
    ConstraintLayout playerView;
    TextView playerCloseBtn;

    //controls
    TextView songNameView,skipPreviousBtn,skipNextBtn,playpauseBtn,repeatModeBtn,playlistBtn;
    TextView homesongNameView, homeSkipPreviousBtn,homePlayPauseBtn,homeSkipNextBtn;

    //wrapper
    ConstraintLayout homeControlWrapper,headWrapper,artworkWrapper,seekbarWrapper, controlWrapper,audioVisualizerWrapper;
    //artwork
    CircleImageView artworkingView;
    //seek bar
    SeekBar seekbar;
    TextView progressView,durationView;
    //Audio Visualizer
    BarVisualizer audioVisualizer;
    //Blur image view
    BlurImageView blurImageView;

    //statusbar & navigation color;
     int defaultStatusColor;

     //repeat Mode
    int repeatMode=1; // repeat all =1, repeat one=2,shuffle all=3

    //is the act.bound?
    boolean isBound = false;
    String folder_name;



    ActivityResultLauncher<String> storagePermissionLauncher;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;

    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    private boolean isRecordPermissionGranted = false;
    private SearchView searchView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //save the status color
        defaultStatusColor = getWindow().getStatusBarColor();
        //set the navigation color
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199)); //0 &255


        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        recyclerView = findViewById(R.id.listViewSong);
        cameraButton = (Button) findViewById(R.id.Camera_btn);

        folder_name = getIntent().getStringExtra("Folder Name");
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.putString("PlaylistFolderName",folder_name);
        editor.apply();

        //recylerView
        //launch storage permission
        // storagePermissionLauncher.launch(permission);
        // runtimePermission();
        permissionLauncher();
        requestPermission();
        //player = new ExoPlayer.Builder(this).build();
        cameraButton.setOnClickListener(this);

        //player = new ExoPlayer.Builder(this).build();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.Camera_btn) {
            Intent intent = new Intent(MainActivity.this, Recognize.class);
            startActivity(intent);
        }
    }

    private void permissionLauncher() {
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                fetchSongs();
            } else {
                userResponses();
            }
        });

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                    isReadPermissionGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);

                }
                if (result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null) {
                    isWritePermissionGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if (result.get(Manifest.permission.RECORD_AUDIO) != null) {
                    isRecordPermissionGranted = result.get(Manifest.permission.RECORD_AUDIO);
                }
            }


        });

        player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playpauseBtn = findViewById(R.id.playpauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playlistBtn = findViewById(R.id.playlistBtn);

        homesongNameView = findViewById(R.id.homeSongNameview);
        homeSkipPreviousBtn= findViewById(R.id.homeSkipPreviousBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn);

        //Wrapper
        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        artworkWrapper = findViewById(R.id.artworkWrapper);
        seekbarWrapper = findViewById(R.id.seekbarWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);

        //artWork

        artworkingView = findViewById(R.id.artworkView);
        //seekbar
        seekbar = findViewById(R.id.seekbar);
        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);
        //audio visualizer
        audioVisualizer = findViewById(R.id.visualizer);
        //blue image view
        blurImageView = findViewById(R.id.blurImageView);
        //player controls method
        playerControls();



        //recyclerView = findViewById(R.id.playlist_rv);

        //bind to the player service,do everything after the binding
        //deBindService();

    }

   /* private void deBindService() {
        Intent playerServiceIntent = new Intent(this,PlayerService.class);
        bindService(playerServiceIntent,playerServiceConnection, Context.BIND_AUTO_CREATE);
    }*/
    /*ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //get the service instance
            PlayerService.ServiceBinder binder = (PlayerService.ServiceBinder) iBinder;
            player = binder.getPlayerService().player;
            isBound = true;
            //ready to show songs
            storagePermissionLauncher.launch(permission);
            //call player control method
            playerControls();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };*/


    private void playerControls() {
        //song name marqee
        songNameView.setSelected(true);
        homesongNameView.setSelected(true);
        //exit the player view
        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
       /* playlistBtn.setOnClickListener(view -> exitPlayerView());*/


      playlistBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              PopupMenu popupMenu = new PopupMenu(getApplicationContext(),view);
              MenuInflater menuInflater = popupMenu.getMenuInflater();
              menuInflater.inflate(R.menu.playlist,popupMenu.getMenu());

              popupMenu.show();
              popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem menuItem) {
                      switch (menuItem.getItemId())
                      {
                          case R.id.Happy_playlist:
                              RecyclerView recyclerView = findViewById(R.id.Happy_playlist1);
                              Gson gson = new Gson();
                              String json = gson.toJson(recyclerView);
                              Log.d("gson",json);
                              for(int i=0;i<songs.size();i++)
                              {
                                  Log.d("Music123","AddToFavotite"+songs.get(i).getTitle());
                              }
                              Intent intent = new Intent(MainActivity.this,HappyMood.class);
                              startActivity(intent);
                              break;

                          case R.id.Sad_playlist:

                              RecyclerView recyclerView1 = findViewById(R.id.sad_playlist1);
                              Intent intent1 = new Intent(MainActivity.this,sad_mode.class);
                              startActivity(intent1);
                              break;
                          case R.id.Angry_playlist:
                              RecyclerView recyclerView2 = findViewById(R.id.angry_playlist1);
                              Intent intent2 = new Intent(MainActivity.this,angry_mood.class);
                              startActivity(intent2);
                          case R.id.Contempt_playlist:
                              RecyclerView recyclerView3 = findViewById(R.id.contempt_playlist1);
                              Intent intent3 = new Intent(MainActivity.this,Contempt_mood.class);
                              startActivity(intent3);
                          case R.id.Disgust_playlist:
                              RecyclerView recyclerView4 = findViewById(R.id.disgust_playlist1);
                              Intent intent4 = new Intent(MainActivity.this,disgust_mood.class);
                              startActivity(intent4);
                          case R.id.Fear_playlist:
                              RecyclerView recyclerView5 = findViewById(R.id.Fear_playlist1);
                              Intent intent5 = new Intent(MainActivity.this,fear_mood.class);
                              startActivity(intent5);
                          case R.id.Neutral_playlist:
                              RecyclerView recyclerView6 = findViewById(R.id.Neutral_playlist1);
                              Intent intent6 = new Intent(MainActivity.this,neutral_mood.class);
                              startActivity(intent6);
                          case R.id.Surprise_playlist:
                              RecyclerView recyclerView7 = findViewById(R.id.Surprise_playlist1);
                              Intent intent7 = new Intent(MainActivity.this,surprice_mood.class);
                              startActivity(intent7);
                          case R.id.Default_playlist:
                              Toast.makeText(MainActivity.this,"Happy Mood",Toast.LENGTH_SHORT).show();
                      }
                      return true;
                  }
              });
          }
      });
      /*  playlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaylistDialog playlistDialog = new PlaylistDialog(allSongs, Song1Adapter);
                playlistDialog.show(getSupportFragmentManager(),playlistDialog.getTag());
            }
        });*/
        //open player view on home control wrapper click
        homeControlWrapper.setOnClickListener(view -> showPlayerView());

        //player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                //show the playing song title
                songNameView.setText(mediaItem.mediaMetadata.title);
                homesongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                seekbar.setProgress((int) player.getCurrentPosition());
                seekbar.setMax((int) player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playpauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_filled_24,0,0,0);
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                //show current Art
                showCurrentArtwork();
                //update the progress positon of a current playing song
                updatePlayerPositonProgress();
                //load artwork animation
                artworkingView.setAnimation(loadRotation());

                //set Audio Visulatizer

                //update player view colors
                updatePlayerColors();

                if(!player.isPlaying()){
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == ExoPlayer.STATE_READY){
                    //set values to player views
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homesongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekbar.setMax((int) player.getDuration());
                    seekbar.setProgress((int) player.getCurrentPosition());

                    playpauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_filled_24,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                    //show current Art
                    showCurrentArtwork();
                    //update the progress positon of a current playing song
                    updatePlayerPositonProgress();
                    //load artwork animation
                    artworkingView.setAnimation(loadRotation());

                    //set Audio Visulatizer

                    //update player view colors
                    updatePlayerColors();

                }
                else {
                    playpauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_filled_24,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
                }
            }
        });

        //skip to next track
        skipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipNextBtn.setOnClickListener(view -> skipToNextSong());

        //skip to previous track
        skipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        homeSkipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());

        //play or pause the player
        playpauseBtn.setOnClickListener(view -> playOrPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        //seek bar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue=0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.getPlaybackState() == ExoPlayer.STATE_READY){
                    seekBar.setProgress(progressValue);
                    progressView.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }
            }
        });

        //repeat mode
        repeatModeBtn.setOnClickListener(view -> {
           if(repeatMode == 1){
               //repeat me
               player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
               repeatMode = 2;
               repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one,0,0,0);
           }

           else if(repeatMode == 2){
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode = 3;
               repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shuffle_24,0,0,0);
            }
           else if(repeatMode == 3){
               //repeat all
               player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
               player.setShuffleModeEnabled(false);
               repeatMode = 1;
               repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repeat_all,0,0,0);

           }
           updatePlayerColors();
        });
    }

    private void playOrPausePlayer() {
        if(player.isPlaying()){
            player.pause();
            playpauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_filled_24,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
            artworkingView.clearAnimation();
        }
        else {
            player.play();
            playpauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_filled_24,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
            artworkingView.startAnimation(loadRotation());
        }

        //update player colors
        updatePlayerColors();
    }

    private void skipToNextSong() {
        if (player.hasNextMediaItem()) {

            player.seekToNext();
        }
    }
    private void skipToPreviousSong() {
        if (player.hasPreviousMediaItem()) {

            player.seekToPrevious();
        }
    }

    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void updatePlayerPositonProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(player.isPlaying()){
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekbar.setProgress((int) player.getCurrentPosition());
                }
                //repeat calling the method
                updatePlayerPositonProgress();
            }

        }, 1000);
    }

    private void showCurrentArtwork() {
        artworkingView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if(artworkingView.getDrawable() == null){
            artworkingView.setImageResource(R.drawable.ic_baseline_music_note_24);
        }
    }


    String getReadableTime(int duration){
        String time;
        int hrs = duration/(1000*60*60);
        int min = (duration%(1000*60*60)/(1000*60));
        int secs = (((duration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if(hrs<1){
            time = min + ":" + secs;
        }
        else {
            time = hrs + ":" + min + ":" + secs;
        }
        return  time;
    }




    private void updatePlayerColors() {
        /*BitmapDrawable bitmapDrawable = (BitmapDrawable) artworkingView.getDrawable();
        if(bitmapDrawable == null){
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.ic_baseline_music_note_24);
        }
        assert bitmapDrawable != null;
        Bitmap bmp = bitmapDrawable.getBitmap();
        //set bitmap to blur image view
        blurImageView.setImageBitmap(bmp);
        blurImageView.setBlur(4);*/

        //player control colors


    }
    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }


    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));//0 to 255

    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //release the player
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
       /* doUnbindService();*/
    }

  /*  private void doUnbindService() {
        if(isBound){
            unbindService(playerServiceConnection);
            isBound = false;
        }
    }*/

    private void userResponses() {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            fetchSongs();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission)) {

                new AlertDialog.Builder(this)
                        .setTitle("Request Permission")
                        .setMessage("Allow us to fetch songs on your device")
                        .setPositiveButton("allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "Your denied to show songs", Toast.LENGTH_LONG).show();

                            }
                        })
                        .show();
            }

        }
    }


    private void requestPermission() {
        isReadPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        isRecordPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<String>();

        if (!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }

        if (!isWritePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!isRecordPermissionGranted) {
            permissionRequest.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!permissionRequest.isEmpty()) {
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));

        }

        if (isRecordPermissionGranted && isWritePermissionGranted && isReadPermissionGranted) {
            fetchSongs();
        }
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
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        //songs Adapter
        songAdapter = new SongAdapter(this, songs,player,playerView,0);

        //set the adapter to reclerview
       recyclerView.setAdapter(songAdapter);







        //recycleview animation
        //ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(songAdapter);
        //scaleInAnimationAdapter.setDuration(1000);
        //scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        //scaleInAnimationAdapter.setFirstOnly(false);
       // recyclerView.setAdapter(scaleInAnimationAdapter);

    }


  /*
    private void playSong(String path) throws IllegalArgumentException,
            IllegalStateException, IOException {
        String extStorageDirectory = Environment.getExternalStorageDirectory()
                .toString();

        path = extStorageDirectory + File.separator + path;

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(path);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

   */

    /*
    public void runtimePermission()
    {

        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();

                    }
                }).check();




    }

 */





/*

    private String[] getMusic() {
        final Cursor mCursor = managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME }, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        int count = mCursor.getCount();

        String[] songs = new String[count];
        int i = 0;
        if (mCursor.moveToFirst()) {
            do {
                songs[i] = mCursor.getString(0);
                i++;
            } while (mCursor.moveToNext());
        }

        mCursor.close();

        return songs;
    }

 */


    /*
    void displaySongs() {
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());

        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");

        }
        ArrayAdapter<String> adp = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_player, R.id.txtsongname, items);

        customAdapter customAdapter = new customAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listView.getItemAtPosition(i);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("pos", i));
            }
        });
    }

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();

        for (File singlefile : files) {
            if (singlefile.isDirectory() && !singlefile.isHidden()) {
                arrayList.addAll(findSong(singlefile));
            } else {
                if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                    arrayList.add(singlefile);
                }
            }
        }
        return arrayList;
    }


    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textsong = myView.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(items[i]);

            return myView;
        }
    }


     */

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        //SearchView searchView = (SearchView) menuItem.getActionView();
        //SearchSong(searchView);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
           case R.id.search:

                //SearchSong(searchView);
                //SearchSong(searchView);
             /*  Intent intent = new Intent(MainActivity.this,search.class);
               startActivity(intent);*/
                Toast.makeText(getApplicationContext(), "Select Search", Toast.LENGTH_LONG).show();
                //return super.onSearchRequested();
                break;

            case R.id.Capture_Emotion:
                    Intent intent = new Intent(MainActivity.this, Recognize.class);
                    startActivity(intent);
                    break;

            case R.id.Login_activity:
                Intent intent1 = new Intent(this,loginpage.class);
                startActivity(intent1);
                break;

            case R.id.signup_activity:
                Intent intent2 = new Intent(this,SignupPage.class);
                startActivity(intent2);
                break;

            case R.id.Manage_Songs:
                Toast.makeText(getApplicationContext(), "Select Manage_Songs", Toast.LENGTH_LONG).show();
                return true;

            case R.id.Settings:
                Toast.makeText(getApplicationContext(), "Select Settings", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
return true;
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