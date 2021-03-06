package flute.tokenizing.exe;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import flute.analysis.analysers.CountLineFromLastUsageOfArgumentDecorator;
import flute.analysis.enumeration.ExpressionType;
import flute.analysis.config.Config;
import flute.data.MultiMap;
import flute.data.typemodel.Variable;
import flute.jdtparser.ProjectParser;
import flute.tokenizing.excode_data.*;
import flute.utils.StringUtils;
import flute.utils.file_processing.JavaTokenizer;
import flute.utils.logging.Logger;

import java.util.*;

public class ArgRecTestGenerator extends MethodCallRecTestGenerator {
    private int lengthLimit = -1;

    public ArgRecTestGenerator(String projectPath, ProjectParser projectParser) {
        super(projectPath, projectParser);
    }

    public void setLengthLimit(int lengthLimit) {
        this.lengthLimit = lengthLimit;
    }

    private <T> List<T> truncateList(List<T> list, boolean fromBegin) {
        if (lengthLimit < 0 || list.size() <= lengthLimit) return list;
        if (fromBegin) {
            return list.subList(list.size() - lengthLimit, list.size());
        } else {
            return list.subList(0, lengthLimit);
        }
    }

    private <T> List<T> truncateList(List<T> list) {
        return truncateList(list, true);
    }

    List<List<Integer>> getCandidatesLocality(List<List<String>> nextLexList) {
        HashMap<String, Variable> localVariableMap = getFileParser().getVisibleVariablesHM();
        List<List<Integer>> candidatesLocality = new ArrayList<>();
        nextLexList.forEach(lexemes -> {
            List<Integer> locality = new ArrayList<>();
            for (String nextLex: lexemes) {
                if (localVariableMap.containsKey(nextLex)) {
                    Variable localVar = localVariableMap.get(nextLex);
                    if (localVar.getScopeDistance() == 0) {
                        locality.add(6);
                    } else {
                        if (localVar.getLocalVariableLevel() == 0) {
                            locality.add(5);
                        } else {
                            locality.add(localVar.getLocalVariableLevel());
                        }
                    }
                } else {
                    // otherwise
                    locality.add(-1);
                }
            };
            candidatesLocality.add(locality);
        });
        return candidatesLocality;
    }

    List<List<Integer>> getCandidatesScopeDistance(List<List<String>> nextLexList) {
        HashMap<String, Variable> localVariableMap = getFileParser().getVisibleVariablesHM();
        List<List<Integer>> candidatesSD = new ArrayList<>();
        nextLexList.forEach(lexemes -> {
            List<Integer> scope_distance = new ArrayList<>();
            for (String nextLex: lexemes) {
                if (localVariableMap.containsKey(nextLex)) {
                    Variable localVar = localVariableMap.get(nextLex);
                    scope_distance.add(localVar.getScopeDistance());
                } else {
                    // otherwise
                    scope_distance.add(-1);
                }
            };
            candidatesSD.add(scope_distance);
        });
        return candidatesSD;
    }

    List<List<Integer>> getCandidatesLastUsageDistance(List<List<String>> nextLexList, MethodCallExpr methodCall, Position argPos) {
        List<List<Integer>> candidatesLUD = new ArrayList<>();
        Node currentNode = methodCall;
        do {
            if (currentNode.getParentNode().isEmpty()) {
                nextLexList.forEach(lexemes -> {
                    List<Integer> lastUsageDistance = new ArrayList<>();
                    for (int i = 0; i < lexemes.size(); ++i) lastUsageDistance.add(-1);
                    candidatesLUD.add(lastUsageDistance);
                });
                break;
            }
            currentNode = currentNode.getParentNode().get();
            if (currentNode instanceof MethodDeclaration || currentNode instanceof ConstructorDeclaration) {
                List<String> operandList = new ArrayList<>();
                List<Position> operandPosList = new ArrayList<>();
                CountLineFromLastUsageOfArgumentDecorator.visit(currentNode, operandList, operandPosList, true);
                Map<String, List<Position>> operandMap = new HashMap<>();
                for (int i = 0; i < operandList.size(); ++i) {
                    if (!operandMap.containsKey(operandList.get(i))) {
                        operandMap.put(operandList.get(i), new ArrayList<>());
                    }
                    operandMap.get(operandList.get(i)).add(operandPosList.get(i));
                }

                nextLexList.forEach(lexemes -> {
                    List<Integer> lastUsageDistance = new ArrayList<>();
                    for (String nextLex: lexemes) {
                        Position lastUsagePos = CountLineFromLastUsageOfArgumentDecorator.searchLastUsage(operandMap.get(nextLex), argPos);
                        if (lastUsagePos != null) {
                            lastUsageDistance.add(argPos.line - lastUsagePos.line);
                        } else {
                            lastUsageDistance.add(-1);
                        }
                    }
                    candidatesLUD.add(lastUsageDistance);
                });
                break;
            }
        } while (true);
        return candidatesLUD;
    }

    @Override
    List<? extends RecTest> generateFromMethodCall(List<NodeSequenceInfo> excodes, int methodCallStartIdx, int methodCallEndIdx,
                                                   MethodCallExpr methodCall, String contextMethodCall, String methodScope, String methodName) {

        List<RecTest> tests = new ArrayList<>();

        // Lack of libraries
        if (!getFileParser().acceptedMethod()) {
            System.err.println("ERROR: Cannot resolve: " + methodCall + ". This may be due to the absence of required libraries.");
            if (Config.LOG_WARNING) System.err.println("WARNING: Corresponding tests will not be generated.");
            return tests;
        }

        String parsedMethodCall = getFileParser().getLastMethodCallGen().replaceAll("[ \r\n]", "");
        if (!parsedMethodCall.equals(methodCall.toString().replaceAll("[ \r\n]", ""))) {
            System.err.println("ERROR: " + getFileParser().getLastMethodCallGen() + " was parsed instead of " + methodCall.toString()
                    + " at " + methodCall.getBegin().get());
            if (Config.LOG_WARNING) System.err.println("WARNING: Corresponding tests will not be generated.");
            return tests;
        }

        String contextArg = contextMethodCall + methodScope + methodName + '(';

        String classQualifiedName;

        try {
            classQualifiedName = getFileParser().getCurMethodInvocation().getClassQualifiedName().orElse(null);
        } catch (Exception e) {
            classQualifiedName = null;
        }

        List<ArgRecTest> oneArgTests = new ArrayList<>();
        int k = methodCallStartIdx + 1;
        int contextIdx = methodCallStartIdx + 1;
        for (int j = 0; j < methodCall.getArguments().size(); ++j) {
            Expression arg = methodCall.getArgument(j);
            while (k <= methodCallEndIdx) {
                if (NodeSequenceInfo.isSEPA(excodes.get(k), ',') && excodes.get(k).oriNode == arg) {
                    MultiMap params = null;
                    try {
                        params = getFileParser().genParamsAt(j);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.err.println(methodCall);
                        System.err.println(methodCall.getBegin().get());
                        throw e;
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println(methodCall);
                        System.err.println(methodCall.getBegin().get());
                        throw e;
                    } catch (NullPointerException e) {
                        System.err.println(methodCall);
                        System.err.println(methodCall.getBegin().get());
                        throw e;
                    }

                    if (params != null) {
                        List<String> nextExcodeList = new ArrayList<>(params.getValue().keySet());
                        List<List<String>> nextLexList = new ArrayList<>();
                        for (String nextExcode : nextExcodeList) {
                            nextLexList.add(params.getValue().get(nextExcode));
                        }

                        ContextInfo context = new ContextInfo(excodes, contextIdx);

                        List<NodeSequenceInfo> argExcodes = new ArrayList<>();
                        for (int t = contextIdx + 1; t < k; ++t) argExcodes.add(excodes.get(t));

                        List<String> tokenizedContextMethodCall = JavaTokenizer.tokenize(contextArg);
                        while (tokenizedContextMethodCall.get(tokenizedContextMethodCall.size() - 1).equals("")) {
                            tokenizedContextMethodCall.remove(tokenizedContextMethodCall.size() - 1);
                        }
                        tokenizedContextMethodCall = truncateList(tokenizedContextMethodCall);

                        List<NodeSequenceInfo> excodeContext = context.getContextFromMethodDeclaration();
                        excodeContext = truncateList(excodeContext);

                        ArgRecTest test = new ArgRecTest();
                        test.setLine(methodCall.getBegin().get().line);
                        test.setCol(methodCall.getBegin().get().column);
                        test.setClassHierarchy(getFileParser().getHierarchy());
                        test.setLex_context(tokenizedContextMethodCall);
                        test.setExcode_context(NodeSequenceInfo.convertListToString(excodeContext));
                        test.setMethodScope_name(getFileParser().getCurMethodScopeName().orElse(""));
                        test.setClass_name(getFileParser().getCurClassScopeName().orElse(""));
                        test.setExpected_excode(NodeSequenceInfo.convertListToString(argExcodes));
                        test.setExpected_lex(arg.toString());
                        test.setStaticMemberAccessLex(getFileParser().getTargetPattern(j));
                        test.setArgType(ExpressionType.get(arg));
                        test.setNext_excode(nextExcodeList);
                        test.setNext_lex(nextLexList);
                        test.setParamTypeKey(params.getParamTypeKey());
                        test.setParamTypeName(getFileParser().getParamTypeName(j).orElse(""));
                        if (getFileParser().getCu().getPackage() != null) {
                            test.setPackageName(getFileParser().getCu().getPackage().getName().toString());
                        }
                        //test.setCandidates_locality(getCandidatesLocality(nextLexList));
                        test.setCandidates_scope_distance(getCandidatesScopeDistance(nextLexList));
                        test.setCandidates_last_usage_distance(getCandidatesLastUsageDistance(nextLexList, methodCall, arg.getBegin().get()));
                        test.setMethodInvoc(methodName);
                        if (methodCall.getScope().isPresent()) {
                            test.setMethodInvocCaller(methodCall.getScope().get().toString());
                        } else {
                            test.setMethodInvocCaller("this");
                        }
                        test.setMethodInvocClassQualifiedName(classQualifiedName);
                        test.setExpected_excode_ori(argExcodes);
                        Logger.testCount(test, getProjectParser());
                        if (RecTestFilter.predictable(argExcodes)) {
                            RecTestNormalizer.normalize(test);
                        } else {
                            test.setIgnored(true);
                        }
                        oneArgTests.add(test);
                    } else {
                        //System.out.println("No candidate generated: " + methodCall);
                    }

                    contextIdx = k;
                    contextArg += arg.toString() + ",";
                    break;
                }
                ++k;
            }
        }

        MultiMap params = null;
        try {
            params = getFileParser().genParamsAt(methodCall.getArguments().size() - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(methodCall);
            System.err.println(methodCall.getBegin().get());
            throw e;
        } catch (IndexOutOfBoundsException e) {
            System.err.println(methodCall);
            System.err.println(methodCall.getBegin().get());
            throw e;
        }

        if (params != null) {
            List<String> nextExcodeList = new ArrayList<>(params.getValue().keySet());
            List<List<String>> nextLexList = new ArrayList<>();
            for (String nextExcode : nextExcodeList) {
                nextLexList.add(params.getValue().get(nextExcode));
            }

            ContextInfo context = new ContextInfo(excodes, contextIdx);

            List<String> tokenizedContextMethodCall = JavaTokenizer.tokenize(contextArg);
            while (!tokenizedContextMethodCall.isEmpty() && tokenizedContextMethodCall.get(tokenizedContextMethodCall.size() - 1).equals("")) {
                tokenizedContextMethodCall.remove(tokenizedContextMethodCall.size() - 1);
            }
            tokenizedContextMethodCall = truncateList(tokenizedContextMethodCall);

            List<NodeSequenceInfo> excodeContext = context.getContextFromMethodDeclaration();
            excodeContext = truncateList(excodeContext);

            ArgRecTest test = new ArgRecTest();
            test.setLine(methodCall.getBegin().get().line);
            test.setCol(methodCall.getBegin().get().column);
            test.setClassHierarchy(getFileParser().getHierarchy());
            test.setLex_context(tokenizedContextMethodCall);
            test.setExcode_context(NodeSequenceInfo.convertListToString(excodeContext));
            test.setMethodScope_name(getFileParser().getCurMethodScopeName().orElse(""));
            test.setClass_name(getFileParser().getCurClassScopeName().orElse(""));
            boolean isClean = true;
            if (methodCall.getArguments().isEmpty()) {
                test.setExpected_excode(excodes.get(methodCallEndIdx).toStringSimple());
                test.setExpected_lex(")");
                test.setCandidates_last_usage_distance(Collections.singletonList(Collections.singletonList(-1)));
                test.setExpected_excode_ori(Collections.singletonList(excodes.get(methodCallEndIdx)));
            } else {
                List<NodeSequenceInfo> argExcodes = new ArrayList<>();
                for (int t = contextIdx + 1; t < methodCallEndIdx; ++t) argExcodes.add(excodes.get(t));

                // Due to Lambda expression
                if (argExcodes.isEmpty()) {
                    isClean = false;
                } else {
                    test.setExpected_excode(NodeSequenceInfo.convertListToString(argExcodes));
                }
                test.setExpected_lex(methodCall.getArgument(methodCall.getArguments().size() - 1).toString());
                test.setArgType(ExpressionType.get(methodCall.getArgument(methodCall.getArguments().size() - 1)));
                test.setCandidates_last_usage_distance(getCandidatesLastUsageDistance(nextLexList, methodCall, methodCall.getArguments().getLast().get().getBegin().get()));
                test.setExpected_excode_ori(argExcodes);
                if (!RecTestFilter.predictable(argExcodes)) isClean = false;
            }
            test.setStaticMemberAccessLex(getFileParser().getTargetPattern(methodCall.getArguments().size() - 1));
            test.setNext_excode(nextExcodeList);
            test.setNext_lex(nextLexList);
            test.setParamTypeKey(params.getParamTypeKey());
            if (methodCall.getArguments().size() > 0) {
                test.setParamTypeName(getFileParser().getParamTypeName(methodCall.getArguments().size() - 1).orElse(""));
            }
            if (getFileParser().getCu().getPackage() != null) {
                test.setPackageName(getFileParser().getCu().getPackage().getName().toString());
            }
            //test.setCandidates_locality(getCandidatesLocality(nextLexList));
            test.setCandidates_scope_distance(getCandidatesScopeDistance(nextLexList));
            test.setMethodInvoc(methodName);
            if (methodCall.getScope().isPresent()) {
                test.setMethodInvocCaller(methodCall.getScope().get().toString());
            } else {
                test.setMethodInvocCaller("this");
            }
            test.setMethodInvocClassQualifiedName(classQualifiedName);
            Logger.testCount(test, getProjectParser());
            if (isClean) {
                RecTestNormalizer.normalize(test);
            } else {
                test.setIgnored(true);
            }
            oneArgTests.add(test);
        } else {
            //System.out.println("No candidate generated: " + methodCall);
        }

        for (int j = 0; j < oneArgTests.size(); ++j) {
            ArgRecTest oneArgTest = oneArgTests.get(j);
            if (j == oneArgTests.size() - 1) {
                oneArgTest.setArgPos(methodCall.getArguments().size());
            } else {
                oneArgTest.setArgPos(j + 1);
            }

            if (oneArgTest.getArgPos() == 0) {
                oneArgTest.setParam_name("");
            } else {
                String paramName = getFileParser().getParamName(oneArgTest.getArgPos() - 1).orElse(null);
//                if (Config.API_CRAWLER && paramName == null) {
//                    try {
//                        if (paramName == null) {
//                            paramName = APICrawler.paramNames(
//                                    getFileParser().getCurMethodInvocation().getClassQualifiedName().orElse(""), getFileParser().getCurMethodInvocation().genMethodString()
//                            ).get(oneArgTest.getArgPos() - 1);
////                        System.out.println(paramName);
//                        }
//                    } catch (Exception e) {
////                    System.out.println(getFileParser().getCurMethodInvocation().getClassQualifiedName());
//                        e.printStackTrace();
//                    }
//                }
                oneArgTest.setParam_name(paramName);
            }
        }

        tests.addAll(oneArgTests);
        return tests;
    }

    @Override
    void postProcess(List<RecTest> tests) {
        for (int j = 0; j < tests.size(); ++j) {
            ArgRecTest test = (ArgRecTest) tests.get(j);
            String expectedExcode = test.getExpected_excode();
            String expectedLex = test.getExpected_lex();
            for (NodeSequenceInfo argExcode : test.getExpected_excode_ori()) {
                if (NodeSequenceInfo.isMethodAccess(argExcode)) {
                    int tmp = StringUtils.indexOf(expectedExcode, "M_ACCESS(");
                    tmp = expectedExcode.indexOf("OPEN_PART", tmp);
                    test.setMethodAccessExcode(expectedExcode.substring(0, tmp + 9));

                    String methodNameArg = argExcode.getAttachedAccess();
                    tmp = StringUtils.indexOf(expectedLex, methodNameArg);
                    tmp = expectedLex.indexOf('(', tmp);
                    test.setMethodAccessLex(expectedLex.substring(0, tmp + 1));

                    break;
                }
                if (NodeSequenceInfo.isObjectCreation(argExcode)) {
                    int tmp = StringUtils.indexOf(expectedExcode, "C_CALL(");
                    tmp = expectedExcode.indexOf("OPEN_PART", tmp);
                    test.setObjectCreationExcode(expectedExcode.substring(0, tmp + 9));

                    String classNameArg = argExcode.getAttachedAccess();
                    tmp = StringUtils.indexOf(expectedLex, classNameArg);
                    tmp = expectedLex.indexOf('(', tmp);
                    test.setObjectCreationLex(expectedLex.substring(0, tmp + 1));

                    break;
                }
            }
        }
    }
}
