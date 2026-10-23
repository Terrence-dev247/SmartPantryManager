package com.example.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns an ingredient name typed by a person into a comparison key, so that
 * "Tomatoes", "tomato " and "fresh tomato" are all treated as the same thing.
 *
 * Steps: lower-case and trim, drop filler words (fresh, tinned...), make the
 * last word singular, then map known synonyms (spaghetti -> pasta).
 * The key is only used for comparing; it is never shown to the user.
 */
public final class NameNormalizer {

    private static final Set<String> FILLER = new HashSet<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        String[] filler = {"fresh", "large", "small", "medium", "ripe", "chopped",
                "sliced", "diced", "tinned", "canned", "frozen", "raw"};
        for (String word : filler) {
            FILLER.add(word);
        }

        alias("oil", "olive oil", "vegetable oil", "sunflower oil", "cooking oil", "canola oil");
        alias("pasta", "spaghetti", "penne", "macaroni", "fusilli", "tagliatelle");
        alias("maize meal", "mealie meal", "mielie meal", "mealie-meal");
        alias("chicken", "chicken breast", "chicken fillet", "chicken thigh");
        alias("yogurt", "yoghurt");
        alias("chili", "chilli");
        alias("eggplant", "aubergine");
        alias("zucchini", "courgette");
        alias("bean", "kidney bean", "haricot bean");
        alias("noodle", "egg noodle", "instant noodle");
        alias("stock cube", "chicken stock cube", "vegetable stock cube", "beef stock cube");
    }

    private NameNormalizer() {
    }

    private static void alias(String canonical, String... variants) {
        for (String variant : variants) {
            ALIASES.put(variant, canonical);
        }
    }

    /** Returns the comparison key for an ingredient name (never null). */
    public static String key(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.toLowerCase(Locale.ROOT).trim().replaceAll("[^\\p{L}\\p{N} -]", " ");
        String[] words = cleaned.split("\\s+");

        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty() || FILLER.contains(word)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(word);
        }
        if (builder.length() == 0) {
            return "";
        }

        // Make only the last word singular: "olive oils" -> "olive oil".
        String phrase = builder.toString();
        int lastSpace = phrase.lastIndexOf(' ');
        String head = lastSpace >= 0 ? phrase.substring(0, lastSpace + 1) : "";
        String last = singular(phrase.substring(lastSpace + 1));
        phrase = head + last;

        String mapped = ALIASES.get(phrase);
        return mapped != null ? mapped : phrase;
    }

    /** Simple English plural -> singular rules. Good enough for pantry food names. */
    static String singular(String word) {
        int n = word.length();
        if (n <= 3) {
            return word;
        }
        String result;
        if (word.endsWith("ies") && n > 4) {
            result = word.substring(0, n - 3) + "y";           // berries -> berry
        } else if (word.endsWith("oes")) {
            result = word.substring(0, n - 2);                 // tomatoes -> tomato
        } else if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            result = word.substring(0, n - 2);                 // sandwiches -> sandwich
        } else if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            result = word;                                     // hummus, asparagus
        } else if (word.endsWith("s")) {
            result = word.substring(0, n - 1);                 // eggs -> egg
        } else {
            result = word;
        }
        // Make "cookie" and "cookies" (-> "cooky") produce the same key.
        if (result.length() > 3 && result.endsWith("ie")) {
            result = result.substring(0, result.length() - 2) + "y";
        }
        return result;
    }
}
