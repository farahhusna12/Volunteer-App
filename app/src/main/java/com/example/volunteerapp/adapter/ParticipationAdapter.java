package com.example.volunteerapp.adapter;

import android.annotation.SuppressLint;
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
import com.example.volunteerapp.model.Participation;
import com.example.volunteerapp.model.User;
import com.example.volunteerapp.remote.ApiUtils;
import com.example.volunteerapp.remote.EventService;
import com.example.volunteerapp.sharedpref.SharedPrefManager;

import java.util.List;

public class ParticipationAdapter extends RecyclerView.Adapter<ParticipationAdapter.ViewHolder> {

    private static List<Participation> mListData;   // list of seller objects
    private Context mContext;       // activity context
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Participation participation);
    }
    static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView eventName;
        private TextView eventLocation;
        private TextView eventDate;
        private TextView eventOrganizer;
        public ImageView eventImage;

        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);

            eventName = itemView.findViewById(R.id.tvEventName);
            eventLocation = itemView.findViewById(R.id.tvLocation);
            eventDate = itemView.findViewById(R.id.tvDate);
            eventOrganizer = itemView.findViewById(R.id.tvOrganizer);
            eventImage =  itemView.findViewById(R.id.activityImage);

            // Set an onClickListener on the itemView
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(mListData.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public ParticipationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the single item layout
        View view = inflater.inflate(R.layout.item_recycle_view, parent, false);

        // Return a new holder instance
        return new ParticipationAdapter.ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipationAdapter.ViewHolder holder, int position) {

        if (mListData != null && !mListData.isEmpty()) {
            Participation m = mListData.get(position);
            holder.eventName.setText(m.getEvent().getEvent_name());
            holder.eventLocation.setText(m.getEvent().getLocation());
            holder.eventDate.setText(m.getEvent().getDate());
            Log.d("Event","Organizer : " + m.getEvent().getOrganizer().getOrganizer_name());
            String username = (m.getEvent() != null && m.getEvent().getOrganizer() != null && m.getEvent().getOrganizer().getOrganizer_name() != null)
                    ? m.getEvent().getOrganizer().getOrganizer_name()
                    : "Unknown User"; // Default value if null

            holder.eventOrganizer.setText(username);

            Log.d("ImageURL", "URL: https://codelah.my/2022484414/api/" + m.getEvent().getImage());
            // Use Glide to load the image into the ImageView
            Glide.with(mContext)
                    .load("https://codelah.my/2022484414/api/" + m.getEvent().getImage())
                    .placeholder(R.drawable.default_cover) // Placeholder image if the URL is empty
                    .error(R.drawable.default_cover) // Error image if there is a problem loading the image
                    .into(holder.eventImage);
        }
    }

    public ParticipationAdapter(Context context, List<Participation> listData, OnItemClickListener listener){
        mListData = listData;
        mContext = context;
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return (mListData == null) ? 0 : mListData.size();
    }

    private Context getmContext(){return mContext;}

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Participation> listData) {
        mListData = listData;
        notifyDataSetChanged();
    }
}
