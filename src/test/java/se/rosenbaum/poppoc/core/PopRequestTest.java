package se.rosenbaum.poppoc.core;

import org.junit.Before;
import org.junit.Test;
import se.rosenbaum.poppoc.core.PopRequest;
import se.rosenbaum.poppoc.service.SimpleService;

import static org.junit.Assert.assertEquals;

public class PopRequestTest {
    PopRequest sut;

    @Before
    public void setup() {
        sut = new PopRequest(1L, new SimpleService());
    }

    @Test
    public void testPopURIEncode() throws Exception {
        test("", "");
        test(" ", " ");
        test("a", "a");
        test("/", "/");
        test("%3D", "=");
        test("%26", "&");
        test("+", "+");
        test("%25", "%");
        test("%E1%82%A0", "Ⴀ");
        test("%C3%85", "Å");
        test("\\", "\\");
        test("%23", "#");
        test("%F0%90%8E%81", "\uD800\uDF81");
        test("ab%C3%85%C3%B6%3D%3D%E1%82%A0%25%25\\", "abÅö==Ⴀ%%\\");
        test(null, null);
    }

    private void test(String expected, String input) {
        assertEquals(expected, sut.popURIEncode(input));
    }
}
