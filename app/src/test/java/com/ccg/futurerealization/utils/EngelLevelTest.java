package com.ccg.futurerealization.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class EngelLevelTest {
    @Test public void classifiesExactBoundariesWithoutRounding() {
        assertEquals("贫穷", EngelLevel.label(6000, 10000));
        assertEquals("温饱", EngelLevel.label(5999, 10000));
        assertEquals("温饱", EngelLevel.label(5000, 10000));
        assertEquals("小康", EngelLevel.label(4999, 10000));
        assertEquals("小康", EngelLevel.label(4000, 10000));
        assertEquals("富裕", EngelLevel.label(3999, 10000));
        assertEquals("富裕", EngelLevel.label(3000, 10000));
        assertEquals("最富裕", EngelLevel.label(2999, 10000));
        assertEquals("最富裕", EngelLevel.label(0, 10000));
        assertEquals("贫穷", EngelLevel.label(10000, 10000));
    }
    @Test public void emptyMonthHasNoClassification() {
        assertEquals("暂无支出", EngelLevel.label(0, 0));
    }
}
