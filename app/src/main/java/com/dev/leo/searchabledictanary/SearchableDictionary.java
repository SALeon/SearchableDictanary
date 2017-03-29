package com.dev.leo.searchabledictanary;
import android.app.Activity;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchableDictionary extends Activity {
    String TAG = "Dictionaryyy";
    private TextView mTextView;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"SearchableDictionary onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG,"SearchableDictionary onNewIntent");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.d(TAG,"SearchableDictionary handleIntent IF");

            Intent wordIntent = new Intent(this, WordActivity.class);
            wordIntent.setData(intent.getData());
            startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d(TAG,"SearchableDictionary handleIntent ELSE IF");

            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }


    private void showResults(String query) {
        Log.d(TAG,"SearchableDictionary  showResults");

        Cursor cursor = getContentResolver().query(DictionaryProvider.CONTENT_URI, null, null,
                new String[]{query}, null);

        if (cursor == null) {
            Log.d(TAG,"SearchableDictionary  showResults IF");

            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } else {
            Log.d(TAG,"SearchableDictionary  showResults ELSE");

            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                    count, new Object[] {count, query});
            mTextView.setText(countString);

      
            String[] from = new String[] { DictionaryDatabase.KEY_WORD,
                    DictionaryDatabase.KEY_DEFINITION };

      
            int[] to = new int[] { R.id.word,
                    R.id.definition };

            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
                    R.layout.result, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mListView.setAdapter(words);

   
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          
                    Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
                            String.valueOf(id));
                    wordIntent.setData(data);
                    startActivity(wordIntent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG,"SearchableDictionary  onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            Log.d(TAG,"SearchableDictionary  onCreateOptionsMenu IF");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"SearchableDictionary  onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.search:
                Log.d(TAG,"SearchableDictionary  onOptionsItemSelected case SEARCH");

                onSearchRequested();
                return true;
            default:
                Log.d(TAG,"SearchableDictionary  onOptionsItemSelected case DEFAULT");

                return false;
        }
    }
}
