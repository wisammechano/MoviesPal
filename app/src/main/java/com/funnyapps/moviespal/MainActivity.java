package com.funnyapps.moviespal;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.funnyapps.moviespal.Models.Movie;
import com.funnyapps.moviespal.Models.PaginatedResponse;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String SCROLL_POS = "scroll_pos";
    private RecyclerView rv;
    private FrameLayout pbWrapper;
    private List<Movie> collection;
    private MovieThumbsAdapter adapter;
    private MainVM mainVM;

    private AlertDialog filterDialog;

    enum Filter {
        POPULAR(R.string.popular),
        TOP_RATED(R.string.top_rated),
        COLLECTION(R.string.collection);

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

        mainVM = ViewModelProviders.of(this).get(MainVM.class);

        initThumbsView();

        mainVM.getCollection().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                collection = movies;
                if(mainVM.getCurrentFilter() == Filter.COLLECTION) {
                    adapter.setItems(movies);
                }
            }
        });

        if (!isConnected()) {
            showNotConnectedDialog();
        }
        mainVM.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                if (movies != null) {
                    adapter.setItems(movies);
                    toggleProgress();
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_loading, Toast.LENGTH_LONG).show();
                }
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POS)) {
            rv.scrollToPosition(savedInstanceState.getInt(SCROLL_POS));
        }
    }

    private void initThumbsView() {
        rv = findViewById(R.id.rv_movies);
        pbWrapper = findViewById(R.id.pb_wrapper);
        int span;
        //dynamic span
        DisplayMetrics dm = getResources().getDisplayMetrics();
        span = (int) (dm.widthPixels / MovieThumbsAdapter.THUMB_WIDTH);
        rv.setLayoutManager(new GridLayoutManager(this, span));

        adapter = new MovieThumbsAdapter(this);
        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Movie m = adapter.getItems().get((Integer) v.getTag());
                if (collection.contains(m)) {
                    Toast.makeText(MainActivity.this, R.string.in_collection, Toast.LENGTH_SHORT).show();
                    return true;
                }
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mainVM.getDb().moviesDao().insertFav(m);
                        collection.add(m);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.added_to_coll, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                return true;
            }
        });
        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Movie m = adapter.getItems().get((Integer) v.getTag());
                Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                if (collection.contains(m)) {
                    m = collection.get(collection.indexOf(m));
                    i.putExtra(DetailsActivity.EXTRA_FAV, true);
                }
                i.putExtra(DetailsActivity.EXTRA_MOVIE, m);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Pair<View, String> pair1 = Pair.create(v, v.getTransitionName());
                    ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(MainActivity.this, pair1);

                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }
            }
        });
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPos = ((GridLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition();
                int totalItems = adapter.getItemCount();
                if (lastVisibleItemPos == totalItems - 1 && lastVisibleItemPos != -1) {
                    mainVM.loadNextPage();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SCROLL_POS, rv.getScrollY());
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
        if (!isConnected()) return;
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
                        mainVM.setFilter(f);
                        dialog.dismiss();
                    }
                }
            }
        });
        filterDialog = b.create();
        filterDialog.show();
    }

    private void toggleProgress() {
        if (pbWrapper.getVisibility() == View.VISIBLE) {
            pbWrapper.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    private void showNotConnectedDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.no_internet);
        dialog.setMessage(R.string.no_internet_desc);
        dialog.setNeutralButton(R.string.show_fav, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainVM.setFilter(Filter.COLLECTION);
                toggleProgress();
                dialog.dismiss();
            }
        });
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
