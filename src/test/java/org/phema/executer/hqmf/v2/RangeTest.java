package org.phema.executer.hqmf.v2;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 5/31/18.
 */
@RunWith(JUnitPlatform.class)
public class RangeTest {

    @Test
    void safeGetHighAsString_Null() {
        Range range = new Range("IPQ_VL", null, null, null);
        Assert.assertNull(range.safeGetHighAsString());
    }

    @Test
    void safeGetHighAsString_Empty() {
        Range range = new Range("IPQ_VL", null, "", null);
        String result = range.safeGetHighAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("", result);

        range = new Range("IPQ_VL", null, new Value("", "", ""), null);
        result = range.safeGetHighAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("", result);
    }

    @Test
    void safeGetHighAsString_Value() {
        Range range = new Range("IPQ_VL", null, "Sample Value", null);
        String result = range.safeGetHighAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("Sample Value", result);

        range = new Range("IPQ_VL", null, new Value("int", "100", ""), null);
        result = range.safeGetHighAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("100", result);
    }

    @Test
    void safeGetLowAsString_Null() {
        Range range = new Range("IPQ_VL", null, null, null);
        Assert.assertNull(range.safeGetLowAsString());
    }

    @Test
    void safeGetLowAsString_Empty() {
        Range range = new Range("IPQ_VL", "", null, null);
        String result = range.safeGetLowAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("", result);

        range = new Range("IPQ_VL", new Value("", "", ""), null, null);
        result = range.safeGetLowAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("", result);
    }

    @Test
    void safeGetLowAsString_Value() {
        Range range = new Range("IPQ_VL", "Sample Value", null, null);
        String result = range.safeGetLowAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("Sample Value", result);

        range = new Range("IPQ_VL", new Value("int", "100", ""), null, null);
        result = range.safeGetLowAsString();
        Assert.assertNotNull(result);
        Assert.assertEquals("100", result);
    }
}