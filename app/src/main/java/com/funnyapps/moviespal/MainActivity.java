package com.funnyapps.moviespal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String FILTER = "filter";
    private static final String SCROLL_POS = "scroll_pos";
    private static final String MOVIES = "movies";
    private RecyclerView rv;
    private FrameLayout pbWrapper;
    private PaginatedResponse<Movie> movies;
    private MovieThumbsAdapter adapter;
    private boolean isLoading = false;
    private Filter currentFilter = Filter.POPULAR;

    private AlertDialog filterDialog;

    enum Filter {
        POPULAR(R.string.popular),
        TOP_RATED(R.string.top_rated);

        private int stringId;

        Filter(int stringId) {
            this.stringId = stringId;
        }

        public int getFilterId() {
            return stringId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        rv = findViewById(R.id.rv_movies);
        pbWrapper = findViewById(R.id.pb_container);

        int span = 3;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            span = 6;
        }
        rv.setLayoutManager(new GridLayoutManager(this, span));
        isConnected();
        adapter = new MovieThumbsAdapter(this);
        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                i.putExtra(DetailsActivity.EXTRA_KEY, (Long) v.getTag());
                startActivity(i);
            }
        });
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPos = ((GridLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition();
                int totalItems = adapter.getItemCount();
                if (movies != null && lastVisibleItemPos == totalItems - 1) {
                    loadMovies();
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIES)) {
            Gson gson = new Gson();
            Movie[] movies = gson.fromJson(savedInstanceState.getString(MOVIES), Movie[].class);
            adapter.setItems(Arrays.asList(movies));
            rv.setScrollY(savedInstanceState.getInt(SCROLL_POS));
            if (savedInstanceState.containsKey(FILTER)) {
                for (Filter f : Filter.values()) {
                    if (getString(f.getFilterId()).equals(savedInstanceState.getString(FILTER))) {
                        currentFilter = f;
                    }
                }
            }
            pbWrapper.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        } else {
            loadMovies();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FILTER, getString(currentFilter.getFilterId()));
        outState.putInt(SCROLL_POS, rv.getScrollY());
        Gson gson = new Gson();
        outState.putString(MOVIES, gson.toJson(adapter.getItems()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                showFilterDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showFilterDialog() {
        if (filterDialog != null) {
            filterDialog.show();
            return;
        }
        final CharSequence[] choices = new String[Filter.values().length];
        int i = 0;
        for (Filter f : Filter.values()) {
            choices[i] = getString(f.getFilterId());
            i++;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.filter_movies);

        b.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (Filter f : Filter.values()) {
                    if (getString(f.getFilterId()).contentEquals(choices[which])) {
                        changeFilter(f);
                        dialog.dismiss();
                    }
                }
            }
        });
        filterDialog = b.create();
        filterDialog.show();
    }

    private void init() {

    }

    private void changeFilter(Filter newFilter) {
        if (currentFilter == newFilter) return;
        currentFilter = newFilter;
        movies = null;
        adapter.clear();
        loadMovies();
    }

    private void loadMovies() {
        if (isLoading) return;
        Long page = 1L;
        if (this.movies != null) {
            page = this.movies.getPage() + 1;
            if (page > 10)
                return; // for now we will return because our adapter will become huge in size
        }
        Call<PaginatedResponse<Movie>> call;
        if (currentFilter == Filter.POPULAR) {
            call = Api.getInstance().endpoints.getPopularMovies(page);
        } else {
            call = Api.getInstance().endpoints.getTopRatedMovies(page);
        }
        isLoading = true;
        call.enqueue(new Callback<PaginatedResponse<Movie>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Movie>> call, @NonNull Response<PaginatedResponse<Movie>> response) {
                movies = response.body();
                if (movies != null) {
                    adapter.setItems(movies.getResults());
                }
                isLoading = false;
                if (pbWrapper.getVisibility() == View.VISIBLE) {
                    pbWrapper.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Movie>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_loading, Toast.LENGTH_LONG).show();
                isLoading = false;
            }
        });
    }

    private void isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                init();
                return;
            }
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.no_internet);
        dialog.setMessage(R.string.no_internet_desc);
        dialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isConnected();
            }
        });
        dialog.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.create().show();
    }

    @Override
    protected void onDestroy() {
        if (filterDialog != null && filterDialog.isShowing())
            filterDialog.dismiss();
        super.onDestroy();
    }
}
