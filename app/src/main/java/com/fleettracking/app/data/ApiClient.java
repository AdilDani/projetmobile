package com.fleettracking.app.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Builds and holds the single Retrofit/ApiService instance. */
public final class ApiClient {

    // 192.168.1.3 = PC's LAN IP for physical device testing.
    // Change back to http://10.0.2.2:8080/ when using the emulator.
    public static final String BASE_URL = "http://192.168.1.3:8080/";

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
