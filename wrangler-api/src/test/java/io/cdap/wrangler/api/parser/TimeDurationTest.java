package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeDurationTest {
    @Test
    public void testTimeDurationParsing() {
        assertEquals(1000000L, new TimeDuration("1ms").getNanoseconds());
        assertEquals(1000000000L, new TimeDuration("1s").getNanoseconds());
        assertEquals(1500000L, new TimeDuration("1.5ms").getNanoseconds());
        assertEquals(60000000000L, new TimeDuration("1m").getNanoseconds());
        assertEquals(500L, new TimeDuration("500ns").getNanoseconds());
    }
  @Test
public void testEdgeCases() {
    assertEquals(0L, new TimeDuration("0ns").getNanoseconds());
    assertEquals(86400000000000L, new TimeDuration("1d").getNanoseconds());
}

@Test(expected = IllegalArgumentException.class)
public void testUnknownUnit() {
    new TimeDuration("1year");
}
}
