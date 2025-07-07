package com.example.volunteerapp.remote;

import com.example.volunteerapp.model.AdminUpdateUser;
import com.example.volunteerapp.model.FileInfo;
import com.example.volunteerapp.model.UpdatePoints;
import com.example.volunteerapp.model.UpdateUser;
import com.example.volunteerapp.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserService {

    @FormUrlEncoded
    @POST("users/login")
    Call<User> login(@Field("username") String username, @Field("password") String
            password);

    @GET("users")
    Call<List<User>> getAllUsers(@Header("api-key") String apiKey);

    @FormUrlEncoded
    @POST("users/login")
    Call<User> loginEmail(@Field("email") String email, @Field("password") String
            password);

    @FormUrlEncoded
    @POST("users/register")
    Call<User> register(@Field("email") String email,
                        @Field("password") String password);
    @PUT("users")
    Call<User> updateUser(
            @Header("api-key") String api_key,
            @Body UpdateUser updatedUser
    );

    @PUT("users")
    Call<User> AdminupdateUser(
            @Header("api-key") String api_key,
            @Body AdminUpdateUser AdminUpdatedUser);

    @POST("users/{id}/points")
    Call<User> updateUserPoints(
            @Header("api-key") String apiKey,
            @Path("id") int id,
            @Body UpdatePoints updatePoints
    );
    @Multipart
    @POST("files")
    Call<FileInfo> uploadFile(@Header ("api-key") String apiKey, @Part MultipartBody.Part file);
}
