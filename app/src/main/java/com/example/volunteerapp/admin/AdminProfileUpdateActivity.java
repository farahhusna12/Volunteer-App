package com.example.volunteerapp.admin;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.ProfileDetailActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.AdminUpdateUser;
import com.example.volunteerapp.model.FileInfo;
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

public class AdminProfileUpdateActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 2;
    private Uri uri;
    private EditText tvUsername;
    private EditText tvEmail;
    private EditText tvFirstName;
    private EditText tvLastName;
    private User user;

    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_update);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileUpdate), (v, insets) -> {
            // Handle window insets if necessary
            return insets;
        });

        user = SharedPrefManager.getInstance(getApplicationContext()).getUser();

        // Initialize views
        tvUsername = findViewById(R.id.edtUsername);
        tvEmail = findViewById(R.id.edtEmail);
        tvFirstName = findViewById(R.id.edtFirstName);
        tvLastName = findViewById(R.id.edtLastName);
        profileImage = findViewById(R.id.profileImage);


        // Load profile image
        String imageUrl = "https://codelah.my/2022484414/api/" + user.getImage();
        Log.d("ImageURL", "URL: " + imageUrl);

        Glide.with(getApplicationContext())
                .load(imageUrl)
                .placeholder(R.drawable.default_prof_pic) // Placeholder image
                .error(R.drawable.default_prof_pic) // Error image
                .into(profileImage);
        ImageView imageBackHome = findViewById(R.id.backHome);
        imageBackHome.setOnClickListener(v -> backHome());

        // Set user data in text fields
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvFirstName.setText(user.getFirstName());
        tvLastName.setText(user.getLastName());

        // Set up button click listener

        Button updateButton = findViewById(R.id.BtnUpdate);
        updateButton.setOnClickListener(v -> updateButtonClicked());

    }

    private void updateButtonClicked() {

        if (uri != null && !Objects.requireNonNull(uri.getPath()).isEmpty()) {
            // New image selected; upload it first
            uploadFile(uri);
        } else {
            // No new image selected; update other fields only
            updateAdmin();
        }
    }
    private void updateAdmin() {
        String newFirstName = tvFirstName.getText().toString();
        String newLastName = tvLastName.getText().toString();
        String newUsername = tvUsername.getText().toString();
        String newEmail = tvEmail.getText().toString();

        // Include the updated image in the API request
        AdminUpdateUser AdminUpdateUser = new AdminUpdateUser(user.getId(), newFirstName, newLastName, newUsername, newEmail, user.getImage());
        UserService userService = ApiUtils.getUserService();

        userService.AdminupdateUser(user.getToken(), AdminUpdateUser).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User AdminUpdatedUser = response.body();
                    SharedPrefManager.getInstance(getApplicationContext()).saveUser(AdminUpdatedUser);

                    MotionToast.Companion.createColorToast(AdminProfileUpdateActivity.this,
                            "Update Successful!",
                            "User " + user.getUsername() + " profile successfully updated",
                            MotionToastStyle.INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminProfileUpdateActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                    // Update UI with the new data
                    tvFirstName.setText(AdminUpdatedUser.getFirstName());
                    tvLastName.setText(AdminUpdatedUser.getLastName());
                    tvEmail.setText(AdminUpdatedUser.getEmail());
                    tvUsername.setText(AdminUpdatedUser.getUsername());

                    Intent intent = new Intent(getApplicationContext(), AdminProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    MotionToast.Companion.createColorToast(AdminProfileUpdateActivity.this,
                            "Error!",
                            "Failed to update profile.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminProfileUpdateActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                MotionToast.Companion.createColorToast(AdminProfileUpdateActivity.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminProfileUpdateActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.e("UpdateError", Objects.requireNonNull(t.getMessage()));
            }
        });
    }


    private void backHome() {
        Intent intent = new Intent(getApplicationContext(), AdminProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
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
                    updateAdmin();
                }

                @Override
                public void onFailure(@NonNull Call<FileInfo> call, @NonNull Throwable throwable) {
                    MotionToast.Companion.createColorToast(AdminProfileUpdateActivity.this,
                            "Error!",
                            "File upload failed",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminProfileUpdateActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            });

        } catch (Exception e) {
            MotionToast.Companion.createColorToast(AdminProfileUpdateActivity.this,
                    "Error!",
                    "Error preparing file for upload!!",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(AdminProfileUpdateActivity.this, www.sanju.motiontoast.R.font.helveticabold));
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

    public void imageIconChangeClicked(View view) {
        MotionToast.Companion.createColorToast(AdminProfileUpdateActivity.this,
                "Clicked!",
                "Button clicked!",
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(AdminProfileUpdateActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        } else {
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
        Log.d("AdminProfileUpdate", "Opening gallery...");
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
                profileImage.setImageURI(uri);
            }
        }
    }



}
