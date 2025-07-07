package com.example.volunteerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.UserService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;


import java.util.Objects;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class SignUpActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText etName = findViewById(R.id.et_name);
        EditText etEmail = findViewById(R.id.editTextTextEmail);
        EditText etPassword = findViewById(R.id.editTextTextPassword);
        EditText etPassword2 = findViewById(R.id.editTextTextPassword2);

        ImageView signUp = findViewById(R.id.signUpButton);
        signUp.setOnClickListener(view -> signUp(etName, etEmail, etPassword, etPassword2));

        TextView SignInText = findViewById(R.id.SignInText);
        SignInText.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void signUp(EditText etName, EditText etEmail, EditText etPassword, EditText etPassword2) {

        String username = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etPassword2.getText().toString().trim();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            MotionToast.Companion.createColorToast(SignUpActivity.this,
                    "Empty field!",
                    "Please fill in all fields",
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(SignUpActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        if (!password.equals(confirmPassword)) {
            MotionToast.Companion.createColorToast(SignUpActivity.this,
                    "Passwords do not match",
                    "Please ensure passwords match",
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(SignUpActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        UserService userService = ApiUtils.getUserService();

        userService.register(email,password).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                // for debug purpose
                Log.d("MyApp:", "Response: " + response.raw());

                if (response.code() == 200) {
                    // user register successfully
                    User newUser = response.body();
                    // display message
                    assert newUser != null;
                    MotionToast.Companion.createColorToast(SignUpActivity.this,
                            "Success!",
                            user.getUsername() + " has successfully created an account",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                    // end this activity and forward user to BookListActivity
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    // server return other error
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {
                MotionToast.Companion.createColorToast(SignUpActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(SignUpActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", Objects.requireNonNull(throwable.getMessage()));

            }
        });
    }
}