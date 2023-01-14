package com.example.videocallapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    String []permissions ={Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    int requestCode =1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton= findViewById(R.id.loginBtn);
        EditText userName = findViewById(R.id.userNameEdit);
        Context context;

        context=this;

        if(!isPermissionGranted()){
            askPermissions();
        }

        FirebaseApp.initializeApp(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name;

                 if(userName.getText().toString() !=null);{
                     name =userName.getText().toString();
                }
                Log.d("in login setonclick"," dekhooo");
                Intent intent = new Intent(MainActivity.this,CallActivity.class);
                intent.putExtra("username",name);
                startActivity(intent);


            }
        });

        }


    private void askPermissions() {
        ActivityCompat.requestPermissions(this,permissions,requestCode);
    }

    private boolean isPermissionGranted() {
        for(String it :permissions){
            if(ActivityCompat.checkSelfPermission(this,it)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
}