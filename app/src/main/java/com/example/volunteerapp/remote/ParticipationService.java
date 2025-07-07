package com.example.volunteerapp.remote;

import com.example.volunteerapp.model.Participation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ParticipationService {

    @GET("participations/")
    Call<List<Participation>> getParticipation(
            @Header("api-key") String api_key,
            @Query("user_Id") int id
    );

    @DELETE("participations/{participation_id}")
    Call<Void> deleteParticipation(
            @Header("api-key") String api_key,
            @Path("participation_id") int participation_Id
    );

    @FormUrlEncoded
    @POST("participations")
    Call<Participation> createParticipation(
            @Header("api-key") String api_key,
            @Field("user_Id") int userId,
            @Field("event_Id") int eventId);

    @GET("participations")
    Call<List<Participation>> getParticipations (@Header("api-key") String api_key, @Query("event_id") int event_id);
}
