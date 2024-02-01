package org.samples;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.utils.SourceRoot;
import com.spun.util.FormattedException;
import org.lambda.query.Query;
import org.lambda.query.Queryable;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParserUtilities {

    public static Range getLineNumbersForMethod(Method method) throws Exception {
        CompilationUnit cu = getCompilationUnit(method);
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class, md -> findMethod(method, md))
                .orElse(null);
        if (methodDeclaration == null) {
            throw new FormattedException("Method Not Found:\n%s.%s(params...)",
                    method.getDeclaringClass().getSimpleName(), method.getName());
        }
        return methodDeclaration.getRange().get();
    }

    private static boolean findMethod(Method compiledMethod, MethodDeclaration parsedMethod) {
        if (!parsedMethod.getNameAsString().equals(compiledMethod.getName())) {
            return false;
        }

        List<String> compiledParameterTypes = Query.select(compiledMethod.getParameterTypes(), Class::getSimpleName);
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

    public static boolean isCompiledTypeSameAsParsedType(Parameter parsed, String compiledType,
            NodeList<TypeParameter> typeParameters) {
        // Get the parsed parameter's type as a string
        return compiledType.equals(convertParsedParameterToCompiledTypeSimpleName(parsed, typeParameters));
    }

    public static CompilationUnit getCompilationUnit(Method method) {
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

    public static String convertParsedParameterToCompiledTypeSimpleName(Parameter parameter, List<TypeParameter> methodTypeParameters) {
        Type type = parameter.getType();

        // Handle varargs, which are syntactically similar to arrays in the type system
        boolean isVarArg = parameter.isVarArgs();

        // Check if the parameter type is a generic type parameter
        if (methodTypeParameters.stream().anyMatch(tp -> type.toString().startsWith(tp.getNameAsString())) || isVarArg) {
            // Adjust for varargs or regular arrays
            long arrayCount = type.toString().chars().filter(ch -> ch == '[').count() + (isVarArg ? 1 : 0); // Add an extra array level for varargs
            String baseType = "Object";
            // Construct the array representation if needed
            String arraySuffix = "";
            for (int i = 0; i < arrayCount; i++) {
                arraySuffix += "[]";
            }
            return baseType + arraySuffix;
        } else {
            // For non-generic types, return the type name directly, removing generics information
            String typeName = type.toString();
            int genericMarkerIndex = typeName.indexOf('<');
            if (genericMarkerIndex != -1) {
                typeName = typeName.substring(0, genericMarkerIndex);
            }
            return typeName;
        }
    }
}
