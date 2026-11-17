package com.ccg.futurerealization.view.activity;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.view.fragment.NotesFragment;

/** Standalone host retained for existing entry points; the main screen uses the same fragment. */
public class NotesActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_notes);
        setTitle("随身笔记");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (state == null) getSupportFragmentManager().beginTransaction().replace(R.id.notes_container, new NotesFragment()).commit();
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) { if (item.getItemId() == android.R.id.home) { finish(); return true; } return super.onOptionsItemSelected(item); }
}
