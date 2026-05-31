package com.fleettracking.app.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Builds and holds the single Retrofit/ApiService instance. */
public final class ApiClient {

    // 10.0.2.2 is the host machine as seen from the Android emulator.
    // Change to your machine's LAN IP when running on a physical device.
    public static final String BASE_URL = "http://192.168.3.66:8080/";

    private static ApiService service;
    private ApiClient() {}

    public static ApiService get() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(ApiService.class);
        }
        return service;
    }
}
