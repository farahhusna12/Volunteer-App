package com.example.volunteerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.volunteerapp.R;
import com.example.volunteerapp.admin.ViewParticipantsActivity;
import com.example.volunteerapp.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminCurrentActivityAdapter extends RecyclerView.Adapter<AdminCurrentActivityAdapter.ViewHolder> {

    private List<Event> curActivityAdmin;
    private Context mContext;
    private OnParticipantButtonClickListener listener;

    public AdminCurrentActivityAdapter(Context context, List<Event> activityList) {
        // Initialize as mutable list
        this.curActivityAdmin = new ArrayList<>(activityList != null ? activityList : new ArrayList<>());
        this.mContext = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_recycle_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = curActivityAdmin.get(position);

        // Set text for event name and location
        holder.eventName.setText(event.getEvent_name());
        holder.location.setText(event.getLocation());

        // Use Glide to load the image into the ImageView
        String imageUrl = "https://codelah.my/2022484414/api/" + event.getImage();
        Log.d("ImageURL", "Loading image from URL: " + imageUrl);
        Glide.with(mContext)
                .load(imageUrl)
                .placeholder(R.drawable.default_cover) // Placeholder image if the URL is empty
                .error(R.drawable.default_cover) // Error image if there is a problem loading the image
                .into(holder.eventImage);

        // Set button click listener
        holder.btnViewParticipant.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ViewParticipantsActivity.class);
            intent.putExtra("event_id", event.getEvent_id()); // Pass event ID
            mContext.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return curActivityAdmin.size();
    }

    // Method to update the data in the adapter
    public void updateData(List<Event> newActivityList) {
        if (newActivityList != null) {
            this.curActivityAdmin.clear(); // Clear the existing data
            this.curActivityAdmin.addAll(newActivityList); // Add the new data
            notifyDataSetChanged(); // Notify RecyclerView to refresh
        }
    }

    public interface OnParticipantButtonClickListener {
        void onViewParticipantsClick(int position, Event activityModel);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventName;
        public TextView location;
        public ImageView eventImage;
        public Button btnViewParticipant;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind views
            eventName = itemView.findViewById(R.id.tvEventName);
            location = itemView.findViewById(R.id.tvLocation);
            eventImage =  itemView.findViewById(R.id.activityImage);
            btnViewParticipant=itemView.findViewById(R.id.btnViewParticipant);
        }
    }
}

