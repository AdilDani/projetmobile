package com.fleettracking.app.data;

import com.fleettracking.app.model.AdminUser;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Entretien;
import com.fleettracking.app.model.Incident;
import com.fleettracking.app.model.Trajet;
import com.fleettracking.app.model.Vehicule;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Retrofit description of the Spring backend REST API. */
public interface ApiService {

    // Auth
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    // Stats
    @GET("api/stats")
    Call<Stats> getStats();

    // Vehicules
    @GET("api/vehicules")
    Call<List<Vehicule>> getVehicules();
    @GET("api/vehicules/{id}")
    Call<Vehicule> getVehicule(@Path("id") String id);
    @POST("api/vehicules")
    Call<Vehicule> createVehicule(@Body Vehicule v);
    @PUT("api/vehicules/{id}")
    Call<Vehicule> updateVehicule(@Path("id") String id, @Body Vehicule v);
    @DELETE("api/vehicules/{id}")
    Call<Void> deleteVehicule(@Path("id") String id);

    // Admins
    @GET("api/admins/{id}")
    Call<AdminUser> getAdmin(@Path("id") String id);
    @PUT("api/admins/{id}")
    Call<AdminUser> updateAdmin(@Path("id") String id, @Body AdminUser a);

    // Chauffeurs
    @GET("api/chauffeurs")
    Call<List<Chauffeur>> getChauffeurs();
    @GET("api/chauffeurs/{id}")
    Call<Chauffeur> getChauffeur(@Path("id") String id);
    @POST("api/chauffeurs")
    Call<Chauffeur> createChauffeur(@Body Chauffeur c);
    @PUT("api/chauffeurs/{id}")
    Call<Chauffeur> updateChauffeur(@Path("id") String id, @Body Chauffeur c);
    @DELETE("api/chauffeurs/{id}")
    Call<Void> deleteChauffeur(@Path("id") String id);

    // Incidents
    @GET("api/incidents")
    Call<List<Incident>> getIncidents();
    @POST("api/incidents")
    Call<Incident> createIncident(@Body Incident i);
    @PUT("api/incidents/{id}")
    Call<Incident> updateIncident(@Path("id") String id, @Body Incident i);
    @DELETE("api/incidents/{id}")
    Call<Void> deleteIncident(@Path("id") String id);

    // Entretiens
    @GET("api/entretiens")
    Call<List<Entretien>> getEntretiens();
    @POST("api/entretiens")
    Call<Entretien> createEntretien(@Body Entretien e);
    @PUT("api/entretiens/{id}")
    Call<Entretien> updateEntretien(@Path("id") String id, @Body Entretien e);
    @PUT("api/entretiens/{id}/done")
    Call<Entretien> markEntretienDone(@Path("id") String id);
    @DELETE("api/entretiens/{id}")
    Call<Void> deleteEntretien(@Path("id") String id);

    // Trajets
    @GET("api/trajets")
    Call<List<Trajet>> getTrajets();
    @POST("api/trajets")
    Call<Trajet> createTrajet(@Body Trajet t);
    @PUT("api/trajets/{id}")
    Call<Trajet> updateTrajet(@Path("id") String id, @Body Trajet t);
    @DELETE("api/trajets/{id}")
    Call<Void> deleteTrajet(@Path("id") String id);
}
