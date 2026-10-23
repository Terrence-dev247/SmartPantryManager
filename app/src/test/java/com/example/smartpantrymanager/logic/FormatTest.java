package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;

public class FormatTest {

    @Test
    public void quantitiesAreTidied() {
        assertEquals("2", Format.qty(2.0));
        assertEquals("0.5", Format.qty(0.5));
        assertEquals("1.25", Format.qty(1.25));
    }

    @Test
    public void daysUntilCountsWholeDays() {
        Calendar now = Calendar.getInstance();
        Calendar later = Calendar.getInstance();
        later.add(Calendar.DAY_OF_YEAR, 3);
        Calendar earlier = Calendar.getInstance();
        earlier.add(Calendar.DAY_OF_YEAR, -2);

        assertEquals(0, Format.daysUntil(now.getTimeInMillis(), now.getTimeInMillis()));
        assertEquals(3, Format.daysUntil(later.getTimeInMillis(), now.getTimeInMillis()));
        assertEquals(-2, Format.daysUntil(earlier.getTimeInMillis(), now.getTimeInMillis()));
    }
}
