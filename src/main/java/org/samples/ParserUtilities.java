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
import org.lambda.query.Query;
import org.lambda.query.Queryable;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParserUtilities {

    public static Range getLineNumbersForMethod(Method method) throws Exception {
        CompilationUnit cu = getCompilationUnit(method);
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class, md -> findMethod(method, md)).orElse(null);
        if (methodDeclaration == null) {
            throw new FormattedException("Method Not Found:\n%s.%s(params...)", method.getDeclaringClass().getSimpleName(), method.getName());
        }
       return methodDeclaration.getRange().get();
    }

    private static boolean findMethod(Method compiledMethod, MethodDeclaration parsedMethod) {
        if (!parsedMethod.getNameAsString().equals(compiledMethod.getName())) {
            return false;
        }

        List<String> compiledParameterTypes = Query.select(compiledMethod.getParameterTypes(), Class::getCanonicalName);
        NodeList<Parameter> parsedParameterTypes = parsedMethod.getParameters();
        if (parsedParameterTypes.size() != compiledParameterTypes.size()) {
            return false;
        }

        NodeList<TypeParameter> typeParameters = parsedMethod.getTypeParameters();
        for (int i = 0; i < parsedParameterTypes.size(); i++) {
            Parameter parsed = parsedParameterTypes.get(i);
            String compiledType = compiledParameterTypes.get(i);
            if (!isCompiledTypeSameAsParsedType(parsed, compiledType, typeParameters)) {
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

        // Check if the parsed type is a List with a wildcard generic type
        if (parsed.getType().isClassOrInterfaceType() && parsed.getType().asClassOrInterfaceType().getNameAsString().equals("List")) {
            return compiledType.equals(List.class.getCanonicalName());
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
