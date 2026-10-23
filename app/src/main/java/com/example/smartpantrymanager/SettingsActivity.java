package com.example.smartpantrymanager;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.DbExecutor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

/** Settings: two switches (saved with SharedPreferences) and a button to clear the pantry. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialSwitch alertsSwitch = findViewById(R.id.alertsSwitch);
        MaterialSwitch almostSwitch = findViewById(R.id.almostSwitch);

        // Show the saved value first, then listen for changes.
        alertsSwitch.setChecked(Prefs.isExpiryAlertsOn(this));
        almostSwitch.setChecked(Prefs.isAlmostThereOn(this));
        alertsSwitch.setOnCheckedChangeListener((button, checked) -> Prefs.setExpiryAlertsOn(this, checked));
        almostSwitch.setOnCheckedChangeListener((button, checked) -> Prefs.setAlmostThereOn(this, checked));

        findViewById(R.id.clearPantryButton).setOnClickListener(v -> confirmClear());

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_settings);
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_pantry_title)
                .setMessage(R.string.clear_pantry_message)
                .setPositiveButton(R.string.clear_pantry, (dialog, which) -> clearPantry())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void clearPantry() {
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            AppDatabase.get(appContext).pantryDao().deleteAll();
            runOnUiThread(() -> Toast.makeText(this, R.string.pantry_cleared, Toast.LENGTH_SHORT).show());
        });
    }
}
