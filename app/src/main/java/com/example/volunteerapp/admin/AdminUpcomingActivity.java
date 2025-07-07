package com.example.volunteerapp.admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volunteerapp.MainActivity;
import com.example.volunteerapp.ProfileActivity;
import com.example.volunteerapp.adapter.AdminUpcomingActivityAdapter;
import com.example.volunteerapp.LoginActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.user.EventListDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AdminUpcomingActivity extends AppCompatActivity {

    Dialog dialog;
    Button btnCancel,btnLogout;
    private EventService eventService;
    private RecyclerView recyclerView4;
    private AdminUpcomingActivityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_upcoming);

        // Apply insets for edge-to-edge design
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upcoming_event), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dialog = new Dialog(AdminUpcomingActivity.this);
        dialog.setContentView(R.layout.custom_dialog_box);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnLogout = dialog.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnLogout.setOnClickListener(v -> logoutClicked());

        // Initialize RecyclerView
        recyclerView4 = findViewById(R.id.recyclerView4);
        recyclerView4.setLayoutManager(new LinearLayoutManager(this));
        recyclerView4.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Initialize empty adapter
        adapter = new AdminUpcomingActivityAdapter(getApplicationContext(), new ArrayList<>(), event -> {
            Intent intent = new Intent(getApplicationContext(), AdminDetailsEvent.class);
            intent.putExtra("event", event); // Pass event safely
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        recyclerView4.setAdapter(adapter);

        // Register context menu for RecyclerView
        registerForContextMenu(recyclerView4);

        // Update RecyclerView with data
        updateRecyclerView();

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_upcoming);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), AdminDashboardActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_upcoming) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), AdminProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                // Show logout confirmation dialog
                dialog.show();
                return true;
            }
            return false;
        });


    }

    private void logoutClicked() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        dialog.dismiss();
        // display message
        MotionToast.Companion.createColorToast(AdminUpcomingActivity.this,
                "Success!",
                "You have successfully logged out.",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(AdminUpcomingActivity.this, www.sanju.motiontoast.R.font.helveticabold));
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

        try {
            // Get the current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date currentDate = dateFormat.parse(dateFormat.format(new Date()));

            // Make API call to get all events for the organizer
            eventService = ApiUtils.getEventService();
            eventService.getEventsOrganizer(token, organizerId).enqueue(new Callback<List<Event>>() {
                @Override
                public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                    Log.d("MyApp:", "Response: " + response.raw());

                    if (response.isSuccessful() && response.body() != null) {
                        List<Event> eventList = response.body();
                        List<Event> filteredEvents = new ArrayList<>();

                        // Filter events where the event date is after the current date
                        for (Event event : eventList) {
                            try {
                                Date eventDate = dateFormat.parse(event.getDate());
                                if (eventDate != null && eventDate.after(currentDate)) {
                                    filteredEvents.add(event);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        // Update RecyclerView
                        if (adapter == null) {
                            adapter = new AdminUpcomingActivityAdapter(getApplicationContext(), filteredEvents, event -> {
                                Intent intent = new Intent(getApplicationContext(), EventListDetailActivity.class);
                                intent.putExtra("event", (Serializable) event);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            });
                            recyclerView4.setAdapter(adapter);
                        } else {
                            adapter.updateData(filteredEvents);
                        }
                    } else if (response.code() == 401) {
                        MotionToast.Companion.createColorToast(AdminUpcomingActivity.this,
                                "Invalid session!",
                                "Please login again",
                                MotionToastStyle.WARNING,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(AdminUpcomingActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        clearSessionAndRedirect();
                    } else {
                        MotionToast.Companion.createColorToast(AdminUpcomingActivity.this,
                                "No Event",
                                "No upcoming event available." +
                                        "Add new event for it to be" +
                                        "displayed",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(AdminUpcomingActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        Log.e("MyApp:", response.toString());
                    }
                }

                @Override
                public void onFailure(Call<List<Event>> call, Throwable t) {
                    MotionToast.Companion.createColorToast(AdminUpcomingActivity.this,
                            "Error connecting to server.",
                            "Check your internet connection",
                            MotionToastStyle.NO_INTERNET,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpcomingActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("MyApp:", t.toString());
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void AddEventClicked(View view) {
        // Forward user to AdminAddEvent activity
        Intent intent = new Intent(getApplicationContext(), AdminAddEvent.class);
        startActivity(intent);
    }

    public void clearSessionAndRedirect() {
        // Clear shared preferences and redirect to login
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish(); // Ensure activity is removed from the back stack
}
}