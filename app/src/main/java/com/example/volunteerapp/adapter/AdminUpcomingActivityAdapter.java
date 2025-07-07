package com.example.volunteerapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.R;
import com.example.volunteerapp.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminUpcomingActivityAdapter extends RecyclerView.Adapter<AdminUpcomingActivityAdapter.ViewHolder>{
    private List<Event> activityList;
    private Context mContext;

    private int currentPos=-1;

    private AdminUpcomingActivityAdapter.OnItemClickListener mListener;

    // Add an interface to handle item clicks
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvEventName;
        public TextView tvDesc;
        public TextView tvDate;
        public TextView tvCategory;
        public TextView tvLocation;
        public ImageView imgEvent;


        public ViewHolder(@NonNull View itemView, final AdminUpcomingActivityAdapter.OnItemClickListener listener, final List<Event> listData) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            imgEvent = itemView.findViewById(R.id.imgEvent);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(listData.get(position)); // Use the passed listData
                }
            });
        }

    }

    public AdminUpcomingActivityAdapter(Context context, List<Event> activityList, AdminUpcomingActivityAdapter.OnItemClickListener listener) {
        // Initialize as mutable list
        this.activityList = activityList;
        this.mContext = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_upcoming_recycle_view, parent, false);

        return new ViewHolder(view, mListener, activityList);
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUpcomingActivityAdapter.ViewHolder holder, int position) {
        Event event = activityList.get(position);

        // Set text for event name, description, category, and location
        holder.tvEventName.setText(event.getEvent_name() != null ? event.getEvent_name() : "N/A");
        holder.tvCategory.setText(event.getCategory() != null ? event.getCategory() : "N/A");
        holder.tvDate.setText(event.getDate() != null ? event.getDate() : "N/A");
        holder.tvLocation.setText(event.getLocation() != null ? event.getLocation() : "N/A");

        // Use Glide to load the image into the ImageView
        String imageUrl = "https://codelah.my/2022484414/api/" + event.getImage();
        Log.d("ImageURL", "URL: " + imageUrl);

        Glide.with(mContext)
                .load(imageUrl)
                .placeholder(R.drawable.default_cover) // Placeholder if the image URL is empty
                .error(R.drawable.default_cover) // Error image if the URL fails
                .into(holder.imgEvent);
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public Event getSelectedItem() {
        if (currentPos >= 0 && activityList != null && currentPos < activityList.size()) {
            return activityList.get(currentPos);
        }
        return null;
    }

    // Method to update the data in the adapter
    public void updateData(List<Event> newActivityList) {
        if (newActivityList != null) {
            this.activityList.clear();
            this.activityList.addAll(newActivityList);
            notifyDataSetChanged();
        }
    }
}
