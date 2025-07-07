package com.example.volunteerapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.example.volunteerapp.user.DashboardActivity;

import java.util.Objects;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ProfileActivity extends AppCompatActivity {

    Dialog dialog;
    Button btnCancel,btnLogout;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.custom_dialog_box);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnLogout = dialog.findViewById(R.id.btnLogout);

        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);

        ImageView activityImage = findViewById(R.id.activityImage);
        Log.d("ImageURL", "URL: https://codelah.my/2022484414/api/" + user.getImage());
        // Use Glide to load the image into the ImageView
        Glide.with(getApplicationContext())
                .load("https://codelah.my/2022484414/api/" + user.getImage())
                .placeholder(R.drawable.default_cover) // Placeholder image if the URL is empty
                .error(R.drawable.default_cover) // Error image if there is a problem loading the image
                .into(activityImage);

        ImageView imageBackHome = findViewById(R.id.backHome);
        imageBackHome.setOnClickListener(v -> backHome());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnLogout.setOnClickListener(v -> logoutClicked());

        TextView logOut = findViewById(R.id.logoutText);
        logOut.setOnClickListener(v -> dialog.show());

        TextView profileDetail = findViewById(R.id.profileDetail);
        profileDetail.setOnClickListener(v -> profileDetailClicked());

        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());

    }

    private void profileDetailClicked() {
        Intent intent = new Intent(getApplicationContext(), ProfileDetailActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    private void logoutClicked() {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        dialog.dismiss();
        // display message
        MotionToast.Companion.createColorToast(ProfileActivity.this,
                "Success!",
                "You have successfully logged out.",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(ProfileActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void backHome() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}