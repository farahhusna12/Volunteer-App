package com.example.volunteerapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.LoginActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AdminDetailsEvent extends AppCompatActivity {

    private EventService eventService;
    private Event event; // Store event object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_details_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        // Retrieve the event object passed from the previous activity
        event = (Event) getIntent().getSerializableExtra("event");

        if (event == null) {
            MotionToast.Companion.createColorToast(this,
                    "Error",
                    "Invalid Event",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
            finish();
            return;
        }

        // Initialize buttons
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnDelete = findViewById(R.id.btnDelete);

        btnUpdate.setOnClickListener(v -> btnUpdateClicked());
        btnDelete.setOnClickListener(v -> deleteEvent(event.getEvent_id(), token));

        // Initialize EventService for API calls
        eventService = ApiUtils.getEventService();

        // Fetch event details from API
        eventService.getEvent(token, event.getEvent_id()).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    event = response.body(); // Update event object

                    // Set values to the views
                    TextView tvEventName = findViewById(R.id.tvEventName);
                    TextView tvDesc = findViewById(R.id.tvEventDescription);
                    TextView tvLocation = findViewById(R.id.tvLocation);
                    TextView tvDate = findViewById(R.id.tvDate);
                    TextView tvCategory = findViewById(R.id.tvCategory);

                    tvEventName.setText(event.getEvent_name());
                    tvLocation.setText(event.getLocation());
                    tvDate.setText(event.getDate());
                    tvDesc.setText(event.getDescription());
                    tvCategory.setText(event.getCategory());

                    ImageView activityImage = findViewById(R.id.imgEvent);
                    Log.d("ImageURL", "URL: https://codelah.my/2022484414/api/" + event.getImage());

                    Glide.with(getApplicationContext())
                            .load("https://codelah.my/2022484414/api/" + event.getImage())
                            .placeholder(R.drawable.default_cover)
                            .error(R.drawable.default_cover)
                            .into(activityImage);
                } else if (response.code() == 401) {
                    MotionToast.Companion.createColorToast(AdminDetailsEvent.this,
                            "Invalid session!",
                            "Please login again",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminDetailsEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    clearSessionAndRedirect();
                } else {
                    MotionToast.Companion.createColorToast(AdminDetailsEvent.this,
                            "Error",
                            "Failed to fetch event details",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminDetailsEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("MyApp:", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(AdminDetailsEvent.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminDetailsEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", t.toString());
            }
        });
    }

    private void btnUpdateClicked() {
        Event event = (Event) getIntent().getSerializableExtra("event");

        // Retrieve the eventId from the Event object
        int eventId = event.getEvent_id();  // Assuming `getId()` is a method in your `Event` model

        // Create an Intent to open the AdminUpdateEvent activity
        Intent intent = new Intent(getApplicationContext(), AdminUpdateEvent.class);

        // Pass the eventId to the AdminUpdateEvent activity
        intent.putExtra("event_id", eventId);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void deleteEvent(int eventId, String token) {
        if (eventId == -1) {
            MotionToast.Companion.createColorToast(this,
                    "Error",
                    "Invalid Event ID",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        eventService.deleteEvent(token, eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    MotionToast.Companion.createColorToast(AdminDetailsEvent.this,
                            "Success!",
                            "Event deleted successfully",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminDetailsEvent.this, www.sanju.motiontoast.R.font.helveticabold));

                    // Redirect back to AdminUpcomingActivity
                    Intent intent = new Intent(getApplicationContext(), AdminUpcomingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    MotionToast.Companion.createColorToast(AdminDetailsEvent.this,
                            "Error",
                            "Failed to delete event",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminDetailsEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(AdminDetailsEvent.this,
                        "Error",
                        "Error deleting event",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminDetailsEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("DeleteEvent", "Error: " + t.getMessage());
            }
        });
    }

    public void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
