package com.example.volunteerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.volunteerapp.admin.AdminDashboardActivity;
import com.example.volunteerapp.model.FailLogin;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.UserService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;
import com.example.volunteerapp.user.DashboardActivity;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    Call<User> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);

        ImageView imageViewLogin = findViewById(R.id.loginButton);
        imageViewLogin.setOnClickListener(v -> loginClick());

        TextView SignUpText = findViewById(R.id.SignUpText);
        SignUpText.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginClick() {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        if (validateLogin(username, password)) {
            // if not empty, login using REST API
            doLogin(username, password);
        }
    }

    private void doLogin(String username, String password) {

        UserService userService = ApiUtils.getUserService();

        if(username.contains("@")){
            call = userService.loginEmail(username, password);
        }else{
            call = userService.login(username, password);
        }

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null && user.getToken() != null) {
                        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
                        spm.userLogin(user);
                        if (user.getRole().equals("admin")) {
                            MotionToast.Companion.createColorToast(LoginActivity.this,
                                    "Login Successful!",
                                    "Welcome admin " + user.getUsername(),
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                            finish();
                            startActivity(new Intent(getApplicationContext(), AdminDashboardActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            MotionToast.Companion.createColorToast(LoginActivity.this,
                                    "Login Successful!",
                                    "Welcome user " + user.getUsername(),
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                            finish();
                            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    } else {
                        MotionToast.Companion.createColorToast(LoginActivity.this,
                                "Login Unsuccessful!",
                                "Wrong username or password",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                } else {
                    String errorResp;
                    try {
                        assert response.errorBody() != null;
                        errorResp = response.errorBody().string();
                        FailLogin e = new Gson().fromJson(errorResp, FailLogin.class);
                        displayToast(e.getError().getMessage());
                    } catch (Exception e) {
                        Log.e("MyApp:", e.toString()); // print error details to error log
                        MotionToast.Companion.createColorToast(LoginActivity.this,
                                "Login Error!",
                                "Wrong username or password",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(LoginActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", t.toString()); // print error details to error log
            }
        });
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            MotionToast.Companion.createColorToast(LoginActivity.this,
                    "Username is required",
                    "Please enter correct username",
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            MotionToast.Companion.createColorToast(LoginActivity.this,
                    "Password is required",
                    "Please enter correct password",
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(LoginActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return false;
        }
        return true;
    }
}