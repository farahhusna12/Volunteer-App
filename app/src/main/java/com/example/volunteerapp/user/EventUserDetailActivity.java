package com.example.volunteerapp.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Participation;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.ParticipationService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.sql.Date; // Use java.sql.Date consistently
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class EventUserDetailActivity extends AppCompatActivity {

    private TextView tvOrganizer;
    Date eventDate;
    String status;
    User user;
    Participation participation;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_user_detail);

        user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        TextView tvEventName = findViewById(R.id.tvEventName);
        TextView tvStatDesc = findViewById(R.id.tvStatDesc);
        //tvOrganizer = findViewById(R.id.tvOrganizer);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvStatus = findViewById(R.id.tvStatus);
        ImageView ivEventImg = findViewById(R.id.ivEventImg);
        TextView tvEventId = findViewById(R.id.EventId);
        TextView participationIdTv = findViewById(R.id.participationIdTv);


        participation = (Participation) getIntent().getSerializableExtra("participation");

        // Get today's date
        Date today = new Date(System.currentTimeMillis());

        if(participation!=null){

            eventDate = Date.valueOf(participation.getEvent().getDate());

            if (eventDate != null && !eventDate.before(today)){
                status = "Upcoming";
            }else{
                status = "Completed";
            }

            tvEventName.setText(participation.getEvent().getEvent_name());
            tvStatDesc.setText(participation.getEvent().getDescription());
            //tvOrganizer.setText(participation.getEvent().getOrganizer());
            tvDate.setText(participation.getEvent().getDate());
            tvAddress.setText(participation.getEvent().getLocation());
            tvStatus.setText(status);
            tvEventId.setText(String.valueOf(participation.getEvent().getEvent_id()));
            participationIdTv.setText(String.valueOf(participation.getParticipation_id()));


            Glide.with(this)
                    .load("https://codelah.my/2022484414/api/" + participation.getEvent().getImage())
                    .placeholder(R.drawable.default_cover)
                    .error(R.drawable.default_cover)
                    .into(ivEventImg);
        }

        ImageView backHome = findViewById(R.id.backHome);
        backHome.setOnClickListener(v -> onBackPressed());

        Button cancel = findViewById(R.id.cancelParticipation);

        cancel.setVisibility(View.VISIBLE); // Show the button

        // Check the status
        if (status.equalsIgnoreCase("Upcoming")) {
            cancel.setText("Cancel Participation");
        } else if (status.equalsIgnoreCase("Completed")) {
            cancel.setText("Delete");
            cancel.setBackgroundColor(Color.RED);
        }

        cancel.setOnClickListener(v -> cancelParticipationClicked());

    }

    private void cancelParticipationClicked() {

        ParticipationService participationService = ApiUtils.getParticipationService();

        participationService.deleteParticipation(user.getToken(),participation.getParticipation_id()).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                if (response.isSuccessful()) {
                    // Inform the user of the success
                    MotionToast.Companion.createColorToast(EventUserDetailActivity.this,
                            "Successful!",
                            "Participation canceled successfully!",
                            MotionToastStyle.DELETE,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EventUserDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    finish();
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    // Optionally update other UI elements if needed
                } else {
                    // Handle API response errors
                    MotionToast.Companion.createColorToast(EventUserDetailActivity.this,
                            "Error!",
                            "Failed to cancel participation. Try again!",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EventUserDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                MotionToast.Companion.createColorToast(EventUserDetailActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EventUserDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", Objects.requireNonNull(throwable.getMessage()));
            }
        });
    }
}