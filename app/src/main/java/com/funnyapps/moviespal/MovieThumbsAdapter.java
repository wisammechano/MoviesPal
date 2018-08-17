package com.funnyapps.moviespal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.funnyapps.moviespal.Models.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class MovieThumbsAdapter extends RecyclerView.Adapter<MovieThumbsAdapter.ViewHolder> {
    public static final float THUMB_WIDTH = 342; //Related to getImageMdUrl() from below.
    private Context ctx;
    private List<Movie> items;
    private View.OnClickListener onItemClickListener;
    private View.OnLongClickListener onLongClickListener;

    MovieThumbsAdapter(Context ctx) {
        this.ctx = ctx;
        items = new ArrayList<>();
    }

    public void setItems(List<Movie> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ViewHolder finalVH = holder;

        if(onItemClickListener != null){
            holder.thumbIv.setTag(position);
            holder.thumbIv.setOnClickListener(onItemClickListener);
            holder.thumbIv.setOnLongClickListener(onLongClickListener);
        }

        String path = items.get(position).getPosterPath();
        Picasso.with(ctx).load(Api.getImageMdUri(path)).into(holder.thumbIv, new Callback() {
            @Override
            public void onSuccess() {
                finalVH.loadingPb.setVisibility(View.GONE);
                finalVH.thumbIv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                finalVH.loadingPb.setVisibility(View.GONE);
                finalVH.errorTv.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Movie> getItems() {
        return items;
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbIv;
        ProgressBar loadingPb;
        TextView errorTv;

        ViewHolder(View itemView) {
            super(itemView);
            thumbIv = itemView.findViewById(R.id.movie_poster);
            loadingPb = itemView.findViewById(R.id.loading_pb);
            errorTv = itemView.findViewById(R.id.error_tv);
        }

    }
}
