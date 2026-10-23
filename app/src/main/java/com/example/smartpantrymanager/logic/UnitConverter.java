package com.example.smartpantrymanager.logic;

import java.util.Locale;

/**
 * Converts quantities to a base unit so that "1 kg" and "1000 g" compare equal.
 * Mass is stored in grams, volume in millilitres and counts as plain numbers.
 */
public final class UnitConverter {

    public enum Dimension {MASS, VOLUME, COUNT}

    private UnitConverter() {
    }

    /** Which kind of thing the unit measures. Unknown or empty units count as COUNT. */
    public static Dimension dimension(String unit) {
        switch (canonical(unit)) {
            case "g":
            case "kg":
            case "oz":
            case "lb":
                return Dimension.MASS;
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return Dimension.VOLUME;
            default:
                return Dimension.COUNT;
        }
    }

    /** Converts a quantity to the base unit of its dimension (g, ml or count). */
    public static double toBase(double quantity, String unit) {
        switch (canonical(unit)) {
            case "kg":
                return quantity * 1000.0;
            case "oz":
                return quantity * 28.3495;
            case "lb":
                return quantity * 453.592;
            case "l":
                return quantity * 1000.0;
            case "tsp":
                return quantity * 5.0;
            case "tbsp":
                return quantity * 15.0;
            case "cup":
                return quantity * 240.0;
            default:
                return quantity; // g, ml and counts are already in base units
        }
    }

    /** Maps spelling variants ("grams", "Litres", "pieces") to one short code. */
    static String canonical(String unit) {
        if (unit == null) {
            return "pcs";
        }
        String u = unit.toLowerCase(Locale.ROOT).trim();
        if (u.endsWith(".")) {
            u = u.substring(0, u.length() - 1);
        }
        switch (u) {
            case "g": case "gm": case "gram": case "grams":
                return "g";
            case "kg": case "kilo": case "kilos": case "kilogram": case "kilograms":
                return "kg";
            case "oz": case "ounce": case "ounces":
                return "oz";
            case "lb": case "lbs": case "pound": case "pounds":
                return "lb";
            case "ml": case "millilitre": case "millilitres": case "milliliter": case "milliliters":
                return "ml";
            case "l": case "litre": case "litres": case "liter": case "liters":
                return "l";
            case "tsp": case "teaspoon": case "teaspoons":
                return "tsp";
            case "tbsp": case "tablespoon": case "tablespoons":
                return "tbsp";
            case "cup": case "cups":
                return "cup";
            default:
                return "pcs"; // pcs, piece(s), unit(s), whole, each, empty...
        }
    }
}
