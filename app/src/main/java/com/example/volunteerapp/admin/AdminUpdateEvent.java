package com.example.volunteerapp.admin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.LoginActivity;
import com.example.volunteerapp.ProfileDetailActivity;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.FileInfo;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AdminUpdateEvent extends AppCompatActivity {

    private EditText txtEventName;
    private EditText txtDescription;
    private EditText txtLocation;
    private Spinner spinCategory;

    private Event event;

    ImageView eventImage;
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

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            c.setTime(date);
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

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new AdminUpdateEvent.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_update_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.update_event), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        int id = intent.getIntExtra("event_id", -1);

         // get view objects references
        txtEventName = findViewById(R.id.txtEventName);
        txtDescription = findViewById(R.id.txtDesc);
        txtLocation = findViewById(R.id.txtLocation);
        tvDate = findViewById(R.id.tvDate);
        spinCategory = findViewById(R.id.spinnerCategory);
        eventImage = findViewById(R.id.imgEvent);

        // set default createdAt value to current date
        date = new Date();
        // display in the label beside the button with specific date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        tvDate.setText(sdf.format(date));

        // retrieve event info from database using the event id
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        EventService eventService = ApiUtils.getEventService();
        eventService.getEvent(user.getToken(), id).enqueue(new Callback<Event>() {

            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.code() == 200) {
                    event = response.body();

                    txtEventName.setText(event.getEvent_name());
                    txtDescription.setText(event.getDescription());
                    txtLocation.setText(event.getLocation());
                    tvDate.setText(event.getDate());

                    // Populate the spinner with categories
                    String[] categories = {"Please Select", "Community", "Education", "Environmental", "Healthcare", "Entertainment"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminUpdateEvent.this, android.R.layout.simple_spinner_dropdown_item, categories);
                    spinCategory.setAdapter(adapter);

                    // Set the current event's category in the spinner
                    String currentCategory = event.getCategory();
                    int categoryPosition = adapter.getPosition(currentCategory);
                    if (categoryPosition >= 0) {
                        spinCategory.setSelection(categoryPosition);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    try {
                        date = sdf.parse(event.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Use Glide to load the image into the ImageView
                    Glide.with(getApplicationContext())
                            .load("https://codelah.my/2022484414/api/" + event.getImage())
                            .placeholder(R.drawable.default_cover)
                            .error(R.drawable.default_cover)
                            .into(eventImage);

                } else if (response.code() == 401) {
                    // handle unauthorized error
                    MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                            "Invalid session!",
                            "Please login again",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    clearSessionAndRedirect();
                } else {
                    MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                            "Error",
                            "Error: " + response.message(),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("MyApp:", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }


    private void clearSessionAndRedirect() {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void updateEvent(View view) {
        if (uri != null && !uri.getPath().isEmpty()) {
            // new image selected
            // Upload file first. if successful, call updateBookRecord in onResponse()
            uploadFile(uri);
        }
        else {
            // no new image selected
            // no need to upload file, update all other fields
            updateEventDetail();
        }
    }

    private void updateEventDetail() {
        // get values in form
        String eventName = txtEventName.getText().toString();
        String eventDesc = txtDescription.getText().toString();
        String eventLocation = txtLocation.getText().toString();
        String eventCategory = spinCategory.getSelectedItem().toString();
        String dateevent= tvDate.getText().toString();



        // Log old information of the activity
        Log.d("MyApp:", "Old Book info: " + event.toString());

// Update the activity model instance with new data
        event.setEvent_name(eventName);
        event.setDescription(eventDesc);
        event.setLocation(eventLocation);
        event.setCategory(eventCategory);
        event.setDate(dateevent);

// Log updated information of the activity
        Log.d("MyApp:", "New Book info: " + event.toString());


        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // send request to update the book record to the REST API
        EventService activityService = ApiUtils.getEventService();
        Call<Event> call = activityService.updateEvent(user.getToken(), event.getEvent_id(), event.getEvent_name(),
                event.getDescription(), event.getImage(), event.getLocation(), event.getDate(),
                event.getCategory(), user.getId());

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                // for debug purpose
                Log.d("MyApp:", "Update Request Response: " + response.raw().toString());

                if (response.code() == 200) {
                    // server return success code for update request
                    // get updated book object from response
                    Event updateEvent = response.body();

                    // display message
                    MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                            "Update Successful!",
                            updateEvent.getEvent_name() + " updated successfully.",
                            MotionToastStyle.INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));

                    Intent intent = new Intent(getApplicationContext(), AdminUpcomingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();


                }
                else if (response.code() == 401) {
                    // unauthorized error. invalid token, ask user to relogin
                    MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                            "Invalid session!",
                            "Please login again",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    clearSessionAndRedirect();
                }
                else {
                    // server return other error
                    MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                            "Error",
                            "Error: " + response.message(),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                        "Error connecting to server.",
                        "Check your internet connection",
                        MotionToastStyle.NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                Log.d("MyApp:", "Error: " + t.getCause().getMessage());
            }
        });
    }

    private void uploadFile(Uri fileUri) {

        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] fileBytes = getBytesFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", getFileName(uri), requestFile);

            // get token user info from shared preference in order to get token value
            SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
            User user = spm.getUser();

            EventService activityService = ApiUtils.getEventService();
            Call<FileInfo> call = activityService.uploadFile(user.getToken(), body);

            call.enqueue(new Callback<FileInfo>() {
                @Override
                public void onResponse(Call<FileInfo> call, Response<FileInfo> response) {
                    if (response.isSuccessful()) {
                        // file uploaded successfully
                        // Now add the book record with the uploaded file name
                        FileInfo fi = response.body();
                        String fileName = fi.getFile();

                        // update book object to newly uploaded image
                        event.setImage(fileName);

                        updateEventDetail();    // update book record with the new information
                    } else {
                        MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                                "Error",
                                "File upload failed",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                }

                @Override
                public void onFailure(Call<FileInfo> call, Throwable t) {
                    MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                            "Error",
                            "File upload failed",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            });
        }
        catch (Exception e) {
            MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                    "Error",
                    "Error preparing file for upload",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
        }
    }

    /**
     * Displaying an alert dialog with a single button
     * @param message - message to be displayed
     */

    /**
     * Displaying an alert dialog with a single button
     * @param message - message to be displayed
     */

    /**
     * Get image bytes from local disk. Used to upload the image bytes to Rest API
     * @param inputStream
     * @return
     * @throws IOException
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

    /**
     * Get image file name from Uri
     * @param uri
     * @return
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                MotionToast.Companion.createColorToast(AdminUpdateEvent.this,
                        "Warning!",
                        "Permission denied",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(AdminUpdateEvent.this, www.sanju.motiontoast.R.font.helveticabold));
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
}