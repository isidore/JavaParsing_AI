package org.samples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;

import java.lang.reflect.Method;
import java.nio.file.Paths;

public class ParserUtilities {

    public static Range getLineNumbersForMethod(Method method) throws Exception {
        // Assuming the source code is in a standard Maven-like structure
        String sourceRootPath = "src/main/java"; // adjust this path if your structure is different

        // Parsing the source file
        SourceRoot sourceRoot = new SourceRoot(Paths.get(sourceRootPath));
        CompilationUnit cu;
        try {
            cu = sourceRoot.parse(method.getDeclaringClass().getPackageName(),
                    method.getDeclaringClass().getSimpleName() + ".java");
        } catch (ParseProblemException e) {
            throw new RuntimeException("Error parsing the source file: " + e.getMessage(), e);
        }

        // Finding the method in the AST
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class,
                md -> md.getNameAsString().equals(method.getName())).orElse(null);

        if (methodDeclaration == null) {
            throw new RuntimeException("Method not found in the source file");
        }

        // Return the range of the method
        return methodDeclaration.getRange().orElseThrow(() ->
                new RuntimeException("Range not found for the method"));
    }
}
