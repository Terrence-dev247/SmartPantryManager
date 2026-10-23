# Smart Pantry Manager

An Android app (written in **Java**) that helps reduce food waste. You tell it what
ingredients you have at home, and it suggests recipes you can cook using **only**
those ingredients - no shopping trip needed.

**Module:** Mobile App Development 700 - Practical Assignment, Richfield Graduate Institute of Technology.

## The core rule: strict matching

A recipe is only suggested when **every** ingredient it needs is in the pantry, in at
least the required quantity. If a recipe needs 5 ingredients and the pantry has 4,
it is not shown. The logic lives in `logic/RecipeMatcher.java` and is covered by unit tests.

To cope with real-world messiness the matcher:

- ignores case and extra spaces, and treats singular/plural as the same (`tomato` = `Tomatoes`),
- ignores filler words (`fresh`, `tinned`...) and maps common synonyms (`spaghetti` = `pasta`, `olive oil` = `oil`),
- converts units (`1 kg` satisfies `150 g`, `0.5 l` satisfies `200 ml`),
- adds up several pantry entries of the same ingredient,
- never matches different kinds of unit (grams vs pieces).

## Features

| Screen | What it does |
| --- | --- |
| **Pantry list** (home) | Shows all ingredients in a RecyclerView; tap to edit, bin icon to delete, **+** to add; warns about items expiring within 3 days |
| **Add / Edit ingredient** | Name, quantity, unit and optional expiry date, with input validation; delete from here too |
| **Suggested recipes** | Only recipes you can make right now; friendly message when nothing matches |
| **Recipe detail** | Full ingredient list (marked *in your pantry* / *missing*) and the method |
| **Settings** | Expiring-soon alerts on/off, optional "Almost There" section on/off, clear pantry |

20 starter recipes are stored in the database and added automatically the first time the app runs.

Navigation uses a bottom navigation bar and Intents (with extras) between Activities.
Dark mode is supported.

## Database choice: SQLite with Room

I chose **SQLite through the Room persistence library**.

- It is stored locally on the phone, so the app works offline and needs no server or account.
- Room checks the SQL queries when the app is compiled, which catches mistakes early.
- It maps Java objects (`PantryItem`, `Recipe`, `RecipeIngredient`) to tables, so the code stays short and readable.
- It matches the persistent-data chapter of the module.

Full CRUD is supported on pantry items, and the data is still there after closing and reopening the app.

### Data model

```
pantry_items        recipes              recipe_ingredients
------------        -------              ------------------
id (PK)             id (PK)              id (PK)
name                name                 recipeId (FK -> recipes.id, cascade delete)
quantity            steps                name
unit                                     quantity
expiryDate (null)                        unit
```

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
  MainActivity.java            pantry list (home)
  EditIngredientActivity.java  add / edit / delete one ingredient
  SuggestedActivity.java       recipes you can cook now
  RecipeDetailActivity.java    one recipe
  SettingsActivity.java        settings
  PantryAdapter.java           RecyclerView adapter for pantry items
  RecipeAdapter.java           RecyclerView adapter for recipes (headers + cards)
  NavHelper.java               bottom navigation using Intents
  Prefs.java                   SharedPreferences for the settings
  data/                        Room: entities, DAOs, database, recipe seeder, background executor
  logic/                       RecipeMatcher, NameNormalizer, UnitConverter, Format (plain Java)
app/src/test/                  JUnit tests for the matching logic and the seeded recipes
```

## Setup and run

Requirements: Android Studio, an emulator or an Android phone (Android 7.0 / API 24 or newer).

1. Clone the repository: `git clone https://github.com/Terrence-dev247/SmartPantryManager.git`
2. Open the folder in Android Studio and wait for the Gradle sync to finish.
3. Choose a device (emulator, or a phone with USB debugging turned on) and press **Run**.
4. Run the tests with **right-click `app/src/test` -> Run Tests**.

## How to try the strict matching

1. Open **Pantry** and add `Bread` (4 pcs) - then open **Recipes**: nothing matches yet.
2. Add `Cheese` (200 g) and `Butter` (100 g) - **Cheese Toastie** appears.
3. Delete `Cheese` - **Cheese Toastie** disappears again.
