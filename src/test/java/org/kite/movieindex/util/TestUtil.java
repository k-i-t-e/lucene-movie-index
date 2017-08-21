package org.kite.movieindex.util;

import org.junit.Assert;

import java.util.List;

/**
 * Created by Mikhail_Miroliubov on 8/21/2017.
 */
public class TestUtil {
    public static void assertListEquals(List<?> expected, List<?> actual) {
        Assert.assertEquals(expected.size(), actual.size());
        int i = 0;
        for (Object e : expected) {
            Assert.assertEquals(e, actual.get(i));
            i++;
        }
    }
}
