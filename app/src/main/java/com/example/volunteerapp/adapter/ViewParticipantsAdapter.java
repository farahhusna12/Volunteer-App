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
import com.example.volunteerapp.model.Participation;

import java.util.List;

public class ViewParticipantsAdapter extends RecyclerView.Adapter<ViewParticipantsAdapter.ViewHolder> {

    private final List<Participation> participants;
    private final Context mContext;

    public ViewParticipantsAdapter(Context context, List<Participation> participantList) {
        this.participants = participantList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_participants_item, parent, false); // Ensure this layout exists
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Participation participant = participants.get(position);

        if (participant != null && participant.getUser() != null) {
            // Bind the username and email of the participant
            holder.username.setText(participant.getUser().getUsername());
            holder.email.setText(participant.getUser().getEmail());

            // Log the image URL for debugging
            String imageUrl = "https://codelah.my/2022484414/api/" + participant.getUser().getImage();
            Log.d("ImageURL", "URL: " + imageUrl);

            // Use Glide to load the image into the ImageView
            Glide.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_prof_pic) // Placeholder image if the URL is empty
                    .error(R.drawable.default_prof_pic) // Error image if there is a problem loading the image
                    .into(holder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return participants != null ? participants.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView email;
        public ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind views for participant item
            username = itemView.findViewById(R.id.tvUsername); // Ensure you have this ID in the layout
            email = itemView.findViewById(R.id.tvEmail);
            profileImage = itemView.findViewById(R.id.profileImage); // Ensure you have this ID in the layout
        }
    }
}
