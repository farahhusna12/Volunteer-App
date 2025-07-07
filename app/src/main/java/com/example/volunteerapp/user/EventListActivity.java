package com.example.volunteerapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volunteerapp.R;
import com.example.volunteerapp.adapter.AllEventAdapter;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListActivity extends AppCompatActivity {

    RecyclerView newAllEventRV;
    EventService eventService;
    AllEventAdapter AlleventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        newAllEventRV = findViewById(R.id.newAllEventRV);
        eventService = ApiUtils.getEventService();

        eventService.getAllEvent(user.getToken()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {

                List<Event> event = response.body();

                if (event != null) {
                    AlleventAdapter = new AllEventAdapter(getApplicationContext(), event, event1 -> {
                        Intent intent = new Intent(getApplicationContext(), EventListDetailActivity.class);
                        intent.putExtra("event", event1);  // Pass the event object
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    });
                }


                newAllEventRV.setAdapter(AlleventAdapter);

                newAllEventRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {

                Toast.makeText(getApplicationContext(), "Error connecting to the server", Toast.LENGTH_LONG).show();
                Log.e("MyApp:", Objects.requireNonNull(t.getMessage()));

            }
        });

        ImageView imageBackHome = findViewById(R.id.backHome);
        imageBackHome.setOnClickListener(v -> backHome());
    }

    private void backHome() {
        Intent intent = new Intent(getApplicationContext(),DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}