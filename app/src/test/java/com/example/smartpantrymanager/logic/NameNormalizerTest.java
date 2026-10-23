package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameNormalizerTest {

    @Test
    public void pluralsBecomeSingular() {
        assertEquals("tomato", NameNormalizer.key("Tomatoes"));
        assertEquals("egg", NameNormalizer.key("eggs"));
        assertEquals("berry", NameNormalizer.key("berries"));
        assertEquals("sandwich", NameNormalizer.key("sandwiches"));
        assertEquals("noodle", NameNormalizer.key("noodles"));
    }

    @Test
    public void wordsThatEndInSAreKept() {
        assertEquals("hummus", NameNormalizer.key("hummus"));
        assertEquals("couscous", NameNormalizer.key("couscous"));
    }

    @Test
    public void spacingAndCaseAreIgnored() {
        assertEquals("peanut butter", NameNormalizer.key("  Peanut   BUTTER "));
    }

    @Test
    public void onlyTheLastWordIsMadeSingular() {
        assertEquals("peanut butter", NameNormalizer.key("peanut butters"));
    }

    @Test
    public void cookieAndCookiesAreTheSame() {
        assertEquals(NameNormalizer.key("cookie"), NameNormalizer.key("cookies"));
    }

    @Test
    public void emptyOrNullGivesEmptyKey() {
        assertEquals("", NameNormalizer.key(null));
        assertEquals("", NameNormalizer.key("   "));
    }
}
