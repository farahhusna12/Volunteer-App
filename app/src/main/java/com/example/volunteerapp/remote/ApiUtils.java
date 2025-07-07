package com.example.volunteerapp.remote;

public class ApiUtils {

    public static final String BASE_URL = "https://codelah.my/2022484414/api/";

    // return UserService instance
    public static UserService getUserService() {
        return RetrofitClient.getClient(BASE_URL).create(UserService.class);
    }

    public static EventService getEventService() {
        return RetrofitClient.getClient(BASE_URL).create(EventService.class);
    }

    public static ParticipationService getParticipationService() {
        return RetrofitClient.getClient(BASE_URL).create(ParticipationService.class);
    }
}
