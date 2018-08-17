package com.funnyapps.moviespal;

import android.net.Uri;

import com.funnyapps.moviespal.Models.Genre;
import com.funnyapps.moviespal.Models.GenresResponse;
import com.funnyapps.moviespal.Models.Movie;
import com.funnyapps.moviespal.Models.PaginatedResponse;
import com.funnyapps.moviespal.Models.Review;
import com.funnyapps.moviespal.Models.Video;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by wisam on Oct 28 17.
 */

public final class Api {
    private static final String API_KEY = "YOUR_API_KEY";
    private static final String API_BASE = "https://api.themoviedb.org/3/";
    private static final String IMAGES_BASE = "https://image.tmdb.org/t/p/";

    private static final String IMAGE_LG = "w780";
    private static final String IMAGE_MD = "w342";
    private static final String IMAGE_SM = "w185";

    private static final String MOVIES = "movie/";
    private static final String GENRES = "genre/";
    private static final String REVIEWS = "/reviews";
    private static final String VIDEOS = "/videos";

    private static final String LIST = "list";
    private static final String POPULAR = "popular/";
    private static final String TOP_RATED = "top_rated/";

    private static Api instance = null;

    public synchronized static Api getInstance() {
        if (instance == null) {
            instance = new Api();
        }
        return instance;
    }

    public Endpoints endpoints;

    private Api() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                HttpUrl originalUrl = request.url();
                Request.Builder b = request.newBuilder()
                        .url(originalUrl.newBuilder(originalUrl.toString())
                                .addEncodedQueryParameter("api_key", API_KEY).build());

                return chain.proceed(b.build());
            }
        });


        Retrofit mainApi = new Retrofit.Builder()
                .baseUrl(API_BASE)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .client(client.build())
                .build();
        endpoints = mainApi.create(Endpoints.class);
    }

    interface Endpoints {
        @GET(MOVIES + POPULAR)
        Call<PaginatedResponse<Movie>> getPopularMovies(@Query("page") Long page);

        @GET(MOVIES + TOP_RATED)
        Call<PaginatedResponse<Movie>> getTopRatedMovies(@Query("page") Long page);

        @GET(MOVIES + "{id}")
        Call<Movie> getMovie(@Path("id") Long movieId);
        @GET(MOVIES + "{id}" + REVIEWS)
        Call<PaginatedResponse<Review>> getMovieReviews(@Path("id") Long movieId, @Query("page") Long page);
        @GET(MOVIES + "{id}" + VIDEOS)
        Call<PaginatedResponse<Video>> getMovieVideos(@Path("id") Long movieId);

        @GET(GENRES + LIST)
        Call<GenresResponse> getGenres();

        @GET(GENRES + "{id}")
        Call<Genre> getGenre(@Path("id") Long genreId); //We will use this later to filter movies
    }

    public static Uri getImageThumbUri(String imagePath) {
        return Uri.parse(IMAGES_BASE + IMAGE_SM + imagePath + "?" + API_KEY);
    }

    public static Uri getImageLgUri(String imagePath) {
        return  Uri.parse(IMAGES_BASE + IMAGE_LG + imagePath + "?" + API_KEY);
    }

    public  static Uri getImageMdUri(String imagePath) {
        return  Uri.parse(IMAGES_BASE + IMAGE_MD + imagePath + "?" + API_KEY);
    }
}