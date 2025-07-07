package com.example.volunteerapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.ProfileActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.adapter.EventAdapter;
import com.example.volunteerapp.adapter.ParticipationAdapter;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.Participation;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.remote.ParticipationService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView eventList, newEventList;
    private ProgressBar progressBar4, progressBar5;
    private EventAdapter adapter;
    private ParticipationAdapter participationAdapter;
    private final Set<Integer> joinedEventIds = new HashSet<>(); // Store joined event IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView tvUsername = findViewById(R.id.tvUsername);
        ImageView evImageUser = findViewById(R.id.ivImgUser);
        progressBar4 = findViewById(R.id.progressBar4);
        progressBar5 = findViewById(R.id.progressBar5);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        eventList = findViewById(R.id.NewEventRV);
        newEventList = findViewById(R.id.currentRV);
        TextView noEventMessage = findViewById(R.id.noEvent);

        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
        tvUsername.setText(user.getUsername());

        Glide.with(getApplicationContext())
                .load("https://codelah.my/2022484414/api/" + user.getImage())
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .into(evImageUser);

        fetchUserJoinedEvents(user, noEventMessage);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_reward) {
                startActivity(new Intent(getApplicationContext(), MissionActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }else if (item.getItemId() == R.id.nav_achievement) {
                startActivity(new Intent(getApplicationContext(), AchievementActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        TextView eventUserAll = findViewById(R.id.seeCurrentActivity);
        eventUserAll.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EventUserActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        TextView eventListAll = findViewById(R.id.seeNewActivity);
        eventListAll.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EventListActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void fetchUserJoinedEvents(User user, TextView noEventMessage) {
        ParticipationService participationService = ApiUtils.getParticipationService();

        participationService.getParticipation(user.getToken(), user.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Participation>> call, @NonNull Response<List<Participation>> response) {
                if (progressBar5 != null) {
                    progressBar5.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Participation> participationList = response.body();

                    for (Participation participation : participationList) {
                        joinedEventIds.add(participation.getEvent().getEvent_id()); // Store joined event IDs
                    }

                    if (participationList.isEmpty()) {
                        newEventList.setVisibility(View.GONE);
                        noEventMessage.setVisibility(View.VISIBLE);
                    } else {
                        participationAdapter = new ParticipationAdapter(getApplicationContext(), participationList, participation -> {
                            Intent intent = new Intent(getApplicationContext(), EventUserDetailActivity.class);
                            intent.putExtra("participation", participation);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        });

                        newEventList.setAdapter(participationAdapter);
                        newEventList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        newEventList.setVisibility(View.VISIBLE);
                        noEventMessage.setVisibility(View.GONE);
                    }
                }

                loadEvents(user); // Load all events after fetching joined events
            }

            @Override
            public void onFailure(@NonNull Call<List<Participation>> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(DashboardActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(DashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void loadEvents(User user) {
        EventService eventService = ApiUtils.getEventService();

        eventService.getAllEvent(user.getToken()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (progressBar4 != null) {
                    progressBar4.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Event> events = response.body();

                    adapter = new EventAdapter(getApplicationContext(), events, event -> {
                        if (joinedEventIds.contains(event.getEvent_id())) {
                            MotionToast.Companion.createColorToast(DashboardActivity.this,
                                    "Already Joined!",
                                    "You have already joined this event",
                                    MotionToastStyle.INFO,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.SHORT_DURATION,
                                    ResourcesCompat.getFont(DashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        } else {
                            Intent intent = new Intent(getApplicationContext(), EventListDetailActivity.class);
                            intent.putExtra("event", event);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    });

                    eventList.setAdapter(adapter);
                    eventList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(DashboardActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(DashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}
