package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteSizeTest {
    @Test
    public void testByteSizeParsing() {
      
        assertEquals(1000L, new ByteSize("1KB").getBytes());
        assertEquals(1024L, new ByteSize("1KIB").getBytes());
        assertEquals(1500000L, new ByteSize("1.5MB").getBytes());
        assertEquals(1572864L, new ByteSize("1.5MIB").getBytes());
        assertEquals(500L, new ByteSize("500B").getBytes());
    }
    @Test
    public void testEdgeCases() {
    assertEquals(0L, new ByteSize("0B").getBytes());
    assertEquals(1125899906842624L, new ByteSize("1PB").getBytes());
}
    @Test(expected = IllegalArgumentException.class)
public void testNegativeValue() {
    new ByteSize("-1MB");
}
}
