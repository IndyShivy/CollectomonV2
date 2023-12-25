package com.example.collectomon;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<CardItem> cardItems;
    private final ArrayList<CardItem> cardStuff;

    public CollectionAdapter(List<CardItem> cardItems) {
        this.cardItems = cardItems;
        this.cardStuff = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_collection_collectionadapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardItem cardItem = cardItems.get(position);

        Picasso.get()
                .load(cardItem.getImageSrc())
                .resize(600, 600)
                .centerInside()
                .into(holder.imageView);

        holder.cardNameTextView.setText(cardItem.getCardName());
        holder.setDetailsTextView.setText(cardItem.getSetDetails());
        holder.cardDetailsTextView.setText(cardItem.getCardDetails());

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(cardItem.isChecked());

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cardItem.setChecked(isChecked);
            if (isChecked) {
                cardStuff.add(cardItem);
            } else {
                cardStuff.remove(cardItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView cardNameTextView;
        TextView setDetailsTextView;
        TextView cardDetailsTextView;
        CheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            cardNameTextView = itemView.findViewById(R.id.cardNameTextView);
            setDetailsTextView = itemView.findViewById(R.id.setDetailsTextView);
            cardDetailsTextView = itemView.findViewById(R.id.cardDetailsTextView);
            checkbox = itemView.findViewById(R.id.checkbox);
            itemView.setOnClickListener(v -> checkbox.setChecked(!checkbox.isChecked()));
        }
    }

    public List<CardItem> getSelectedCardItems() {
        List<CardItem> selectedItems = new ArrayList<>(cardStuff);
        cardStuff.clear();
        return selectedItems;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<CardItem> filteredList) {
        cardItems = filteredList;
        notifyDataSetChanged();
    }
}
