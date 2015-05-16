package se.rosenbaum.poppoc.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PopEncodeDecodeTest {

    @Test
    public void testPopURIEncode() throws Exception {
        testEnc("", "");
        testEnc(" ", " ");
        testEnc("a", "a");
        testEnc("/", "/");
        testEnc("%3D", "=");
        testEnc("%26", "&");
        testEnc("+", "+");
        testEnc("%25", "%");
        testEnc("%E1%82%A0", "Ⴀ");
        testEnc("%C3%85", "Å");
        testEnc("\\", "\\");
        testEnc("%23", "#");
        testEnc("%F0%90%8E%81", "\uD800\uDF81");
        testEnc("ab%C3%85%C3%B6%3D%3D%E1%82%A0%25%25\\", "abÅö==Ⴀ%%\\");
        testEnc(null, null);
    }

    @Test
    public void testPopURIDecode() throws Exception {
        testDec("", "");
        testDec(" ", " ");
        testDec("a", "a");
        testDec("/", "/");
        testDec("%3D", "=");
        testDec("%26", "&");
        testDec("+", "+");
        testDec("%25", "%");
        testDec("%E1%82%A0", "Ⴀ");
        testDec("%C3%85", "Å");
        testDec("\\", "\\");
        testDec("%25a", "%a");
        testDec("%23", "#");
        testDec("%F0%90%8E%81", "\uD800\uDF81");
        testDec("ab%C3%85%C3%B6%3D%3D%E1%82%A0%25%25\\", "abÅö==Ⴀ%%\\");
        testDec(null, null);
    }

    private void testEnc(String expected, String input) {
        assertEquals(expected, PopEncodeDecode.popURIEncode(input));
    }

    private void testDec(String input, String expected) {
        assertEquals(expected, PopEncodeDecode.popURIDecode(input));
    }
}
