package com.example.volunteerapp.admin;

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
import com.example.volunteerapp.MainActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AdminProfileActivity extends AppCompatActivity {

    Dialog dialog;
    Button btnCancel,btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        dialog = new Dialog(AdminProfileActivity.this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnLogout = dialog.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnLogout.setOnClickListener(v -> logoutClicked());

        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);

        ImageView activityImage = findViewById(R.id.profileImage);
        Log.d("ImageURL", "URL: https://codelah.my/2022484414/api/" + user.getImage());
        // Use Glide to load the image into the ImageView
        Glide.with(getApplicationContext())
                .load("https://codelah.my/2022484414/api/" + user.getImage())
                .placeholder(R.drawable.default_prof_pic) // Placeholder image if the URL is empty
                .error(R.drawable.default_prof_pic) // Error image if there is a problem loading the image
                .into(activityImage);

        Button updateprofile = findViewById(R.id.BtnUpdate);
        updateprofile.setOnClickListener(v -> BtnProfileClicked());
        
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), AdminDashboardActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_upcoming) {
                startActivity(new Intent(getApplicationContext(), AdminUpcomingActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                dialog.show();
                return true;
            }
            return false;
        });
    }

    private void logoutClicked() {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        dialog.dismiss();
        // display message
        MotionToast.Companion.createColorToast(AdminProfileActivity.this,
                "Success!",
                "You have successfully logged out.",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(AdminProfileActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void BtnProfileClicked() {
        Intent intent = new Intent(getApplicationContext(), AdminProfileUpdateActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
}