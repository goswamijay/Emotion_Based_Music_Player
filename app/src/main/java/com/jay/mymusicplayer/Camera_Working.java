package com.jay.mymusicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Camera_Working extends AppCompatActivity {

    Button btncapture;
    ImageView imagecapture;
    private static final int Image_Capture_Code = 1;
    private static final int pic_id = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_working);

        btncapture = (Button) findViewById(R.id.btn_open);
        imagecapture = (ImageView) findViewById(R.id.image_view);


        if (ContextCompat.checkSelfPermission(Camera_Working.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Camera_Working.this, new String[]{
                            Manifest.permission.CAMERA
                    },
                    100);
        }

        btncapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, pic_id);


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       /* if(requestCode == 100)
        {
            Bitmap captureImage = (Bitmap) data.getExtras().get("Data");
            imagecapture.setImageBitmap(captureImage);
        }

        */

        if (requestCode == pic_id) {
            // BitMap is data structure of image file
            // which stor the image in memory
            Bitmap photo = (Bitmap) data.getExtras()
                    .get("data");
            // Set the image in imageview for display
            imagecapture.setImageBitmap(photo);

        }
    }
}

