package com.funnyapps.moviespal;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

import com.funnyapps.moviespal.GsonConverters.GenreTypeConverter;
import com.funnyapps.moviespal.GsonConverters.LanguageTypeConverter;
import com.funnyapps.moviespal.GsonConverters.ReviewTypeConverter;
import com.funnyapps.moviespal.Models.Movie;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
@TypeConverters({ReviewTypeConverter.class, GenreTypeConverter.class, LanguageTypeConverter.class})
public abstract class MoviesDatabase extends RoomDatabase {
    private static final String TAG = MoviesDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "Favorites";
    private static MoviesDatabase sInstance;

    public static MoviesDatabase getInstance(Context ctx) {
        if(sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new Database Instance");
                sInstance = Room.databaseBuilder(ctx.getApplicationContext(), MoviesDatabase.class, DB_NAME).build();
            }
        }
        Log.d(TAG, "Getting database instance");
        return sInstance;
    }

    public abstract MoviesDao moviesDao();
}
