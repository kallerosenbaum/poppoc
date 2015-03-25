package se.rosenbaum.poppoc.servlet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PopServletTest {
    PopServlet sut;

    @Before
    public void setup() {
        sut = new PopServlet();
    }

    @Test
    public void testGetRequestId() throws Exception {
        assertEquals(0, sut.getRequestId("abra/Pop/0"));
        assertEquals(0, sut.getRequestId("abra/Pop/0/"));
        assertEquals(-1, sut.getRequestId("abra/Pop/0/Z"));
        assertEquals(-1, sut.getRequestId("abra/Pop/"));
        assertEquals(123, sut.getRequestId("abra/Pop/123"));
        assertEquals(123, sut.getRequestId("/Pop/123"));
        assertEquals(-1, sut.getRequestId("Pop/123"));
        assertEquals(-1, sut.getRequestId("Pop/123a"));
        assertEquals(-1, sut.getRequestId("Pop/123//"));
        assertEquals(-1, sut.getRequestId("Pop//"));
        assertEquals(-1, sut.getRequestId("Pop/00012/"));
    }
}
