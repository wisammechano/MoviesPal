package com.funnyapps.moviespal.GsonConverters;

import android.arch.persistence.room.TypeConverter;

import com.funnyapps.moviespal.Models.Review;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ReviewTypeConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<Review> stringToReviewList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Review>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String reviewListToString(List<Review> reviews) {
        return gson.toJson(reviews);
    }
}
