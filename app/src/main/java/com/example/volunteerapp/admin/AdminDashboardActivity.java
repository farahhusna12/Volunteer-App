package com.example.volunteerapp.admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volunteerapp.LoginActivity;
import com.example.volunteerapp.MainActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.adapter.AdminCurrentActivityAdapter;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AdminDashboardActivity extends AppCompatActivity {

    Dialog dialog;
    Button btnCancel,btnLogout;
    //private List<Event> activityList;
    private AdminCurrentActivityAdapter adapter;
    String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dialog = new Dialog(AdminDashboardActivity.this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnLogout = dialog.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnLogout.setOnClickListener(v -> logoutClicked());

        TextView curDate = findViewById(R.id.current_date);
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        curDate.setText(date);

        // Initialize RecyclerView
        RecyclerView recyclerView3 = findViewById(R.id.recyclerView3);
        recyclerView3.setLayoutManager(new LinearLayoutManager(this));
        recyclerView3.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Initialize empty adapter
        adapter = new AdminCurrentActivityAdapter(this, List.of());
        recyclerView3.setAdapter(adapter);

        // Register context menu for RecyclerView
        registerForContextMenu(recyclerView3);

        // Update RecyclerView with data
        updateRecyclerView();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_upcoming) {
                startActivity(new Intent(getApplicationContext(), AdminUpcomingActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), AdminProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
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
        MotionToast.Companion.createColorToast(AdminDashboardActivity.this,
                "Success!",
                "You have successfully logged out.",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(AdminDashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void updateRecyclerView() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();
        int organizerId = user.getId();

        EventService eventService = ApiUtils.getEventService();

        // Get today's date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // First, make the API call to get all events (which includes both past and future events)
        eventService.getEventsOrganizer(token,organizerId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> allEvents = response.body();

                    // Count the number of upcoming events
                    int upcomingCount = countUpcomingEvents(allEvents, currentDate);
                    TextView upcomingCountTextView = findViewById(R.id.total_upcoming);
                    upcomingCountTextView.setText(String.valueOf(upcomingCount));
                } else {
                    Log.e("MyApp:", "Failed to get all events: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(AdminDashboardActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminDashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", t.toString());
            }
        });

        // Get events filtered by date and organizerId
        eventService.getEventsByDateAndOrganizer(token, currentDate, organizerId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                Log.d("MyApp:", "Response: " + response.raw());

                if (response.isSuccessful() && response.body() != null) {
                    List<Event> allEvents = response.body();
                    adapter.updateData(allEvents); // Update adapter with all events

                    // Count the number of current events
                    int currentCount = allEvents.size();
                    TextView eventCountTextView = findViewById(R.id.total_current);
                    eventCountTextView.setText(String.valueOf(currentCount));
                } else if (response.code() == 401) {
                    MotionToast.Companion.createColorToast(AdminDashboardActivity.this,
                            "Invalid session!",
                            "Please login again",MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminDashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    clearSessionAndRedirect();
                } else {
                    // Handle other errors
                    MotionToast.Companion.createColorToast(AdminDashboardActivity.this,
                            "No Event Created",
                            "Please create event first for" +
                                    "it to be displayed",
                            MotionToastStyle.INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminDashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("MyApp:", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                // Handle failure to connect
                MotionToast.Companion.createColorToast(AdminDashboardActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminDashboardActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", t.toString());
            }
        });
    }

    // Count upcoming events based on the current date
    private int countUpcomingEvents(List<Event> events, String currentDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int upcomingCount = 0;

        for (Event event : events) {
            try {
                String eventDateString = event.getDate();
                Log.d("EventDate", "Event Date: " + eventDateString);

                if (eventDateString == null || eventDateString.isEmpty()) {
                    Log.e("EventDate", "Event date is null or empty.");
                    continue;
                }

                Date eventDate = dateFormat.parse(eventDateString);
                Date todayDate = dateFormat.parse(currentDate);

                if (eventDate != null && eventDate.after(todayDate)) {
                    upcomingCount++;
                }
            } catch (Exception e) {
                Log.e("EventDate", "Error parsing date for event: " + e.getMessage());
            }
        }

        Log.d("UpcomingCount", "Upcoming Events Count: " + upcomingCount);
        return upcomingCount;
    }

    public void clearSessionAndRedirect() {
        // Clear shared preferences and redirect to login
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}