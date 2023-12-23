package com.example.collectomon;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionFragment extends Fragment {
    private ArrayAdapter<String> arrayAdapter;
    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private static final String ARTIST_KEY = "artist";
    private CardDatabase databaseHelper;
    private Context context;
    private RecyclerView recyclerView;
    private CollectionAdapter collectionAdapter;
    private Spinner spinnerArtists;
    private List<String> artistList;
    ImageButton deleteCards;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        databaseHelper = new CardDatabase(context);
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        collectionAdapter = new CollectionAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ListView listViewArtists = rootView.findViewById(R.id.listViewArtists);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        deleteCards = rootView.findViewById(R.id.deleteCardButton);
        Set<String> artistSet = sharedPreferences.getStringSet(ARTIST_KEY, null);
        assert artistSet != null;
        artistList = new ArrayList<>(artistSet);

        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, artistList);
        listViewArtists.setAdapter(arrayAdapter);
        EditText searchEditText = rootView.findViewById(R.id.searchEditText1);
        searchEditText.addTextChangedListener(textWatcher);
        listViewArtists.setOnItemClickListener((parent, view, position, id) -> {
            String selectedArtist = arrayAdapter.getItem(position);
            List<CardItem> cardItems = databaseHelper.getCardsByArtist(selectedArtist);
            collectionAdapter = new CollectionAdapter(cardItems);
            recyclerView.setAdapter(collectionAdapter);
        });

        deleteCards.setOnClickListener(v -> {
            List<CardItem> selectedCardItems = collectionAdapter.getSelectedCardItems();
            databaseHelper.deleteCards(selectedCardItems);
            String selectedArtist = spinnerArtists.getSelectedItem().toString();
            List<CardItem> updated = databaseHelper.getCardsByArtist(selectedArtist);
            collectionAdapter = new CollectionAdapter(updated);
            recyclerView.setAdapter(collectionAdapter);
            collectionAdapter.notifyDataSetChanged();
            Toast.makeText(context, "Cards have been removed!", Toast.LENGTH_SHORT).show();
            pulseAnimation();
        });

        spinnerArtists = rootView.findViewById(R.id.spinnerArtists);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, artistList);
        spinnerArtists.setAdapter(spinnerAdapter);

        spinnerArtists.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                spinnerArtists.performClick();
                return true;
            }
            return false;
        });

        spinnerArtists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArtist = artistList.get(position);
                List<CardItem> cardItems = databaseHelper.getCardsByArtist(selectedArtist);
                collectionAdapter = new CollectionAdapter(cardItems);
                recyclerView.setAdapter(collectionAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            filterCardItems(s.toString());
        }

        private void filterCardItems(String searchText) {
            List<CardItem> filteredList = new ArrayList<>();

            for (CardItem cardItem : databaseHelper.getCardsByArtist(spinnerArtists.getSelectedItem().toString())) {
                if (cardItem.getCardName().toLowerCase().startsWith(searchText.toLowerCase())) {
                    filteredList.add(cardItem);
                }
            }

            collectionAdapter.filterList(filteredList);
        }
    };

    private void pulseAnimation() {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                deleteCards,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        );
        scaleDown.setDuration(500);
        scaleDown.setRepeatCount(ObjectAnimator.RESTART);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();
    }
}
