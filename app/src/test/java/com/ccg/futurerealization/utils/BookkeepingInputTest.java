package com.ccg.futurerealization.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class BookkeepingInputTest {
    @Test public void parsesYuanExactlyAndKeepsDescription() {
        assertEquals(1, BookkeepingInput.parse("0.01").cents);
        assertEquals(2550, BookkeepingInput.parse("午餐25.50元").cents);
        assertEquals("午餐", BookkeepingInput.parse("午餐25.50元").description);
        assertEquals(1000, BookkeepingInput.parse(" ¥10 ").cents);
        assertEquals(Integer.MAX_VALUE, BookkeepingInput.parse("21474836.47").cents);
        assertEquals("42949672.94", BookkeepingInput.format(2L * Integer.MAX_VALUE));
    }

    @Test public void rejectsAmbiguousAndUnrepresentableAmounts() {
        for (String text : new String[]{"", "午餐", "0", "0.00", "-10", "−10", "+10", "10.001", "21474836.48", "9999999999999999999", "午餐25晚餐30", "1,000", ".50", "10.", "10+20", "1e3", "2026-11-17午餐25"}) {
            try { BookkeepingInput.parse(text); fail("Should reject " + text); }
            catch (IllegalArgumentException expected) { }
        }
    }
}
