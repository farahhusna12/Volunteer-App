package com.example.volunteerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.model.FileInfo;
import com.example.volunteerapp.model.UpdateUser;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.UserService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ProfileDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 2;
    private Uri uri;
    EditText tvFirstName;
    EditText tvLastName;
    ImageView activityImage;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        TextView tvUsername = findViewById(R.id.username);
        TextView tvPassword = findViewById(R.id.password);
        tvFirstName = findViewById(R.id.firstName);
        tvLastName = findViewById(R.id.lastName);

        Log.d("Firstname : "+user.getFirstName(), "Lastname : "+user.getLastName());

        activityImage = findViewById(R.id.activityImage);
        Log.d("ImageURL", "URL: https://codelah.my/2022484414/api/" + user.getImage());
        // Use Glide to load the image into the ImageView
        Glide.with(getApplicationContext())
                .load("https://codelah.my/2022484414/api/" + user.getImage())
                .placeholder(R.drawable.default_prof_pic) // Placeholder image if the URL is empty
                .error(R.drawable.default_prof_pic) // Error image if there is a problem loading the image
                .into(activityImage);

        ImageView imageBackHome = findViewById(R.id.backHome);
        imageBackHome.setOnClickListener(v -> backHome());

        tvUsername.setText(user.getUsername());
        tvPassword.setText(user.getEmail());
        tvFirstName.setText(user.getFirstName());
        tvLastName.setText(user.getLastName());
        
        Button updateButton = findViewById(R.id.profle_update_btn);
        updateButton.setOnClickListener(v -> updateButtonClicked());

    }

    private void updateButtonClicked() {

        if (uri != null && !Objects.requireNonNull(uri.getPath()).isEmpty()) {
            // New image selected; upload it first
            uploadFile(uri);
        } else {
            // No new image selected; update other fields only
            updateUser();
        }
    }

    private void uploadFile(Uri fileUri) {
        try{
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            assert inputStream != null;
            byte[] fileBytes = getBytesFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(Objects.requireNonNull(getContentResolver().getType(fileUri))),
                    fileBytes
            );
            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    getFileName(uri),
                    requestFile
            );

            SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
            user = spm.getUser();

            UserService userService = ApiUtils.getUserService();
            Call<FileInfo> call = userService.uploadFile(user.getToken(),body);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<FileInfo> call, @NonNull Response<FileInfo> response) {
                    FileInfo fi = response.body();
                    assert fi != null;
                    String fileName = fi.getFile();
                    user.setImage(fileName);
                    updateUser();
                }

                @Override
                public void onFailure(@NonNull Call<FileInfo> call, @NonNull Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "File upload failed", Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error preparing file for upload", Toast.LENGTH_LONG).show();
        }

    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void updateUser() {
        String newFirstName = tvFirstName.getText().toString();
        String newLastName = tvLastName.getText().toString();

        Log.d("UserUpdate", "Image URI: " + user.getImage());
        UpdateUser updateUser = new UpdateUser(user.getId(),newFirstName, newLastName,user.getImage());

        UserService userService = ApiUtils.getUserService();

        userService.updateUser(user.getToken(),updateUser).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                if (response.isSuccessful()) {
                    // Handle success and update the UI
                    User updatedUser = response.body();
                    if (updatedUser != null) {
                        // Update shared preferences with the new user data
                        SharedPrefManager.getInstance(getApplicationContext()).saveUser(updatedUser);

                        // Inform the user of success
                        MotionToast.Companion.createColorToast(ProfileDetailActivity.this,
                                "Update Successful!",
                                "User " + user.getUsername() + " profile successfully updated",
                                MotionToastStyle.INFO,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(ProfileDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                        // Optionally update the UI here, for example, refresh the TextViews
                        tvFirstName.setText(updatedUser.getFirstName());
                        tvLastName.setText(updatedUser.getLastName());
                    }
                } else {
                    // Handle API errors
                    MotionToast.Companion.createColorToast(ProfileDetailActivity.this,
                            "Error!",
                            "Failed to update profile.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ProfileDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(ProfileDetailActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ProfileDetailActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("MyApp:", Objects.requireNonNull(t.getMessage()));
            }
        });    }

    private void backHome() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public void imageIconChangeClicked(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+, require READ_MEDIA_IMAGES permission
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        } else {
            // For Android 12 and below, require READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // image picker activity returned a value
            if (data != null) {
                // get file URI
                uri = data.getData();
                // set the selected image to the ImageView
                activityImage.setImageURI(uri);
            }
        }
    }
}