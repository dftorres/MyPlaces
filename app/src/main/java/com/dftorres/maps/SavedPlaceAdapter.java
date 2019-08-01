package com.dftorres.maps;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SavedPlaceAdapter extends RecyclerView.Adapter<SavedPlaceAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<SavedPlace> mSavedPlaces;

    public SavedPlaceAdapter(Context context, ArrayList<SavedPlace> savedPlaces) {
        mContext = context;
        mSavedPlaces = savedPlaces;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.card_saved_places, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedPlace currentSavedPlace = mSavedPlaces.get(position);
        holder.imageView_savedPlace.setImageBitmap(BitmapFactory.decodeByteArray(currentSavedPlace.getPlaceImage(), 0, currentSavedPlace.getPlaceImage().length));
        holder.textView_placeName.setText(currentSavedPlace.getPlaceName());
        holder.textView_placeLocation.setText(currentSavedPlace.getPlaceAddress());
    }

    @Override
    public int getItemCount() {
        return mSavedPlaces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView_savedPlace;
        TextView textView_placeName, textView_placeLocation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView_savedPlace = itemView.findViewById(R.id.imageView_savedPlace);
            textView_placeName = itemView.findViewById(R.id.textView_placeName);
            textView_placeLocation = itemView.findViewById(R.id.textView_placeAddress);
        }
    }
}