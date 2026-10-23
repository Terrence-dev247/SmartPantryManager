package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Unit tests for the strict-matching rule (Section 2.3 of the brief). */
public class RecipeMatcherTest {

    private static PantryItem pantry(String name, double qty, String unit) {
        PantryItem item = new PantryItem();
        item.name = name;
        item.quantity = qty;
        item.unit = unit;
        return item;
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.name = name;
        ingredient.quantity = qty;
        ingredient.unit = unit;
        return ingredient;
    }

    private static List<RecipeIngredient> fiveIngredientRecipe() {
        return Arrays.asList(
                need("egg", 2, "pcs"), need("flour", 150, "g"), need("milk", 200, "ml"),
                need("sugar", 20, "g"), need("butter", 10, "g"));
    }

    @Test
    public void allFiveIngredientsPresent_isSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                pantry("egg", 6, "pcs"), pantry("flour", 1, "kg"), pantry("milk", 1, "l"),
                pantry("sugar", 500, "g"), pantry("butter", 250, "g"));
        assertTrue(RecipeMatcher.evaluate(fiveIngredientRecipe(), pantry).isComplete());
    }

    @Test
    public void fourOfFiveIngredients_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                pantry("egg", 6, "pcs"), pantry("flour", 1, "kg"), pantry("milk", 1, "l"),
                pantry("sugar", 500, "g")); // butter missing
        RecipeMatcher.Result result = RecipeMatcher.evaluate(fiveIngredientRecipe(), pantry);
        assertFalse(result.isComplete());
        assertEquals(1, result.getMissing().size());
        assertEquals("butter", result.getMissing().get(0).name);
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        List<RecipeIngredient> recipe = Arrays.asList(need("egg", 3, "pcs"));
        assertFalse(RecipeMatcher.evaluate(recipe, Arrays.asList(pantry("egg", 2, "pcs"))).isComplete());
        assertTrue(RecipeMatcher.evaluate(recipe, Arrays.asList(pantry("egg", 3, "pcs"))).isComplete());
    }

    @Test
    public void singularAndPluralNamesMatch() {
        List<RecipeIngredient> recipe = Arrays.asList(need("tomato", 3, "pcs"), need("potato", 2, "pcs"));
        List<PantryItem> pantry = Arrays.asList(pantry("Tomatoes", 4, "pcs"), pantry("POTATOES ", 2, "pcs"));
        assertTrue(RecipeMatcher.evaluate(recipe, pantry).isComplete());
    }

    @Test
    public void pluralInRecipeAndSingularInPantryMatch() {
        List<RecipeIngredient> recipe = Arrays.asList(need("eggs", 2, "pcs"));
        assertTrue(RecipeMatcher.evaluate(recipe, Arrays.asList(pantry("egg", 2, "pcs"))).isComplete());
    }

    @Test
    public void unitDifferencesAreConverted() {
        List<RecipeIngredient> recipe = Arrays.asList(need("flour", 150, "g"), need("milk", 200, "ml"));
        assertTrue(RecipeMatcher.evaluate(recipe,
                Arrays.asList(pantry("flour", 1, "kg"), pantry("milk", 0.5, "l"))).isComplete());
        assertFalse(RecipeMatcher.evaluate(recipe,
                Arrays.asList(pantry("flour", 0.1, "kg"), pantry("milk", 0.5, "l"))).isComplete());
    }

    @Test
    public void incompatibleUnitTypesDoNotMatch() {
        List<RecipeIngredient> recipe = Arrays.asList(need("cheese", 50, "g"));
        assertFalse(RecipeMatcher.evaluate(recipe, Arrays.asList(pantry("cheese", 2, "pcs"))).isComplete());
    }

    @Test
    public void duplicatePantryEntriesAreAddedUp() {
        List<RecipeIngredient> recipe = Arrays.asList(need("egg", 4, "pcs"));
        List<PantryItem> pantry = Arrays.asList(pantry("egg", 2, "pcs"), pantry("Eggs", 2, "pcs"));
        assertTrue(RecipeMatcher.evaluate(recipe, pantry).isComplete());
    }

    @Test
    public void synonymsAndFillerWordsMatch() {
        List<RecipeIngredient> recipe = Arrays.asList(
                need("oil", 15, "ml"), need("pasta", 100, "g"), need("tuna", 1, "pcs"));
        List<PantryItem> pantry = Arrays.asList(
                pantry("Olive Oil", 500, "ml"), pantry("spaghetti", 500, "g"), pantry("Tinned tuna", 2, "pcs"));
        assertTrue(RecipeMatcher.evaluate(recipe, pantry).isComplete());
    }

    @Test
    public void emptyPantry_nothingIsSuggested() {
        assertFalse(RecipeMatcher.evaluate(fiveIngredientRecipe(), new ArrayList<PantryItem>()).isComplete());
    }

    @Test
    public void recipeWithoutIngredients_isNeverSuggested() {
        assertFalse(RecipeMatcher.evaluate(new ArrayList<RecipeIngredient>(),
                Arrays.asList(pantry("egg", 2, "pcs"))).isComplete());
    }

    @Test
    public void removingAnIngredientRemovesTheRecipe() {
        List<RecipeIngredient> recipe = Arrays.asList(need("bread", 2, "pcs"), need("cheese", 40, "g"));
        List<PantryItem> both = Arrays.asList(pantry("bread", 4, "pcs"), pantry("cheese", 200, "g"));
        List<PantryItem> onlyBread = Arrays.asList(pantry("bread", 4, "pcs"));
        assertTrue(RecipeMatcher.evaluate(recipe, both).isComplete());
        assertFalse(RecipeMatcher.evaluate(recipe, onlyBread).isComplete());
    }
}
