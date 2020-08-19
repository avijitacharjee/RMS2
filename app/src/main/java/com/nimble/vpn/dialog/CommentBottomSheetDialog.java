package com.nimble.vpn.dialog;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nimble.vpn.Interface.DialogPeopleTagListner;
import com.nimble.vpn.Interface.RecyclerViewClickListener;
import com.nimble.vpn.R;
import com.nimble.vpn.adapter.ServerListAdapterFree;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class CommentBottomSheetDialog extends BottomSheetDialog implements View.OnClickListener {

    private static CommentBottomSheetDialog instance;
    private final Activity context;
    private ServerListAdapterFree adapter;
    private RecyclerView recycler_video_comments;

    private CommentBottomSheetDialog(@NonNull Activity context) {
        super(context, R.style.AppBottomSheetDialogTheme);
        this.context = context;
        create();
    }

    public static CommentBottomSheetDialog getInstance(@NonNull Activity context) {
        return instance == null ? new CommentBottomSheetDialog(context) : instance;
    }

    @Override
    public void onClick(View v) {
    }

    public void setData(ArrayList<CountryData> countryArrayList, DialogPeopleTagListner dialogPeopleTagListner) {
        RecyclerViewClickListener listener = (view, position) -> {
            dialogPeopleTagListner.getUserId(position);
        };
        adapter = new ServerListAdapterFree(countryArrayList, getContext(), listener);
        recycler_video_comments.setAdapter(adapter);
    }

    public void create() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.comment_bottom_sheet_layout, null);
        setContentView(bottomSheetView);
        LinearLayout linearLayout = bottomSheetView.findViewById(R.id.root);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) linearLayout.getLayoutParams();
        params.height = Resources.getSystem().getDisplayMetrics().heightPixels - 500;
        bottomSheetView.setLayoutParams(params);
        recycler_video_comments = bottomSheetView.findViewById(R.id.recycler_video_comments);
        recycler_video_comments.setHasFixedSize(true);
        recycler_video_comments.setLayoutManager(new LinearLayoutManager(getContext()));
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        };
    }
}
 