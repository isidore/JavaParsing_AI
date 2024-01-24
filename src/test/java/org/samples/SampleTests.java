package org.samples;


import com.github.javaparser.Range;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTests
{
  @Test
  public void testLineNumbersForPerson() throws Exception {
    String expected = """
    
    """;
    var m = Person.class.getMethod("getFirstName");
    Range range = getLineNumbersForMethod(m);
    assertEquals(5, 5);
  }
}
