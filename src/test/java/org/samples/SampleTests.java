package org.samples;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.UseReporter;
import org.approvaltests.reporters.macosx.VisualStudioCodeReporter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTests {
    @Test
    @UseReporter(VisualStudioCodeReporter.class)
    public void testLineNumbersForPerson() throws Exception {
        var expected = """
                Person.getFirstName() Lines:8-10
                Person.getAge(int) Lines:13-14
                Person.getAge(int,java.lang.Object) Lines:16-17
                Person.getAge(int,java.lang.Object[]) Lines:19-20
                Person.getAge(int,java.util.List) Lines:22-23
                Person.getAge(int,java.util.Map) Lines:25-26
                """;

        var m = new Method[] {
                Person.class.getMethod("getFirstName"),
                Person.class.getMethod("getAge", int.class),
                Person.class.getMethod("getAge", int.class, Object.class),
                Person.class.getMethod("getAge", int.class, Object[].class),
                Person.class.getMethod("getAge", int.class, List.class),
                Person.class.getMethod("getAge", int.class, Map.class)

        };
        Approvals.verifyAll("", m, SampleTests::getLineNumbers, new Options().inline(expected));
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

    @Test
    public void testParameterConversion() throws Exception {
        CompilationUnit cu = ParserUtilities.getCompilationUnit(ParameterSamples.class.getMethods()[0]);
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class, md -> true).get();
        assertEquals("Map", getConvertedParameterType(methodDeclaration, 0));
        assertEquals("Object", getConvertedParameterType(methodDeclaration, 1));
        assertEquals("Object[]", getConvertedParameterType(methodDeclaration, 2));
        assertEquals("Object[][]", getConvertedParameterType(methodDeclaration, 3));
        assertEquals("Object[][]", getConvertedParameterType(methodDeclaration, 4));
    }

    private static String getConvertedParameterType(MethodDeclaration methodDeclaration, int parameterindex) {
        return ParserUtilities.convertParsedParameterToCompiledTypeSimpleName(
                methodDeclaration.getParameters().get(parameterindex),
                methodDeclaration.getTypeParameters());
    }
}
