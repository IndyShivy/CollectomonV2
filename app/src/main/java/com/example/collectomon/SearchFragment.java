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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SearchFragment extends Fragment {

    ActionBarDrawerToggle drawerToggle;
    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private static final String ARTIST_KEY = "artist";
    Spinner spinnerArtists;
    CustomSpinnerAdapter spinnerAdapter;
    Context context;
    EditText searchEditText;
    ImageButton addCards;
    private CardAdapter cardAdapter;
    private List<CardItem> cardItems;
    private CardDatabase db;
    private AppCompatActivity activity;


    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String searchText = s.toString().toLowerCase().trim();

            if (!searchText.isEmpty()) {
                filterCardItems(searchText);
            } else {
                cardAdapter.filterList(cardItems);
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            activity = (AppCompatActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up the onBackPressed callback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Replace the current fragment with HomeFragment
                getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_search, container, false);
        context = requireContext();
        db = new CardDatabase(context);

        addCards = rootView.findViewById(R.id.addCardButton);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        List<String> artistNames = new ArrayList<>();
        Set<String> artistSet = sharedPreferences.getStringSet(ARTIST_KEY, null);
        if (artistSet != null) {
            artistNames = new ArrayList<>(artistSet);
        }
        Collections.sort(artistNames);
        spinnerArtists = rootView.findViewById(R.id.spinnerArtists);
        spinnerAdapter = new CustomSpinnerAdapter(context, artistNames);
        spinnerArtists.setAdapter(spinnerAdapter);

        searchEditText = rootView.findViewById(R.id.searchCard);
        searchEditText.addTextChangedListener(textWatcher);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        cardItems = new ArrayList<>();
        cardAdapter = new CardAdapter(cardItems, context);
        recyclerView.setAdapter(cardAdapter);



        spinnerArtists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                webScrape(spinnerArtists.getSelectedItem().toString());
                if (!searchEditText.getText().toString().equals("")) {
                    searchEditText.setText("");
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerArtists.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Get the height of a single item in the dropdown list
                    int itemHeight = 48;
                    // Calculate the total height for the number of items you want to display at a time
                    int totalHeight = itemHeight * 5; // Change this to the number of items you want to display at a time
                    // Set the dropdown height
                    spinnerArtists.setDropDownVerticalOffset(totalHeight);
                }
                return false;
            }
        });

        addCards.setOnClickListener(v -> {
            List<CardItem> selectedCardItems = cardAdapter.getSelectedCardItems();
            cardAdapter.notifyDataSetChanged();
            db.addCards(selectedCardItems);
            Toast.makeText(context, "Cards have been added!", Toast.LENGTH_SHORT).show();
            pulseAnimation();
        });

        return rootView;
    }

    private void filterCardItems(String searchText) {
        List<CardItem> filteredList = new ArrayList<>();

        for (CardItem cardItem : cardItems) {
            if (cardItem.getCardName().toLowerCase().startsWith(searchText.toLowerCase())) {
                filteredList.add(cardItem);
            }
        }
        cardAdapter.filterList(filteredList);
    }

    public void webScrape(String name) {
        cardItems.clear();
        String stringWithoutGaps = name.replaceAll("\\s+", "");
        String modifiedName = stringWithoutGaps.toLowerCase();
        String theLink = "https://www.serebii.net/card/dex/artist/" + modifiedName + ".shtml";

        System.out.println(theLink);
        @SuppressLint("NotifyDataSetChanged") Thread webScrapingThread = new Thread(() -> {
            try {
                Document doc = Jsoup.connect(theLink).get();
                Element tableElement = doc.select("table.dextable").first();
                assert tableElement != null;
                Elements rowElements = tableElement.select("tr");
                ArrayList<CardItem> cards = new ArrayList<>();

                cardItems.clear();

                for (int i = 1; i < rowElements.size(); i++) {
                    Element row = rowElements.get(i);
                    Elements columnElements = row.select("td");

                    if (columnElements.size() >= 3 && columnElements.get(0).selectFirst("a img") != null) {
                        Element imageLink = columnElements.get(0).selectFirst("a");
                        String imageSrc = (imageLink != null) ? Objects.requireNonNull(imageLink.selectFirst("img")).attr("src") : "";
                        String imageSrc1 = "https://www.serebii.net/" + imageSrc;
                        Element cardNameElement = columnElements.get(1).selectFirst("font");
                        String cardName = (cardNameElement != null) ? cardNameElement.text() : "";

                        if (cardName.equals("")) {
                            Elements aElements = columnElements.get(1).select("a");
                            cardName = aElements.text();
                        }

                        Element setLink = columnElements.get(2).selectFirst("a");
                        String setDetails = (setLink != null) ? setLink.text() : "";

                        String cardDetails = columnElements.get(2).ownText();
                        String cardId = name + cardName + setDetails + cardDetails;
                        CardItem cardItem = new CardItem(name, cardId, imageSrc1, cardName, setDetails, cardDetails);

                        boolean isDuplicate = false;
                        for (CardItem card : cards) {
                            if (card.getCardId().equals(cardItem.getCardId())) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            cards.add(cardItem);
                            cardItems.add(cardItem);
                        }
                    }
                }

                if (activity != null) {
                    activity.runOnUiThread(() -> cardAdapter.notifyDataSetChanged());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        webScrapingThread.start();
    }
    private void pulseAnimation() {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                addCards,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        );
        scaleDown.setDuration(500);
        scaleDown.setRepeatCount(ObjectAnimator.RESTART);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();
    }


}

