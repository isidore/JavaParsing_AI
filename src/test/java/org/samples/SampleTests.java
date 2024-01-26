package org.samples;


import com.github.javaparser.Range;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTests
{
  @Test
  public void testLineNumbersForPerson() throws Exception {
    var expected = """
      (line 7,col 3)-(line 9,col 3)
      """;
    var m = Person.class.getMethod("getFirstName");
    Range range = ParserUtilities.getLineNumbersForMethod(m);
    Approvals.verify(range, new Options().inline(expected));
  }

  @Test
  public void testLineNumbersForOverload() throws Exception {
    var expected = """
      (line 12,col 3)-(line 13,col 3)
      """;
    var m = Person.class.getMethod("getAge", int.class);
    Range range = ParserUtilities.getLineNumbersForMethod(m);
    Approvals.verify(range, new Options().inline(expected));
  }

  @Test
  public void testLineNumbersForGenerics() throws Exception {
    var expected = """
      (line 15,col 3)-(line 16,col 3)
      """;
    var m = Person.class.getMethod("getAge", int.class, Object.class);
    Range range = ParserUtilities.getLineNumbersForMethod(m);
    Approvals.verify(range, new Options().inline(expected));
  }

  @Test
  public void testLineNumbersForGenericsArrays() throws Exception {
    var expected = """
      (line 18,col 3)-(line 19,col 3)
      """;
    var m = Person.class.getMethod("getAge", int.class, Object[].class);
    Range range = ParserUtilities.getLineNumbersForMethod(m);
    Approvals.verify(range, new Options().inline(expected));
  }

  @Test
  public void testLineNumbersForGenericLists() throws Exception {
    var expected = """
      (line 21,col 3)-(line 22,col 3)
      """;
    var m = Person.class.getMethod("getAge", int.class, List.class);
    Range range = ParserUtilities.getLineNumbersForMethod(m);
    Approvals.verify(range, new Options().inline(expected));
  }
}
