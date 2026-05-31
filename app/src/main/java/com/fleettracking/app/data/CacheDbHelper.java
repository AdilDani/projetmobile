package com.fleettracking.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database used as an offline cache. Each row stores the JSON body
 * of one API response keyed by endpoint name, so the app can show the last
 * known data when the backend is unreachable.
 */
public class CacheDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fleet_cache.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "api_cache";
    public static final String COL_KEY = "cache_key";
    public static final String COL_JSON = "json";
    public static final String COL_UPDATED = "updated_at";

    public CacheDbHelper(Context ctx) {
        super(ctx.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_KEY + " TEXT PRIMARY KEY, "
                + COL_JSON + " TEXT, "
                + COL_UPDATED + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /** Insert or replace the cached JSON for a key. Runs synchronously. */
    public void save(String key, String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_JSON, json);
        cv.put(COL_UPDATED, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /** Read the cached JSON for a key, or null if absent. Runs synchronously. */
    public String load(String key) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, new String[]{COL_JSON},
                COL_KEY + "=?", new String[]{key}, null, null, null);
        String json = null;
        if (c.moveToFirst()) json = c.getString(0);
        c.close();
        return json;
    }
}
