package com.example.volunteerapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volunteerapp.LoginActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.adapter.ViewParticipantsAdapter;
import com.example.volunteerapp.model.Participation;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.ParticipationService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ViewParticipantsActivity extends AppCompatActivity {

    //private ViewParticipantsAdapter adapter;
    private RecyclerView rvParticipants;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_participants);

        rvParticipants = findViewById(R.id.participantList);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();
        //int user_id = user.getId();

        ParticipationService participantService = ApiUtils.getParticipationService();

        int event_id = getIntent().getIntExtra("event_id", -1);
        participantService.getParticipations(token,event_id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Participation>> call, @NonNull Response<List<Participation>> response) {
                Log.d("MyApp:", "Response: " + response.raw());

                if (response.code() == 200) {
                    // Get list of book object from response
                    List<Participation> participants = response.body();

                    // initialize adapter
                    ViewParticipantsAdapter adapter = new ViewParticipantsAdapter(getApplicationContext(), participants);

                    // set adapter to the RecyclerView
                    rvParticipants.setAdapter(adapter);

                    // set layout to recycler view
                    rvParticipants.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    // add separator between item in the list
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvParticipants.getContext(),
                            DividerItemDecoration.VERTICAL);
                    rvParticipants.addItemDecoration(dividerItemDecoration);
                } else if (response.code() == 401) {
                    // invalid token, ask user to re-login
                    MotionToast.Companion.createColorToast(ViewParticipantsActivity.this,
                            "Invalid session!",
                            "Please login again",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ViewParticipantsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    clearSessionAndRedirect();
                } else {
                    MotionToast.Companion.createColorToast(ViewParticipantsActivity.this,
                            "No Participant",
                            "No one join the event yet",
                            MotionToastStyle.INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ViewParticipantsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    // server return other error
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Participation>> call, @NonNull Throwable t) {
                // Handle failure in API call
                MotionToast.Companion.createColorToast(ViewParticipantsActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ViewParticipantsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });

        ImageView backHome = findViewById(R.id.backHome);
        backHome.setOnClickListener(v -> {
            Intent intent = new Intent(ViewParticipantsActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });
    }

    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}