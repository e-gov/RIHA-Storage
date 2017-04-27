package ee.eesti.riha.rest.logic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;

// TODO: Auto-generated Javadoc
/**
 * The Class StringHelper.
 */
public final class StringHelper {

  private StringHelper() {

  }

  /**
   * Are equal.
   *
   * @param a the a
   * @param b the b
   * @return true, if successful
   */
  public static boolean areEqual(String a, String b) {
    if (a == null || b == null) {
      return false;
    }
    return a.toLowerCase().equals(b.toLowerCase());
  }

  /**
   * Contains.
   *
   * @param searchKey the search key
   * @param data the data
   * @return true, if successful
   */
  public static boolean contains(String searchKey, Collection<String> data) {
    boolean contains = false;
    for (String s : data) {
      if (areEqual(s, searchKey)) {
        contains = true;
      }
    }
    return contains;
  }

  /**
   * Contains.
   *
   * @param searchKey the search key
   * @param data the data
   * @return true, if successful
   */
  public static boolean contains(String searchKey, String[] data) {
    return contains(searchKey, Arrays.asList(data));
  }

  /**
   * To lower.
   *
   * @param s the s
   * @return the string
   */
  public static String toLower(String s) {
    if (s != null) {
      return s.toLowerCase();
    }
    return s;
  }

  /**
   * Convert to string.
   *
   * @param values the values
   * @return the string[]
   */
  public static String[] convertToString(Object[] values) {
    String[] array = new String[values.length];
    for (int i = 0; i < array.length; i++) {
      array[i] = "" + values[i];
    }
    return array;
  }

  /**
   * Convert to string.
   *
   * @param <T> the generic type
   * @param values the values
   * @return the list
   */
  public static <T> List<String> convertToString(List<T> values) {
    List<String> strings = new ArrayList<>();
    for (int i = 0; i < values.size(); i++) {
      strings.add("" + values.get(i));
    }
    return strings;
  }

  /**
   * Checks if is number.
   *
   * @param s the s
   * @return true, if is number
   */
  public static boolean isNumber(String s) {
    try {
      Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   * String of array to string array.
   *
   * @param stringOfArray the string of array
   * @return the string[]
   */
  // "[dfg]" -> ["dfg"]
  public static String[] stringOfArrayToStringArray(String stringOfArray) {
    String[] result = null;
    if (stringOfArray != null) {
      String withBrackets = stringOfArray.trim();
      String withOutBrackets = null;
      if (withBrackets.length() > 1 && withBrackets.charAt(0) == '['
          && withBrackets.charAt(withBrackets.length() - 1) == ']') {
        withOutBrackets = withBrackets.substring(1, withBrackets.length() - 1);
      } else {
        throw new IllegalArgumentException("Value must be json array: " + stringOfArray);
      }

      String[] splitted = withOutBrackets.split(", ");
      if (splitted.length == 1) {
        splitted = withOutBrackets.split(",");
      }
      // remove escaped quotes if exist
      result = new String[splitted.length];
      for (int i = 0; i < splitted.length; i++) {
        result[i] = removeSurroundingChar(splitted[i], '\"');
      }

    }
    return result;
  }

  /**
   * Escape strings in array.
   *
   * @param stringOfArray the string of array
   * @return the string
   */
  // "[dfg,asd]" -> [\"dfg\",\"asd\"]
  public static String escapeStringsInArray(String stringOfArray) {
    String[] array = stringOfArrayToStringArray(stringOfArray);
    JsonArray jsonArray = (JsonArray) JsonHelper.GSON.fromJson(JsonHelper.GSON.toJson(array), JsonArray.class);
    return jsonArray.toString();
  }

  /**
   * Removes the surrounding char.
   *
   * @param original the original
   * @param c the c
   * @return the string
   */
  public static String removeSurroundingChar(String original, char c) {
    return removeFirstAndLastChar(original, c, c);
  }

  /**
   * Removes the first and last char.
   *
   * @param original the original
   * @param first the first
   * @param last the last
   * @return the string
   */
  public static String removeFirstAndLastChar(String original, char first, char last) {
    if (original.length() > 1 && original.charAt(0) == first && original.charAt(original.length() - 1) == last) {
      return original.substring(1, original.length() - 1);
    }
    return original;
  }

  /**
   * Multiple equals.
   *
   * @param valueToCompare the value to compare
   * @param values the values
   * @return true, if successful
   */
  public static boolean multipleEquals(String valueToCompare, String... values) {
    for (String s : values) {
      if (!valueToCompare.equals(s)) {
        return false;
      }
    }
    return true;
  }
}
