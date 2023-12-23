package com.example.collectomon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "card_database";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "cards";
    public static final String ARTIST_NAME = "artist_name";
    public static final String CARD_ID = "card_id";
    public static final String COLUMN_IMAGE_SRC = "image_src";
    public static final String COLUMN_CARD_NAME = "card_name";
    public static final String COLUMN_SET_DETAILS = "set_details";
    public static final String COLUMN_CARD_DETAILS = "card_details";
    Context context;
    String dbPath;

    public CardDatabase(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                ARTIST_NAME + " TEXT, " +
                CARD_ID + " TEXT, " +
                COLUMN_IMAGE_SRC + " TEXT, " +
                COLUMN_CARD_NAME + " TEXT, " +
                COLUMN_SET_DETAILS + " TEXT, " +
                COLUMN_CARD_DETAILS + " TEXT)";
        db.execSQL(createTableQuery);
        dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


//save and restore backup


    public void saveBackup() {
        String backupFileName = "CollectomonDatabase.db";
        File backupFile = new File(context.getExternalFilesDir(null), backupFileName);

        try {
            SQLiteDatabase db = getWritableDatabase();

            File dbFile = new File(db.getPath());
            FileInputStream fis = new FileInputStream(dbFile);
            FileOutputStream fos = new FileOutputStream(backupFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.flush();
            fos.close();
            fis.close();

            Toast.makeText(context,"Database backup created successfully",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"Failed to create backup",Toast.LENGTH_SHORT).show();
        }
    }

    public void restoreBackup() {
        String backupFileName = "CollectomonDatabase.db";
        File backupFile = new File(context.getExternalFilesDir(null), backupFileName);

        try {
            SQLiteDatabase db = getWritableDatabase();

            // Clear the existing table
            db.execSQL("DELETE FROM " + TABLE_NAME);

            FileInputStream fis = new FileInputStream(backupFile);
            FileOutputStream fos = new FileOutputStream(db.getPath());

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.flush();
            fos.close();
            fis.close();

            Toast.makeText(context,"Database backup restored successfully",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"Failed to restore database backup, missing backup",Toast.LENGTH_SHORT).show();
        }
    }

    public List<CardItem> getCardsByArtist(String artistName) {
        List<CardItem> cardList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                ARTIST_NAME,
                CARD_ID,
                COLUMN_IMAGE_SRC,
                COLUMN_CARD_NAME,
                COLUMN_SET_DETAILS,
                COLUMN_CARD_DETAILS
        };

        String selection = ARTIST_NAME + " = ?";
        String[] selectionArgs = {artistName};

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );


        if (cursor.moveToFirst()) {
            do {
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST_NAME));
                String cardId = cursor.getString(cursor.getColumnIndexOrThrow(CARD_ID));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_SRC));
                String cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NAME));
                String setDetails = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SET_DETAILS));
                String cardDetails = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_DETAILS));

                CardItem cardItem = new CardItem(artist, cardId, imageUrl, cardName, setDetails, cardDetails);
                cardList.add(cardItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return cardList;
    }


    public void addCards(List<CardItem> cards) {
        SQLiteDatabase db = getWritableDatabase();

        for (CardItem card : cards) {
            if (!isCardIdExists(db, card.getCardId())) {
                ContentValues values = new ContentValues();
                values.put(ARTIST_NAME, card.getArtistName());
                values.put(CARD_ID, card.getCardId());
                values.put(COLUMN_IMAGE_SRC, card.getImageUrl());
                values.put(COLUMN_CARD_NAME, card.getCardName());
                values.put(COLUMN_SET_DETAILS, card.getSetDetails());
                values.put(COLUMN_CARD_DETAILS, card.getCardDetails());
                db.insert(TABLE_NAME, null, values);
            }
        }

        db.close();
    }

    public void deleteCards(List<CardItem> cards) {
        SQLiteDatabase db = getWritableDatabase();

        for (CardItem card : cards) {
            String cardId = card.getCardId();

            if (isCardIdExists(db, cardId)) {
                db.delete(TABLE_NAME, CARD_ID + " = ?", new String[]{cardId});
            }
        }

        db.close();
    }


    private boolean isCardIdExists(SQLiteDatabase db, String cardId) {
        String[] columns = {CARD_ID};
        String selection = CARD_ID + " = ?";
        String[] selectionArgs = {cardId};
        String limit = "1";

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }

        return exists;
    }

    public boolean isCardExists(String cardId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {CARD_ID};
        String selection = CARD_ID + " = ?";
        String[] selectionArgs = {cardId};
        String limit = "1";

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        assert cursor != null;
        cursor.close();
        db.close();

        return exists;
    }


}

