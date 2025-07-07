package com.example.volunteerapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.volunteerapp.ProfileActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.UserService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        // Get the current user from SharedPrefManager
        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        // Get the UI elements
        TextView totalpoint = findViewById(R.id.point);
        ImageView tier = findViewById(R.id.tierImage);

        // Set the total points text
        String pointsString = String.valueOf(user.getPoints());
        totalpoint.setText(pointsString);

        // Fetch all users and update tier image
        fetchAllUsersAndUpdateTier(pointsString);

        // Setup the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_achievement);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_reward) {
                startActivity(new Intent(getApplicationContext(), MissionActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else return item.getItemId() == R.id.nav_achievement;
        });
    }

    private void fetchAllUsersAndUpdateTier(String pointsString) {
        UserService userService = ApiUtils.getUserService();
        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
        String apiKey = user.getToken();
        Call<List<User>> call = userService.getAllUsers(apiKey);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    List<User> users = response.body();
                    if (users != null) {
                        updateTierImageBasedOnAllUsers(users, pointsString);
                    }
                } else {
                    Log.e("API_ERROR", "Failed to fetch users: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("API_ERROR", "Failed to fetch users: " + t.getMessage());
            }
        });
    }

    private void updateTierImageBasedOnAllUsers(List<User> users, String pointsString) {
        User currentUser = SharedPrefManager.getInstance(getApplicationContext()).getUser();
        int currentUserPoints = Integer.parseInt(pointsString);
        ImageView tierImageView = findViewById(R.id.tierImage);
        int rank = 1;
        for (User user : users) {
            if (user.getPoints() > currentUserPoints) {
                rank++;
            }
        }

        if (currentUserPoints == 0) {
            tierImageView.setImageResource(R.drawable.no_medal);
        } else if (rank == 1) {
            tierImageView.setImageResource(R.drawable.gold);  // Gold for rank 1
        } else if (rank == 2) {
            tierImageView.setImageResource(R.drawable.silver);  // Silver for rank 2
        } else if (rank == 3) {
            tierImageView.setImageResource(R.drawable.bronze);  // Bronze for rank 3
        } else {
            tierImageView.setImageResource(R.drawable.no_medal);  // No medal for others
        }
    }
}