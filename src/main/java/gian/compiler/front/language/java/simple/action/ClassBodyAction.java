package gian.compiler.front.language.java.simple.action;

import gian.compiler.front.language.java.simple.JavaConstants;
import gian.compiler.front.language.java.simple.bean.ClazzConstructor;
import gian.compiler.front.language.java.simple.bean.ClazzField;
import gian.compiler.front.language.java.simple.bean.ClazzMethod;
import gian.compiler.front.language.java.simple.env.JavaEnvironment;
import gian.compiler.front.lexical.transform.LexConstants;
import gian.compiler.front.syntactic.element.SyntaxTree;
import gian.compiler.front.syntaxDirected.SyntaxDirectedContext;
import gian.compiler.front.syntaxDirected.SyntaxDirectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaojian on 2019/3/28.
 */
public class ClassBodyAction {

    public static String product = "classBody → { classBodyDeclaration }";

    public static class ClassBodyEnterListener extends SyntaxDirectedListener{

        public ClassBodyEnterListener(){
            this.matchProductTag = product;
            this.matchSymbol = "{";
            this.matchIndex = 0;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            // TODO 生成作用域
            JavaEnvironment environment = new JavaEnvironment();
            context.getGlobalPropertyMap().put(JavaConstants.CURRENT_ENV, environment);
            context.getGlobalPropertyMap().put(JavaConstants.CLASS_ENV, environment);

            return null;
        }
    }

    public static class ClassBodyDeclarationListener extends SyntaxDirectedListener{

        public ClassBodyDeclarationListener(){
            this.matchProductTag = product;
            this.matchSymbol = "classBodyDeclaration";
            this.matchIndex = 1;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {

            currentTreeNode.getPropertyMap().get(LexConstants.SYNTAX_DIRECT_PROPERTY_SYN).put(JavaConstants.FIELD_LIST, new ArrayList<>());
            currentTreeNode.getPropertyMap().get(LexConstants.SYNTAX_DIRECT_PROPERTY_SYN).put(JavaConstants.CONSTRUCTOR_LIST, new ArrayList<>());
            currentTreeNode.getPropertyMap().get(LexConstants.SYNTAX_DIRECT_PROPERTY_SYN).put(JavaConstants.METHOD_LIST, new ArrayList<>());

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }
    }

    public static class ClassBodyExitListener extends SyntaxDirectedListener{

        public ClassBodyExitListener(){
            this.matchProductTag = product;
            this.matchSymbol = "}";
            this.matchIndex = 2;
            this.isLeaf = true;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            // TODO 移除作用域
            context.getGlobalPropertyMap().remove(JavaConstants.CURRENT_ENV);
            context.getGlobalPropertyMap().remove(JavaConstants.CLASS_ENV);

            return null;
        }
    }

}