package com.fleettracking.app.data;

import android.content.Context;
import android.os.AsyncTask;

import com.fleettracking.app.model.AdminUser;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Entretien;
import com.fleettracking.app.model.Incident;
import com.fleettracking.app.model.Trajet;
import com.fleettracking.app.model.Vehicule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Single data access point for the whole app.
 *
 * Each read goes to the Spring backend through Retrofit. On success the JSON
 * body is written to the SQLite offline cache (via AsyncTask) and returned.
 * When the network call fails, the last cached response is loaded from SQLite
 * (also via AsyncTask) so the UI keeps working offline.
 */
public class Repository {

    private final ApiService api;
    private final CacheDbHelper db;
    private final Gson gson = new Gson();

    // Cache keys
    private static final String K_VEHICULES = "vehicules";
    private static final String K_CHAUFFEURS = "chauffeurs";
    private static final String K_INCIDENTS = "incidents";
    private static final String K_ENTRETIENS = "entretiens";
    private static final String K_TRAJETS = "trajets";
    private static final String K_STATS = "stats";

    public Repository(Context ctx) {
        this.api = ApiClient.get();
        this.db = new CacheDbHelper(ctx);
    }

    // ---------------------------------------------------------------- AsyncTasks

    /** Writes a JSON blob to the SQLite cache off the UI thread. */
    private class SaveCacheTask extends AsyncTask<Void, Void, Void> {
        private final String key;
        private final String json;
        SaveCacheTask(String key, String json) { this.key = key; this.json = json; }
        @Override protected Void doInBackground(Void... v) { db.save(key, json); return null; }
    }

    /** Removes one or more cached keys off the UI thread (cache invalidation on writes). */
    private class DeleteCacheTask extends AsyncTask<Void, Void, Void> {
        private final String[] keys;
        DeleteCacheTask(String... keys) { this.keys = keys; }
        @Override protected Void doInBackground(Void... v) {
            for (String k : keys) db.delete(k);
            return null;
        }
    }

    /** Reads a JSON blob from the SQLite cache off the UI thread, then parses it. */
    private class LoadCacheTask<R> extends AsyncTask<Void, Void, String> {
        private final String key;
        private final Type type;
        private final RepoCallback<R> cb;
        LoadCacheTask(String key, Type type, RepoCallback<R> cb) {
            this.key = key; this.type = type; this.cb = cb;
        }
        @Override protected String doInBackground(Void... v) { return db.load(key); }
        @Override protected void onPostExecute(String json) {
            if (json == null) { cb.onError("Aucune donnée hors-ligne disponible"); return; }
            try { cb.onResult(gson.<R>fromJson(json, type)); }
            catch (Exception e) { cb.onError("Cache illisible"); }
        }
    }

    // ---------------------------------------------------------------- generic flows

    private <T> void fetchList(Call<List<T>> call, final String key,
                               final Type listType, final RepoCallback<List<T>> cb) {
        call.enqueue(new Callback<List<T>>() {
            @Override public void onResponse(Call<List<T>> c, Response<List<T>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<T> data = r.body();
                    new SaveCacheTask(key, gson.toJson(data)).execute();
                    cb.onResult(data);
                } else {
                    new LoadCacheTask<List<T>>(key, listType, cb).execute();
                }
            }
            @Override public void onFailure(Call<List<T>> c, Throwable t) {
                new LoadCacheTask<List<T>>(key, listType, cb).execute();
            }
        });
    }

    private <T> void fetchObject(Call<T> call, final String key,
                                 final Type type, final RepoCallback<T> cb) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) {
                if (r.isSuccessful() && r.body() != null) {
                    T data = r.body();
                    new SaveCacheTask(key, gson.toJson(data)).execute();
                    cb.onResult(data);
                } else {
                    new LoadCacheTask<T>(key, type, cb).execute();
                }
            }
            @Override public void onFailure(Call<T> c, Throwable t) {
                new LoadCacheTask<T>(key, type, cb).execute();
            }
        });
    }

    // ---------------------------------------------------------------- reads

    public void getAdmin(String id, RepoCallback<AdminUser> cb) {
        enqueueObject(api.getAdmin(id), cb);
    }

    public void updateAdmin(String id, AdminUser a, RepoCallback<AdminUser> cb) {
        enqueueObject(api.updateAdmin(id, a), cb);
    }

    public void getVehicules(RepoCallback<List<Vehicule>> cb) {
        fetchList(api.getVehicules(), K_VEHICULES,
                new TypeToken<List<Vehicule>>(){}.getType(), cb);
    }

    public void getChauffeurs(RepoCallback<List<Chauffeur>> cb) {
        fetchList(api.getChauffeurs(), K_CHAUFFEURS,
                new TypeToken<List<Chauffeur>>(){}.getType(), cb);
    }

    public void getIncidents(RepoCallback<List<Incident>> cb) {
        fetchList(api.getIncidents(), K_INCIDENTS,
                new TypeToken<List<Incident>>(){}.getType(), cb);
    }

    public void getIncident(final String id, final RepoCallback<Incident> cb) {
        getIncidents(new RepoCallback<List<Incident>>() {
            @Override public void onResult(List<Incident> list) {
                for (Incident i : list) if (i.id.equals(id)) { cb.onResult(i); return; }
                cb.onError("Incident introuvable");
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }

    public void getEntretiens(RepoCallback<List<Entretien>> cb) {
        fetchList(api.getEntretiens(), K_ENTRETIENS,
                new TypeToken<List<Entretien>>(){}.getType(), cb);
    }

    public void getTrajets(RepoCallback<List<Trajet>> cb) {
        fetchList(api.getTrajets(), K_TRAJETS,
                new TypeToken<List<Trajet>>(){}.getType(), cb);
    }

    public void getStats(RepoCallback<Stats> cb) {
        fetchObject(api.getStats(), K_STATS, Stats.class, cb);
    }

    /** Look up one vehicule by id from the (cached) list, so it works offline too. */
    public void getVehicule(final String id, final RepoCallback<Vehicule> cb) {
        getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                for (Vehicule v : list) if (v.id.equals(id)) { cb.onResult(v); return; }
                cb.onError("Véhicule introuvable");
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }

    /**
     * The vehicule currently assigned to a chauffeur (matched on conducteurId).
     * Only "En mission" cars carry a conducteurId, so a chauffeur who is not
     * driving returns an error instead of an arbitrary vehicle.
     */
    public void getCurrentVehicule(final String chauffeurId, final RepoCallback<Vehicule> cb) {
        getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                if (chauffeurId != null && !chauffeurId.isEmpty()) {
                    for (Vehicule v : list) if (chauffeurId.equals(v.conducteurId)) { cb.onResult(v); return; }
                }
                cb.onError("Aucun véhicule affecté");
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }

    /** Look up one chauffeur by id from the (cached) list. */
    public void getChauffeur(final String id, final RepoCallback<Chauffeur> cb) {
        getChauffeurs(new RepoCallback<List<Chauffeur>>() {
            @Override public void onResult(List<Chauffeur> list) {
                for (Chauffeur c : list) if (c.id.equals(id)) { cb.onResult(c); return; }
                cb.onError("Chauffeur introuvable");
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }

    // ---------------------------------------------------------------- auth

    public void login(String login, String password, final RepoCallback<LoginResponse> cb) {
        api.login(new LoginRequest(login, password)).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> c, Response<LoginResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().success) {
                    cb.onResult(r.body());
                } else {
                    cb.onError("Identifiants incorrects");
                }
            }
            @Override public void onFailure(Call<LoginResponse> c, Throwable t) {
                cb.onError("Serveur injoignable");
            }
        });
    }

    // ---------------------------------------------------------------- writes
    //
    // Every successful write drops the affected cache key(s) so the next read
    // is forced to the backend. This keeps the SQLite cache in sync with the
    // database instead of serving stale rows (the cause of the chauffeur-list
    // not updating after an edit).

    public void createIncident(Incident i, RepoCallback<Incident> cb) {
        enqueueObject(api.createIncident(i), cb, K_INCIDENTS, K_STATS);
    }

    public void updateIncident(String id, Incident i, RepoCallback<Incident> cb) {
        enqueueObject(api.updateIncident(id, i), cb, K_INCIDENTS, K_STATS);
    }

    public void deleteIncident(String id, RepoCallback<Void> cb) {
        enqueueVoid(api.deleteIncident(id), cb, K_INCIDENTS, K_STATS);
    }

    public void createVehicule(Vehicule v, RepoCallback<Vehicule> cb) {
        enqueueObject(api.createVehicule(v), cb, K_VEHICULES, K_STATS);
    }

    public void updateVehicule(String id, Vehicule v, RepoCallback<Vehicule> cb) {
        enqueueObject(api.updateVehicule(id, v), cb, K_VEHICULES, K_STATS);
    }

    public void deleteVehicule(String id, RepoCallback<Void> cb) {
        enqueueVoid(api.deleteVehicule(id), cb, K_VEHICULES, K_STATS);
    }

    public void createChauffeur(Chauffeur c, RepoCallback<Chauffeur> cb) {
        enqueueObject(api.createChauffeur(c), cb, K_CHAUFFEURS, K_STATS);
    }

    public void updateChauffeur(String id, Chauffeur c, RepoCallback<Chauffeur> cb) {
        enqueueObject(api.updateChauffeur(id, c), cb, K_CHAUFFEURS, K_STATS);
    }

    public void deleteChauffeur(String id, RepoCallback<Void> cb) {
        enqueueVoid(api.deleteChauffeur(id), cb, K_CHAUFFEURS, K_STATS);
    }

    public void createEntretien(Entretien e, RepoCallback<Entretien> cb) {
        enqueueObject(api.createEntretien(e), cb, K_ENTRETIENS, K_STATS);
    }

    public void markEntretienDone(String id, RepoCallback<Entretien> cb) {
        enqueueObject(api.markEntretienDone(id), cb, K_ENTRETIENS);
    }

    public void createTrajet(Trajet t, RepoCallback<Trajet> cb) {
        enqueueObject(api.createTrajet(t), cb, K_TRAJETS);
    }

    public void updateTrajet(String id, Trajet t, RepoCallback<Trajet> cb) {
        enqueueObject(api.updateTrajet(id, t), cb, K_TRAJETS);
    }

    private <T> void enqueueObject(Call<T> call, final RepoCallback<T> cb, final String... invalidate) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) {
                if (r.isSuccessful()) {
                    if (invalidate.length > 0) new DeleteCacheTask(invalidate).execute();
                    cb.onResult(r.body());
                } else cb.onError("Échec de l'enregistrement (" + r.code() + ")");
            }
            @Override public void onFailure(Call<T> c, Throwable t) { cb.onError("Serveur injoignable"); }
        });
    }

    private void enqueueVoid(Call<Void> call, final RepoCallback<Void> cb, final String... invalidate) {
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    if (invalidate.length > 0) new DeleteCacheTask(invalidate).execute();
                    cb.onResult(null);
                } else cb.onError("Échec de la suppression (" + r.code() + ")");
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError("Serveur injoignable"); }
        });
    }
}
