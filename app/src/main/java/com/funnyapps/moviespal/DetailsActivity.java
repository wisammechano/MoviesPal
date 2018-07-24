package com.funnyapps.moviespal;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.funnyapps.moviespal.databinding.ActivityDetailsBinding;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {
    public static final String EXTRA_KEY = "movie_id";
    ActivityDetailsBinding binding;
    private MovieDetails movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        setSupportActionBar(binding.mainToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        if (i.hasExtra(EXTRA_KEY)) {
            loadMovie(i.getLongExtra(EXTRA_KEY, -1));
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
    }

    private void loadMovie(final long movie_id) {
        Call<MovieDetails> call = Api.getInstance().endpoints.getMovie(movie_id);
        call.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetails> call, @NonNull Response<MovieDetails> response) {
                DetailsActivity.this.movie = response.body();
                binding.setMovie(movie);
                setTitle(movie.getTitle());

                binding.originalTitleTv.setText(movie.getOriginalTitle());

                Picasso.with(DetailsActivity.this).load(Api.getImageLgUri(movie.getBackdropPath())).into(binding.movieBackdrop);
                Picasso.with(DetailsActivity.this).load(Api.getImageMdUri(movie.getPosterPath())).into(binding.moviePoster);
                binding.progressBar.setVisibility(View.GONE);
                binding.root.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetails> call, @NonNull Throwable t) {
                Toast.makeText(DetailsActivity.this, R.string.error_occurred, Toast.LENGTH_LONG).show();
                DetailsActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
