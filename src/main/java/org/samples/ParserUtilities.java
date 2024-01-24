package org.samples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.SourceRoot;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ParserUtilities {

    public static Range getLineNumbersForMethod(Method method) throws Exception {
        CompilationUnit cu = getCompilationUnit(method);

        // Find the method in the AST
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class, md -> findMethod(method, md)).orElse(null);

        if (methodDeclaration == null) {
            throw new RuntimeException("Method not found in the source file");
        }

        // Return the range of the method
        return methodDeclaration.getRange().orElseThrow(() ->
                new RuntimeException("Range not found for the method"));
    }

    private static boolean findMethod(Method method, MethodDeclaration md) {
        // Convert the method's parameter types to a list of their class names
        List<String> paramTypes = List.of(method.getParameterTypes()).stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toList());

        if (!md.getNameAsString().equals(method.getName())) {
            return false;
        }
        List<String> astParamTypes = md.getParameters().stream()
                .map(p -> p.getType().asString())
                .collect(Collectors.toList());
        return astParamTypes.equals(paramTypes);
    }

    private static CompilationUnit getCompilationUnit(Method method) {
        String sourceRootPath = "src/main/java"; // Adjust this path if your structure is different

        // Parsing the source file
        SourceRoot sourceRoot = new SourceRoot(Paths.get(sourceRootPath));
        CompilationUnit cu;
        try {
            cu = sourceRoot.parse(method.getDeclaringClass().getPackageName(),
                    method.getDeclaringClass().getSimpleName() + ".java");
        } catch (ParseProblemException e) {
            throw new RuntimeException("Error parsing the source file: " + e.getMessage(), e);
        }
        return cu;
    }
}
