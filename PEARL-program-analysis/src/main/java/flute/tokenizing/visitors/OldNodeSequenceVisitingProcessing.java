/**
 * 
 */
package flute.tokenizing.visitors;

import java.util.ArrayList;
import java.util.Stack;

import flute.tokenizing.excode_data.MethodInfo;
import flute.tokenizing.excode_data.NodeInfo;
import flute.tokenizing.excode_data.NodeSequenceConstant;
import flute.tokenizing.excode_data.NodeSequenceInfo;
import flute.tokenizing.excode_data.TypeInfo;

/**
 * @author ANH
 *
 */
public class OldNodeSequenceVisitingProcessing {
	public static long curNodeID = 0;

	public synchronized static void init() {
		curNodeID = 0;
	}
	public synchronized static NodeSequenceInfo addOPBLKNode(ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getOPBLK();
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}
	public synchronized static NodeSequenceInfo addCLBLKNode(ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getCLBLK();
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}
	public synchronized static NodeSequenceInfo addControlNode(short controlType, Stack<NodeSequenceInfo> nodeSequenceStack,
			MethodInfo methodInfo, TypeInfo typeInfo, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.CONTROL;
		short startEnd = NodeSequenceConstant.START;
		String attachedType = null;
		String attachedVar = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);
		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addElseControlNode(short controlType, Stack<NodeSequenceInfo> nodeSequenceStack,
			MethodInfo methodInfo, TypeInfo typeInfo, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.peek();
		}
		short nodeType = NodeSequenceConstant.CONTROL;
		short startEnd = NodeSequenceConstant.START;
		String attachedType = null;
		String attachedVar = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);

		// nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;

		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addEndControlNode(short controlType, long sameControlSeqID,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = nodeSequenceStack.pop();

		short nodeType = NodeSequenceConstant.CONTROL;
		short startEnd = NodeSequenceConstant.END;
		String attachedType = null;
		String attachedVar = null;
		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, sameControlSeqID, previousNode, nodeType,
				startEnd, controlType, attachedType, attachedVar, methodInfo, typeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addClassNode(String className,
			Stack<NodeSequenceInfo> nodeSequenceStack, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}

		short nodeType = NodeSequenceConstant.CLASS;
		short startEnd = NodeSequenceConstant.START;
		String attachedType = className;
		String attachedVar = null;
		short controlType = NodeSequenceConstant.UNKNOWN;
		MethodInfo methodInfo = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addEndClassNode(String className, long sameControlSeqID,
			Stack<NodeSequenceInfo> nodeSequenceStack, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = nodeSequenceStack.pop();

		short nodeType = NodeSequenceConstant.CLASS;
		short startEnd = NodeSequenceConstant.END;
		String attachedType = className;
		String attachedVar = null;
		short controlType = NodeSequenceConstant.UNKNOWN;
		MethodInfo methodInfo = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, sameControlSeqID, previousNode, nodeType,
				startEnd, controlType, attachedType, attachedVar, methodInfo, typeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}
	
	public synchronized static NodeSequenceInfo addEnumNode(String className,
			Stack<NodeSequenceInfo> nodeSequenceStack, TypeInfo typeInfo,
            ArrayList<NodeSequenceInfo> nodeSequenceList) {
        NodeSequenceInfo previousNode = null;
        if (nodeSequenceStack.size() > 0) {
            previousNode = nodeSequenceStack.pop();
        }

        short nodeType = NodeSequenceConstant.ENUM;
        short startEnd = NodeSequenceConstant.START;
        String attachedType = className;
        String attachedVar = null;
        short controlType = NodeSequenceConstant.UNKNOWN;
        MethodInfo methodInfo = null;

        NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
                controlType, attachedType, attachedVar, methodInfo, typeInfo);

        nodeSequenceStack.push(nodeSequenceInfo);
        nodeSequenceList.add(nodeSequenceInfo);

        curNodeID++;
        return nodeSequenceInfo;
    }

    public synchronized static NodeSequenceInfo addEndEnumNode(String className, long sameControlSeqID,
    		Stack<NodeSequenceInfo> nodeSequenceStack, TypeInfo typeInfo,
            ArrayList<NodeSequenceInfo> nodeSequenceList) {
        NodeSequenceInfo previousNode = nodeSequenceStack.pop();

        short nodeType = NodeSequenceConstant.ENUM;
        short startEnd = NodeSequenceConstant.END;
        String attachedType = className;
        String attachedVar = null;
        short controlType = NodeSequenceConstant.UNKNOWN;
        MethodInfo methodInfo = null;

        NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, sameControlSeqID, previousNode, nodeType,
                startEnd, controlType, attachedType, attachedVar, methodInfo, typeInfo);

        nodeSequenceStack.push(nodeSequenceInfo);
        nodeSequenceList.add(nodeSequenceInfo);

        curNodeID++;
        return nodeSequenceInfo;
    }

	public synchronized static NodeSequenceInfo addMethodDecNode(short nodeType, Stack<NodeSequenceInfo> nodeSequenceStack,
			TypeInfo typeInfo, MethodInfo methodInfo, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}

		short startEnd = NodeSequenceConstant.START;
		String attachedType = null;
		String attachedVar = null;
		short controlType = NodeSequenceConstant.UNKNOWN;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addEndMethodDecNode(short nodeType, long sameControlSeqID,
			Stack<NodeSequenceInfo> nodeSequenceStack, TypeInfo typeInfo, MethodInfo methodInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = nodeSequenceStack.pop();

		short startEnd = NodeSequenceConstant.END;
		String attachedType = null;
		String attachedVar = null;
		short controlType = NodeSequenceConstant.UNKNOWN;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, sameControlSeqID, previousNode, nodeType,
				startEnd, controlType, attachedType, attachedVar, methodInfo, typeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addMethodAccessNode(NodeInfo nodeInfo, String varName, String attachedType,
			String methodName, Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList, int numOfArgs) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.METHODACCESS;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		// String attachedType = null;
		String attachedVar = varName;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodName, methodInfo, typeInfo, nodeInfo);
		// SONNV
		nodeSequenceInfo.setNumOfArgs(numOfArgs);
		// nodeSequenceInfo.setRetType();

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addConstructorNode(NodeInfo nodeInfo, String varName, String methodName,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.CONSTRUCTORCALL;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = varName;
		String attachedVar = varName;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodName, methodInfo, typeInfo, nodeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addFieldAccessNode(NodeInfo nodeInfo, String varName, String fieldName,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.FIELDACCESS;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = null;
		String attachedVar = varName;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, fieldName, methodInfo, typeInfo, nodeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addMethodReferenceNode(NodeInfo nodeInfo, String varName, String fieldName,
																	   Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
																	   ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.METHODREFERENCE;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = null;
		String attachedVar = varName;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, fieldName, methodInfo, typeInfo, nodeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addAssignmentNode(NodeInfo nodeInfo, String assignType,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.ASSIGN;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = assignType;
		String attachedVar = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);
		// Logger.log("addAssignmentNode: " + nodeSequenceInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;

	}

	public synchronized static NodeSequenceInfo addLiteralNode(NodeInfo nodeInfo, String literalType,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList, boolean isParam) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.LITERAL;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		// short controlType = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.NPARAM;
		if (isParam) {
			controlType = NodeSequenceConstant.PARAM;
		}
		String attachedType = literalType;
		String attachedVar = null;
		String attachedAccess = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, attachedAccess, methodInfo, typeInfo, nodeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addTypeNode(NodeInfo nodeInfo, String type,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.TYPE;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = type;
		String attachedVar = null;
		String attachedAccess = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, attachedAccess, methodInfo, typeInfo, nodeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addVarNode(NodeInfo nodeInfo, String varName,
			Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo,
			ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.VAR;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = null;
		String attachedVar = varName;
		String attachedAccess = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, attachedAccess, methodInfo, typeInfo, nodeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addOperatorNode(String operatorType, Stack<NodeSequenceInfo> nodeSequenceStack,
			MethodInfo methodInfo, TypeInfo typeInfo, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.OPERATOR;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = operatorType;
		String attachedVar = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);
		// Logger.log("addAssignmentNode: " + nodeSequenceInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;

	}

	public synchronized static NodeSequenceInfo addUOperatorNode(String operatorType, Stack<NodeSequenceInfo> nodeSequenceStack,
			MethodInfo methodInfo, TypeInfo typeInfo, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short nodeType = NodeSequenceConstant.UOPERATOR;
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = operatorType;
		String attachedVar = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);
		// Logger.log("addAssignmentNode: " + nodeSequenceInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;

	}

	public synchronized static NodeSequenceInfo addOtherNode(short nodeType, String type,
	        Stack<NodeSequenceInfo> nodeSequenceStack, MethodInfo methodInfo, TypeInfo typeInfo, 
	        ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo previousNode = null;
		if (nodeSequenceStack.size() > 0) {
			previousNode = nodeSequenceStack.pop();
		}
		short startEnd = NodeSequenceConstant.UNKNOWN;
		short controlType = NodeSequenceConstant.UNKNOWN;
		String attachedType = type;
		String attachedVar = null;

		NodeSequenceInfo nodeSequenceInfo = new NodeSequenceInfo(curNodeID, curNodeID, previousNode, nodeType, startEnd,
				controlType, attachedType, attachedVar, methodInfo, typeInfo);

		nodeSequenceStack.push(nodeSequenceInfo);
		nodeSequenceList.add(nodeSequenceInfo);

		curNodeID++;
		return nodeSequenceInfo;

	}
	
	public synchronized static NodeSequenceInfo addSTStmNode(short stm, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getSTSTM(stm);
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addENStmNode(short stm, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getENSTM(stm);
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}
	
	public synchronized static NodeSequenceInfo addSTMethodNode(String type, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getStartMethod(type);
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addENMethodNode(ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getEndMethod();
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}
	
	public synchronized static NodeSequenceInfo addFieldDecNode(ArrayList<NodeSequenceInfo> nodeSequenceList) {
        NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getFieldDeclaration();
        nodeSequenceList.add(nodeSequenceInfo);
        return nodeSequenceInfo;
    }

	public synchronized static NodeSequenceInfo addPartNode(short endMt, ArrayList<NodeSequenceInfo> nodeSequenceList, boolean isOpen) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getPartNode(endMt, isOpen);
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addSEPANode(short sepa, ArrayList<NodeSequenceInfo> nodeSequenceList, char type) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getSEPA(sepa,  type);
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addConditionalExprNode(short conditionalExpr, ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getConditionalExpr();
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addLambdaExprNode(ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getLambdaExpr();
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}

	public synchronized static NodeSequenceInfo addUnknownNode(ArrayList<NodeSequenceInfo> nodeSequenceList) {
		NodeSequenceInfo nodeSequenceInfo = NodeSequenceInfo.getUnknown();
		nodeSequenceList.add(nodeSequenceInfo);
		return nodeSequenceInfo;
	}
}
