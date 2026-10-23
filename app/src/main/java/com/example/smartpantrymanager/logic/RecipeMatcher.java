package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE (the core logic of the app).
 *
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. One missing ingredient, or not enough of
 * one, means the recipe is NOT suggested.
 *
 * To cope with real-world messiness the matcher:
 *  - compares ingredient names by a normalised key (tomato = Tomatoes),
 *  - converts units to a base unit (1 kg satisfies 150 g),
 *  - adds up several pantry entries of the same ingredient.
 */
public final class RecipeMatcher {

    /** The outcome of checking one recipe against the pantry. */
    public static final class Result {
        private final boolean hasIngredients;
        private final List<RecipeIngredient> missing;

        Result(boolean hasIngredients, List<RecipeIngredient> missing) {
            this.hasIngredients = hasIngredients;
            this.missing = missing;
        }

        /** True only when the recipe has ingredients and none are missing. */
        public boolean isComplete() {
            return hasIngredients && missing.isEmpty();
        }

        /** The ingredients the user does not have (or does not have enough of). */
        public List<RecipeIngredient> getMissing() {
            return missing;
        }
    }

    private RecipeMatcher() {
    }

    public static Result evaluate(List<RecipeIngredient> needed, List<PantryItem> pantry) {
        Map<String, Double> available = totalsByKey(pantry);
        List<RecipeIngredient> missing = new ArrayList<>();

        for (RecipeIngredient ingredient : needed) {
            double required = UnitConverter.toBase(ingredient.quantity, ingredient.unit);
            Double have = available.get(mapKey(ingredient.name, ingredient.unit));
            if (have == null || have + 1e-9 < required) {
                missing.add(ingredient);
            }
        }
        return new Result(!needed.isEmpty(), missing);
    }

    /** Adds up the pantry by ingredient and dimension, in base units. */
    private static Map<String, Double> totalsByKey(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = mapKey(item.name, item.unit);
            double base = UnitConverter.toBase(item.quantity, item.unit);
            Double current = totals.get(key);
            totals.put(key, (current == null ? 0.0 : current) + base);
        }
        return totals;
    }

    /** Same ingredient only counts as a match if the unit type (mass/volume/count) also matches. */
    private static String mapKey(String name, String unit) {
        return NameNormalizer.key(name) + "|" + UnitConverter.dimension(unit);
    }
}
