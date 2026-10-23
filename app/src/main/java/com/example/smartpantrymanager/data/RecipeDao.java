package com.example.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/** Data access for recipes and their ingredient lists. */
@Dao
public interface RecipeDao {
    @Insert
    long insertRecipe(Recipe recipe);

    @Insert
    void insertIngredients(List<RecipeIngredient> list);

    @Query("SELECT * FROM recipes ORDER BY name")
    List<Recipe> getAll();

    @Query("SELECT * FROM recipes WHERE id = :id")
    Recipe getById(int id);

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    List<RecipeIngredient> getIngredients(int recipeId);

    @Query("SELECT * FROM recipe_ingredients")
    List<RecipeIngredient> getAllIngredients();

    @Query("SELECT COUNT(*) FROM recipes")
    int count();
}
