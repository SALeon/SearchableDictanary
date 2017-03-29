package com.dev.leo.searchabledictanary;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


public class DictionaryDatabase {
  private static final String TAG = "Dictionaryyy";




    public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;

    private static final String DATABASE_NAME = "dictionary";
    private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";
    private static final int DATABASE_VERSION = 2;

    private final DictionaryOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();


    public DictionaryDatabase(Context context) {
        Log.d(TAG,"DictionaryDatabase DictionaryDatabase");

        mDatabaseOpenHelper = new DictionaryOpenHelper(context);
    }


    private static HashMap<String,String> buildColumnMap() {
        Log.d(TAG,"DictionaryDatabase load buildColumnMap");
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_WORD, KEY_WORD);
        map.put(KEY_DEFINITION, KEY_DEFINITION);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }


    public Cursor getWord(String rowId, String[] columns) {
        Log.d(TAG,"DictionaryDatabase DictionaryData");
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);

     
    }

    public Cursor getWordMatches(String query, String[] columns) {
        Log.d(TAG,"DictionaryDatabase getWordMathces");

        String selection = KEY_WORD + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);


    }


    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        Log.d(TAG,"DictionaryDatabase queru");

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            Log.d(TAG,"DictionaryDatabase queru IF");

            return null;
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG,"DictionaryDatabase queru ELSE");

            cursor.close();
            return null;
        }
        return cursor;
    }


    private static class DictionaryOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;


        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        KEY_WORD + ", " +
                        KEY_DEFINITION + ");";

        DictionaryOpenHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.d(TAG,"DictionaryOpenHelper DictionaryOpenHelper");

            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG,"DictionaryOpenHelper onCreate");

            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

  
        private void loadDictionary() {
            Log.d(TAG,"DictionaryOpenHelper loadDictionary");

            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords() throws IOException {
            Log.d(TAG, "DictionaryDatabase Loading words...");
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.definitions);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "-");
                    if (strings.length < 2) continue;
                    long id = addWord(strings[0].trim(), strings[1].trim());
                    if (id < 0) {
                        Log.e(TAG, " DictionaryDatabaseunable to add word: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
            Log.d(TAG, "DictionaryDatabase DONE loading words.");
        }

 
        public long addWord(String word, String definition) {
            Log.d(TAG,"DictionaryOpenHelper addWord");

            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_WORD, word);
            initialValues.put(KEY_DEFINITION, definition);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, " DictionaryDatabase Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }

}
