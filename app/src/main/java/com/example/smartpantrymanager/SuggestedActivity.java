package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.DbExecutor;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.Recipe;
import com.example.smartpantrymanager.data.RecipeIngredient;
import com.example.smartpantrymanager.logic.Format;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists only the recipes the user can cook right now, using the strict-matching
 * rule in RecipeMatcher. An optional, clearly separate "Almost There" section
 * (off by default, switch it on in Settings) lists recipes missing exactly one ingredient.
 */
public class SuggestedActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private RecipeAdapter adapter;
    private TextView noMatchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        noMatchView = findViewById(R.id.noMatchView);
        RecyclerView recycler = findViewById(R.id.recipeRecycler);
        adapter = new RecipeAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        final Context appContext = getApplicationContext();
        final boolean showAlmost = Prefs.isAlmostThereOn(this);

        DbExecutor.run(() -> {
            AppDatabase db = AppDatabase.get(appContext);
            List<Recipe> recipes = db.recipeDao().getAll();
            List<RecipeIngredient> allIngredients = db.recipeDao().getAllIngredients();
            List<PantryItem> pantry = db.pantryDao().getAll();

            // Group the ingredients by the recipe they belong to.
            Map<Integer, List<RecipeIngredient>> byRecipe = new HashMap<>();
            for (RecipeIngredient ingredient : allIngredients) {
                List<RecipeIngredient> list = byRecipe.get(ingredient.recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    byRecipe.put(ingredient.recipeId, list);
                }
                list.add(ingredient);
            }

            List<RecipeAdapter.Row> canCook = new ArrayList<>();
            List<RecipeAdapter.Row> almost = new ArrayList<>();

            for (Recipe recipe : recipes) {
                List<RecipeIngredient> needed = byRecipe.get(recipe.id);
                if (needed == null) {
                    needed = new ArrayList<>();
                }
                RecipeMatcher.Result result = RecipeMatcher.evaluate(needed, pantry);

                if (result.isComplete()) {
                    // STRICT: every single ingredient is available.
                    canCook.add(RecipeAdapter.Row.recipe(recipe.id, recipe.name,
                            appContext.getString(R.string.subtitle_complete, needed.size())));
                } else if (showAlmost && result.getMissing().size() == 1) {
                    RecipeIngredient gap = result.getMissing().get(0);
                    String what = Format.capitalise(gap.name) + " (" + Format.qty(gap.quantity) + " " + gap.unit + ")";
                    almost.add(RecipeAdapter.Row.recipe(recipe.id, recipe.name,
                            appContext.getString(R.string.subtitle_missing, what)));
                }
            }

            final List<RecipeAdapter.Row> rows = new ArrayList<>();
            if (!canCook.isEmpty()) {
                rows.add(RecipeAdapter.Row.header(appContext.getString(R.string.header_can_cook, canCook.size())));
                rows.addAll(canCook);
            }
            if (!almost.isEmpty()) {
                rows.add(RecipeAdapter.Row.header(appContext.getString(R.string.header_almost, almost.size())));
                rows.addAll(almost);
            }
            final boolean noStrictMatches = canCook.isEmpty();

            runOnUiThread(() -> {
                adapter.setRows(rows);
                // Friendly message instead of a blank screen when nothing matches.
                noMatchView.setVisibility(noStrictMatches ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    public void onRecipeClick(int recipeId) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        startActivity(intent);
    }
}
