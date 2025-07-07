package com.example.volunteerapp.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.volunteerapp.model.User;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "volunteersharedpref";
    private static final String KEY_ID = "keyid";
    private static final String KEY_USERNAME = "keyusername";
    private static final String KEY_EMAIL = "keyemail";
    private static final String KEY_FNAME = "keyfname";
    private static final String KEY_LNAME = "keylname";
    private static final String KEY_IMAGE = "keyimage";
    private static final String KEY_TOKEN = "keytoken";
    private static final String KEY_ROLE = "keyrole";
    private static final String KEY_POINTS = "keypoints";
    //private static final String KEY_ORG = "keyorg";

    private static SharedPrefManager mInstance;
    private final Context mCtx;

    public SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }
    /**
     * method to let the user login
     * this method will store the user data in shared preferences
     * @param user
     */
    public void userLogin(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_FNAME, user.getFirstName());
        editor.putString(KEY_LNAME, user.getLastName());
        editor.putString(KEY_IMAGE, user.getImage());
        editor.putString(KEY_TOKEN, user.getToken());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putInt(KEY_POINTS, user.getPoints());
        editor.apply();
    }

    /**
     * this method will checker whether user is already logged in or not.
     * return True if already logged in
     */

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    /**
     * this method will give the information of logged in user, retrieved from SharedPreferences
     */
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        User user = new User();
        user.setId(sharedPreferences.getInt(KEY_ID, -1));
        user.setUsername(sharedPreferences.getString(KEY_USERNAME, null));
        user.setEmail(sharedPreferences.getString(KEY_EMAIL, null));
        user.setFirstName(sharedPreferences.getString(KEY_FNAME, null));
        user.setLastName(sharedPreferences.getString(KEY_LNAME, null));
        user.setImage(sharedPreferences.getString(KEY_IMAGE,null));
        user.setToken(sharedPreferences.getString(KEY_TOKEN, null));
        user.setRole(sharedPreferences.getString(KEY_ROLE, null));
        user.setPoints(sharedPreferences.getInt(KEY_POINTS, 0));

        return user;
    }

    /**
     * this method will logout the user. clear the SharedPreferences
     */
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void saveUser(User updatedUser) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, updatedUser.getId());
        editor.putString(KEY_USERNAME, updatedUser.getUsername());
        editor.putString(KEY_EMAIL, updatedUser.getEmail());
        editor.putString(KEY_FNAME, updatedUser.getFirstName());
        editor.putString(KEY_LNAME, updatedUser.getLastName());
        editor.putString(KEY_IMAGE, updatedUser.getImage());
        editor.putString(KEY_TOKEN, updatedUser.getToken());
        editor.putString(KEY_ROLE, updatedUser.getRole());
        editor.putInt(KEY_POINTS, updatedUser.getPoints());
        editor.apply();
    }
}
