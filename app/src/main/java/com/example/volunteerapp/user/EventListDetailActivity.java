package com.example.volunteerapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.Participation;
import com.example.volunteerapp.model.UpdatePoints;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.ParticipationService;
import com.example.volunteerapp.remote.UserService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import org.w3c.dom.Text;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class EventListDetailActivity extends AppCompatActivity {

    Event event;
    UpdatePoints updatePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_detail);

        TextView tvEventName = findViewById(R.id.tvEventName);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView eventOrganizer = findViewById(R.id.tvOrganizer);
        Button joinEventBtn = findViewById(R.id.joinEventBtn);
        ImageView tvActivityImage = findViewById(R.id.tvActivityImage);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvPoint = findViewById(R.id.textView13);

        // Get the event object from the Intent
        event = (Event) getIntent().getSerializableExtra("event");

        if (event != null) {
            tvEventName.setText(event.getEvent_name());
            tvAddress.setText(event.getLocation());
            tvDate.setText(event.getDate());
            tvDescription.setText(event.getDescription());
            eventOrganizer.setText(event.getOrganizer().getOrganizer_name());
            int points = getPointsByCategory(event.getCategory());
            tvPoint.setText(String.valueOf(points));

            Glide.with(this)
                    .load("https://codelah.my/2022484414/api/" + event.getImage())
                    .placeholder(R.drawable.default_cover)
                    .error(R.drawable.default_cover)
                    .into(tvActivityImage);
        }

        ImageView backHome = findViewById(R.id.backHome);
        backHome.setOnClickListener(v -> onBackPressed());

        joinEventBtn.setOnClickListener(v -> joinButtonClicked());
    }

    private void joinButtonClicked() {

        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        ParticipationService participationService = ApiUtils.getParticipationService();

        participationService.createParticipation(user.getToken(), user.getId(), event.getEvent_id()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Participation> call, @NonNull Response<Participation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MotionToast.Companion.createColorToast(EventListDetailActivity.this,
                            "Successful!",
                            "You successfully join the activity",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EventListDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                    // Determine points based on the event category
                    int pointsToAdd = getPointsByCategory(event.getCategory());

                    // Update the user's points
                    updateUserPoints(pointsToAdd);

                    // Optionally, you can update the UI or navigate to another activity
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    MotionToast.Companion.createColorToast(EventListDetailActivity.this,
                            "Error!",
                            "Failed to join the event. Please try again.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EventListDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("Join Event", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Participation> call, @NonNull Throwable throwable) {
                MotionToast.Companion.createColorToast(EventListDetailActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EventListDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", Objects.requireNonNull(throwable.getMessage()));
            }
        });
    }

    private int getPointsByCategory(String category) {
        switch (category) {
            case "Community":
                return 500;
            case "Healthcare":
                return 400;
            case "Environmental":
                return 300;
            case "Education":
                return 250;
            case "Entertainment":
                return 200;
            default:
                return 0;
        }
    }

    private void updateUserPoints(int pointsToAdd) {
        // Add the points to the user's total
        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
        int newTotalPoints = user.getPoints() + pointsToAdd;

        UpdatePoints updatePoints = new UpdatePoints(user.getId(),newTotalPoints);

        UserService userService = ApiUtils.getUserService();

        // Call the API
        userService.updateUserPoints(user.getToken(), user.getId(), updatePoints).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User updatedUser = response.body();
                    if (updatedUser != null) {
                        SharedPrefManager.getInstance(getApplicationContext()).saveUser(updatedUser);
                    }
                    Log.d("Update Points", "User points updated successfully!");
                    Log.d("Update Points", "User points " + newTotalPoints);
                    assert response.body() != null;
                    Log.d("API Response", response.body().toString());


                    // Optionally update UI or notify user
                } else {
                    Log.e("Update Points", "Takleh add point lagi check error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("Update Points", "Failed to update points: " + t.getMessage());
            }
        });
    }
}