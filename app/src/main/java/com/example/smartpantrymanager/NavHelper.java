package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires up the bottom navigation bar shared by the three main screens
 * (Pantry, Recipes, Settings) using Intents.
 *
 * MainActivity is the "root" screen. When you leave Recipes or Settings for
 * another tab they close themselves, so the Back button always returns to the
 * pantry instead of piling up copies of the same screen.
 */
public final class NavHelper {

    private NavHelper() {
    }

    public static void setup(final Activity activity, BottomNavigationView nav, final int selectedId) {
        nav.setSelectedItemId(selectedId); // set before the listener so it does not fire
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true;
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);

            if (!(activity instanceof MainActivity)) {
                activity.finish();
            }
            return true;
        });
    }
}
