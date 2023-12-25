package com.example.collectomon;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArtistAdapter extends ArrayAdapter<String> {
    private static final String ARTIST_KEY = "artist";
    private SharedPreferences sharedPreferences;
    List<String> artistsGlobal;

    public ArtistAdapter(Context context, List<String> artists, SharedPreferences sharedPreferences) {
        super(context, 0, artists);
        this.sharedPreferences = sharedPreferences;
        artistsGlobal = artists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.frag_home_artist_list_item, parent, false);
        }

        String artist = getItem(position);

        TextView artistName = convertView.findViewById(R.id.artist_name);
        artistName.setText(artist);

        Button deleteButton = convertView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(artist);
                saveArtistList(artistsGlobal);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
    private void saveArtistList(List<String> artistList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(artistList);
        editor.putStringSet(ARTIST_KEY, set);
        editor.apply();
    }
}