package com.example.collectomon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<String> artistList;

    public CustomSpinnerAdapter(Context context, List<String> artistList) {
        inflater = LayoutInflater.from(context);
        this.artistList = artistList;
    }

    @Override
    public int getCount() {
        return artistList.size();
    }

    @Override
    public String getItem(int position) {
        return artistList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.frag_search_artist_select_spinner, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.artistName = convertView.findViewById(R.id.artistName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set data to the views
        String artist = artistList.get(position);
        viewHolder.artistName.setText(artist);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.frag_search_artist_select_spinner, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.artistName = convertView.findViewById(R.id.artistName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set data to the views
        String artist = artistList.get(position);
        viewHolder.artistName.setText(artist);

        return convertView;
    }

    private static class ViewHolder {
        TextView artistName;
    }
}
