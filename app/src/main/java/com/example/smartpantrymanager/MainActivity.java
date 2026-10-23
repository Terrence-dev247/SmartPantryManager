package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.DbExecutor;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.RecipeSeeder;
import com.example.smartpantrymanager.logic.Format;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Home screen: the pantry list.
 * Lets the user see every ingredient, open one to edit it, delete it,
 * or add a new one with the + button.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private PantryAdapter adapter;
    private RecyclerView recycler;
    private TextView emptyView;
    private TextView alertBanner;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.pantryRecycler);
        emptyView = findViewById(R.id.emptyView);
        alertBanner = findViewById(R.id.alertBanner);
        searchInput = findViewById(R.id.pantrySearch);

        adapter = new PantryAdapter(this);
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
                updateEmptyState();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Explicit Intent: open the Add/Edit screen with no item id = "add new".
        findViewById(R.id.addFab).setOnClickListener(v ->
                startActivity(new Intent(this, EditIngredientActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_pantry);

        // Put the starter recipes in the database the first time the app runs.
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> RecipeSeeder.seedIfEmpty(appContext));
    }

    /** Reload every time we come back, so changes made on other screens show up. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            final List<PantryItem> items = AppDatabase.get(appContext).pantryDao().getAll();
            runOnUiThread(() -> showItems(items));
        });
    }

    private void showItems(List<PantryItem> items) {
        adapter.setItems(items);
        updateEmptyState();
        showExpiryBanner(items);
    }

    private void updateEmptyState() {
        boolean noResults = adapter.getItemCount() == 0;
        boolean searching = searchInput != null && !searchInput.getText().toString().trim().isEmpty();
        if (noResults && searching) {
            emptyView.setText(R.string.pantry_search_empty);
        } else {
            emptyView.setText(R.string.pantry_empty);
        }
        emptyView.setVisibility(noResults ? View.VISIBLE : View.GONE);
        recycler.setVisibility(noResults ? View.GONE : View.VISIBLE);
    }

    /** Shows a warning banner for items that expire within 3 days (if enabled in Settings). */
    private void showExpiryBanner(List<PantryItem> items) {
        if (!Prefs.isExpiryAlertsOn(this)) {
            alertBanner.setVisibility(View.GONE);
            return;
        }

        long now = System.currentTimeMillis();
        List<String> names = new ArrayList<>();
        for (PantryItem item : items) {
            if (item.expiryDate != null && Format.daysUntil(item.expiryDate, now) <= 3) {
                names.add(Format.capitalise(item.name));
            }
        }

        if (names.isEmpty()) {
            alertBanner.setVisibility(View.GONE);
        } else {
            alertBanner.setText(getString(R.string.expiring_banner, TextUtils.join(", ", names)));
            alertBanner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(PantryItem item) {
        // Explicit Intent carrying the id of the item to edit.
        Intent intent = new Intent(this, EditIngredientActivity.class);
        intent.putExtra(EditIngredientActivity.EXTRA_ITEM_ID, item.id);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, Format.capitalise(item.name)))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteItem(final PantryItem item) {
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            AppDatabase.get(appContext).pantryDao().delete(item);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                loadPantry();
            });
        });
    }
}
