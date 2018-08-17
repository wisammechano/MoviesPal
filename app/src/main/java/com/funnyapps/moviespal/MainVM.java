package com.funnyapps.moviespal;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.funnyapps.moviespal.Models.Movie;
import com.funnyapps.moviespal.Models.PaginatedResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainVM extends AndroidViewModel {
    private MutableLiveData<List<Movie>> movies;
    private LiveData<List<Movie>> collection;
    private MoviesDatabase db;
    private long totalPages = 0, loadedPages = 0;
    private boolean isLoading = false;

    private MainActivity.Filter currentFilter = MainActivity.Filter.POPULAR;


    public MainVM(@NonNull Application application) {
        super(application);
        db = MoviesDatabase.getInstance(this.getApplication());
        collection = db.moviesDao().getFavMovies();
        movies = new MutableLiveData<>();
        loadPage(1L);
    }

    private void loadPage(long page) {
        isLoading = true;
        Call<PaginatedResponse<Movie>> call;
        if (currentFilter == MainActivity.Filter.POPULAR) {
            call = Api.getInstance().endpoints.getPopularMovies(page);
        } else {
            call = Api.getInstance().endpoints.getTopRatedMovies(page);
        }
        call.enqueue(new Callback<PaginatedResponse<Movie>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Movie>> call, Response<PaginatedResponse<Movie>> response) {
                PaginatedResponse<Movie> res = response.body();
                List<Movie> current = movies.getValue();
                if (res == null && current == null) {
                    //if we have no loaded pages we set to null to notify activity
                    movies.setValue(null);
                    isLoading = false;
                    return;
                } else if (res == null) {
                    //if we have loaded pages do nothing
                    isLoading = false;
                    return;
                }
                //request is successful
                loadedPages = res.getPage();
                totalPages = res.getTotalPages();
                if (current == null) {
                    //if first page we set init value
                    movies.setValue(res.getResults());
                } else {
                    //else we add new movies to list and notify observer
                    current.addAll(res.getResults());
                    movies.setValue(current);
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Movie>> call, Throwable t) {
                List<Movie> current = movies.getValue();
                if (current == null) {
                    movies.setValue(null);
                }
                isLoading = false;
            }
        });
        Log.d("MOVIES", "Loading Movies");
    }

    public void loadNextPage() {
        //Loading 15 pages should do for now
        if (loadedPages < totalPages && loadedPages < 15 && !isLoading)
            loadPage(loadedPages + 1);
    }

    public LiveData<List<Movie>> getCollection() {
        return collection;
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public void setFilter(MainActivity.Filter newFilter) {
        if (newFilter == currentFilter) return;
        this.currentFilter = newFilter;
        this.totalPages = this.loadedPages = 0;
        if (currentFilter == MainActivity.Filter.COLLECTION) {
            movies.setValue(new ArrayList<Movie>());
            movies.setValue(collection.getValue());
        } else {
            movies.setValue(new ArrayList<Movie>());
            loadPage(1);
        }
    }

    public MainActivity.Filter getCurrentFilter() {
        return currentFilter;
    }

    public MoviesDatabase getDb() {
        return db;
    }
}
