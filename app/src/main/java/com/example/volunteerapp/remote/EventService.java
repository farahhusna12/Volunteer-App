package com.example.volunteerapp.remote;

import com.example.volunteerapp.model.DeleteResponse;
import com.example.volunteerapp.model.Event;
import com.example.volunteerapp.model.FileInfo;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EventService {

    @GET("events")
    Call<List<Event>> getAllEvent(@Header("api-key") String api_key);

    @GET("events/")
    Call<List<Event>>getEventsByDateAndOrganizer(
            @Header("api-key") String api_key,
            @Query("date") String date,
            @Query("organizer_id") int organizer_id);

    @GET("events/")
    Call<List<Event>>getEventsOrganizer(
            @Header("api-key") String api_key,
            @Query("organizer_id") int organizer_id);

    @GET("events/{id}")
    Call<Event> getEvent(@Header("api-key") String api_key, @Path("id") int id);

    @GET("events/participants")
    Call<List<Event>> getParticipants(
            @Header("api-key") String apiKey,
            @Query("organizer_id") int organizerId
    );
    @FormUrlEncoded
    @POST("events")
    Call<Event> addEvent(@Header ("api-key") String apiKey,
                         @Field("event_name") String event_name,
                         @Field("description") String description,
                         @Field("image") String image,
                         @Field("location") String location,
                         @Field("date") String date_event ,
                         @Field("category") String category,
                         @Field("organizer_id") int organizer_id);

    @FormUrlEncoded
    @POST("events/{id}")
    Call<Event> updateEvent(@Header ("api-key") String apiKey, @Path("id") int id,
                            @Field("event_name") String event_name,
                            @Field("description") String description,
                            @Field("image") String image,
                            @Field("location") String location,
                            @Field("date") String date_event ,
                            @Field("category") String category,
                            @Field("organizer_id") int organizer_id);

    @GET("events/")
    Call<List<Event>> getEventsCategory(
            @Header("api-key") String apiKey,
            @Query("category") String category
    );

    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Header ("api-key") String apiKey, @Path("id") int id);

    @Multipart
    @POST("files")
    Call<FileInfo> uploadFile(@Header ("api-key") String apiKey, @Part MultipartBody.Part file);
}
