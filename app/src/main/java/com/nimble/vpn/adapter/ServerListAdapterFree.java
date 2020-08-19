package com.nimble.vpn.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anchorfree.partner.api.data.Country;
import com.nimble.vpn.R;
import com.nimble.vpn.activity.MainActivity;
import com.nimble.vpn.dialog.CountryData;
import com.nimble.vpn.Interface.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.Locale;

public class ServerListAdapterFree extends RecyclerView.Adapter<ServerListAdapterFree.mViewhoder> {

    public Context context;
    private ArrayList<CountryData> datalist;
    private RecyclerViewClickListener mListener;

    public ServerListAdapterFree(ArrayList<CountryData> datalist2, Context ctx, RecyclerViewClickListener listener) {
        mListener = listener;
        this.datalist = datalist2;
        this.context = ctx;
    }

    @NonNull
    public mViewhoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new mViewhoder(LayoutInflater.from(parent.getContext()).inflate(R.layout.server_list_free, parent, false));
    }

    public void onBindViewHolder(@NonNull mViewhoder holder, int position) {
        final CountryData datanew = this.datalist.get(holder.getAdapterPosition());
        final Country data = datanew.getCountryvalue();
        Locale locale = new Locale("", data.getCountry());
        if (position == 0) {
            holder.flag.setImageResource(this.context.getResources().getIdentifier("drawable/flag_default", null, this.context.getPackageName()));
            holder.app_name.setText(R.string.best_performance_server);
            holder.limit.setVisibility(View.GONE);
        } else {
            ImageView imageView = holder.flag;
            Resources resources = this.context.getResources();
            String sb = "drawable/" + data.getCountry().toLowerCase();
            imageView.setImageResource(resources.getIdentifier(sb, null, this.context.getPackageName()));
            holder.app_name.setText(locale.getDisplayCountry());
            holder.limit.setImageResource(R.drawable.server_signal_3);
        }
        if (datanew.isPro()) {
            holder.pro.setVisibility(View.VISIBLE);
        } else {
            holder.pro.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                mListener.onClick(view, data.getCountry());
                Intent intent = new Intent(ServerListAdapterFree.this.context, MainActivity.class);
                intent.putExtra("c", data.getCountry());
                intent.addFlags(268435456);
                intent.addFlags(67108864);
                context.startActivity(intent);

            }
        });
    }

    public int getItemCount() {
        return this.datalist.size();
    }

    static class mViewhoder extends RecyclerView.ViewHolder {
        TextView app_name;
        ImageView flag, pro;
        ImageView limit;

        mViewhoder(View itemView) {
            super(itemView);
            this.app_name = itemView.findViewById(R.id.region_title);
            this.limit = itemView.findViewById(R.id.region_limit);
            this.flag = itemView.findViewById(R.id.country_flag);
            this.pro = itemView.findViewById(R.id.pro);
        }
    }
}