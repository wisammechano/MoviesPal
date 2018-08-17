package com.funnyapps.moviespal;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.funnyapps.moviespal.Models.Movie;

import java.util.List;

@Dao
public interface MoviesDao {
    @Query("SELECT * FROM Movies ORDER BY releaseDate")
    LiveData<List<Movie>> getFavMovies();

    @Insert
    void insertFav(Movie m);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFav(Movie m);

    @Delete
    void deleteFav(Movie m);
}
