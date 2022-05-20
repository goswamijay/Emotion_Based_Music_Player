package com.jay.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import android.os.Bundle;

public class Mood_Selector extends AppCompatActivity {
    Button recognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selector);
        recognize = findViewById(R.id.recognise);

        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Mood_Selector.this, Recognize.class);
                startActivity(intent);
            }
        });
    }
   /* private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void activityRecognize(View v) {

        if(isNetworkAvailable()) {
            //
        }else {
            Snackbar.make(v, "This requires Internet Connection", Snackbar.LENGTH_LONG).show();
        }
    }*/

    public void Happy(View view) {
        Intent intent = new Intent(Mood_Selector.this,HappyMood.class);
        startActivity(intent);

    }

    public void Sad(View view) {
        Intent intent = new Intent(Mood_Selector.this,sad_mode.class);
        startActivity(intent);

    }

    public void Angry(View view) {
        Intent intent = new Intent(Mood_Selector.this,angry_mood.class);
        startActivity(intent);

    }

    public void Contempt(View view) {
        Intent intent = new Intent(Mood_Selector.this,Contempt_mood.class);
        startActivity(intent);

    }

    public void Disgust(View view) {
        Intent intent = new Intent(Mood_Selector.this,disgust_mood.class);
        startActivity(intent);

    }

    public void Fear(View view) {
        Intent intent = new Intent(Mood_Selector.this,fear_mood.class);
        startActivity(intent);

    }

    public void Neutral(View view) {
        Intent intent = new Intent(Mood_Selector.this,neutral_mood.class);
        startActivity(intent);

    }

    public void Surprise(View view) {
        Intent intent = new Intent(Mood_Selector.this,surprice_mood.class);
        startActivity(intent);

    }
}