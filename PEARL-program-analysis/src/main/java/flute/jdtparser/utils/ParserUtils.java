package flute.jdtparser.utils;

import flute.communicate.SocketClient;
import flute.data.MultiMap;
import flute.data.typemodel.Variable;
import flute.jdtparser.ClassParser;
import flute.jdtparser.ProjectParser;
import flute.utils.file_processing.FileProcessor;
import org.eclipse.jdt.core.dom.*;

import flute.config.Config;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ParserUtils {
    private static ITypeBinding curType;
    private static ITypeBinding nextType;

    public static final List<String> numberInfixOperation = Arrays.asList(new String[]{"+", "-", "*", "/", "%", "<", "<=", ">", ">="});
    public static final List<String> boolInfixOperation = Arrays.asList(new String[]{"||", "&&"});

    private static List<IVariableBinding> innerGetAllSuperFields(ITypeBinding iTypeBinding) {
        ITypeBinding superClass = iTypeBinding.getSuperclass();
        if (superClass == null) return new ArrayList<>();
        List<IVariableBinding> variableBindings = new ArrayList<>();

        //add from parent
        nextType = superClass;
        addVariableToList(Arrays.asList(superClass.getDeclaredFields()), variableBindings);
        //add from interface
        nextType = curType;
        addVariableToList(getAllInterfaceFields(iTypeBinding), variableBindings);
        //add from parent of parent
        addVariableToList(getAllSuperFields(superClass), variableBindings);

        return variableBindings;
    }

    public static List<IVariableBinding> getAllSuperFields(ITypeBinding iTypeBinding) {
        curType = iTypeBinding;
        return innerGetAllSuperFields(iTypeBinding);
    }

    public static List<IVariableBinding> getAllInterfaceFields(ITypeBinding iTypeBinding) {
        List<IVariableBinding> variableBindings = new ArrayList<>();
        for (ITypeBinding anInterface : iTypeBinding.getInterfaces()) {
            Collections.addAll(variableBindings, anInterface.getDeclaredFields());
            if (Config.FEATURE_ADD_FIELD_AND_METHOD_FROM_SUPER_INTERFACE) {
                addVariableToList(getAllInterfaceFields(anInterface), variableBindings);
                //variableBindings.addAll(getAllInterfaceFields(anInterface));
            }
        }
        return variableBindings;
    }

    public static boolean checkVariableInList(IVariableBinding variableBinding, List<IVariableBinding> list) {
        for (IVariableBinding variableBindingItem : list) {
            if (variableBindingItem.getName().equals(variableBinding.getName())) return true;
        }
        return false;
    }

    public static void addVariableToList(List<IVariableBinding> variableBindings, List<IVariableBinding> list) {
        for (IVariableBinding declaredField : variableBindings) {
            if (!checkVariableInList(declaredField, list)
                    && !Modifier.isPrivate(declaredField.getModifiers())
                    && (curType.getPackage() == nextType.getPackage() || !Modifier.isDefault(declaredField.getModifiers()))) {
                list.add(declaredField);
            }
        }
    }

    public static List<SimpleName> parseSimpleName(ASTNode expr) {
        List<SimpleName> simpleNameList = new ArrayList<>();

        expr.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName simpleName) {
                if (simpleName.resolveBinding() instanceof IVariableBinding) {
                    simpleNameList.add(simpleName);
                }
                return true;
            }
        });
        return simpleNameList;
    }

    public static MethodDeclaration findMethodDeclaration(IMethodBinding iMethodBinding, CompilationUnit curCu, ProjectParser projectParser) {
        ASTNode methodDeclaration = curCu.findDeclaringNode(iMethodBinding.getKey());
        if (methodDeclaration != null) {
            return (MethodDeclaration) methodDeclaration;
        }
        //create a compilation unit from binding class
        CompilationUnit virtualCu = projectParser.createCU(iMethodBinding.getDeclaringClass().getName(), iMethodBinding.getDeclaringClass().toString());
        return (MethodDeclaration) virtualCu.findDeclaringNode(iMethodBinding.getKey());
    }

    public static MultiMap methodMap(Optional<List<IMethodBinding>> methodList) {
        MultiMap multiMap = new MultiMap();

        if (!methodList.isPresent()) return multiMap;

        for (IMethodBinding method : methodList.get()) {
            String exCode = "M_ACCESS("
                    + method.getDeclaringClass().getName() + "," + method.getName() + "," + method.getParameterTypes().length + ")";
            multiMap.put(exCode, method.getName());
        }
        return multiMap;
    }

    static SocketClient socketClient;
    static HashSet<String> commonNames;

    static {
        if (Config.FEATURE_LIMIT_CANDIDATES) {
            try {
                socketClient = new SocketClient(Config.PARAM_SERVICE_PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            commonNames = FileProcessor.readLineByLineToSet(Config.STORAGE_DIR + "/dict/common.txt");
        }
    }

    public static boolean checkImportantVariable(String name, String paramName, List<String> localNameList) {
        System.out.println("a");
        if (localNameList.contains(name)) {
            return true;
        }
        //Tan check common name
        try {
            if (paramName == null) return true;
            if (commonNames.contains(name)) return true;
            if (socketClient.lexSimService(name, paramName).orElse(-1f) < 0.5)
                return false;
        } catch (IOException e) {
            return true;
        }

        return true;
    }

    public static boolean compareSpecialCase(ITypeBinding type1, ITypeBinding type2, IMethodBinding methodBinding) {
        //type2 is generic type
        if ((type1 == null || type2 == null || type1.getTypeDeclaration() == null) || (type1.getTypeDeclaration() != type2.getTypeDeclaration()
                && type1.getTypeDeclaration().isAssignmentCompatible(type2.getTypeDeclaration()))) return false;

        if (type1.getTypeArguments().length != type2.getTypeArguments().length) return false;
        for (int i = 0; i < type2.getTypeArguments().length; i++) {
            if (!Arrays.stream(methodBinding.getTypeArguments()).collect(Collectors.toList()).contains(
                    type2.getTypeArguments()[i]
            )) {
                if (type1.getTypeArguments()[i].isAssignmentCompatible(type2.getTypeArguments()[i])) {
                    return false;
                }
            }
            //next if it's generic type or compatible type
        }

        return true;
    }

    public static List<IMethodBinding> withOuterClassParserMethods(ClassParser classParser) {
        List<IMethodBinding> outerMethods = classParser.getMethods();
        return withOuterClassParserMethods(outerMethods, classParser);
    }

    private static List<IMethodBinding> withOuterClassParserMethods(List<IMethodBinding> methodInvocations, ClassParser classParser) {
        ITypeBinding outerClass = classParser.getOrgType().getDeclaringClass();
        if (outerClass == null) return methodInvocations;


        ClassParser outerClassParser = new ClassParser(outerClass);
        outerClassParser.getMethods().forEach(method -> {
            if (!containMethod(method, methodInvocations)) {
                methodInvocations.add(method);
            }
        });
        return methodInvocations;
    }

    private static boolean containMethod(IMethodBinding method, List<IMethodBinding> methodBindings) {
        for (IMethodBinding methodBinding :
                methodBindings) {
            if (method.getName().equals(methodBinding.getName())) {
                return true;
            }
        }
        return false;
    }

    public static List<IMethodBinding> getConstructors(ITypeBinding iTypeBinding) {
        if (iTypeBinding.isWildcardType()) {
            iTypeBinding = iTypeBinding.getSuperclass();
        }
        return Arrays.stream(iTypeBinding.getDeclaredMethods()).filter(methodBinding -> {
            return methodBinding.isConstructor();
        }).collect(Collectors.toList());
    }

    public static boolean compareParamList(IMethodBinding method, IMethodBinding targetMethod) {
        if (method.getParameterTypes().length != targetMethod.getParameterTypes().length) return false;
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            ITypeBinding paramType = method.getParameterTypes()[i];
            if (!paramType.isAssignmentCompatible(targetMethod.getParameterTypes()[i])) {
                return false;
            }
        }
        return true;
    }

    public static ITypeBinding checkConstructorReference(ITypeBinding interfaceType) {
        if (interfaceType.getDeclaredMethods().length != 0) {
            for (IMethodBinding methodBinding : interfaceType.getDeclaredMethods()) {
                if (Modifier.isAbstract(methodBinding.getModifiers())) {
                    if (methodBinding.getParameterTypes().length == 0) return methodBinding.getReturnType();
                    for (IMethodBinding constructors : getConstructors(methodBinding.getReturnType())) {
                        if (compareParamList(constructors, methodBinding)) return methodBinding.getReturnType();
                    }
                }
            }
        }
        return null;
    }

    public static Variable getThisVariable(List<Variable> list) {
        for (Variable var : list) {
            if (var.getName().equals("this")) return var;
        }
        return null;
    }

    public static boolean findField(IVariableBinding[] fields, String fieldName) {
        for (IVariableBinding field : fields) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean findMethod(IMethodBinding[] methods, IMethodBinding methodBinding) {
        for (IMethodBinding method : methods) {
            if (ClassParser.compareMethod(method, methodBinding)) {
                return true;
            }
        }
        return false;
    }
}
