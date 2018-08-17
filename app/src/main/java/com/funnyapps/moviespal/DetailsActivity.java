package com.funnyapps.moviespal;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.funnyapps.moviespal.Models.Movie;
import com.funnyapps.moviespal.Models.Review;
import com.funnyapps.moviespal.Models.Video;
import com.funnyapps.moviespal.databinding.ActivityDetailsBinding;
import com.funnyapps.moviespal.databinding.TrailerItemBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DetailsActivity extends AppCompatActivity implements Observer<Movie> {
    public static final String EXTRA_MOVIE = "movie";
    public static final String EXTRA_FAV = "isFav";
    ActivityDetailsBinding binding;
    private ReviewsBottomSheet reviewsDialog;
    private boolean isFav;
    private Movie movie;
    private DetailsVM detailsVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        setSupportActionBar(binding.mainToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent i = getIntent();
        if (i.hasExtra(EXTRA_MOVIE)) {
            movie = (Movie) i.getSerializableExtra(EXTRA_MOVIE);
            setTitle(movie.getTitle());
            Picasso.with(DetailsActivity.this).load(Api.getImageLgUri(movie.getBackdropPath())).into(binding.movieBackdrop);
            Picasso.with(DetailsActivity.this).load(Api.getImageMdUri(movie.getPosterPath())).into(binding.moviePoster);
            binding.setMovie(movie);

            detailsVM = ViewModelProviders.of(this, new DetailsVM.DetailsVMFactory(getApplication(), movie.getId())).get(DetailsVM.class);
            detailsVM.getMovie().observe(this, this);
            detailsVM.getReviews().observe(this, getReviewsObserver());
            detailsVM.getVideos().observe(this, getVideosObserver());

            if (i.hasExtra(EXTRA_FAV)) {
                isFav = i.getBooleanExtra(EXTRA_FAV, false);
            }
        } else {
            Toast.makeText(this, R.string.no_id, Toast.LENGTH_LONG).show();
            finish();
        }

        binding.imdbB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getImdbUrl()));
                startActivity(browserIntent);
            }
        });

        binding.showReviews.setOnClickListener(getReviewsButtonClickListener());

        if (isFav) {
            binding.floatingFavB.setImageResource(R.drawable.ic_favorite);

            if (movie.getReviews() != null && movie.getReviews().size() > 0) {
                binding.showReviews.setText(R.string.reviews);
            } else {
                binding.showReviews.setEnabled(false);
                binding.showReviews.setText(R.string.no_reviews);
            }
        }

        binding.floatingFavB.setOnClickListener(getFavButtonClickListener());
    }

    private Observer<List<Video>> getVideosObserver() {
        return new Observer<List<Video>>() {
            @Override
            public void onChanged(@Nullable List<Video> videos) {
                if (videos != null)
                    createTrailersList(videos);
            }
        };
    }

    private Observer<List<Review>> getReviewsObserver() {
        return new Observer<List<Review>>() {
            @Override
            public void onChanged(@Nullable List<Review> reviews) {
                if (reviews == null) return;
                movie.setReviews(reviews);
                if (reviews.size() == 0) {
                    binding.showReviews.setEnabled(false);
                    binding.showReviews.setText(R.string.no_reviews);
                } else {
                    binding.showReviews.setEnabled(true);
                    binding.showReviews.setText(R.string.reviews);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            detailsVM.getDb().moviesDao().updateFav(movie);
                        }
                    });
                }
            }
        };
    }

    @Override
    public void onChanged(@Nullable final Movie movie) {
        if (movie == null) {
            if (!isFav)
                Toast.makeText(DetailsActivity.this, R.string.error_occurred, Toast.LENGTH_LONG).show();
            return;
        }

        //update collection with full movie details
        if (isFav && this.movie.getGenres() == null && movie.getGenres() != null) {
            //Movie is saved without details
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    detailsVM.getDb().moviesDao().updateFav(movie);
                }
            });
        }
        this.movie = movie;
        binding.setMovie(movie);
    }

    @NonNull
    private View.OnClickListener getReviewsButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (movie.getReviews() == null) return;
                if (reviewsDialog == null) {
                    reviewsDialog = new ReviewsBottomSheet();
                    reviewsDialog.setReviews(movie.getReviews());
                }
                if (reviewsDialog.isVisible()) return;
                reviewsDialog.show(getSupportFragmentManager(), getString(R.string.reviews));
            }
        };
    }

    private void createTrailersList(List<Video> videos) {
        for (Video v : videos) {
            LayoutInflater inflater = LayoutInflater.from(this);
            TrailerItemBinding binding = TrailerItemBinding.inflate(inflater, DetailsActivity.this.binding.videosWrapper, true);
            binding.trailerTitle.setText(v.getName());
            final String url = "https://" + v.getSite() + ".com/watch?v=" + v.getKey();
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reviewsDialog != null && reviewsDialog.isVisible()) {
            reviewsDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        binding.floatingFavB.setVisibility(View.GONE);
    }

    public View.OnClickListener getFavButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFav) {
                            detailsVM.getDb().moviesDao().deleteFav(movie);
                            binding.floatingFavB.setImageResource(R.drawable.ic_favorite_border);
                            isFav = false;
                        } else {
                            detailsVM.getDb().moviesDao().insertFav(movie);
                            binding.floatingFavB.setImageResource(R.drawable.ic_favorite);
                            isFav = true;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(binding.coordinator, !isFav ? R.string.removed_from_coll : R.string.added_to_coll, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.undo, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (!isFav) {
                                                            detailsVM.getDb().moviesDao().insertFav(movie);
                                                            binding.floatingFavB.setImageResource(R.drawable.ic_favorite);
                                                            isFav = true;
                                                        } else {
                                                            detailsVM.getDb().moviesDao().deleteFav(movie);
                                                            binding.floatingFavB.setImageResource(R.drawable.ic_favorite_border);
                                                            isFav = false;
                                                        }
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Snackbar.make(binding.coordinator, isFav ? R.string.added_to_coll : R.string.removed_from_coll, Snackbar.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }).show();

                            }
                        });
                    }
                });
            }
        };
    }
}
