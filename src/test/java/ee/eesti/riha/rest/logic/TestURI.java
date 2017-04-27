package ee.eesti.riha.rest.logic;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class TestURI {

  @Test
  public void testConstructUri() throws Exception {
    int year = Calendar.getInstance().get(Calendar.YEAR);
    String expected = "urn:fdc:riha.eesti.ee:" + year + ":area:41342321";
    assertEquals(expected, URI.constructUri("area", 41342321));
  }

}
