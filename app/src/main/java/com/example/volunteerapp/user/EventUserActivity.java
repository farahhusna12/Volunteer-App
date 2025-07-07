package com.example.volunteerapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volunteerapp.R;
import com.example.volunteerapp.adapter.AllParticipationAdapter;
import com.example.volunteerapp.model.Participation;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.ParticipationService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventUserActivity extends AppCompatActivity {

    RecyclerView userAllEventRV;
    ParticipationService participationService;
    AllParticipationAdapter AllparticipationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_user);

        User user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        userAllEventRV = findViewById(R.id.userAllEventRV);
        participationService = ApiUtils.getParticipationService();

        participationService.getParticipation(user.getToken(),user.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Participation>> call, @NonNull Response<List<Participation>> response) {

                Log.d("MyApp:", "Response: " + response.raw());

                List<Participation> participation = response.body();

                if (participation != null) {
                    AllparticipationAdapter = new AllParticipationAdapter(getApplicationContext(), participation, participation1 -> {
                        Intent intent = new Intent(getApplicationContext(), EventUserDetailActivity.class);
                        intent.putExtra("participation", participation1);  // Pass the event object
                        intent.putExtra("allUser", "true");
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    });
                }
                userAllEventRV.setAdapter(AllparticipationAdapter);

                userAllEventRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            }

            @Override
            public void onFailure(@NonNull Call<List<Participation>> call, @NonNull Throwable t) {
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