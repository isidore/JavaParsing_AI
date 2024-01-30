package org.samples;


import com.github.javaparser.Range;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTests {
    @Test
    public void testLineNumbersForPerson() throws Exception {

        var m = new Method[]{
                Person.class.getMethod("getFirstName"),
                Person.class.getMethod("getAge", int.class),
                Person.class.getMethod("getAge", int.class, Object.class),
                Person.class.getMethod("getAge", int.class, Object[].class),
                Person.class.getMethod("getAge", int.class, List.class),
                Person.class.getMethod("getAge", int.class, Map.class)

        };
        Approvals.verifyAll("", m, SampleTests::getLineNumbers);
    }

    private static String getLineNumbers(Method m) {
        String methodName = m.toString().substring("public void org.samples.".length());
        try {

            Range range = ParserUtilities.getLineNumbersForMethod(m);
            var text = String.format("%s Lines:%s-%s", methodName, range.begin.line, range.end.line);
            return text;
        } catch (Exception e) {
            return String.format("%s %s", methodName, e.getClass().getSimpleName());
        }
    }


}
