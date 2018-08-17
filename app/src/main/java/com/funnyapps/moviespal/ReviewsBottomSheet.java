package com.funnyapps.moviespal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.funnyapps.moviespal.Models.Review;

import java.util.List;

public class ReviewsBottomSheet extends BottomSheetDialogFragment {
    private List<Review> reviews;


    public ReviewsBottomSheet() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_reviews_bottom_sheet, container, false);
        RecyclerView mainRv = root.findViewById(R.id.reviews_rv);
        Toolbar tb = root.findViewById(R.id.frag_toolbar);
        tb.setTitle(R.string.reviews);
        tb.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewsBottomSheet.this.dismiss();
            }
        });
        mainRv.setLayoutManager(new LinearLayoutManager(getContext()));
        ReviewsAdapter adapter = new ReviewsAdapter(getContext());
        adapter.setReviews(reviews);
        mainRv.setAdapter(adapter);
        return root;
    }

    public void setReviews(List<Review> reviews){
        this.reviews = reviews;
    }
}
