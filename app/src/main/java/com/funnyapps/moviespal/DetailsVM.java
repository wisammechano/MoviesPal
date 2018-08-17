package com.funnyapps.moviespal;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.util.Log;

import com.funnyapps.moviespal.Models.Movie;
import com.funnyapps.moviespal.Models.PaginatedResponse;
import com.funnyapps.moviespal.Models.Review;
import com.funnyapps.moviespal.Models.Video;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsVM extends AndroidViewModel {
    private MutableLiveData<Movie> movie;
    private MutableLiveData<List<Review>> reviews;
    private MutableLiveData<List<Video>> videos;
    //private LiveData<List<Movie>> collection;
    private MoviesDatabase db;

    public DetailsVM(@NonNull Application application, long movieId) {
        super(application);
        db = MoviesDatabase.getInstance(this.getApplication());
        //collection = db.moviesDao().getFavMovies();
        movie = new MutableLiveData<>();
        reviews = new MutableLiveData<>();
        videos = new MutableLiveData<>();
        loadMovie(movieId);
    }

    private void loadMovie(final long id) {
        Call<Movie> call = Api.getInstance().endpoints.getMovie(id);
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                movie.setValue(response.body());
                loadVideos(id);
                loadReviews(id, 1);
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                movie.setValue(null);
            }
        });
        Log.d("MOVIES", "Loading Movie id " + id);
    }

    private void loadReviews(final long id, final long page) {
        Call<PaginatedResponse<Review>> call = Api.getInstance().endpoints.getMovieReviews(id, page);
        call.enqueue(new Callback<PaginatedResponse<Review>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Review>> call, @NonNull Response<PaginatedResponse<Review>> response) {
                PaginatedResponse<Review> res = response.body();
                if (res == null || res.getTotalPages() == 0) {
                    reviews.setValue(new ArrayList<Review>());
                    return;
                }
                if (res.getTotalPages() > page) {
                    loadReviews(id, page + 1);
                }
                if (page == res.getTotalPages()) {
                    reviews.setValue(res.getResults());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Review>> call, Throwable t) {

            }
        });
    }

    private void loadVideos(final long movieId) {
        Call<PaginatedResponse<Video>> call = Api.getInstance().endpoints.getMovieVideos(movieId);
        call.enqueue(new Callback<PaginatedResponse<Video>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Video>> call, @NonNull Response<PaginatedResponse<Video>> response) {
                List<Video> videos = response.body().getResults();
                if (videos != null) {
                    DetailsVM.this.videos.setValue(videos);
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Video>> call, Throwable t) {

            }
        });
    }


    /*public LiveData<List<Movie>> getCollection() {
        return collection;
    }*/

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public MutableLiveData<List<Review>> getReviews() {
        return reviews;
    }

    public MutableLiveData<List<Video>> getVideos() {
        return videos;
    }

    public MoviesDatabase getDb() {
        return db;
    }

    static class DetailsVMFactory extends ViewModelProvider.NewInstanceFactory {
        private Application application;
        private long movieId;


        public DetailsVMFactory(Application application, long movieId) {
            this.application = application;
            this.movieId = movieId;
        }


        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DetailsVM(application, movieId);
        }
    }
}
