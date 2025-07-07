package com.example.volunteerapp.admin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.example.volunteerapp.LoginActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.FileInfo;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AdminAddEvent extends AppCompatActivity {
    private EditText txtEventName;
    private EditText txtDescription;
    private EditText txtLocation;
    private EditText txtCategory;
    private Spinner spinCategory;
    ImageView eventImage;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvDate; // static because need to be accessed by DatePickerFragment
    private static Date date; // static because need to be accessed by DatePickerFragment

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 2;
    private Uri uri;
    /**
     * Date picker fragment class
     * Reference: https://developer.android.com/guide/topics/ui/controls/pickers
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

            // create a date object from selected year, month and day
            date = new GregorianCalendar(year, month, day).getTime();

            // display in the label beside the button with specific date format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            tvDate.setText( sdf.format(date) );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_event);

        // get view objects references
        txtEventName = findViewById(R.id.txtEventName);
        txtDescription = findViewById(R.id.txtDesc);
        txtLocation = findViewById(R.id.txtLocation);
        tvDate = findViewById(R.id.tvDate);
        //txtCategory = findViewById(R.id.txtCategory);
        eventImage = findViewById(R.id.imgEvent);
        spinCategory = findViewById(R.id.spinnerCategory);


        String[] categories = {"Please Select","Community", "Education", "Environmental", "Healthcare","Entertainment"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinCategory.setAdapter(adapter);

        // set default createdAt value to current date
        date = new Date();
        // display in the label beside the button with specific date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        tvDate.setText( sdf.format(date) );
    }

    /**
     * Called when pick date button is clicked. Display a date picker dialog
     */
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void addEvent(View view) {
        if (uri != null && !Objects.requireNonNull(uri.getPath()).isEmpty()) {
            // Upload file first
            uploadFile(uri);
        } else {
            // No file to upload, proceed with adding book record with default image
            addNewEvent("3-default.png");
        }

    }

    /**
     * Upload selected image to server through REST API
     * @param fileUri   full path of the file
     */
    private void uploadFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            assert inputStream != null;
            byte[] fileBytes = getBytesFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse(Objects.requireNonNull(getContentResolver().getType(fileUri))), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", getFileName(uri), requestFile);

            // get token user info from shared preference in order to get token value
            SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
            User user = spm.getUser();

            EventService activityService = ApiUtils.getEventService();
            Call<FileInfo> call = activityService.uploadFile(user.getToken(), body);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<FileInfo> call, @NonNull Response<FileInfo> response) {
                    if (response.isSuccessful()) {
                        // file uploaded successfully
                        // Now add the book record with the uploaded file name
                        FileInfo fi = response.body();
                        assert fi != null;
                        String fileName = fi.getFile();
                        addNewEvent(fileName);
                    } else {
                        MotionToast.Companion.createColorToast(AdminAddEvent.this,
                                "Error!",
                                "File upload failed",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FileInfo> call, @NonNull Throwable t) {
                    MotionToast.Companion.createColorToast(AdminAddEvent.this,
                            "Error!",
                            "Error uploading file!!",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            MotionToast.Companion.createColorToast(AdminAddEvent.this,
                    "Error!",
                    "Error preparing file for upload!!",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));
        }
    }

    /**
     * Add a new book record
     * @param fileName  image file name
     */
    private void addNewEvent(String fileName) {

        // get values in form
        String eventName = txtEventName.getText().toString();
        String eventDesc = txtDescription.getText().toString();
        String eventLocation = txtLocation.getText().toString();
        String eventCategory = spinCategory.getSelectedItem().toString();
        //String eventCategory = txtCategory.getText().toString();

        // convert createdAt date to format in DB
        // reference: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String eventDate = sdf.format(date);

        // set updated_at with the same value as created_at

        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        int categoryId = user.getId();

        // send request to add new book to the REST API
        EventService bookService = ApiUtils.getEventService();
        Call<Event> call = bookService.addEvent(user.getToken(), eventName, eventDesc, fileName, eventLocation, eventDate,
                eventCategory, categoryId);

        // execute
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {

                // for debug purpose
                Log.d("MyApp:", "Response: " + response.raw());

                if (response.code() == 201) {
                    // book added successfully
                    Event addedBook = response.body();
                    // display message
                    assert addedBook != null;
                    MotionToast.Companion.createColorToast(AdminAddEvent.this,
                            "Successful!",
                            "Event " + addedBook.getEvent_name() + "successfully added!",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));

                    // end this activity and forward user to BookListActivity
                    Intent intent = new Intent(getApplicationContext(), AdminUpcomingActivity.class);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 401) {
                    // invalid token, ask user to re-login
                    MotionToast.Companion.createColorToast(AdminAddEvent.this,
                            "Invalid session!",
                            "Please login again",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    clearSessionAndRedirect();
                } else {
                    MotionToast.Companion.createColorToast(AdminAddEvent.this,
                            "Error connecting to server.",
                            "Check your internet connection",
                            MotionToastStyle.NO_INTERNET,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Error [" + t.getMessage() + "]",
                        Toast.LENGTH_LONG).show();
                // for debug purpose
                Log.d("MyApp:", "Error: " + Objects.requireNonNull(t.getCause()).getMessage());
            }
        });
    }


    public void clearSessionAndRedirect() {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    public void uploadEventImg(View view) {
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

    /**
     * User clicked deny or allow in permission request dialog
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                MotionToast.Companion.createColorToast(AdminAddEvent.this,
                        "Permission denied",
                        "You have no access to gallery",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminAddEvent.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        }
    }

    /**
     * Open Image Picker Activity
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    /**
     * Callback for Activity, in this case will handle the result of Image Picker Activity
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // image picker activity return a value
            if(data != null) {
                // get file uri
                uri = data.getData();
                // set image to imageview
                eventImage.setImageURI(uri);
            }
        }
    }

    /**
     * Get image file name from Uri
     */
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

    /**
     * Get image bytes from local disk. Used to upload the image bytes to Rest API
     */
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
}