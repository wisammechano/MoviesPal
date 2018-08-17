package com.funnyapps.moviespal.GsonConverters;

import android.arch.persistence.room.TypeConverter;

import com.funnyapps.moviespal.Models.Genre;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class GenreTypeConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<Genre> stringToGenreList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Genre>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String genreListToString(List<Genre> genres) {
        return gson.toJson(genres);
    }
}
