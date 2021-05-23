package flute.testing;

import flute.data.testcase.Candidate;

import java.util.Stack;

public class CandidateMatcher {
    private static String identifieRegex = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";

    public static String preprocess(String target) {
        target = emptyStringLiteral(target);
//        System.out.println(target);
        target = removeArrayAccessIndex(target);
//        System.out.println(target);
        return target;
    }

    private static String emptyStringLiteral(String target) {
        StringBuilder sb = new StringBuilder();
        boolean insideStringLiteral = false;
        while (target.contains("\"")) {
            int pos = target.indexOf('"');
            if (!insideStringLiteral) sb.append(target, 0, pos);
            if (pos == 0 || target.charAt(pos - 1) != '\\') {
                sb.append('"');
                insideStringLiteral = !insideStringLiteral;
            }
            target = target.substring(pos + 1);
        }
        sb.append(target);
        return sb.toString();
    }

    private static String removeArrayAccessIndex(String target) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < target.length(); ++i) {
            char c = target.charAt(i);
            if (c == ']') {
                while (true) {
                    if (stack.pop() == '[') break;
                }
            }
            stack.add(c);
        }
        StringBuilder sb = new StringBuilder();
        for (char c: stack) {
            if (c == ']') {
                sb.append('[');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static boolean matches(Candidate candidate, String target) {
        target = preprocess(target);
        if (matchesTarget(candidate, target)) return true;
        if (target.startsWith("this.")) {
            if (matchesTarget(candidate, target.substring(5))) return true;
        } else {
            if (matchesTarget(candidate, "this." + target)) return true;
        }
        return false;
    }

    private static boolean matchesTarget(Candidate candidate, String target) {
        if (equalsLexical(candidate, target)) return true;
        if (matchesLiteral(candidate, target)) return true;
        if (matchesMethodCall(candidate, target)) return true;
        if (matchesObjectCreation(candidate, target)) return true;
        if (matchesClassExpr(candidate, target)) return true;
        return false;
    }

    public static boolean equalsLexical(Candidate candidate, String target) {
        return candidate.getName().compareTo(target) == 0;
    }

    public static boolean matchesMethodCall(Candidate candidate, String target) {
        if (!candidate.getExcode().matches(".*M_ACCESS\\("+"\\w+"+","+"\\w+"+",\\d+\\) OPEN_PART")) return false;
        if (!target.matches(".*"+"\\w+"+"\\(.*\\)$")) return false;
        return target.startsWith(candidate.getName());
    }

    public static boolean matchesObjectCreation(Candidate candidate, String target) {
        if (!candidate.getExcode().matches("^C_CALL\\("+"\\w+"+","+"\\w+\\) OPEN_PART")) return false;
        if (!target.matches("^new "+"\\w+"+"\\(.*\\)$")) return false;
        return target.startsWith(candidate.getName());
    }

    public static boolean matchesLiteral(Candidate candidate, String target) {
        if (!candidate.getExcode().matches("^LIT\\([a-zA-Z]+\\)$")) return false;
        if (matchesStringLiteral(candidate, target)) return true;
        if (matchesNumLiteral(candidate, target)) return true;
        return false;
    }

    public static boolean matchesStringLiteral(Candidate candidate, String target) {
        if (!target.matches("^\".*\"$")) return false;
        if (candidate.getExcode().compareTo("LIT(String)") != 0) return false;
        return candidate.getName().compareTo("\"\"") == 0;
    }

    public static boolean matchesNumLiteral(Candidate candidate, String target) {
        if (candidate.getExcode().compareTo("LIT(num)") != 0) return false;
        try {
            Double.parseDouble(target);
        } catch (NumberFormatException e) {
            return false;
        }
        return candidate.getName().compareTo("0") == 0;
    }

    public static boolean matchesClassExpr(Candidate candidate, String target) {
        if (!target.endsWith(".class")) return false;
        if (candidate.getExcode().compareTo("VAR(Class)") != 0) return false;
        return candidate.getName().compareTo(".class") == 0;
    }

    public static void main(String[] args) {
        Candidate candidate = new Candidate("VAR(Animal,this) M_ACCESS(Animal,moveTo,2) OPEN_PART", "this.moveTo(");
        System.out.println(CandidateMatcher.matches(candidate, "a.get[b.go[\"\\\"<![CDATA[\\\"\"]]"));
    }
}
