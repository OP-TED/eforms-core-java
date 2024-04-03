package eu.europa.ted.eforms.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SdkVersionTest {
  @Test
  void testGetMajor() {
    assertEquals("1", new SdkVersion("1.2.3").getMajor());
  }

  @Test
  void testGetMinor() {
    assertEquals("2", new SdkVersion("1.2.3").getMinor());
  }

  @Test
  void testGetPatch() {
    assertEquals("3", new SdkVersion("1.2.3").getPatch());
  }

  @Test
  void testGetNextMajor() {
    assertEquals("2.2.3", new SdkVersion("1.2.3").getNextMajor());
  }

  @Test
  void testGetNextMinor() {
    assertEquals("1.3.3", new SdkVersion("1.2.3").getNextMinor());
  }

  @Test
  void testIsMajor() {
    // SdkVersion always has a minor version number, so isMajor can never be true
    assertFalse(new SdkVersion("1.0").isMajor());
    assertFalse(new SdkVersion("1.2").isMajor());
  }

  @Test
  void testIsMinor() {
    assertTrue(new SdkVersion("2.0").isMinor());

    assertFalse(new SdkVersion("1.2.3").isMinor());
  }

  @Test
  void testIsPatch() {
    assertEquals(false, new SdkVersion("1.2").isPatch());
    
    assertEquals(true, new SdkVersion("1.2.3").isPatch());
    assertEquals(true, new SdkVersion("1.2.3-rc.4").isPatch());
  }

  @Test
  void testToNormalisedStringWithPatch() {
    assertEquals("1.2.3", new SdkVersion("1.2.3").toNormalisedString(true));
    assertEquals("1.2.3", new SdkVersion("1.2.3-SNAPSHOT").toNormalisedString(true));
    assertEquals("1.2.3", new SdkVersion("1.2.3-rc.4").toNormalisedString(true));
  }

  @Test
  void testToStringWithoutPatch() {
    assertEquals("1.2", new SdkVersion("1.2.3").toStringWithoutPatch());
    assertEquals("1.2", new SdkVersion("1.2.3-SNAPSHOT").toStringWithoutPatch());
    assertEquals("1.2", new SdkVersion("1.2.3-rc.4").toStringWithoutPatch());
  }

  @Test
  void testCompare() {
    assert(new SdkVersion("1.2.3").compareTo(new SdkVersion("1.2.2")) > 0);
    assert(new SdkVersion("1.2.3").compareTo(new SdkVersion("1.2")) > 0);
    assert(new SdkVersion("1.2.3").compareTo(new SdkVersion("1.2.3-SNAPSHOT")) > 0);
    assert(new SdkVersion("1.2.3").compareTo(new SdkVersion("1.2.3-rc.3")) > 0);
    assert(new SdkVersion("2.0.0").compareTo(new SdkVersion("2.0.0-alpha.1")) > 0);
  }
}
