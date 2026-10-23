package com.example.smartpantrymanager;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.DbExecutor;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.Recipe;
import com.example.smartpantrymanager.data.RecipeIngredient;
import com.example.smartpantrymanager.logic.Format;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Collections;
import java.util.List;

/** Shows one recipe: every ingredient (marked as in your pantry or missing) and the method. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private MaterialToolbar toolbar;
    private TextView statusText;
    private TextView stepsText;
    private LinearLayout ingredientContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_recipe);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationContentDescription(R.string.back);
        toolbar.setNavigationOnClickListener(v -> finish());

        statusText = findViewById(R.id.statusText);
        stepsText = findViewById(R.id.stepsText);
        ingredientContainer = findViewById(R.id.ingredientContainer);

        final int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            AppDatabase db = AppDatabase.get(appContext);
            final Recipe recipe = db.recipeDao().getById(recipeId);
            final List<RecipeIngredient> ingredients = db.recipeDao().getIngredients(recipeId);
            final List<PantryItem> pantry = db.pantryDao().getAll();
            runOnUiThread(() -> show(recipe, ingredients, pantry));
        });
    }

    private void show(Recipe recipe, List<RecipeIngredient> ingredients, List<PantryItem> pantry) {
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setTitle(recipe.name);
        stepsText.setText(recipe.steps);

        boolean canCook = RecipeMatcher.evaluate(ingredients, pantry).isComplete();
        statusText.setText(canCook ? R.string.can_cook_now : R.string.cannot_cook_now);

        ingredientContainer.removeAllViews();
        for (RecipeIngredient ingredient : ingredients) {
            // Check this one ingredient on its own against the pantry.
            boolean have = RecipeMatcher.evaluate(Collections.singletonList(ingredient), pantry).isComplete();
            addIngredientLine(ingredient, have);
        }
    }

    private void addIngredientLine(RecipeIngredient ingredient, boolean have) {
        String mark = have ? "\u2713 " : "\u2717 ";
        String note = getString(have ? R.string.have_it : R.string.missing_it);
        String text = mark + Format.capitalise(ingredient.name) + " - "
                + Format.qty(ingredient.quantity) + " " + ingredient.unit + " (" + note + ")";

        TextView line = new TextView(this);
        line.setText(text);
        line.setTextSize(16);
        line.setPadding(0, 6, 0, 6);
        line.setTextColor(ContextCompat.getColor(this, have ? R.color.green_700 : R.color.red_700));
        line.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ingredientContainer.addView(line);
    }
}
