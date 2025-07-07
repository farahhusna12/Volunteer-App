package com.example.volunteerapp;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.volunteerapp.admin.AdminDashboardActivity;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.example.volunteerapp.user.DashboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatButton loginButton = findViewById(R.id.button);
        AppCompatButton signUpButton = findViewById(R.id.button2);

        loginButton.setOnClickListener(v -> {
            //  handle login button click
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        signUpButton.setOnClickListener(v -> {
            Intent signupIntent = new Intent(MainActivity.this,SignUpActivity.class);
            startActivity(signupIntent);
        });

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (spm.isLoggedIn()) { //session record
            if(spm.getUser().getRole().equalsIgnoreCase("user")){
                Intent intent = new Intent(this, DashboardActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        }
    }
}