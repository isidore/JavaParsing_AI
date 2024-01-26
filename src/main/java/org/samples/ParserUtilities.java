package org.samples;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.utils.SourceRoot;
import com.spun.util.FormattedException;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParserUtilities {

    public static Range getLineNumbersForMethod(Method method) throws Exception {
        CompilationUnit cu = getCompilationUnit(method);

        // Find the method in the AST
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class, md -> findMethod(method, md)).orElse(null);

        if (methodDeclaration == null) {
            throw new FormattedException("Method Not Found:\n%s.%s(params...)", method.getDeclaringClass().getSimpleName(), method.getName());
        }

        // Return the range of the method
        return methodDeclaration.getRange().orElseThrow(() ->
                new RuntimeException("Range not found for the method"));
    }

    private static boolean findMethod(Method method, MethodDeclaration md) {

        if (!md.getNameAsString().equals(method.getName())) {
            return false;
        }

        // Convert the method's parameter types to a list of their class names
        List<String> compiledParameterTypes = Arrays.stream(method.getParameterTypes())
                .map(Class::getCanonicalName)
                .collect(Collectors.toList());
        // Compare parameter types, allowing for generics
        NodeList<Parameter> parsedParameterTypes = md.getParameters();
        if (parsedParameterTypes.size() != compiledParameterTypes.size()) {
            return false;
        }

        for (int i = 0; i < parsedParameterTypes.size(); i++) {
            Parameter parsed = parsedParameterTypes.get(i);
            NodeList<TypeParameter> typeParameters = md.getTypeParameters();
            String compiledType = compiledParameterTypes.get(i);
            boolean matched = isCompiledTypeSameAsParsedType(parsed, compiledType, typeParameters);
            if (!matched) {
                return false;
            }
        }

        return true;
    }



    public static boolean isCompiledTypeSameAsParsedType(Parameter parsed, String compiledType, NodeList<TypeParameter> typeParameters) {
        // Get the parsed parameter's type as a string
        String parsedType = parsed.getType().asString();

        // Handle array types
        boolean isArray = parsed.getType().isArrayType();
        String arrayComponentType = isArray ? parsed.getType().getElementType().asString() : null;

        // If parsed type matches the compiled type directly, return true
        if (parsedType.equals(compiledType)) {
            return true;
        }

        // Check if the parsed type is a type parameter (generic)
        for (TypeParameter typeParameter : typeParameters) {
            if (isArray && arrayComponentType.equals(typeParameter.getNameAsString())) {
                // Check if it's an array of generics and compiled type is an array of Object
                return compiledType.equals(Object[].class.getCanonicalName());
            } else if (parsedType.equals(typeParameter.getNameAsString())) {
                // If the parsed type is a generic type, check if it's meant to be Object (due to type erasure)
                return compiledType.equals(Object.class.getCanonicalName());
            }
        }

        // If none of the above conditions are met, the types are not the same
        return false;
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
