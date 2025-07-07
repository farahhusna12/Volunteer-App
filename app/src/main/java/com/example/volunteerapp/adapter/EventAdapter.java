package com.example.volunteerapp.adapter;

import android.content.Context;
import android.util.Log;
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

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> mListData;   // list of seller objects
    private Context mContext;       // activity context
    private OnItemClickListener mListener;

    // Add an interface to handle item clicks
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }
    static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView eventName;
        private TextView eventLocation;
        private TextView eventDate;
        private TextView eventOrganizer;

        public ImageView eventImage;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener, final List<Event> listData) {
            super(itemView);

            eventName = itemView.findViewById(R.id.tvEventName);
            eventLocation = itemView.findViewById(R.id.tvLocation);
            eventDate = itemView.findViewById(R.id.tvDate);
            eventOrganizer = itemView.findViewById(R.id.tvOrganizer);
            eventImage = itemView.findViewById(R.id.activityImage);

            // Set an onClickListener on the itemView
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(listData.get(position)); // Use the passed listData
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the single item layout
        View view = inflater.inflate(R.layout.item_recycle_view, parent, false);

        // Pass mListData and mListener to the ViewHolder
        return new ViewHolder(view, mListener, mListData);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Event m = mListData.get(position);
        holder.eventName.setText(m.getEvent_name());
        holder.eventLocation.setText(m.getLocation());
        holder.eventDate.setText(m.getDate());

        String username = (m.getOrganizer() != null && m.getOrganizer().getOrganizer_name() != null)
                ? m.getOrganizer().getOrganizer_name()
                : "Unknown User"; // Default username if null

        holder.eventOrganizer.setText(username);

        Log.d("ImageURL", "URL: https://codelah.my/2022484414/api/" + m.getImage());
        // Use Glide to load the image into the ImageView
        Glide.with(mContext)
                .load("https://codelah.my/2022484414/api/" + m.getImage())
                .placeholder(R.drawable.default_cover) // Placeholder image if the URL is empty
                .error(R.drawable.default_cover) // Error image if there is a problem loading the image
                .into(holder.eventImage);
    }

    public EventAdapter(Context context, List<Event> listData, OnItemClickListener listener) {
        this.mListData = listData; // No longer shared
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public void updateData(List<Event> newList) {
        this.mListData = newList;
        notifyDataSetChanged();
    }

}