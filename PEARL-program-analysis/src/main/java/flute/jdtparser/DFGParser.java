package flute.jdtparser;

import com.google.common.collect.Lists;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class DFGParser {
    public static boolean checkVariable(VariableDeclarationFragment variable, ASTNode curNode, int curPos) {
        if (variable.getInitializer() != null) return true;
        if (variable.getStartPosition() > curPos) return false;
        Block declareBlock = getDFGBlock(variable);
        Block useBlock = getDFGBlock(curNode);

//        boolean result = isInitialized(variable.getName().toString(), declareBlock, curNode, stopPos);
//        System.out.println(variable.getParent());

//        boolean result = isInitialized(variable.getName().toString(), declareBlock, getDFGBlock(curNode), curPos);
//        if (result) {
//            System.out.println(variable);
//        }

        boolean result = blockLoop(variable.getName().toString(), declareBlock, useBlock, curPos);
        return result;
    }

    public static boolean blockLoop(String variableName, Block curBlock, Block useBlock, int curPos) {
        if (curBlock == null) return false;
        for (Object item : curBlock.statements()) {
            if (item instanceof Statement) {
                Statement statement = (Statement) item;
                if (statement.getStartPosition() > curPos) return false;
                if (checkConditionStatement(variableName, statement)) return true;
                if (checkContains(statement, curPos))
                    if (statement instanceof SwitchStatement)
                        return blockLoopSwitchStatement(variableName, (SwitchStatement) statement, useBlock, curPos);
                    else return blockLoop(variableName, getTopDFGBlock(useBlock, statement), useBlock, curPos);
                if (isInitialized(variableName, statement, curPos)) return true;
            }
        }
        return false;
    }

    public static boolean blockLoopSwitchStatement(String variableName, SwitchStatement switchStatement, Block useBlock, int curPos) {
        if (switchStatement == null) return false;
        List statements = switchStatement.statements();
        int i = 0;
        for (Object item : Lists.reverse(statements)) {
            if (item instanceof SwitchCase) {
                SwitchCase switchCaseItem = (SwitchCase) item;
                if (switchCaseItem.getStartPosition() < curPos) {
                    statements = statements.subList(statements.size() - i, statements.size());
                    break;
                }
            }
            i++;
        }

        for (Object item : statements) {
            if (item instanceof Statement) {
                Statement statement = (Statement) item;
                if (statement.getStartPosition() > curPos) return false;
                if (checkConditionStatement(variableName, statement)) return true;
                if (checkContains(statement, curPos))
                    if (statement instanceof SwitchStatement)
                        return blockLoopSwitchStatement(variableName, (SwitchStatement) statement, useBlock, curPos);
                    else if (statement instanceof Block)
                        return blockLoop(variableName, (Block) statement, useBlock, curPos);
                    else return blockLoop(variableName, getTopDFGBlock(useBlock, statement), useBlock, curPos);
                if (isInitialized(variableName, statement, curPos)) return true;
            }
        }
        return false;
    }

    public static boolean isInitialized(String variableName, Statement statement, int curPos) {
        if (statement == null || statement.getStartPosition() > curPos) return false;

        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expressionStatement = (ExpressionStatement) statement;
            if (expressionStatement.getExpression() instanceof Assignment) {
                Assignment assignment = (Assignment) expressionStatement.getExpression();
                if (assignment.getLeftHandSide() instanceof SimpleName) {
                    if (assignment.getLeftHandSide().toString().equals(variableName)) return true;
                }
            }
        } else if (statement instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statement;
            if (isInitialized(variableName, ifStatement.getThenStatement(), curPos)
                    && (ifStatement.getElseStatement() != null && isInitialized(variableName, ifStatement.getElseStatement(), curPos)))
                return true;
        } else if (statement instanceof DoStatement) {
            return isInitialized(variableName, ((DoStatement) statement).getBody(), curPos);
        } else if (statement instanceof TryStatement) {
            TryStatement tryStatement = (TryStatement) statement;
            if (!isInitialized(variableName, tryStatement.getBody(), curPos)) return false;
            for (Object catchObject : tryStatement.catchClauses()) {
                if (catchObject instanceof CatchClause) {
                    if (!isInitialized(variableName, ((CatchClause) catchObject).getBody(), curPos)
                            && !checkThrowInCatchBlock(((CatchClause) catchObject).getBody())) {
                        return false;
                    }
                }
            }
            return true;
        } else if (statement instanceof SwitchStatement) {
            SwitchStatement switchStatement = (SwitchStatement) statement;
            boolean findedCase = false;
            for (Object subStatement : switchStatement.statements()) {
                if (subStatement instanceof SwitchCase) findedCase = true;
                else if (subStatement instanceof Statement) {
                    if (isInitialized(variableName, (Statement) subStatement, curPos) && findedCase) {
                        findedCase = false;
                    }
                }
            }
            if (!findedCase) return true;
        } else if (statement instanceof Block) {
            Block block = (Block) statement;
            for (Object item : block.statements()) {
                if (item instanceof Statement) {
                    Statement statementItem = (Statement) item;
                    if (isInitialized(variableName, statementItem, curPos)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean checkThrowInCatchBlock(Block catchBlock) {
        for (Object statement : catchBlock.statements()) {
            if (statement instanceof ThrowStatement) return true;
        }
        return false;
    }

    public static boolean checkConditionStatement(String variableName, Statement statement) {
        Expression expression = null;
        if (statement instanceof IfStatement) {
            expression = ((IfStatement) statement).getExpression();
        } else if (statement instanceof WhileStatement) {
            expression = ((WhileStatement) statement).getExpression();
        } else if (statement instanceof ForStatement) {
            ForStatement forStatement = (ForStatement) statement;
            for (Object initializer : forStatement.initializers()) {
                if (initializer instanceof Assignment) {
                    if (((Assignment) initializer).getLeftHandSide().toString().equals(variableName)) return true;
                } else if (initializer instanceof InfixExpression) {
                    if (checkInfixExpression(variableName, (InfixExpression) initializer)) return true;
                }
            }

            if (forStatement.getExpression() instanceof InfixExpression) {
                if (checkInfixExpression(variableName, (InfixExpression) forStatement.getExpression())) return true;
            }
            return false;
        } else {
            return false;
        }


        if (expression != null && expression instanceof InfixExpression) {
            return checkInfixExpression(variableName, (InfixExpression) expression);
        }
        return false;
    }

    public static boolean checkInfixExpression(String variableName, InfixExpression infixExpression) {
        if (infixExpression.getLeftOperand() instanceof InfixExpression) {
            if (checkInfixExpression(variableName, (InfixExpression) infixExpression.getLeftOperand())) return true;
        } else if (infixExpression.getRightOperand() instanceof InfixExpression) {
            if (checkInfixExpression(variableName, (InfixExpression) infixExpression.getRightOperand())) return true;
        } else if (infixExpression.getLeftOperand() instanceof ParenthesizedExpression) {
            if (checkParenthesizedExpression(variableName, (ParenthesizedExpression) infixExpression.getLeftOperand()))
                return true;
        } else if (infixExpression.getRightOperand() instanceof ParenthesizedExpression) {
            if (checkParenthesizedExpression(variableName, (ParenthesizedExpression) infixExpression.getRightOperand()))
                return true;
        }

        return false;
    }

    public static boolean checkParenthesizedExpression(String variableName, ParenthesizedExpression parenthesizedExpression) {
        if (parenthesizedExpression.getExpression() instanceof Assignment) {
            Assignment assignment = (Assignment) parenthesizedExpression.getExpression();
            return assignment.getLeftHandSide().toString().equals(variableName);
        } else if (parenthesizedExpression.getExpression() instanceof InfixExpression) {
            return checkInfixExpression(variableName, (InfixExpression) parenthesizedExpression.getExpression());
        }
        return false;
    }

    public static boolean checkContains(ASTNode parent, int pos) {
        if (parent.getStartPosition() <= pos
                && (parent.getStartPosition() + parent.getLength()) > pos)
            return true;

        return false;
    }

    public static Block getTopDFGBlock(Block curUseBlock, Statement curStatement) {
        if (curUseBlock == null) return null;
        if (curUseBlock.getParent() == curStatement) {
            return curUseBlock;
        } else {
            return getTopDFGBlock(getDFGBlock(curUseBlock), curStatement);
        }
    }

    public static Block getDFGBlock(ASTNode astNode) {
        if (astNode == null) return null;
        ASTNode parentNode = astNode.getParent();
        if (parentNode instanceof Block) {
            return (Block) parentNode;
        } else return getDFGBlock(parentNode);
    }
}
