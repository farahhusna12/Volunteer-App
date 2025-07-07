package com.example.volunteerapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volunteerapp.ProfileActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.adapter.EventAdapter;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionActivity extends AppCompatActivity {

    private RecyclerView rvCommunity;
    private RecyclerView rvHealthcare;
    private RecyclerView rvEnvironmental;
    private RecyclerView rvEducation;
    private RecyclerView rvEntertainment;
    private EventService eventService;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mission);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mission), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerViews
        rvCommunity = findViewById(R.id.Community);
        rvHealthcare = findViewById(R.id.Healthcare);
        rvEnvironmental = findViewById(R.id.Enviromental);
        rvEducation = findViewById(R.id.Education);
        rvEntertainment = findViewById(R.id.Entertainment);

        // Get the logged-in user
        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();
        eventService = ApiUtils.getEventService();

        // Fetch events by category
        fetchEventsByCategory("Community", rvCommunity, user.getToken());
        fetchEventsByCategory("Healthcare", rvHealthcare, user.getToken());
        fetchEventsByCategory("Environmental", rvEnvironmental, user.getToken());
        fetchEventsByCategory("Education", rvEducation, user.getToken());
        fetchEventsByCategory("Entertainment", rvEntertainment, user.getToken());
    }

    private void fetchEventsByCategory(String category, RecyclerView recyclerView, String token) {
        eventService.getEventsCategory(token, category).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                Log.d("MyApp", "Response for " + category + ": " + response.raw().toString());

                List<Event> events = response.body();
                if (events != null) {
                    // Set up the adapter with a click listener
                    adapter = new EventAdapter(getApplicationContext(), events, event -> {
                        Intent intent = new Intent(getApplicationContext(), EventListDetailActivity.class);
                        intent.putExtra("event", event);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    });

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Error fetching " + category + " events", Toast.LENGTH_LONG).show();
                Log.e("MyApp", Objects.requireNonNull(t.getMessage()));
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_reward);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_reward) {
                return true;
            }
            return false;
});
}

}