package gian.compiler.language.simplejava.action;

import gian.compiler.front.lexical.parser.Token;
import gian.compiler.front.lexical.transform.LexConstants;
import gian.compiler.front.syntactic.element.SyntaxTree;
import gian.compiler.front.syntaxDirected.SyntaxDirectedContext;
import gian.compiler.front.syntaxDirected.SyntaxDirectedListener;
import gian.compiler.language.simplejava.JavaConstants;
import gian.compiler.language.simplejava.ast.expression.New;
import gian.compiler.language.simplejava.ast.expression.StringJoin;
import gian.compiler.language.simplejava.ast.ref.*;
import gian.compiler.language.simplejava.bean.Variable;
import gian.compiler.language.simplejava.ast.expression.Constant;
import gian.compiler.language.simplejava.ast.expression.Expr;
import gian.compiler.language.simplejava.bean.VariableType;
import gian.compiler.language.simplejava.utils.JavaDirectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaojian on 2019/4/2.
 */
public class ExpressionAction {

    public static String product_1 = "expression → identifierReference ( expressionList ) methodRefRest";
    public static class MethodCallListener extends SyntaxDirectedListener{
        public MethodCallListener(){
            this.matchProductTag = product_1;
            this.matchSymbol = "methodRefRest";
            this.matchIndex = 4;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) context.getBrotherNodeList().get(currentIndex - 4).getSynProperty(JavaConstants.CODE);
            List<Expr> paramList = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 2).getSynProperty(JavaConstants.CALL_PARAM_LIST);

            // TODO 这里由于文法“#变量/方法引用链声明”，因此不确定最后一个引用是否是变量引用，需要根据后续情况进行替换
            MethodRefNode methodRefNode = JavaDirectUtils.methodRefNode(JavaDirectUtils.getLastRef(refCall).getCallName(), paramList);
            refCall = JavaDirectUtils.updateLastRef(refCall, methodRefNode);

            currentTreeNode.putInhProperty(JavaConstants.CODE, refCall);
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_1_1 = "identifierReference → Identifier identifierDeclaratorIdRest";
    public static class LocalFieldCallListener extends SyntaxDirectedListener{
        public LocalFieldCallListener(){
            this.matchProductTag = product_1_1;
            this.matchSymbol = "identifierDeclaratorIdRest";
            this.matchIndex = 1;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            String fieldName = context.getBrotherNodeList().get(currentIndex - 1).getIdToken().getToken();
            FieldRefNode fieldRefNode = JavaDirectUtils.fieldRefNode(fieldName);
            
            currentTreeNode.putInhProperty(JavaConstants.CODE, fieldRefNode);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_1_2 = "identifierReference → this . Identifier identifierDeclaratorIdRest";
    public static class ThisFieldCallListener extends SyntaxDirectedListener{
        public ThisFieldCallListener(){
            this.matchProductTag = product_1_2;
            this.matchSymbol = "identifierDeclaratorIdRest";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {

            ThisRefNode thisRefNode = JavaDirectUtils.thisRefNode(context);

            String fieldName = context.getBrotherNodeList().get(currentIndex - 1).getIdToken().getToken();
            FieldRefNode fieldRefNode = JavaDirectUtils.fieldRefNode(fieldName);

            thisRefNode.setNextRef(fieldRefNode);

            currentTreeNode.putInhProperty(JavaConstants.CODE, thisRefNode);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_1_3_1 = "identifierDeclaratorIdRest → arrayRest . Identifier identifierDeclaratorIdRest";
    public static class RefCallRestListener extends SyntaxDirectedListener{
        public RefCallRestListener(){
            this.matchProductTag = product_1_3_1;
            this.matchSymbol = "identifierDeclaratorIdRest";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);

            // 将变量引用替换成数组引元素用
            List<Expr> arrayIndex = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 3).getSynProperty(JavaConstants.REF_ARRAY_INDEX);
            if(arrayIndex != null){
                ArrayElementRefNode arrayElementRefNode = JavaDirectUtils.arrayElementRefNode(JavaDirectUtils.getLastRef(refCall).getCallName(), arrayIndex);
                refCall = JavaDirectUtils.updateLastRef(refCall, arrayElementRefNode);
            }

            String callName = context.getBrotherNodeList().get(currentIndex - 1).getIdToken().getToken();
            // TODO 这里不清楚是变量名称还是方法名称，先处理成变量引用，交由上层文法判断，如果是方法引用再做替换处理
            FieldRefNode fieldRefNode = JavaDirectUtils.fieldRefNode(callName);

            JavaDirectUtils.appendRef(refCall, fieldRefNode);

            currentTreeNode.putInhProperty(JavaConstants.CODE, refCall);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_1_3_2 = "identifierDeclaratorIdRest → ε";
    public static class RefEmptyRestCallListener extends SyntaxDirectedListener{
        public RefEmptyRestCallListener(){
            this.matchProductTag = product_1_3_2;
            this.matchSymbol = "ε";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_1_4_1 = "arrayRest → [ Digit ] arrayRest";
    public static class ArrayRefCallListener extends SyntaxDirectedListener{
        public ArrayRefCallListener(){
            this.matchProductTag = product_1_4_1;
            this.matchSymbol = "arrayRest";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            List<Expr> index = new ArrayList<>();
            List<Expr> subIndex = (List<Expr>) currentTreeNode.getSynProperty(JavaConstants.REF_ARRAY_INDEX);
            Token indexNum = context.getBrotherNodeList().get(currentIndex - 2).getIdToken();
            index.add(JavaDirectUtils.constant(indexNum));
            if(subIndex != null){
                index.addAll(subIndex);
            }

            context.getParentNode().putSynProperty(JavaConstants.REF_ARRAY_INDEX, index);

            return null;
        }
    }

    public static String product_1_4_2 = "arrayRest → [ refVariable ] arrayRest";
    public static class ArrayExprRefCallListener extends SyntaxDirectedListener{
        public ArrayExprRefCallListener(){
            this.matchProductTag = product_1_4_2;
            this.matchSymbol = "arrayRest";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            List<Expr> index = new ArrayList<>();
            List<Expr> subIndex = (List<Expr>) currentTreeNode.getSynProperty(JavaConstants.REF_ARRAY_INDEX);
            Expr indexVariable = (Expr) context.getBrotherNodeList().get(currentIndex - 2).getSynProperty(JavaConstants.REF_VARIABLE);
            index.add(indexVariable);
            if(subIndex != null){
                index.addAll(subIndex);
            }

            context.getParentNode().putSynProperty(JavaConstants.REF_ARRAY_INDEX, index);

            return null;
        }
    }

    public static String product_2 = "expression → new Identifier ( expressionList )";
    public static class NewVariableListener extends SyntaxDirectedListener{
        public NewVariableListener(){
            this.matchProductTag = product_2;
            this.matchSymbol = "expressionList";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {

            String newClassName = context.getBrotherNodeList().get(currentIndex - 2).getIdToken().getToken();
            List<Variable> paramList = (List<Variable>) currentTreeNode.getSynProperty(JavaConstants.CALL_PARAM_LIST);
            New newNode = JavaDirectUtils.newNode(new VariableType(newClassName, VariableType.CLAZZ.getWidth()), paramList);

            context.getParentNode().putSynProperty(JavaConstants.CODE, newNode);

            return null;
        }
    }

    public static String product_2_1_1 = "methodRefRest → . Identifier arrayRest methodRefRest";
    public static class MethodRefVariableListener extends SyntaxDirectedListener{
        public MethodRefVariableListener(){
            this.matchProductTag = product_2_1_1;
            this.matchSymbol = "methodRefRest";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode preRef = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);
            String callName = context.getBrotherNodeList().get(currentIndex - 2).getIdToken().getToken();
            List<Expr> arrayIndex = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.REF_ARRAY_INDEX);
            if(arrayIndex != null){
                JavaDirectUtils.appendRef(preRef, JavaDirectUtils.arrayElementRefNode(callName, arrayIndex));
            }else{
                JavaDirectUtils.appendRef(preRef, JavaDirectUtils.fieldRefNode(callName));
            }

            currentTreeNode.putInhProperty(JavaConstants.CODE, preRef);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_2_1_2 = "methodRefRest → . Identifier ( expressionList ) methodRefRest";
    public static class MethodRefMethodListener extends SyntaxDirectedListener{
        public MethodRefMethodListener(){
            this.matchProductTag = product_2_1_2;
            this.matchSymbol = "methodRefRest";
            this.matchIndex = 5;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode preRef = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);
            String callName = context.getBrotherNodeList().get(currentIndex - 4).getIdToken().getToken();
            List<Expr> paramList = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 2).getSynProperty(JavaConstants.CALL_PARAM_LIST);

            JavaDirectUtils.appendRef(preRef, JavaDirectUtils.methodRefNode(callName, paramList));

            currentTreeNode.putInhProperty(JavaConstants.CODE, preRef);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_2_1_3 = "methodRefRest → ε";
    public static class MethodRefEndListener extends SyntaxDirectedListener{
        public MethodRefEndListener(){
            this.matchProductTag = product_2_1_3;
            this.matchSymbol = "ε";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);
            return null;
        }
    }

    public static String product_2_2_1 = "refVariable → Identifier arrayRest targetVariableRest";
    public static class RefVariableListener extends SyntaxDirectedListener{
        public RefVariableListener(){
            this.matchProductTag = product_2_2_1;
            this.matchSymbol = "targetVariableRest";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            String callName = context.getBrotherNodeList().get(currentIndex - 2).getIdToken().getToken();
            List<Expr> arrayIndex = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.REF_ARRAY_INDEX);
            RefNode refCall = null;
            if(arrayIndex != null){
                refCall = JavaDirectUtils.arrayElementRefNode(callName, arrayIndex);
            }else{
                refCall = JavaDirectUtils.fieldRefNode(callName);
            }

            currentTreeNode.putInhProperty(JavaConstants.CODE, refCall);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_2_2_2 = "refVariable → this . Identifier arrayRest targetVariableRest";
    public static class ThisRefVariableListener extends SyntaxDirectedListener{
        public ThisRefVariableListener(){
            this.matchProductTag = product_2_2_2;
            this.matchSymbol = "targetVariableRest";
            this.matchIndex = 4;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode thisRef = JavaDirectUtils.thisRefNode(context);

            String callName = context.getBrotherNodeList().get(currentIndex - 2).getIdToken().getToken();
            List<Expr> arrayIndex = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.REF_ARRAY_INDEX);

            if(arrayIndex != null){
                JavaDirectUtils.appendRef(thisRef, JavaDirectUtils.arrayElementRefNode(callName, arrayIndex));
            }else{
                JavaDirectUtils.appendRef(thisRef, JavaDirectUtils.fieldRefNode(callName));
            }

            currentTreeNode.putInhProperty(JavaConstants.CODE, thisRef);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_2_2_3 = "refVariable → this";
    public static class ThisRefListener extends SyntaxDirectedListener{
        public ThisRefListener(){
            this.matchProductTag = product_2_2_3;
            this.matchSymbol = "this";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode thisRef = JavaDirectUtils.thisRefNode(context);
            context.getParentNode().putSynProperty(JavaConstants.CODE, thisRef);

            return null;
        }
    }

    public static String product_2_3_1 = "targetVariableRest → . Identifier arrayRest targetVariableRest";
    public static class TargetVariableRefListener extends SyntaxDirectedListener{
        public TargetVariableRefListener(){
            this.matchProductTag = product_2_3_1;
            this.matchSymbol = "targetVariableRest";
            this.matchIndex = 3;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode preRef = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);

            String callName = context.getBrotherNodeList().get(currentIndex - 2).getIdToken().getToken();
            List<Expr> arrayIndex = (List<Expr>) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.REF_ARRAY_INDEX);
            if(arrayIndex != null){
                JavaDirectUtils.appendRef(preRef, JavaDirectUtils.arrayElementRefNode(callName, arrayIndex));
            }else{
                JavaDirectUtils.appendRef(preRef, JavaDirectUtils.fieldRefNode(callName));
            }

            currentTreeNode.putInhProperty(JavaConstants.CODE, preRef);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_2_3_2 = "targetVariableRest → ε";
    public static class TargetVariableRefEndListener extends SyntaxDirectedListener{
        public TargetVariableRefEndListener(){
            this.matchProductTag = product_2_3_2;
            this.matchSymbol = "ε";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            RefNode refCall = (RefNode) context.getParentNode().getInhProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, refCall);

            return null;
        }
    }

    public static String product_3 = "expression → expression + term";
    public static class AddListener extends SyntaxDirectedListener{
        public AddListener(){
            this.matchProductTag = product_3;
            this.matchSymbol = "term";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Expr expr = (Expr) context.getBrotherNodeList().get(currentIndex - 2).getSynProperty(JavaConstants.CODE);
            Expr term = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);

            Expr add = JavaDirectUtils.term(expr, term, JavaConstants.JAVA_OPERATOR_ADD);
            context.getParentNode().putSynProperty(JavaConstants.CODE, add);

            return null;
        }
    }

    public static String product_4 = "expression → expression - term";
    public static class ReduceListener extends SyntaxDirectedListener{
        public ReduceListener(){
            this.matchProductTag = product_4;
            this.matchSymbol = "term";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Expr expr = (Expr) context.getBrotherNodeList().get(currentIndex - 2).getSynProperty(JavaConstants.CODE);
            Expr term = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);

            Expr reduce = JavaDirectUtils.term(expr, term, JavaConstants.JAVA_OPERATOR_REDUCE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, reduce);

            return null;
        }
    }

    public static String product_5 = "expression → term";
    public static class TermListener extends SyntaxDirectedListener{
        public TermListener(){
            this.matchProductTag = product_5;
            this.matchSymbol = "term";
            this.matchIndex = 0;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Expr expr = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, expr);

            return null;
        }
    }

    public static String product_6 = "expression → Identifier ++";
    public static class VariableIncListener extends SyntaxDirectedListener{
        public VariableIncListener(){
            this.matchProductTag = product_6;
            this.matchSymbol = "++";
            this.matchIndex = 1;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            String variableName = context.getBrotherNodeList().get(currentIndex - 1).getIdToken().getToken();
            Variable variable = JavaDirectUtils.factor(variableName);

            Expr incExpr = JavaDirectUtils.term(variable, Constant.DIGIT_ONE, JavaConstants.JAVA_OPERATOR_ADD);
            context.getParentNode().putSynProperty(JavaConstants.CODE, incExpr);

            return null;
        }
    }

    public static String product_7 = "expression → Identifier --";
    public static class VariableDecListener extends SyntaxDirectedListener{
        public VariableDecListener(){
            this.matchProductTag = product_7;
            this.matchSymbol = "--";
            this.matchIndex = 1;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            String variableName = context.getBrotherNodeList().get(currentIndex - 1).getIdToken().getToken();
            Variable variable = JavaDirectUtils.factor(variableName);

            Expr decExpr = JavaDirectUtils.term(variable, Constant.DIGIT_ONE, JavaConstants.JAVA_OPERATOR_REDUCE);
            context.getParentNode().putSynProperty(JavaConstants.CODE, decExpr);

            return null;
        }
    }

    public static String product_8 = "expression → String stringRest";
    public static class StringListener extends SyntaxDirectedListener{
        public StringListener(){
            this.matchProductTag = product_8;
            this.matchSymbol = "stringRest";
            this.matchIndex = 1;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Token token = context.getBrotherNodeList().get(currentIndex - 1).getIdToken();
            Constant constant = JavaDirectUtils.constant(token);

            Expr nextExpr = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);
            if(nextExpr != null){
                StringJoin stringJoin = JavaDirectUtils.stringJoin(constant, nextExpr);
                context.getParentNode().putSynProperty(JavaConstants.CODE, stringJoin);
            }else{
                context.getParentNode().putSynProperty(JavaConstants.CODE, constant);
            }

            return null;
        }
    }
    
    public static String product_8_1 = "stringRest → + refVariable stringRest";
    public static class StringJoinRefVariableListener extends SyntaxDirectedListener{
        public StringJoinRefVariableListener(){
            this.matchProductTag = product_8_1;
            this.matchSymbol = "stringRest";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Expr refVariable = (Expr) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.REF_VARIABLE);
            Expr nextExpr = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);
            if(nextExpr != null){
                StringJoin stringJoin = JavaDirectUtils.stringJoin(refVariable, nextExpr);
                context.getParentNode().putSynProperty(JavaConstants.CODE, stringJoin);
            }else{
                context.getParentNode().putSynProperty(JavaConstants.CODE, refVariable);
            }

            return null;
        }
    }
    
    public static String product_8_2 = "expression → + Number stringRest";
    public static class StringJoinNumberListener extends SyntaxDirectedListener{
        public StringJoinNumberListener(){
            this.matchProductTag = product_8_2;
            this.matchSymbol = "stringRest";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Token number = context.getBrotherNodeList().get(currentIndex - 1).getIdToken();
            Constant constant = JavaDirectUtils.constant(number);

            Expr nextExpr = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);
            if(nextExpr != null){
                StringJoin stringJoin = JavaDirectUtils.stringJoin(constant, nextExpr);
                context.getParentNode().putSynProperty(JavaConstants.CODE, stringJoin);
            }else{
                context.getParentNode().putSynProperty(JavaConstants.CODE, constant);
            }

            return null;
        }
    }

    public static String product_8_3 = "expression → + Digit stringRest";
    public static class StringJoinDigitListener extends SyntaxDirectedListener{
        public StringJoinDigitListener(){
            this.matchProductTag = product_8_3;
            this.matchSymbol = "stringRest";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Token digit = context.getBrotherNodeList().get(currentIndex - 1).getIdToken();
            Constant constant = JavaDirectUtils.constant(digit);

            Expr nextExpr = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);
            if(nextExpr != null){
                StringJoin stringJoin = JavaDirectUtils.stringJoin(constant, nextExpr);
                context.getParentNode().putSynProperty(JavaConstants.CODE, stringJoin);
            }else{
                context.getParentNode().putSynProperty(JavaConstants.CODE, constant);
            }

            return null;
        }
    }

    public static String product_8_4 = "stringRest → + String stringRest";
    public static class StringJoinStringListener extends SyntaxDirectedListener{
        public StringJoinStringListener(){
            this.matchProductTag = product_8_4;
            this.matchSymbol = "stringRest";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Token str = context.getBrotherNodeList().get(currentIndex - 1).getIdToken();
            Constant constant = JavaDirectUtils.constant(str);

            Expr nextExpr = (Expr) currentTreeNode.getSynProperty(JavaConstants.CODE);
            if(nextExpr != null){
                StringJoin stringJoin = JavaDirectUtils.stringJoin(constant, nextExpr);
                context.getParentNode().putSynProperty(JavaConstants.CODE, stringJoin);
            }else{
                context.getParentNode().putSynProperty(JavaConstants.CODE, constant);
            }

            return null;
        }
    }

    public static String product_9 = "expression → true";
    public static class FalseListener extends SyntaxDirectedListener{
        public FalseListener(){
            this.matchProductTag = product_9;
            this.matchSymbol = "true";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            // 设置code
            currentTreeNode.getParentNode().putSynProperty(JavaConstants.CODE, Constant.True);

            return null;
        }
    }

    public static String product_10 = "expression → false";
    public static class TrueListener extends SyntaxDirectedListener{
        public TrueListener(){
            this.matchProductTag = product_10;
            this.matchSymbol = "false";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            // 设置code
            currentTreeNode.getParentNode().putSynProperty(JavaConstants.CODE, Constant.False);

            return null;
        }
    }

    public static String product_11 = "expressionList → expression expressionListRest";
    public static class ExpressionListListener extends SyntaxDirectedListener{
        public ExpressionListListener(){
            this.matchProductTag = product_11;
            this.matchSymbol = "expressionListRest";
            this.matchIndex = 1;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Expr expr1 = (Expr) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.CODE);
            List<Expr> restParamList = (List<Expr>) currentTreeNode.getSynProperty(JavaConstants.CALL_PARAM_LIST);

            List<Expr> paramList = new ArrayList<>();
            paramList.add(expr1);
            if(restParamList != null){
                paramList.addAll(restParamList);
            }

            context.getParentNode().putSynProperty(JavaConstants.CALL_PARAM_LIST, paramList);

            return null;
        }
    }

    public static String product_13 = "expressionListRest → , expression expressionListRest";
    public static class ExpressionListRestListener extends SyntaxDirectedListener{
        public ExpressionListRestListener(){
            this.matchProductTag = product_13;
            this.matchSymbol = "expressionListRest";
            this.matchIndex = 2;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            Expr expr1 = (Expr) context.getBrotherNodeList().get(currentIndex - 1).getSynProperty(JavaConstants.CODE);
            List<Expr> restParamList = (List<Expr>) currentTreeNode.getSynProperty(JavaConstants.CALL_PARAM_LIST);

            List<Expr> paramList = new ArrayList<>();
            paramList.add(expr1);
            if(restParamList != null){
                paramList.addAll(paramList);
            }

            context.getParentNode().putSynProperty(JavaConstants.CALL_PARAM_LIST, paramList);

            return null;
        }
    }

    public static List<SyntaxDirectedListener> getAllListener() {
        List<SyntaxDirectedListener> allListener = new ArrayList<>();
        allListener.add(new MethodCallListener());
        allListener.add(new LocalFieldCallListener());
        allListener.add(new ThisFieldCallListener());
        allListener.add(new RefCallRestListener());
        allListener.add(new RefEmptyRestCallListener());
        allListener.add(new ArrayRefCallListener());
        allListener.add(new ArrayExprRefCallListener());
        allListener.add(new NewVariableListener());
        allListener.add(new MethodRefVariableListener());
        allListener.add(new MethodRefMethodListener());
        allListener.add(new MethodRefEndListener());
        allListener.add(new RefVariableListener());
        allListener.add(new ThisRefVariableListener());
        allListener.add(new ThisRefListener());
        allListener.add(new TargetVariableRefListener());
        allListener.add(new TargetVariableRefEndListener());
        allListener.add(new AddListener());
        allListener.add(new ReduceListener());
        allListener.add(new TermListener());
        allListener.add(new VariableIncListener());
        allListener.add(new VariableDecListener());
        allListener.add(new StringListener());
        allListener.add(new StringJoinRefVariableListener());
        allListener.add(new StringJoinNumberListener());
        allListener.add(new StringJoinDigitListener());
        allListener.add(new StringJoinStringListener());
        allListener.add(new FalseListener());
        allListener.add(new TrueListener());
        allListener.add(new ExpressionListListener());
        allListener.add(new ExpressionListRestListener());
        return allListener;
    }

}