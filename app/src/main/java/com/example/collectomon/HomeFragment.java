package com.example.collectomon;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HomeFragment extends Fragment{
    public List<String> artistNames;
    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private static final String ARTIST_KEY = "artist";
    private SharedPreferences sharedPreferences;
    Button backup, restore,addArtistButton;
    CardDatabase db;
    Context context;
    private ListView listViewArtists;
    private ArrayAdapter<String> storedArtistNames;
    private AutoCompleteTextView addArtist;

    //stored list of artist names
    String[] artistSuggestions = {
            "Akira Komayama", "Atsuko Nishida", "Chibi", "Hasuno","Hataya",
            "Hyogonosuke", "Kawayoo", "Kiyotaka Oshiyama", "Kurumitsu", "Kyokou Umemato",
            "Mahou", "Mina Nakai", "Ooyama", "Saya Tsuruta", "Shibuzoh.", "Sowsow", "Sui",
            "Tetsuya Koizumi", "Tika Matsuno", "Tokiya"
    };

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home, container, false);

        addArtistButton = view.findViewById(R.id.addArtistButton);
        addArtist = view.findViewById(R.id.searchCard);
        backup = view.findViewById(R.id.backupButton);
        restore = view.findViewById(R.id.restoreButton);
        context = requireContext();
        db = new CardDatabase(context);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        artistNames = new ArrayList<>();
        //addArtistButton.getDrawable().setAlpha(200);
        //deleteArtistButton.getDrawable().setAlpha(200);


        storedArtistNames = new ArrayAdapter<>(requireContext(), R.layout.frag_home_artist_list_item, artistNames);

        Set<String> artistSet = sharedPreferences.getStringSet(ARTIST_KEY, null);
        if (artistSet != null) {
            artistNames = new ArrayList<>(artistSet);
        }
        saveArtistList(artistNames);


        listViewArtists = view.findViewById(R.id.listViewArtists);  // Find the ListView
//        storedArtistNames = new ArrayAdapter<>(requireContext(), R.layout.list_item_artist, artistNames);
//        listViewArtists.setAdapter(storedArtistNames);

        storedArtistNames = new ArtistAdapter(context, artistNames, sharedPreferences);
        listViewArtists.setAdapter(storedArtistNames);
        listViewArtists.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        backup.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);
            LayoutInflater backupInflate = getLayoutInflater();
            View dialogView = backupInflate.inflate(R.layout.frag_home_backup_restore_dialog, null);
            builder.setView(dialogView);

            // Find the views in the custom layout and set their properties
            TextView title = dialogView.findViewById(R.id.dialog_title);
            title.setText("Backup");
            //set the colour based on hex code
            //title.setTextColor(Color.parseColor("#5A0715"));
            title.setTextColor(Color.WHITE);



            TextView message = dialogView.findViewById(R.id.dialog_message);
            message.setText("Do you want to backup your collection?");
            message.setTextColor(Color.WHITE);  // Set the color

            builder.setPositiveButton("Yes", (dialog, which) -> db.saveBackup())
                    .setNegativeButton("No", null)
                    .show();
        });

        restore.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);
            LayoutInflater backupInflate = getLayoutInflater();
            View dialogView = backupInflate.inflate(R.layout.frag_home_backup_restore_dialog, null);
            builder.setView(dialogView);

            // Find the views in the custom layout and set their properties
            TextView title = dialogView.findViewById(R.id.dialog_title);
            title.setText("Restore");

            //title.setTextColor(Color.parseColor("#343455"));
            title.setTextColor(Color.WHITE);



            TextView message = dialogView.findViewById(R.id.dialog_message);
            message.setText("Do you want to restore a previous backup?");
            message.setTextColor(Color.WHITE);  // Set the color

            builder.setPositiveButton("Yes", (dialog, which) -> db.restoreBackup())
                    .setNegativeButton("No", null)
                    .show();
        });


        addArtistButton.setOnClickListener(v -> {
            if (addArtist.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "No artist name", Toast.LENGTH_SHORT).show();
                pulseAnimation(addArtistButton);
            } else {
                String name = addArtist.getText().toString();
                addArtistToList(name);
                pulseAnimation(addArtistButton);
            }
        });


        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_list, artistSuggestions);
        addArtist.setAdapter(adapter);


        addArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addArtist.showDropDown();
            }
        });
        addArtist.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addArtist.showDropDown();
                }
            }
        });

        addArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Filter results and count matches
                adapter.getFilter().filter(charSequence, count -> {
                    int maxItemsToShow = 6; // Max items to show
                    int itemsToShow = Math.min(count, maxItemsToShow);

                    System.out.println("itemsToShow: " + itemsToShow);
                    // Calculate item height dynamically or use a specific height
                    int singleItemHeight = 155; // Define or calculate the single item height
                    int dropdownHeight = itemsToShow * singleItemHeight;

                    // Set the height dynamically
                    addArtist.setDropDownHeight(dropdownHeight);
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void addArtistToList(String name) {
        if (!artistNames.contains(name)) {
            artistNames.add(name);
            storedArtistNames.notifyDataSetChanged();
            addArtist.setText("");

            saveArtistList(artistNames);
            hideKeyboard();

        } else {
            Toast.makeText(context, "Artist " + name + " is already in the list.", Toast.LENGTH_SHORT).show();
            addArtist.setText("");
            hideKeyboard();
        }
    }

    private void saveArtistList(List<String> artistList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //alphabetical order
        artistList.sort(String::compareToIgnoreCase);
        Set<String> set = new HashSet<>(artistList);
        editor.putStringSet(ARTIST_KEY, set);
        editor.apply();
    }

    private void pulseAnimation(Button button) {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                button,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        );
        scaleDown.setDuration(500);
        scaleDown.setRepeatCount(ObjectAnimator.RESTART);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();

    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
