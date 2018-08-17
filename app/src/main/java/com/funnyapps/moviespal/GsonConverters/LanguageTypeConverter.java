package com.funnyapps.moviespal.GsonConverters;

import android.arch.persistence.room.TypeConverter;

import com.funnyapps.moviespal.Models.SpokenLanguage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class LanguageTypeConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<SpokenLanguage> stringToLanguageList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<SpokenLanguage>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String langListToString(List<SpokenLanguage> langs) {
        return gson.toJson(langs);
    }
}
