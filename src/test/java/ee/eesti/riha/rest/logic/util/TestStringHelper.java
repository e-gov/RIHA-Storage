package ee.eesti.riha.rest.logic.util;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

public class TestStringHelper {

  @Test
  public void testStringOfArrayToString() {
    String input = "[linnaleht, oluline, proovtag]";
    String expected = "[\"linnaleht\",\"oluline\",\"proovtag\"]";

    assertEquals(expected, StringHelper.escapeStringsInArray(input));
  }

  @Test
  public void testStringOfArrayToStringEscaped() {
    String input = "[\"linnaleht\", \"oluline\", \"proovtag\"]";
    String expected = "[\"linnaleht\",\"oluline\",\"proovtag\"]";

    assertEquals(expected, StringHelper.escapeStringsInArray(input));
  }

  @Test
  public void testStringOfArrayToStringEscapedNoSpaces() {
    String input = "[\"linnaleht\",\"oluline\",\"proovtag\"]";
    String expected = "[\"linnaleht\",\"oluline\",\"proovtag\"]";

    assertEquals(expected, StringHelper.escapeStringsInArray(input));
  }

  @Test
  public void testStringOfArrayToStringNoSpaces() {
    String input = "[linnaleht,oluline,proovtag]";
    String expected = "[\"linnaleht\",\"oluline\",\"proovtag\"]";

    assertEquals(expected, StringHelper.escapeStringsInArray(input));
  }

  @Test
  public void testStringOfArrayToStringSingle() {
    String input = "[linnaleht]";
    String expected = "[\"linnaleht\"]";

    assertEquals(expected, StringHelper.escapeStringsInArray(input));
  }

  @Test
  public void testStringOfArrayToStringNoBrackets() {
    String input = "linnaleht, oluline";
    String expected = "Value must be json array: " + input;
    IllegalArgumentException iae = null;
    try {
      StringHelper.escapeStringsInArray(input);
    } catch (IllegalArgumentException e) {
      iae = e;
    }
    assertEquals(expected, iae.getMessage());
  }

}
