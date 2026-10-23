package com.example.smartpantrymanager.data;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills the recipes table with 20 starter recipes the first time the app runs.
 * Ingredient names are stored lowercase and singular so the strict-matching
 * logic can compare them reliably. Quantities use base units: g, ml or pcs.
 */
public class RecipeSeeder {

    /** Call on a background thread. Does nothing if recipes already exist. */
    public static void seedIfEmpty(Context context) {
        final AppDatabase db = AppDatabase.get(context);
        final RecipeDao dao = db.recipeDao();
        if (dao.count() > 0) {
            return;
        }
        db.runInTransaction(new Runnable() {
            @Override
            public void run() {
                insertAll(dao);
            }
        });
    }

    /** Writes the starter recipes through the given DAO (public so it can be unit-tested). */
    public static void insertAll(RecipeDao dao) {
        add(dao, "Scrambled Eggs on Toast",
                "1. Whisk the eggs with a pinch of salt.\n2. Melt the butter in a pan on low heat.\n3. Add the eggs and stir gently until just set.\n4. Toast the bread and serve the eggs on top.",
                ing("egg", 2, "pcs"), ing("bread", 2, "pcs"), ing("butter", 10, "g"), ing("salt", 1, "g"));

        add(dao, "Tomato Pasta",
                "1. Boil the pasta in salted water until soft, then drain.\n2. Fry chopped onion and garlic in olive oil.\n3. Add chopped tomatoes and simmer for 10 minutes.\n4. Mix in the pasta and serve.",
                ing("pasta", 100, "g"), ing("tomato", 3, "pcs"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("olive oil", 15, "ml"), ing("salt", 2, "g"));

        add(dao, "Cheese Omelette",
                "1. Beat the eggs with salt.\n2. Melt the butter in a pan.\n3. Pour in the eggs and cook until almost set.\n4. Add grated cheese, fold over and serve.",
                ing("egg", 3, "pcs"), ing("cheese", 30, "g"), ing("butter", 10, "g"), ing("salt", 1, "g"));

        add(dao, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry chopped onion and carrot in oil.\n3. Push to the side, scramble the eggs in the pan.\n4. Add the rice and soy sauce and stir-fry for 5 minutes.",
                ing("rice", 150, "g"), ing("egg", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("carrot", 1, "pcs"), ing("soy sauce", 15, "ml"), ing("oil", 15, "ml"));

        add(dao, "Pancakes",
                "1. Mix the flour, sugar, egg and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in a ladle of batter and cook 2 minutes per side.\n4. Repeat with the rest of the batter.",
                ing("flour", 150, "g"), ing("milk", 200, "ml"), ing("egg", 1, "pcs"),
                ing("sugar", 20, "g"), ing("butter", 10, "g"));

        add(dao, "Cheese Toastie",
                "1. Butter the outside of both bread slices.\n2. Put the cheese between them.\n3. Fry in a pan for 2 minutes per side until golden.",
                ing("bread", 2, "pcs"), ing("cheese", 40, "g"), ing("butter", 10, "g"));

        add(dao, "Chicken Stir-Fry",
                "1. Slice the chicken, onion and carrot.\n2. Fry the chicken in hot oil until cooked through.\n3. Add the vegetables and stir-fry for 5 minutes.\n4. Add soy sauce and cook for 1 more minute.",
                ing("chicken", 200, "g"), ing("onion", 1, "pcs"), ing("carrot", 1, "pcs"),
                ing("soy sauce", 20, "ml"), ing("oil", 15, "ml"));

        add(dao, "Mashed Potato",
                "1. Peel and cut the potatoes, then boil until soft.\n2. Drain and mash with butter and warm milk.\n3. Season with salt and serve.",
                ing("potato", 4, "pcs"), ing("butter", 20, "g"), ing("milk", 50, "ml"), ing("salt", 2, "g"));

        add(dao, "Vegetable Soup",
                "1. Chop the potatoes, carrots and onion.\n2. Fry the onion in oil for 3 minutes.\n3. Add the other vegetables, a stock cube and 500 ml of water.\n4. Simmer for 20 minutes and season with salt.",
                ing("potato", 2, "pcs"), ing("carrot", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("stock cube", 1, "pcs"), ing("salt", 2, "g"), ing("oil", 10, "ml"));

        add(dao, "Tuna Pasta",
                "1. Boil and drain the pasta.\n2. Fry chopped onion in olive oil.\n3. Add the drained tuna and warm through.\n4. Toss with the pasta and serve.",
                ing("pasta", 100, "g"), ing("tuna", 1, "pcs"), ing("onion", 1, "pcs"), ing("olive oil", 15, "ml"));

        add(dao, "Egg Noodles",
                "1. Boil the noodles for 3 minutes and drain.\n2. Scramble the egg in hot oil.\n3. Add the noodles and soy sauce and toss for 2 minutes.",
                ing("noodle", 100, "g"), ing("egg", 1, "pcs"), ing("soy sauce", 15, "ml"), ing("oil", 10, "ml"));

        add(dao, "Pap with Tomato Gravy",
                "1. Boil water, stir in the maize meal and cook on low heat for 20 minutes.\n2. Fry the onion in oil, add chopped tomatoes and salt.\n3. Simmer the gravy for 10 minutes.\n4. Serve the pap with the gravy.",
                ing("maize meal", 250, "g"), ing("tomato", 3, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 15, "ml"), ing("salt", 2, "g"));

        add(dao, "Banana Pancakes",
                "1. Mash the bananas in a bowl.\n2. Mix in the egg and flour to make a batter.\n3. Fry spoonfuls in a pan for 2 minutes per side.",
                ing("banana", 2, "pcs"), ing("egg", 1, "pcs"), ing("flour", 50, "g"));

        add(dao, "Garlic Bread",
                "1. Mix softened butter with crushed garlic.\n2. Spread on the bread slices.\n3. Bake at 200 C for 8 to 10 minutes until golden.",
                ing("bread", 4, "pcs"), ing("butter", 40, "g"), ing("garlic", 2, "pcs"));

        add(dao, "Baked Beans on Toast",
                "1. Toast and butter the bread.\n2. Heat the baked beans in a small pot.\n3. Pour the beans over the toast.",
                ing("baked beans", 1, "pcs"), ing("bread", 2, "pcs"), ing("butter", 10, "g"));

        add(dao, "Rice and Beans",
                "1. Cook the rice.\n2. Fry chopped onion in oil, add the beans and salt.\n3. Simmer for 10 minutes.\n4. Serve the beans over the rice.",
                ing("rice", 150, "g"), ing("bean", 200, "g"), ing("onion", 1, "pcs"),
                ing("oil", 15, "ml"), ing("salt", 2, "g"));

        add(dao, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Top with sliced banana.",
                ing("bread", 2, "pcs"), ing("peanut butter", 30, "g"), ing("banana", 1, "pcs"));

        add(dao, "Chicken and Rice",
                "1. Fry the chicken pieces in oil until browned.\n2. Add chopped onion and garlic and cook for 3 minutes.\n3. Add the rice and 300 ml of water, with salt.\n4. Cover and simmer for 20 minutes until the rice is cooked.",
                ing("chicken", 200, "g"), ing("rice", 150, "g"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("oil", 15, "ml"), ing("salt", 2, "g"));

        add(dao, "Potato Fritters",
                "1. Grate the potatoes and squeeze out the water.\n2. Mix with the egg and flour.\n3. Fry spoonfuls in hot oil until golden on both sides.",
                ing("potato", 3, "pcs"), ing("egg", 1, "pcs"), ing("flour", 30, "g"), ing("oil", 30, "ml"));

        add(dao, "Creamy Cheese Pasta",
                "1. Boil and drain the pasta.\n2. Melt the butter, add the milk and cheese.\n3. Stir until the sauce is smooth.\n4. Mix in the pasta and serve.",
                ing("pasta", 100, "g"), ing("cheese", 50, "g"), ing("milk", 100, "ml"), ing("butter", 15, "g"));
    }

    private static void add(RecipeDao dao, String name, String steps, RecipeIngredient... items) {
        Recipe recipe = new Recipe();
        recipe.name = name;
        recipe.steps = steps;
        long recipeId = dao.insertRecipe(recipe);

        List<RecipeIngredient> list = new ArrayList<>();
        for (RecipeIngredient item : items) {
            item.recipeId = (int) recipeId;
            list.add(item);
        }
        dao.insertIngredients(list);
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        RecipeIngredient item = new RecipeIngredient();
        item.name = name;
        item.quantity = quantity;
        item.unit = unit;
        return item;
    }
}
