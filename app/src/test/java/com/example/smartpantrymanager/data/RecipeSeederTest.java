package com.example.smartpantrymanager.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.logic.RecipeMatcher;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Checks the starter recipes against the strict-matching rule, using an in-memory fake DAO. */
public class RecipeSeederTest {

    /** Minimal in-memory stand-in for the Room DAO. */
    private static class FakeRecipeDao implements RecipeDao {
        final List<Recipe> recipes = new ArrayList<>();
        final List<RecipeIngredient> ingredients = new ArrayList<>();

        @Override
        public long insertRecipe(Recipe recipe) {
            recipe.id = recipes.size() + 1;
            recipes.add(recipe);
            return recipe.id;
        }

        @Override
        public void insertIngredients(List<RecipeIngredient> list) {
            ingredients.addAll(list);
        }

        @Override
        public List<Recipe> getAll() {
            return recipes;
        }

        @Override
        public Recipe getById(int id) {
            return recipes.get(id - 1);
        }

        @Override
        public List<RecipeIngredient> getIngredients(int recipeId) {
            List<RecipeIngredient> result = new ArrayList<>();
            for (RecipeIngredient item : ingredients) {
                if (item.recipeId == recipeId) {
                    result.add(item);
                }
            }
            return result;
        }

        @Override
        public List<RecipeIngredient> getAllIngredients() {
            return ingredients;
        }

        @Override
        public int count() {
            return recipes.size();
        }
    }

    private FakeRecipeDao dao;

    @Before
    public void seed() {
        dao = new FakeRecipeDao();
        RecipeSeeder.insertAll(dao);
    }

    @Test
    public void hasBetween15And20Recipes() {
        assertTrue(dao.recipes.size() >= 15 && dao.recipes.size() <= 20);
    }

    @Test
    public void everyRecipeIsWellFormed() {
        Set<String> names = new HashSet<>();
        for (Recipe recipe : dao.recipes) {
            assertTrue(names.add(recipe.name)); // no duplicate names
            assertFalse(recipe.steps.trim().isEmpty());
            List<RecipeIngredient> list = dao.getIngredients(recipe.id);
            assertFalse(list.isEmpty());
            for (RecipeIngredient item : list) {
                assertTrue(item.quantity > 0);
                assertTrue(item.unit.equals("g") || item.unit.equals("ml") || item.unit.equals("pcs"));
            }
        }
    }

    @Test
    public void aFullPantryUnlocksEveryRecipe() {
        List<PantryItem> pantry = new ArrayList<>();
        for (RecipeIngredient item : dao.ingredients) {
            PantryItem stock = new PantryItem();
            stock.name = item.name;
            stock.quantity = item.quantity * 10;
            stock.unit = item.unit;
            pantry.add(stock);
        }
        for (Recipe recipe : dao.recipes) {
            assertTrue(recipe.name, RecipeMatcher.evaluate(dao.getIngredients(recipe.id), pantry).isComplete());
        }
    }

    @Test
    public void takingOutOneIngredientRemovesThatRecipe() {
        for (Recipe recipe : dao.recipes) {
            List<RecipeIngredient> needed = dao.getIngredients(recipe.id);
            for (RecipeIngredient left : needed) {
                // Pantry holds everything this recipe needs EXCEPT `left`.
                List<PantryItem> pantry = new ArrayList<>();
                for (RecipeIngredient item : needed) {
                    if (item == left) {
                        continue;
                    }
                    PantryItem stock = new PantryItem();
                    stock.name = item.name;
                    stock.quantity = item.quantity;
                    stock.unit = item.unit;
                    pantry.add(stock);
                }
                // (Skip the odd case where another ingredient is the same thing under a different name.)
                RecipeMatcher.Result result = RecipeMatcher.evaluate(needed, pantry);
                assertFalse(recipe.name + " without " + left.name, result.isComplete());
                assertEquals(1, result.getMissing().size());
            }
        }
    }
}
