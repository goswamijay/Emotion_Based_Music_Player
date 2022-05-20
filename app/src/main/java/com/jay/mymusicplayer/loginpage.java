package com.jay.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class loginpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);


        TextView username= (TextView)findViewById(R.id.Username);
        TextView password = (TextView) findViewById(R.id.password);


        MaterialButton loginbtn = (MaterialButton) findViewById(R.id.loginbtn);
        MaterialButton signup = (MaterialButton) findViewById(R.id.signup);


        //admin and admin
//        loginbtn = findViewById(R.id.)
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().equals("admin") && password.getText().toString().equals("admin")){
                    //Correct Password
                    Toast.makeText(loginpage.this, "Login Succussfully",Toast.LENGTH_SHORT).show();
                }
                else{
                    //Incorrect
                    Toast.makeText(loginpage.this, "Login Failed !!!",Toast.LENGTH_SHORT).show();
                }
            }

        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginpage.this,SignupPage.class);
                startActivity(intent);
            }
        });
    }
}